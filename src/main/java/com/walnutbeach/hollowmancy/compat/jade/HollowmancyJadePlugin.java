package com.walnutbeach.hollowmancy.compat.jade;

import com.walnutbeach.hollowmancy.Hollowmancy;

import com.walnutbeach.hollowmancy.block.InfuserBlockEntity;
import com.walnutbeach.hollowmancy.block.ManaBeaconBlockEntity;
import com.walnutbeach.hollowmancy.familiar.FamiliarEntity;
import com.walnutbeach.hollowmancy.magic.MagicStage;
import com.walnutbeach.hollowmancy.ritual.InfusionRitual;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade (the "what am I looking at" overlay) support. Only loaded when Jade is installed.
 * Look at a battery, engine, Infuser or Mana Beacon to see its mana and what it's doing,
 * or at a familiar to see its bond.
 */
@WailaPlugin
public class HollowmancyJadePlugin implements IWailaPlugin {
    public static final ResourceLocation BLOCKS = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "blocks");
    public static final ResourceLocation FAMILIARS = ResourceLocation.fromNamespaceAndPath(Hollowmancy.MODID, "familiars");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BlockData.INSTANCE, BlockEntity.class);
        registration.registerEntityDataProvider(FamiliarData.INSTANCE, FamiliarEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BlockTooltip.INSTANCE, Block.class);
        registration.registerEntityComponent(FamiliarTooltip.INSTANCE, FamiliarEntity.class);
    }

    static boolean ours(BlockState state) {
        return Hollowmancy.MODID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    static String id(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }

    static MutableComponent key(String name, Object... args) {
        return Component.translatable("jade.hollowmancy." + name, args);
    }

    /** A five-segment bar like ■■■□□. */
    static String bar(int value, int max) {
        int filled = max <= 0 ? 0 : Math.round(5f * Math.min(value, max) / max);
        return "■".repeat(filled) + "□".repeat(5 - filled);
    }

    // ---------------- Server side: what the client can't see on its own ----------------

    public enum BlockData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BlockEntity be = accessor.getBlockEntity();
            if (be == null || !ours(accessor.getBlockState())) return;

            if (be instanceof InfuserBlockEntity infuser) {
                data.putInt("mana", infuser.getMana());
                data.putInt("progress", infuser.getProgressPercent());
                data.putInt("attuned", infuser.getAttunedStage());
                InfusionRitual recipe = InfuserBlockEntity.findInfusion(infuser.getInput().getStackInSlot(0));
                if (recipe != null) {
                    data.putInt("cost", recipe.getManaCost());
                    data.putString("making", new ItemStack(recipe.getResult()).getHoverName().getString());
                }
                String problem = infuser.problem();
                if (problem != null) data.putString("problem", problem);
                return;
            }
            if (be instanceof ManaBeaconBlockEntity beacon) {
                data.putInt("mana", beacon.getMana());
                data.putInt("max", ManaBeaconBlockEntity.BUFFER);
                if (beacon.getBlessing() != null) data.putString("blessing", beacon.getBlessing().name());
                return;
            }

            // Batteries, engines and anything else: find the mana in its saved data.
            CompoundTag saved = be.saveWithoutMetadata(accessor.getLevel().registryAccess());
            for (String name : saved.getAllKeys()) {
                if (!saved.contains(name, Tag.TAG_ANY_NUMERIC)) continue;
                String lower = name.toLowerCase();
                if (lower.contains("max") || lower.contains("capacity")) data.putInt("max", saved.getInt(name));
                else if (lower.contains("mana") || lower.contains("stored") || lower.contains("energy")) data.putInt("mana", saved.getInt(name));
                else if (lower.contains("progress") || lower.contains("burn") || lower.contains("fuel")) data.putInt("work", saved.getInt(name));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return BLOCKS;
        }
    }

    public enum FamiliarData implements IServerDataProvider<EntityAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof FamiliarEntity familiar)) return;
            data.putInt("level", familiar.getBondLevel());
            data.putInt("bond", familiar.getBond());
            int next = familiar.getBondLevel() < FamiliarEntity.LEVEL_AT.length ? FamiliarEntity.LEVEL_AT[familiar.getBondLevel()] : -1;
            data.putInt("next", next);
            data.putString("perk", familiar.describe(familiar.getBondLevel()));
        }

        @Override
        public ResourceLocation getUid() {
            return FAMILIARS;
        }
    }

    // ---------------- Client side: the lines in the overlay ----------------

    public enum BlockTooltip implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockState state = accessor.getBlockState();
            if (!ours(state)) return;
            CompoundTag data = accessor.getServerData();
            String id = id(state);

            switch (id) {
                case "vault_door" -> { tooltip.add(key("vault_door").withStyle(ChatFormatting.GOLD)); return; }
                case "warded_stone" -> { tooltip.add(key("warded").withStyle(ChatFormatting.DARK_PURPLE)); return; }
                case "portcullis" -> { tooltip.add(key("portcullis").withStyle(ChatFormatting.GRAY)); return; }
                default -> { }
            }

            // Charge levels and on/off read straight from the block state.
            for (Property<?> property : state.getProperties()) {
                if (property instanceof IntegerProperty ints && property.getName().equals("charge")) {
                    int max = ints.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(4);
                    int charge = state.getValue(ints);
                    tooltip.add(key("charge", bar(charge, max), charge * 100 / Math.max(1, max)).withStyle(ChatFormatting.AQUA));
                }
                if (property instanceof BooleanProperty bools && (property.getName().equals("active") || property.getName().equals("lit"))) {
                    boolean on = state.getValue(bools);
                    tooltip.add(key(on ? "running" : "idle").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.GRAY));
                }
            }

            if (data.contains("mana")) {
                int mana = data.getInt("mana");
                if (data.contains("max")) tooltip.add(key("mana_of", mana, data.getInt("max"), bar(mana, data.getInt("max"))).withStyle(ChatFormatting.LIGHT_PURPLE));
                else tooltip.add(key("mana", mana).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            if (data.contains("cost")) tooltip.add(key("cost", data.getInt("cost"), data.getString("making")).withStyle(ChatFormatting.GRAY));
            if (data.contains("progress") && data.getInt("progress") > 0) tooltip.add(key("progress", data.getInt("progress"), bar(data.getInt("progress"), 100)));
            if (data.contains("attuned")) {
                int stage = data.getInt("attuned");
                tooltip.add(stage > 0 ? key("attuned", MagicStage.name(stage)).withStyle(ChatFormatting.DARK_AQUA)
                                      : key("unattuned").withStyle(ChatFormatting.RED));
            }
            if (data.contains("problem")) tooltip.add(problemText(data.getString("problem")).withStyle(ChatFormatting.YELLOW));
            if (id.equals("mana_beacon")) {
                tooltip.add(data.contains("blessing") ? key("blessing", data.getString("blessing"), ManaBeaconBlockEntity.RADIUS).withStyle(ChatFormatting.GOLD)
                                                      : key("no_blessing").withStyle(ChatFormatting.GRAY));
            }
        }

        private static MutableComponent problemText(String problem) {
            java.util.regex.Matcher waiting = java.util.regex.Pattern.compile("waiting for mana \\((\\d+)/(\\d+)\\)\\.").matcher(problem);
            if (waiting.matches()) return Component.translatable("hollowmancy.infuser.problem.waiting", waiting.group(1), waiting.group(2));
            String key = switch (problem) {
                case "idle. Give it an Infusion offering." -> "hollowmancy.infuser.problem.idle";
                case "this infusion is beyond whoever attuned it." -> "hollowmancy.infuser.problem.beyond";
                case "full. Take the finished item." -> "hollowmancy.infuser.problem.full";
                default -> null;
            };
            return key == null ? Component.literal(capitalise(problem)) : Component.translatable(key);
        }

        private static String capitalise(String s) {
            return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }

        @Override
        public ResourceLocation getUid() {
            return BLOCKS;
        }
    }

    public enum FamiliarTooltip implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.contains("level")) return;
            int next = data.getInt("next");
            tooltip.add(next < 0 ? key("bond_max", data.getInt("level")).withStyle(ChatFormatting.LIGHT_PURPLE)
                                 : key("bond", data.getInt("level"), data.getInt("bond"), next).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal(data.getString("perk")).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        @Override
        public ResourceLocation getUid() {
            return FAMILIARS;
        }
    }
}
