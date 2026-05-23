package me.mar.campfires;

import com.mojang.serialization.Codec;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MarsCampfires.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> CAMPFIRE_EXPIRES_AT =
            ATTACHMENTS.register("campfire_expires_at", () -> AttachmentType.builder(() -> -1L)
                    .serialize(Codec.LONG.fieldOf("expires_at"), expiresAt -> expiresAt >= 0L)
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
