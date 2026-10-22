package com.walnutbeach.hollowmancy.magic;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.walnutbeach.hollowmancy.Hollowmancy;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Hollowmancy.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal(Hollowmancy.MODID)
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("stage")
                                .then(Commands.literal("get").executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    MagicData data = MagicHelper.get(player);
                                    ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.stage_mana_knowledge_corruption_learned", data.stage(), MagicStage.name(data.stage()), data.mana(), data.maxMana(), data.knowledge(), data.corruption(), data.learned()), false);
                                    return 1;
                                }))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("stage", IntegerArgumentType.integer(1, 7)).executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int stage = IntegerArgumentType.getInteger(ctx, "stage");
                                            MagicHelper.set(player, MagicHelper.get(player).withStage(stage));
                                            ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.stage_set_to", stage, MagicStage.name(stage)), false);
                                            return 1;
                                        }))))

                        .then(Commands.literal("mana")
                                .then(Commands.literal("fill").executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    MagicData data = MagicHelper.get(player);
                                    MagicHelper.set(player, data.withMana(data.maxMana()));
                                    ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.mana_filled"), false);
                                    return 1;
                                }))
                                .then(Commands.literal("empty").executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    MagicHelper.set(player, MagicHelper.get(player).withMana(0));
                                    ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.mana_emptied"), false);
                                    return 1;
                                }))
                                .then(Commands.literal("max")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000)).executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            MagicHelper.set(player, MagicHelper.get(player).withMaxMana(amount));
                                            ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.max_mana_set_to", amount), false);
                                            return 1;
                                        }))))

                        .then(Commands.literal("knowledge")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000)).executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            MagicData data = MagicHelper.get(player);
                                            MagicHelper.set(player, data.withKnowledge(data.knowledge() + amount));
                                            ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.added_knowledge", amount), false);
                                            return 1;
                                        }))))

                        .then(Commands.literal("corruption")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            MagicHelper.set(player, MagicHelper.get(player).withCorruption(amount));
                                            ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.corruption_set_to", amount), false);
                                            return 1;
                                        }))))

                        .then(Commands.literal("spells")
                                .then(Commands.literal("forget").executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    MagicHelper.set(player, MagicHelper.get(player).clearLearned());
                                    ctx.getSource().sendSuccess(() -> Component.translatable("hollowmancy.mod_commands.forgot_all_learned_spells"), false);
                                    return 1;
                                })))
        );
    }
}