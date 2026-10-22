package com.walnutbeach.hollowmancy.magic;

import com.walnutbeach.hollowmancy.Hollowmancy;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Hollowmancy.MODID);

    public static final Supplier<AttachmentType<MagicData>> MAGIC = ATTACHMENT_TYPES.register("magic",
            () -> AttachmentType.builder(() -> MagicData.DEFAULT)
                    .serialize(MagicData.CODEC)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
