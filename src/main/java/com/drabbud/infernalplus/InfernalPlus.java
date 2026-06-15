package com.drabbud.infernalplus;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.config.IPConfig;
import com.drabbud.infernalplus.modifier.IPModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(InfernalPlus.MODID)
public class InfernalPlus {
    public static final String MODID = "infernalplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InfernalPlus(IEventBus modBus, ModContainer container) {
        IPModifiers.MODIFIERS.register(modBus);
        IPAttachments.ATTACHMENTS.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, IPConfig.SPEC);

        LOGGER.info("Infernal Plus cargado.");
    }
}
