package com.walnutbeach.hollowmancy.item;

import com.walnutbeach.hollowmancy.Hollowmancy;
import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import com.walnutbeach.hollowmancy.HollowmancyConfig;

public class HolyWaterItem extends Item {

    public static final int DRINK_CLEANSE = 15;
    public static final int SPLASH_CLEANSE = 8;
    public static final float SPLASH_DAMAGE = 8.0f;
    public static final int DRINK_COOLDOWN = 600; // 30 seconds

    /** Mobs that holy water burns: the undead and the Hollow's servants. */
    public static final TagKey<EntityType<?>> UNHOLY =
        TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "unholy"));

    public HolyWaterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            MagicData data = MagicHelper.get(player);
            int after = Math.max(0, data.corruption() - HollowmancyConfig.HOLY_WATER_CLEANSE.get());
            MagicHelper.set(player, data.withCorruption(after));
            player.removeEffect(MobEffects.DARKNESS);
            player.getCooldowns().addCooldown(this, DRINK_COOLDOWN);
            player.displayClientMessage((data.corruption() > 0 ? Component.translatable("hollowmancy.holy_water_item.the_water_runs_clean_through_you", after) : Component.translatable("hollowmancy.holy_water_item.it_tastes_of_nothing_at_all"))
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), true);
            ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.4, 0.6, 0.4, 0.02);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (stack.isEmpty()) return bottle;
                if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            }
        }
        return stack;
    }

    /** What a splash does where it lands. Shared with the thrown bottle. */
    public static void splash(ServerLevel level, Vec3 pos, Entity thrower) {
        AABB area = AABB.ofSize(pos, 8.0, 4.0, 8.0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            if (victim instanceof ServerPlayer player) {
                MagicData data = MagicHelper.get(player);
                if (data.corruption() > 0) {
                    MagicHelper.set(player, data.withCorruption(data.corruption() - SPLASH_CLEANSE));
                    player.removeEffect(MobEffects.DARKNESS);
                }
            } else if (victim.getType().is(UNHOLY)) {
                victim.hurt(level.damageSources().indirectMagic(thrower, thrower), SPLASH_DAMAGE);
                level.sendParticles(ParticleTypes.SMOKE, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(),
                    15, 0.3, 0.4, 0.3, 0.02);
            }
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.5f, 1.6f);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("hollowmancy.holy_water_item.drink_corruption", DRINK_CLEANSE).withStyle(ChatFormatting.AQUA));
    }
}
