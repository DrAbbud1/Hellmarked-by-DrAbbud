package com.drabbud.infernalplus.attachment;

import com.drabbud.infernalplus.InfernalPlus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class IPAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, InfernalPlus.MODID);

    public static final Supplier<AttachmentType<InfernalData>> INFERNAL =
            ATTACHMENTS.register("infernal", () -> AttachmentType
                    .builder(() -> InfernalData.EMPTY)
                    .serialize(InfernalData.CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<com.drabbud.infernalplus.awareness.AwarenessData>> AWARENESS =
            ATTACHMENTS.register("awareness", () -> AttachmentType
                    .builder(() -> com.drabbud.infernalplus.awareness.AwarenessData.EMPTY)
                    .serialize(com.drabbud.infernalplus.awareness.AwarenessData.CODEC)
                    .build());

    // cuántos corazones de vida ha consumido el jugador (para tope y para reaplicar el bonus)
    public static final Supplier<AttachmentType<Integer>> LIFE_HEARTS =
            ATTACHMENTS.register("life_hearts", () -> AttachmentType
                    .builder(() -> 0)
                    .serialize(com.mojang.serialization.Codec.INT)
                    .copyOnDeath()
                    .build());

    private IPAttachments() {}
}
