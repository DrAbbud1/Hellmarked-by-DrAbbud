package com.drabbud.infernalplus.item;

import com.drabbud.infernalplus.InfernalPlus;
import com.drabbud.infernalplus.config.IPConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class IPItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, InfernalPlus.MODID);

    // Corazón infernal: se consume para subir la vida máxima permanente (estilo Terraria).
    public static final Supplier<Item> LIFE_HEART =
            ITEMS.register("life_heart", () -> new LifeHeartItem(new Item.Properties().stacksTo(16)));

    // Cristales infernales: clic derecho sobre equipo para un bonus permanente (tope configurable).
    public static final Supplier<Item> ARMOR_CRYSTAL =
            ITEMS.register("armor_crystal", () -> new CrystalItem(
                    new Item.Properties().stacksTo(16),
                    CrystalItem.Type.ARMOR,
                    () -> IPConfig.ARMOR_CRYSTAL_MAX.get()));

    public static final Supplier<Item> DAMAGE_CRYSTAL =
            ITEMS.register("damage_crystal", () -> new CrystalItem(
                    new Item.Properties().stacksTo(16),
                    CrystalItem.Type.DAMAGE,
                    () -> IPConfig.DAMAGE_CRYSTAL_MAX.get()));

    public static final Supplier<Item> SPEED_CRYSTAL =
            ITEMS.register("speed_crystal", () -> new CrystalItem(
                    new Item.Properties().stacksTo(16),
                    CrystalItem.Type.SPEED,
                    () -> IPConfig.SPEED_CRYSTAL_MAX.get()));

    /** Añade los ítems del mod a una pestaña del inventario creativo. */
    public static void addToCreativeTab(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.INGREDIENTS) {
            event.accept(LIFE_HEART.get());
            event.accept(ARMOR_CRYSTAL.get());
            event.accept(DAMAGE_CRYSTAL.get());
            event.accept(SPEED_CRYSTAL.get());
        }
    }

    private IPItems() {}
}
