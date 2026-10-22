package com.walnutbeach.hollowmancy.sound;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.spell.MagicSchool;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Hollowmancy's own sounds: spell casts (one voice per school), the Listener, the Hollow's creatures, the Mana Battery,
 * and the Hollow's music and ambience. The files are in assets/hollowmancy/sounds, listed in assets/hollowmancy/sounds.json.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Hollowmancy.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CAST_GENERAL = sound("spell.cast.general");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CAST_ELEMENTAL = sound("spell.cast.elemental");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CAST_WARDING = sound("spell.cast.warding");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CAST_DIVINATION = sound("spell.cast.divination");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CAST_NATURE = sound("spell.cast.nature");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CAST_FORBIDDEN = sound("spell.cast.forbidden");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_FIZZLE = sound("spell.fizzle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_LEARN = sound("spell.learn");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_TIER_UP = sound("spell.tier_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_LEGENDARY = sound("spell.legendary");
    public static final DeferredHolder<SoundEvent, SoundEvent> LISTENER_AMBIENT = sound("listener.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LISTENER_HURT = sound("listener.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LISTENER_DEATH = sound("listener.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> LISTENER_SPEAK = sound("listener.speak");
    public static final DeferredHolder<SoundEvent, SoundEvent> LISTENER_ROAR = sound("listener.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> LISTENER_SUMMON = sound("listener.summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_AMBIENT = sound("wisp.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_HURT = sound("wisp.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_DEATH = sound("wisp.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_MOTH_AMBIENT = sound("crystal_moth.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_MOTH_HURT = sound("crystal_moth.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_MOTH_DEATH = sound("crystal_moth.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLIMMERHORN_AMBIENT = sound("glimmerhorn.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLIMMERHORN_HURT = sound("glimmerhorn.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLIMMERHORN_DEATH = sound("glimmerhorn.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLLOW_SHADE_AMBIENT = sound("hollow_shade.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLLOW_SHADE_HURT = sound("hollow_shade.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLLOW_SHADE_DEATH = sound("hollow_shade.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLLOW_SHADE_BLINK = sound("hollow_shade.blink");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARDLING_AMBIENT = sound("shardling.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARDLING_HURT = sound("shardling.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARDLING_DEATH = sound("shardling.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARDLING_STEP = sound("shardling.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRROR_LURKER_AMBIENT = sound("mirror_lurker.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRROR_LURKER_HURT = sound("mirror_lurker.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRROR_LURKER_DEATH = sound("mirror_lurker.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BATTERY_CHARGE = sound("battery.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> BATTERY_DRAIN = sound("battery.drain");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_HOLLOW = sound("music.hollow");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_CRYSTAL_BARRENS = sound("music.crystal_barrens");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_HOLLOW_GROVE = sound("music.hollow_grove");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_MIRROR_SEA = sound("music.mirror_sea");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_REVENANT_ISLE = sound("music.revenant_isle");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_HOLLOW_DEPTHS = sound("music.hollow_depths");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_VOID_WIND = sound("ambient.void_wind");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_CRYSTAL_HUM = sound("ambient.crystal_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_GROVE_RUSTLE = sound("ambient.grove_rustle");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_SEA_LAP = sound("ambient.sea_lap");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, name)));
    }

    /** The cast sound for a spell's school. */
    public static SoundEvent castFor(MagicSchool school) {
        return switch (school) {
            case ELEMENTAL -> SPELL_CAST_ELEMENTAL.get();
            case WARDING -> SPELL_CAST_WARDING.get();
            case DIVINATION -> SPELL_CAST_DIVINATION.get();
            case NATURE -> SPELL_CAST_NATURE.get();
            case FORBIDDEN -> SPELL_CAST_FORBIDDEN.get();
            default -> SPELL_CAST_GENERAL.get();
        };
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }

    private ModSounds() {}
}
