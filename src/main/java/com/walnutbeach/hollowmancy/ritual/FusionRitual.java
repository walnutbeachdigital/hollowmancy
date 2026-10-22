package com.walnutbeach.hollowmancy.ritual;

import com.walnutbeach.hollowmancy.magic.MagicHelper;
import com.walnutbeach.hollowmancy.spell.Spell;
import com.walnutbeach.hollowmancy.spell.Spells;
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

/** Binds the offering (main hand) and a catalyst (other hand) into one item. */
public class FusionRitual extends Ritual {

    private final Supplier<? extends Item> catalyst;
    private final Supplier<? extends Item> result;
    private Spell requiredSpell = null;

    public FusionRitual(String id, String name, String howTo, int requiredStage,
                        Supplier<? extends Item> offering, Supplier<? extends Item> catalyst,
                        int manaCost, Supplier<? extends Item> result) {
        super(id, name, howTo, requiredStage, offering, manaCost);
        this.catalyst = catalyst;
        this.result = result;
    }

    public FusionRitual parts(List<BlockPos> offsets, Predicate<BlockState> test) {
        requireAll(offsets, test);
        return this;
    }

    /** The player must know this spell to perform the fusion. */
    public FusionRitual requireSpell(Spell spell) {
        this.requiredSpell = spell;
        return this;
    }

    public Item getCatalyst() {
        return catalyst.get();
    }

    public Item getResult() {
        return result.get();
    }

    // Checked before anything is used up
    @Override
    public boolean canPerform(ServerPlayer player) {
        if (requiredSpell != null && !Spells.isKnown(MagicHelper.get(player), requiredSpell)) {
            player.displayClientMessage(Component.translatable("hollowmancy.fusion_ritual.you_must_know_to_bind_it", requiredSpell.getName())
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (!player.getOffhandItem().is(catalyst.get())) {
            String catalystName = new ItemStack(catalyst.get()).getHoverName().getString();
            player.displayClientMessage(Component.translatable("hollowmancy.fusion_ritual.hold_the_in_your_other_hand", catalystName)
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        return true;
    }

    @Override
    public void perform(ServerPlayer player, ServerLevel level, BlockPos altar) {
        performWithOffering(player, level, altar, ItemStack.EMPTY);
    }

    @Override
    public void performWithOffering(ServerPlayer player, ServerLevel level, BlockPos altar, ItemStack offering) {
        if (!player.getAbilities().instabuild) player.getOffhandItem().shrink(1);

        // Keep the offering's enchantments, wear and name on the fused item
        ItemStack fused = offering.isEmpty() ? new ItemStack(result.get()) : offering.transmuteCopy(result.get(), 1);
        Block.popResource(level, altar.above(), fused);

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, altar.getX() + 0.5, altar.getY() + 1.2, altar.getZ() + 0.5,
                40, 0.3, 0.4, 0.3, 0.3);
        level.playSound(null, altar, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
        player.sendSystemMessage(Component.translatable("hollowmancy.fusion_ritual.two_become_one", fused.getHoverName().getString())
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }
}