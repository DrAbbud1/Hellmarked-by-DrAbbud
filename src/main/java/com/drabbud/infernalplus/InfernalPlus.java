package com.drabbud.infernalplus;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.config.IPConfig;
import com.drabbud.infernalplus.modifier.IPModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(InfernalPlus.MODID)
public class InfernalPlus {
    public static final String MODID = "infernalplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InfernalPlus(IEventBus modBus, ModContainer container) {
        IPModifiers.MODIFIERS.register(modBus);
        IPAttachments.ATTACHMENTS.register(modBus);
        com.drabbud.infernalplus.item.IPItems.ITEMS.register(modBus);
        com.drabbud.infernalplus.item.IPComponents.COMPONENTS.register(modBus);

        // el registry custom debe anunciarse al motor, no solo crearse
        modBus.addListener(this::registerRegistries);
        modBus.addListener(com.drabbud.infernalplus.item.IPItems::addToCreativeTab);

        container.registerConfig(ModConfig.Type.COMMON, IPConfig.SPEC);

        LOGGER.info("Hellmarked by DrAbbud cargado.");
    }

    private void registerRegistries(NewRegistryEvent event) {
        event.register(IPModifiers.REGISTRY);
    }
}
