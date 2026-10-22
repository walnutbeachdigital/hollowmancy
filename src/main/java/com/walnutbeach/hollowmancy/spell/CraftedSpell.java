package com.walnutbeach.hollowmancy.spell;

import com.walnutbeach.hollowmancy.item.SpellCraftingItems;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A spell put together from runes: a shape, an element, and (optionally) a modifier.
 * All 36 combinations are registered at startup; a player knows the ones they've inscribed and read.
 */
public class CraftedSpell extends Spell {

    public enum Shape {
        BOLT("bolt", "Bolt", 18, 30, 1.0f, "A bolt of %s that flies where you aim", () -> SpellCraftingItems.BOLT_RUNE.get()),
        NOVA("nova", "Nova", 32, 120, 0.8f, "A burst of %s all around you", () -> SpellCraftingItems.NOVA_RUNE.get()),
        BEAM("beam", "Beam", 26, 70, 0.9f, "A beam of %s that pierces everything in a line", () -> SpellCraftingItems.BEAM_RUNE.get());

        public final String id, name, text;
        public final int mana, cooldown;
        public final float damage;
        public final Supplier<Item> rune;

        Shape(String id, String name, int mana, int cooldown, float damage, String text, Supplier<Item> rune) {
            this.id = id; this.name = name; this.mana = mana; this.cooldown = cooldown; this.damage = damage; this.text = text; this.rune = rune;
        }
    }

    public enum Element {
        FIRE("fire", "Fire", 0, 6.0f, MagicSchool.ELEMENTAL, "flame. It sets what it touches alight", () -> SpellCraftingItems.FIRE_RUNE.get()),
        FROST("frost", "Frost", 2, 5.0f, MagicSchool.ELEMENTAL, "frost. It slows and chills", () -> SpellCraftingItems.FROST_RUNE.get()),
        VOID("void", "Void", 6, 8.0f, MagicSchool.FORBIDDEN, "void. It hits hardest, weakens, and corrupts you a little", () -> SpellCraftingItems.VOID_RUNE.get());

        public final String id, name, text;
        public final int mana;
        public final float damage;
        public final MagicSchool school;
        public final Supplier<Item> rune;

        Element(String id, String name, int mana, float damage, MagicSchool school, String text, Supplier<Item> rune) {
            this.id = id; this.name = name; this.mana = mana; this.damage = damage; this.school = school; this.text = text; this.rune = rune;
        }
    }

    public enum Modifier {
        NONE("", "", 0, 0, "", null),
        SPLIT("split", "Splitting", 12, 20, "Splitting: three at once (a second pulse, for a nova)",
            () -> SpellCraftingItems.SPLIT_RUNE.get()),
        HOMING("homing", "Seeking", 8, 10, "Seeking: finds the nearest enemy on its own",
            () -> SpellCraftingItems.HOMING_RUNE.get()),
        LINGERING("lingering", "Lingering", 10, 30, "Lingering: leaves the ground burning with it for a few seconds",
            () -> SpellCraftingItems.LINGERING_RUNE.get());

        public final String id, name, text;
        public final int mana, cooldown;
        public final Supplier<Item> rune;

        Modifier(String id, String name, int mana, int cooldown, String text, Supplier<Item> rune) {
            this.id = id; this.name = name; this.mana = mana; this.cooldown = cooldown; this.text = text; this.rune = rune;
        }
    }

    public final Shape shape;
    public final Element element;
    public final Modifier modifier;

    public CraftedSpell(Shape shape, Element element, Modifier modifier) {
        super(id(shape, element, modifier), name(shape, element, modifier), describe(shape, element, modifier), element.school,
            MagicStage.ARCANE_ARTS, 1, shape.mana + element.mana + modifier.mana, shape.cooldown + modifier.cooldown);
        this.shape = shape;
        this.element = element;
        this.modifier = modifier;
    }

    public static String id(Shape shape, Element element, Modifier modifier) {
        return "craft_" + shape.id + "_" + element.id + (modifier == Modifier.NONE ? "" : "_" + modifier.id);
    }

    private static String name(Shape shape, Element element, Modifier modifier) {
        return (modifier == Modifier.NONE ? "" : modifier.name + " ") + element.name + " " + shape.name;
    }

    private static String describe(Shape shape, Element element, Modifier modifier) {
        String text = String.format(shape.text, element.text) + ".";
        return modifier == Modifier.NONE ? text : text + " " + modifier.text + ".";
    }

    /** Knowledge it costs to read the scroll. */
    public int scrollCost() {
        return 3 + (modifier == Modifier.NONE ? 0 : 2);
    }

    /** Called once from Spells: registers all 36 crafted spells. */
    public static List<Spell> registerAll(UnaryOperator<Spell> register) {
        List<Spell> all = new ArrayList<>();
        for (Shape shape : Shape.values())
            for (Element element : Element.values())
                for (Modifier modifier : Modifier.values())
                    all.add(register.apply(new CraftedSpell(shape, element, modifier)));
        return all;
    }

    @Override
    public int getCorruption() {
        return element == Element.VOID ? 2 : 0;
    }

    @Override
    public boolean cast(ServerPlayer player) {
        CraftedMagic.cast(player, this);
        return true;
    }
}
