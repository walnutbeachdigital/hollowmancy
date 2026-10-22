package com.walnutbeach.hollowmancy.entity;

import com.walnutbeach.hollowmancy.entity.hollow.HollowSummons;
import com.walnutbeach.hollowmancy.sound.ModSounds;
import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.ending.Ending;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.walnutbeach.hollowmancy.archmage.EchoOfTheListener;

/**
 * The Listener: what the Circle found in the silence. The source of the Hollow.
 * A Wither in shape (skulls, flight, arrow-proof below half health) with the Hollow's own powers on top.
 * It never breaks blocks. Its death ends the story.
 */
public class Listener extends WitherBoss {

    private record Eruption(Vec3 pos, long due) {}
    private final List<Eruption> voidRain = new ArrayList<>();
    private int phase = 0;
    private boolean greeted = false;

    public Listener(EntityType<? extends WitherBoss> type, Level level) {
        super(type, level);
        this.xpReward = 2000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WitherBoss.createAttributes()
            .add(Attributes.MAX_HEALTH, 900.0)
            .add(Attributes.ARMOR, 6.0)
            .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel level)) return;
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(48),
            p -> p.isAlive() && !p.isSpectator() && !p.isCreative());

        int newPhase = getHealth() > getMaxHealth() * 0.66f ? 1 : getHealth() > getMaxHealth() * 0.33f ? 2 : 3;
        if (phase == 0) phase = newPhase;
        if (!greeted && !players.isEmpty()) {
            greeted = true;
            speak("I LISTENED TO THEM. NOW I LISTEN TO YOU.");
        }
        if (newPhase > phase) {
            phase = newPhase;
            speak(phase == 2 ? "THE CIRCLE SPOKE. I ANSWERED." : "WITHOUT ME THERE IS ONLY SILENCE.");
            level.playSound(null, getX(), getY(), getZ(), ModSounds.LISTENER_ROAR.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        tickVoidRain(level);
        if (players.isEmpty()) return;

        // Every phase: it draws everything toward itself, into the dark
        if (tickCount % 240 == 0) {
            for (ServerPlayer player : players) {
                Vec3 pull = position().subtract(player.position()).normalize().scale(1.2);
                player.setDeltaMovement(pull.x, Math.max(0.3, pull.y), pull.z);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
            }
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 2.5f, 0.5f);
        }

        // Phase 2+: void rain under every player, and the echoes of its servants
        if (phase >= 2 && tickCount % 160 == 80) {
            long due = level.getGameTime() + 30;
            for (ServerPlayer player : players) voidRain.add(new Eruption(player.position(), due));
        }
        if (phase >= 2 && tickCount % 400 == 200) summonEchoes(level, players.get(0));

        // Phase 3: it drinks your mana, and unmakes
        if (phase >= 3 && tickCount % 20 == 0) {
            for (ServerPlayer player : players) {
                MagicData data = MagicHelper.get(player);
                if (data.mana() > 0) MagicHelper.set(player, data.withMana(data.mana() - 4));
            }
        }
        if (phase >= 3 && tickCount % 200 == 100) {
            for (ServerPlayer player : players) unmake(level, player);
        }
    }

    private void tickVoidRain(ServerLevel level) {
        long now = level.getGameTime();
        Iterator<Eruption> it = voidRain.iterator();
        while (it.hasNext()) {
            Eruption e = it.next();
            if (now < e.due()) {
                if (now % 3 == 0) {
                    for (int y = 0; y < 12; y += 2) {
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, e.pos().x, e.pos().y + y, e.pos().z, 2, 0.3, 0.1, 0.3, 0.0);
                    }
                }
                continue;
            }
            it.remove();
            level.sendParticles(ParticleTypes.SQUID_INK, e.pos().x, e.pos().y + 1, e.pos().z, 40, 0.4, 1.5, 0.4, 0.05);
            level.playSound(null, e.pos().x, e.pos().y, e.pos().z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0f, 0.6f);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(e.pos(), 3, 5, 3),
                    v -> v != this && v.isAlive() && !(v instanceof Vex) && !(v instanceof HollowCultist))) {
                victim.hurt(damageSources().indirectMagic(this, this), 10.0f);
                victim.setDeltaMovement(victim.getDeltaMovement().add(0, 0.8, 0));
                victim.hurtMarked = true;
            }
        }
    }

    private void summonEchoes(ServerLevel level, ServerPlayer target) {
        for (int i = 0; i < 2; i++) {
            HollowCultist cultist = ModEntities.HOLLOW_CULTIST.get().create(level);
            if (cultist != null) {
                cultist.moveTo(target.getX() + (i == 0 ? 4 : -4), target.getY(), target.getZ(), 0, 0);
                cultist.setTarget(target);
                level.addFreshEntity(cultist);
            }
            // A Hollow Shade rises beside each cultist (vanilla Vexes aren't allowed in the Hollow)
            HollowSummons.summon(level, this, target, target.getX() + (i == 0 ? -3 : 3), target.getY(), target.getZ() + 3, 20 * 30);
        }
        level.playSound(null, getX(), getY(), getZ(), ModSounds.LISTENER_SUMMON.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
    }

    private void unmake(ServerLevel level, ServerPlayer player) {
        Vec3 from = position().add(0, 2, 0), path = player.getEyePosition().subtract(from);
        for (double d = 1; d < path.length(); d += 0.4) {
            Vec3 p = from.add(path.normalize().scale(d));
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        player.hurt(damageSources().indirectMagic(this, this), 12.0f);
        MagicData data = MagicHelper.get(player);
        MagicHelper.set(player, data.withCorruption(data.corruption() + 3));
    }

    private void speak(String line) {
        level().playSound(null, getX(), getY(), getZ(), ModSounds.LISTENER_SPEAK.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
        for (ServerPlayer player : ((ServerLevel) level()).getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(96))) {
            player.sendSystemMessage(Component.translatableWithFallback("hollowmancy.listener.say." + com.walnutbeach.hollowmancy.spell.Spell.slug(line), line)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }
    }

    @Override
    public void die(DamageSource source) {
        boolean alreadyDead = this.dead;
        super.die(source);
        if (alreadyDead || !(level() instanceof ServerLevel level)) return;
        if (EchoOfTheListener.isEcho(this)) {
            speak("...an echo of an echo...");
            EchoOfTheListener.defeated(level, this);
            return;
        }
        speak("...silence...");
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.5, getZ(), 300, 2.0, 2.0, 2.0, 0.3);
        Ending.begin(level, this.position());
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return ModSounds.LISTENER_AMBIENT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.LISTENER_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return ModSounds.LISTENER_DEATH.get();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** It never breaks the world: neither it nor its skulls grief blocks. */
    @EventBusSubscriber(modid = Hollowmancy.MODID)
    public static class NoGriefing {
        @SubscribeEvent
        public static void onGrief(EntityMobGriefingEvent event) {
            if (event.getEntity() instanceof Listener
                    || event.getEntity() instanceof WitherSkull skull && skull.getOwner() instanceof Listener) {
                event.setCanGrief(false);
            }
        }
    }
}
