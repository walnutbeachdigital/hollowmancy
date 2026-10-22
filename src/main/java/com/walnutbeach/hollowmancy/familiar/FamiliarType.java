package com.walnutbeach.hollowmancy.familiar;

import com.walnutbeach.hollowmancy.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public enum FamiliarType {
    OWL("owl", "Owl", () -> Items.FEATHER, () -> ModEntities.OWL_FAMILIAR.get()),
    WISP("wisp", "Wisp", () -> Items.GLOWSTONE_DUST, () -> ModEntities.WISP_FAMILIAR.get()),
    GOLEM("golem", "Golem", () -> Items.IRON_BLOCK, () -> ModEntities.GOLEM_FAMILIAR.get()),
    MOTH("moth", "Crystal Moth", () -> Items.SPORE_BLOSSOM, () -> HollowFamiliars.MOTH_FAMILIAR.get()),
    GLIMMERHORN("glimmerhorn", "Glimmerhorn", () -> Items.GLOW_BERRIES, () -> HollowFamiliars.GLIMMERHORN_FAMILIAR.get());

    private final String id;
    private final String displayName;
    private final Supplier<Item> offering;
    private final Supplier<EntityType<? extends FamiliarEntity>> entityType;

    FamiliarType(String id, String displayName, Supplier<Item> offering, Supplier<EntityType<? extends FamiliarEntity>> entityType) {
        this.id = id;
        this.displayName = displayName;
        this.offering = offering;
        this.entityType = entityType;
    }

    public String id() { return id; }
    public String displayName() { return net.minecraft.locale.Language.getInstance().getOrDefault("hollowmancy.familiar.type." + id, displayName); }
    public Supplier<Item> offering() { return offering; }
    public EntityType<? extends FamiliarEntity> entityType() { return entityType.get(); }

    public static FamiliarType byId(String id) {
        for (FamiliarType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }
}
