package com.drabbud.infernalplus.item;

import com.drabbud.infernalplus.InfernalPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Aplica los bonus de los cristales AL JUGADOR según el equipo que lleva puesto.
 * No toca los componentes de los ítems, así que nunca rompe la armadura/daño base.
 *
 * Para cada tipo de cristal, suma los niveles de todas las piezas relevantes que el jugador
 * tiene equipadas (armadura para ARMOR; mano principal para DAMAGE/SPEED) y aplica un único
 * AttributeModifier con ese total. Idempotente: reemplaza el modificador anterior cada vez.
 */
public final class CrystalBonusHandler {

    private static ResourceLocation playerModId(CrystalItem.Type type) {
        return ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, "player_" + type.modId.getPath());
    }

    /** Llamar periódicamente para el jugador (cada ~10 ticks basta; el bonus no es urgente). */
    public static void apply(Player player) {
        for (CrystalItem.Type type : CrystalItem.Type.values()) {
            double total = sumForType(player, type);
            AttributeInstance inst = player.getAttribute(type.attribute);
            if (inst == null) continue;

            ResourceLocation id = playerModId(type);
            // quitar el modificador previo y, si hay bonus, volver a ponerlo con el total actual
            inst.removeModifier(id);
            if (total > 0) {
                inst.addTransientModifier(new AttributeModifier(id, total, type.op));
            }
        }
    }

    /** Suma los niveles de un tipo de cristal en las piezas relevantes del jugador. */
    private static double sumForType(Player player, CrystalItem.Type type) {
        double total = 0;
        if (type.worksOnArmor) {
            // armadura: las 4 piezas
            for (EquipmentSlot slot : new EquipmentSlot[]{
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                total += CrystalItem.getLevel(player.getItemBySlot(slot), type) * type.perUse;
            }
            // y también la mano principal si lleva un arma/escudo con cristal de armadura
            total += CrystalItem.getLevel(player.getMainHandItem(), type) * type.perUse;
        } else {
            // daño/velocidad: solo la mano principal
            total += CrystalItem.getLevel(player.getMainHandItem(), type) * type.perUse;
        }
        return total;
    }

    private CrystalBonusHandler() {}
}
