package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.magic.MagicData;
import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class InfusionRitual extends Ritual {

    private final Supplier<? extends Item> result;

    public InfusionRitual(String id, String name, String howTo, int requiredStage,
                          Supplier<? extends Item> offering, int manaCost, Supplier<? extends Item> result) {
        super(id, name, howTo, requiredStage, offering, manaCost);
        this.result = result;
    }

    /** Add pattern pieces. Returns itself so calls can be chained. */
    public InfusionRitual parts(List<BlockPos> offsets, Predicate<BlockState> test) {
        requireAll(offsets, test);
        return this;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        ItemStack artifact = new ItemStack(result.get());
        Block.popResource(level, altar.above(), artifact);

        player.sendSystemMessage(Component.translatable("hollowmancy.infusion_ritual.the_offering_is_reforged", artifact.getHoverName().getString())
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));

        // The first artifact ever created promotes the player to Mastery
        MagicData data = MagicHelper.get(player);
        if (data.stage() == MagicStage.ARCANE_ARTS) {
            MagicHelper.set(player, data.withStage(MagicStage.MASTERY));
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(),
                    80, 0.5, 1.0, 0.5, 0.4);
            level.playSound(null, altar, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(Component.translatable("hollowmancy.infusion_ritual.you_have_made_something_that_did")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
    }

    public Item getResult() {
        return result.get();
    }
}