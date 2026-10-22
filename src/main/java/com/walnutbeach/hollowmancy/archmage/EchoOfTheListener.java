package com.walnutbeach.hollowmancy.archmage;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.entity.Listener;
import com.walnutbeach.hollowmancy.entity.hollow.HollowCreatures;
import com.walnutbeach.hollowmancy.entity.hollow.HollowShade;
import com.walnutbeach.hollowmancy.fx.HollowmancyParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

/**
 * The Echo of the Listener: the last fight, for Archmages who want one more.
 * Twice the health, heavier armour, harder hits, and it keeps calling Hollow Shades to its side.
 * Silencing it drops an Echo Heart and a new Silent Seal, so it can be fought again.
 *
 * This also gives the ordinary Listener its servants back: the Vexes it used to call are vanilla mobs,
 * which the Hollow refuses, so both Listeners now call Hollow Shades.
 */
@EventBusSubscriber(modid = Hollowmancy.MODID)
public final class EchoOfTheListener {
    public static final String TAG = "hollowmancy.echo";
    public static final String SHADE_TAG = "hollowmancy.echo_shade";
    public static final double HEALTH = 1800.0;
    public static final double ARMOR = 14.0;
    /** Summoned Shades fade after a minute so the arena doesn't fill up. */
    private static final int SHADE_LIFE = 20 * 60;
    private static final int MAX_SHADES = 8;

    public static boolean isEcho(Entity entity) {
        return entity.getTags().contains(TAG);
    }

    /** Turn a freshly made Listener into the Echo. Everything here is saved with the entity. */
    public static void empower(Listener echo) {
        echo.addTag(TAG);
        echo.setCustomName(Component.translatable("entity.hollowmancy.listener_echo").withStyle(ChatFormatting.DARK_PURPLE));
        AttributeInstance health = echo.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(HEALTH);
        AttributeInstance armor = echo.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(ARMOR);
        AttributeInstance toughness = echo.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null) toughness.setBaseValue(6.0);
        echo.setHealth((float) HEALTH);
        echo.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, MobEffectInstance.INFINITE_DURATION, 1, false, false));
        echo.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
    }

    /** Called from Listener.die when the Echo falls (instead of the ending). */
    public static void defeated(ServerLevel level, Listener echo) {
        echo.spawnAtLocation(new ItemStack(ArchmageItems.ECHO_HEART.get()));
        echo.spawnAtLocation(new ItemStack(ArchmageItems.SILENT_SEAL.get()));
        level.sendParticles(HollowmancyParticles.VOID_WISP.get(), echo.getX(), echo.getY() + 1.5, echo.getZ(), 200, 2.0, 2.0, 2.0, 0.2);
        level.sendParticles(HollowmancyParticles.RING_VOID.get(), echo.getX(), echo.getY() + 1.0, echo.getZ(), 1, 0, 0, 0, 0);

        // The Shades go with it
        for (HollowShade shade : level.getEntitiesOfClass(HollowShade.class, echo.getBoundingBox().inflate(96),
                s -> s.getTags().contains(SHADE_TAG))) {
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), shade.getX(), shade.getY() + 1, shade.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
            shade.discard();
        }

        AdvancementHolder silenced = level.getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "echo_silenced"));
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, echo.getBoundingBox().inflate(128))) {
            if (silenced != null) player.getAdvancements().award(silenced, "silenced");
            player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 80, 30));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("hollowmancy.echo.subtitle").withStyle(ChatFormatting.GOLD)));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("hollowmancy.echo.title").withStyle(ChatFormatting.DARK_PURPLE)));
            player.sendSystemMessage(Component.translatable("hollowmancy.echo.defeated").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 1.1f);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (entity instanceof Listener listener) {
            tickListener(level, listener);
        } else if (entity instanceof HollowShade shade && shade.getTags().contains(SHADE_TAG) && shade.tickCount > SHADE_LIFE) {
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), shade.getX(), shade.getY() + 1, shade.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
            shade.discard();
        }
    }

    private static void tickListener(ServerLevel level, Listener listener) {
        if (!listener.isAlive()) return;
        boolean echo = isEcho(listener);
        float share = listener.getHealth() / listener.getMaxHealth();
        int every;
        int count;
        if (echo) {
            every = share < 0.33F ? 200 : 300;
            count = share < 0.33F ? 3 : 2;
            if (listener.tickCount % 10 == 0) {
                level.sendParticles(HollowmancyParticles.VOID_WISP.get(), listener.getX(), listener.getY() + 1.8, listener.getZ(), 3, 0.8, 0.8, 0.8, 0.02);
            }
        } else {
            return; // the ordinary Listener calls its own Shades, alongside its cultists
        }
        if (listener.tickCount % every != every / 2) return;

        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, listener.getBoundingBox().inflate(48),
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative());
        if (players.isEmpty()) return;
        int present = level.getEntitiesOfClass(HollowShade.class, listener.getBoundingBox().inflate(64),
                s -> s.getTags().contains(SHADE_TAG)).size();
        int room = Math.max(0, MAX_SHADES - present);

        for (int i = 0; i < Math.min(count, room); i++) {
            ServerPlayer target = players.get(i % players.size());
            HollowShade shade = HollowCreatures.HOLLOW_SHADE.get().create(level);
            if (shade == null) continue;
            double angle = level.random.nextDouble() * Math.PI * 2;
            double x = target.getX() + Math.cos(angle) * 5, z = target.getZ() + Math.sin(angle) * 5;
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, target.getY(), z));
            double y = Math.abs(ground.getY() - target.getY()) < 6 ? ground.getY() : target.getY();
            shade.moveTo(x, y, z, level.random.nextFloat() * 360F, 0);
            shade.addTag(SHADE_TAG);
            shade.finalizeSpawn(level, level.getCurrentDifficultyAt(shade.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            shade.setTarget(target);
            level.addFreshEntity(shade);
            level.sendParticles(HollowmancyParticles.RING_VOID.get(), x, y + 0.1, z, 1, 0, 0, 0, 0);
            level.sendParticles(HollowmancyParticles.VOID_WISP.get(), x, y + 1, z, 20, 0.3, 0.8, 0.3, 0.05);
        }
        if (room > 0) level.playSound(null, listener.getX(), listener.getY(), listener.getZ(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 0.4f);
    }

    private EchoOfTheListener() {}
}
