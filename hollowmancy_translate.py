#!/usr/bin/env python3
"""
Hollowmancy translation pass.

Finds player-facing English text in the Java code and moves it into
src/main/resources/assets/hollowmancy/lang/en_us.json, so the mod can be translated by adding
another file next to it (de_de.json, fr_fr.json, ...).

    Component.literal("Not enough mana (needs " + cost + ")")
becomes
    Component.translatable("hollowmancy.rituals.not_enough_mana_needs", cost)
with  "hollowmancy.rituals.not_enough_mana_needs": "Not enough mana (needs %s)"  in en_us.json.

Also converts text drawn on screens:  drawString(font, "Knowledge: " + k, ...),
drawCenteredString(...), and FormattedText.of("...").

Usage (from your project folder):
    python3 hollowmancy_translate.py --dry     show what it would change, change nothing
    python3 hollowmancy_translate.py           do it (originals are copied to .hollowmancy_backup/ first)

Anything it can't safely convert is listed in hollowmancy_untranslated.txt.
"""
import json, os, re, shutil, sys

JAVA = "src/main/java"
LANG = "src/main/resources/assets/hollowmancy/lang/en_us.json"
BACKUP = ".hollowmancy_backup"
REPORT = "hollowmancy_untranslated.txt"
DRY = "--dry" in sys.argv

# Where text appears, and how to rewrite the converted text in that spot.
#   kind "component": the call already takes a Component -> replace the whole call
#   kind "string":    the argument is a String -> wrap the converted text in Component.translatable(...)
TARGETS = [
    ("Component.literal(", "component"),
    ("FormattedText.of(", "component"),
]
DRAW_CALLS = ["drawString(", "drawCenteredString("]   # first argument is the font, second is the text

# ------------------------------------------------------------------ a small Java reader

def skip_string(src, i):
    """src[i] is a quote: return index just past the closing quote."""
    q = src[i]
    i += 1
    while i < len(src):
        if src[i] == "\\": i += 2; continue
        if src[i] == q: return i + 1
        i += 1
    return i

def code_spans(src):
    """Yields (start, end) of regions that are code (not strings or comments)."""
    i, start = 0, 0
    while i < len(src):
        c = src[i]
        if src.startswith("//", i):
            yield start, i
            i = src.find("\n", i); i = len(src) if i < 0 else i; start = i
        elif src.startswith("/*", i):
            yield start, i
            i = src.find("*/", i + 2); i = len(src) if i < 0 else i + 2; start = i
        elif c in "\"'":
            if src.startswith('"""', i):          # text block: leave alone
                yield start, i
                j = src.find('"""', i + 3); i = len(src) if j < 0 else j + 3; start = i
                continue
            i = skip_string(src, i)
        else:
            i += 1
    yield start, len(src)

def in_code(src):
    mask = bytearray(len(src))
    for a, b in code_spans(src):
        for k in range(a, b): mask[k] = 1
    # strings are "not code" for finding calls; mark them off explicitly
    i = 0
    while i < len(src):
        if mask[i] and src[i] in "\"'":
            j = skip_string(src, i)
            for k in range(i, j): mask[k] = 0
            i = j
        else:
            i += 1
    return mask

def matching_paren(src, open_idx):
    depth, i = 0, open_idx
    while i < len(src):
        c = src[i]
        if c in "\"'": i = skip_string(src, i); continue
        if c in "([{": depth += 1
        elif c in ")]}":
            depth -= 1
            if depth == 0: return i
        i += 1
    return -1

def split_top(expr, sep):
    """Split an expression on a character at nesting depth 0 (outside strings)."""
    parts, depth, i, last = [], 0, 0, 0
    while i < len(expr):
        c = expr[i]
        if c in "\"'": i = skip_string(expr, i); continue
        if c in "([{": depth += 1
        elif c in ")]}": depth -= 1
        elif c == sep and depth == 0:
            parts.append(expr[last:i]); last = i + 1
        i += 1
    parts.append(expr[last:])
    return parts

def has_top(expr, chars):
    depth, i = 0, 0
    while i < len(expr):
        c = expr[i]
        if c in "\"'": i = skip_string(expr, i); continue
        if c in "([{": depth += 1
        elif c in ")]}": depth -= 1
        elif c in chars and depth == 0: return True
        i += 1
    return False

STRING_RE = re.compile(r'^"((?:[^"\\]|\\.)*)"$', re.S)

def java_unescape(s):
    out, i = [], 0
    while i < len(s):
        if s[i] == "\\" and i + 1 < len(s):
            n = s[i + 1]
            out.append({"n": "\n", "t": "\t", '"': '"', "'": "'", "\\": "\\", "r": "", "0": "\0"}.get(n, n))
            if n == "u" and i + 5 < len(s):
                out[-1] = chr(int(s[i + 2:i + 6], 16)); i += 6; continue
            i += 2
        else:
            out.append(s[i]); i += 1
    return "".join(out)

# ------------------------------------------------------------------ deciding what to convert

def parse_text(expr):
    """'"Mana: " + x + "/" + y' -> ("Mana: %s/%s", ["x", "y"]); None if it isn't plain text + values."""
    expr = expr.strip()
    if not expr or has_top(expr, "?,") or "->" in expr:
        return None
    pieces = [p.strip() for p in split_top(expr, "+")]
    # Java joins text left to right; the first or second piece must be text, or '+' would add numbers
    if not pieces or not (STRING_RE.match(pieces[0]) or (len(pieces) > 1 and STRING_RE.match(pieces[1]))):
        return None
    text, args = "", []
    for p in pieces:
        m = STRING_RE.match(p)
        if m:
            text += java_unescape(m.group(1)).replace("%", "%%")
        else:
            if not p: return None
            text += "%s"
            args.append(p)
    plain = re.sub(r"%[%s]", "", text)
    if not re.search(r"[A-Za-z]{2}", plain):
        return None          # symbols, numbers, single letters: nothing to translate
    if not args and re.fullmatch(r"[a-z0-9_.:/-]+", plain.strip()):
        return None          # looks like an id or a key, not prose
    return text, args

def split_ternary(expr):
    """'c ? a : b' at the top level -> (c, a, b), else None."""
    depth, i, q = 0, 0, -1
    nested = 0
    while i < len(expr):
        ch = expr[i]
        if ch in "\"'": i = skip_string(expr, i); continue
        if ch in "([{": depth += 1
        elif ch in ")]}": depth -= 1
        elif depth == 0 and ch == "?":
            if q < 0: q = i
            else: nested += 1
        elif depth == 0 and ch == ":" and q >= 0 and not expr.startswith("::", i) and expr[i - 1] != ":":
            if nested: nested -= 1
            else: return expr[:q], expr[q + 1:i], expr[i + 1:]
        i += 1
    return None

def convert(expr, key_for, path):
    """Java code for a Component showing this text, or None if there's nothing to translate."""
    expr = expr.strip()
    while expr.startswith("(") and matching_paren(expr, 0) == len(expr) - 1:
        expr = expr[1:-1].strip()
    t = split_ternary(expr)
    if t:
        cond, a, b = t
        ca, cb = convert(a, key_for, path), convert(b, key_for, path)
        if ca is None and cb is None: return None
        ca = ca or "Component.literal(" + a.strip() + ")"
        cb = cb or "Component.literal(" + b.strip() + ")"
        return "(" + cond.strip() + " ? " + ca + " : " + cb + ")"
    parsed = parse_text(expr)
    if parsed is None: return None
    text, args = parsed
    return "Component.translatable(\"" + key_for(path, text) + "\"" + "".join(", " + a for a in args) + ")"

def slug(text):
    words = re.findall(r"[a-z0-9]+", re.sub(r"%s", " ", text.lower()))
    s = "_".join(words[:6])[:40].strip("_")
    return s or "text"

def owner(path):
    name = os.path.splitext(os.path.basename(path))[0]
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()

# ------------------------------------------------------------------ main pass

def main():
    if not os.path.isdir(JAVA):
        sys.exit("Run this from your project folder (cd ~/Mods/hollowmancy first).")
    lang = json.load(open(LANG, encoding="utf-8")) if os.path.exists(LANG) else {}
    by_text = {v: k for k, v in lang.items()}
    added, changed_files, leftovers, total = {}, [], [], 0

    def key_for(path, text):
        if text in by_text: return by_text[text]
        base = f"hollowmancy.{owner(path)}.{slug(text)}"
        key, n = base, 2
        while key in lang or key in added:
            key = f"{base}_{n}"; n += 1
        added[key] = text
        by_text[text] = key
        return key

    for dirpath, _, files in os.walk(JAVA):
        for fname in sorted(files):
            if not fname.endswith(".java"): continue
            path = os.path.join(dirpath, fname)
            src = open(path, encoding="utf-8").read()
            mask = in_code(src)
            edits = []    # (start, end, replacement)

            def visit(start, kind, arg_start, arg_end, whole_end):
                inner = src[arg_start:arg_end]
                line = src.count("\n", 0, start) + 1
                code = convert(inner, key_for, path)
                if code is None:
                    if kind != "draw" and '"' in inner:
                        leftovers.append(f"{path}:{line}: {' '.join(src[start:whole_end + 1].split())[:160]}")
                    return
                if kind == "component":
                    edits.append((start, whole_end + 1, code))
                else:
                    edits.append((arg_start, arg_end, code))

            for needle, kind in TARGETS:
                for m in re.finditer(re.escape(needle), src):
                    s = m.start()
                    if not mask[s] or (s > 0 and (src[s - 1].isalnum() or src[s - 1] == "_")): continue
                    open_i = m.end() - 1
                    close = matching_paren(src, open_i)
                    if close < 0: continue
                    visit(s, kind, open_i + 1, close, close)

            for needle in DRAW_CALLS:
                for m in re.finditer(r"\b" + re.escape(needle), src):
                    s = m.start()
                    if not mask[s]: continue
                    open_i = m.end() - 1
                    close = matching_paren(src, open_i)
                    if close < 0: continue
                    args = split_top(src[open_i + 1:close], ",")
                    if len(args) < 2: continue
                    a0 = open_i + 1 + len(args[0]) + 1
                    a1 = a0 + len(args[1])
                    # keep the leading whitespace of the argument
                    lead = len(args[1]) - len(args[1].lstrip())
                    trail = len(args[1]) - len(args[1].rstrip())
                    visit(s, "draw", a0 + lead, a1 - trail, close)

            if not edits: continue
            # apply from the end so positions stay valid; drop overlaps (outer call wins)
            edits.sort(key=lambda e: (e[0], -e[1]))
            kept, last_end = [], -1
            for e in edits:
                if e[0] >= last_end:
                    kept.append(e); last_end = e[1]
            new = src
            for a, b, r in sorted(kept, key=lambda e: -e[0]):
                new = new[:a] + r + new[b:]
            if "import net.minecraft.network.chat.Component;" not in new and "Component.translatable" in new:
                new = re.sub(r"(package [^;]+;\n)", r"\1\nimport net.minecraft.network.chat.Component;", new, count=1)
            total += len(kept)
            changed_files.append((path, len(kept)))
            if not DRY:
                os.makedirs(os.path.join(BACKUP, os.path.dirname(path)), exist_ok=True)
                if not os.path.exists(os.path.join(BACKUP, path)):
                    shutil.copy2(path, os.path.join(BACKUP, path))
                open(path, "w", encoding="utf-8").write(new)

    print(("Would convert" if DRY else "Converted") + f" {total} pieces of text in {len(changed_files)} files:")
    for p, n in changed_files:
        print(f"  {n:4d}  {p}")
    print(f"New language entries: {len(added)} (identical text shares one entry)")
    if not DRY:
        lang.update(added)
        os.makedirs(os.path.dirname(LANG), exist_ok=True)
        json.dump(lang, open(LANG, "w", encoding="utf-8"), indent=2, ensure_ascii=False)
        with open(REPORT, "w", encoding="utf-8") as f:
            f.write("Text the translation pass left alone (built from variables, or a choice between two texts).\n"
                    "These still show in English. Send this file to Claude to convert them by hand.\n\n")
            f.write("\n".join(leftovers) + "\n")
        print(f"Left alone: {len(leftovers)} (listed in {REPORT})")
        print(f"Originals saved in {BACKUP}/. Undo everything with: git checkout -- src && rm -rf {BACKUP}")
        print("\nDone. Build and run to check.")
    else:
        print(f"Would leave alone: {len(leftovers)}")

if __name__ == "__main__":
    main()
