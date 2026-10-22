package com.walnutbeach.hollowmancy.familiar;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

/**
 * A companion bound to one player. It can't be hurt, isn't saved with the world (it travels with its
 * owner instead), and grows stronger with its bond. The bond itself is stored on the player.
 */
public abstract class FamiliarEntity extends TamableAnimal {

    /** Bond needed for levels 1-5. */
    public static final int[] LEVEL_AT = {0, 10, 30, 60, 100};
    public static final int TELEPORT_DISTANCE = 20;

    private int bond = 0;

    protected FamiliarEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FLYING_SPEED, 0.6)
            .add(Attributes.ATTACK_DAMAGE, 4.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    public abstract FamiliarType getFamiliarType();

    /** What this familiar does for its owner at a given level (one line, for the status message). */
    public abstract String describe(int level);

    /** Called every 2 seconds while the owner is near. */
    protected abstract void blessOwner(ServerPlayer owner, int level);

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
    }

    // ---------------- Bond ----------------

    public int getBond() {
        return bond;
    }

    public int getBondLevel() {
        int level = 1;
        for (int i = 0; i < LEVEL_AT.length; i++) {
            if (bond >= LEVEL_AT[i]) level = i + 1;
        }
        return level;
    }

    public void setBond(int bond) {
        this.bond = bond;
        onLevelChanged(getBondLevel());
    }

    public void addBond(int amount) {
        int before = getBondLevel();
        bond += amount;
        if (getOwner() instanceof ServerPlayer owner) Familiars.saveBond(owner, getFamiliarType(), bond);
        int after = getBondLevel();
        if (after > before) {
            onLevelChanged(after);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + getBbHeight(), getZ(), 20, 0.4, 0.4, 0.4, 0.1);
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8f, 1.4f);
            }
            if (getOwner() instanceof ServerPlayer owner) {
                owner.sendSystemMessage(Component.translatable("hollowmancy.familiar_entity.your_s_bond_deepens_level", getFamiliarType().displayName(), after, describe(after)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
        }
    }

    /** Subclasses update their stats here. */
    protected void onLevelChanged(int level) {
    }

    public String statusLine() {
        int level = getBondLevel();
        String progress = level >= LEVEL_AT.length ? FamiliarText.tr("hollowmancy.familiar.max_bond", "max bond")
                : FamiliarText.tr("hollowmancy.familiar.bond", "bond %s/%s", bond, LEVEL_AT[level]);
        return FamiliarText.tr("hollowmancy.familiar.status", "%s, level %s (%s). %s", getFamiliarType().displayName(), level, progress, describe(level));
    }

    // ---------------- Following ----------------

    protected ServerPlayer ownerPlayer() {
        return getOwner() instanceof ServerPlayer player && player.level() == level() ? player : null;
    }

    /** Flying familiars: glide toward a point. */
    protected void hoverToward(Vec3 target) {
        Vec3 toTarget = target.subtract(position());
        double distance = toTarget.length();
        Vec3 motion = getDeltaMovement().scale(0.7);
        if (distance > 0.15) motion = motion.add(toTarget.normalize().scale(Math.min(distance * 0.05, 0.15)));
        setDeltaMovement(motion);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        ServerPlayer owner = ownerPlayer();
        if (owner == null) return;

        double distSqr = distanceToSqr(owner);
        if (!isOrderedToSit() && distSqr > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            moveTo(owner.getX() + random.nextDouble() - 0.5, owner.getY() + 0.5, owner.getZ() + random.nextDouble() - 0.5);
            getNavigation().stop();
        }

        if (distSqr < 24 * 24 && tickCount % 40 == 0) blessOwner(owner, getBondLevel());
        if (distSqr < 16 * 16 && server.getGameTime() % 1200 == 0) addBond(HollowmancyConfig.BOND_PER_MINUTE.get());   // a point of bond for every minute together
    }

    // ---------------- Talking to it ----------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isOwnedBy(player)) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                Familiars.dismiss(serverPlayer);
                player.displayClientMessage(Component.translatable("hollowmancy.familiar_entity.you_release_your_it_will_remember", getFamiliarType().displayName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            } else {
                setOrderedToSit(!isOrderedToSit());
                player.displayClientMessage(Component.literal(statusLine() + " ").append(Component.translatable(isOrderedToSit() ? "hollowmancy.familiar.staying" : "hollowmancy.familiar.following"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    // ---------------- Not a normal animal ----------------

    @Override
    public boolean shouldBeSaved() {
        return false;   // it travels with its owner; Familiars brings it back on login
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return !(target instanceof FamiliarEntity) && !(target instanceof Player) && !(target instanceof TamableAnimal tame && tame.isTame());
    }
}
