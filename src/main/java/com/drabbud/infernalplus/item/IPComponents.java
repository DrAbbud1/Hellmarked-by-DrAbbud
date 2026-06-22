package com.drabbud.infernalplus.item;

import com.drabbud.infernalplus.InfernalPlus;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Componente de datos que guarda cuántos puntos de armadura extra lleva una pieza de equipo
 * gracias a los cristales infernales aplicados. Se usa para el tope (+10) y para que el
 * event handler sepa cuánto bonus aplicar cuando el ítem está equipado.
 */
public final class IPComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, InfernalPlus.MODID);

    public static final Supplier<DataComponentType<Integer>> ARMOR_BONUS =
            COMPONENTS.register("armor_bonus", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                    .build());

    // mapa tipo_de_cristal -> nivel aplicado (ARMOR/DAMAGE/SPEED -> cuántos)
    public static final Supplier<DataComponentType<java.util.Map<String, Integer>>> CRYSTAL_LEVELS =
            COMPONENTS.register("crystal_levels", () -> DataComponentType.<java.util.Map<String, Integer>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.map(
                            java.util.HashMap::new,
                            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8,
                            net.minecraft.network.codec.ByteBufCodecs.VAR_INT))
                    .build());

    private IPComponents() {}
}
