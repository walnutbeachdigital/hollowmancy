# Translating Hollowmancy

All of Hollowmancy's text lives in language files:

- `src/main/resources/assets/hollowmancy/lang/en_us.json`: every message, tooltip, button and screen label
- `src/main/resources/assets/hollowmancy/patchouli_books/arcane_journal/en_us/`: the Arcane Journal

## Adding a language

1. Copy `en_us.json` to the new language's code, for example `de_de.json` (German), `fr_fr.json` (French),
   `es_es.json` (Spanish), `pt_br.json` (Brazilian Portuguese), `zh_cn.json` (Simplified Chinese).
2. Translate the text on the right of each line. Never change the key on the left.
3. Keep every `%s`. Each one is filled in by the game (a number, a name, a spell). If a language needs them in a
   different order, use `%1$s`, `%2$s` and so on to say which is which.
4. `%%` shows a single `%`. `\n` starts a new line.
5. For the journal, copy the `en_us` folder to the new code (for example `de_de`) and translate the text inside.

Anything missing from a translation falls back to English, so a half-finished file still works.

## Adding new text to the mod

Write it the usual way (`Component.literal("...")`), then run `python3 hollowmancy_translate.py` again.
It only touches text that isn't translated yet. Use `--dry` first to see what it will change.
