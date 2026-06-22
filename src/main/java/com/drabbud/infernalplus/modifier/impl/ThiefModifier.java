package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Roba un ítem del jugador al golpear (con cooldown) y lo suelta al suelo junto al mob.
 *
 * Diseño simple y a prueba de fallos: el ítem se MUEVE de las manos del jugador al suelo
 * en un solo paso. Nunca se destruye (no se usa removeItem sin guardar) y nunca se duplica
 * (no se copia dejándoselo al jugador). No se equipa en el mob, así que no hay riesgo de
 * pérdida por dropChance. Funciona igual con ítems vanilla y modded.
 */
public class ThiefModifier implements IModifier {
    public String id() { return "thief"; }
    public int weight() { return 6; }

    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        if (!(target instanceof Player player)) return;
        if (self.level().isClientSide()) return;

        var data = self.getData(com.drabbud.infernalplus.attachment.IPAttachments.INFERNAL.get());
        if (!data.ready("thief")) return;

        ItemStack stolen = takeOneItem(player);
        if (stolen.isEmpty()) return;

        data.setCooldown("thief", 200);
        // soltar al suelo junto al mob: nunca se destruye ni se duplica
        self.spawnAtLocation(stolen, 0.5F);
    }

    /** Quita 1 ítem del jugador: primero la mano principal, si no el primer slot no vacío. */
    private static ItemStack takeOneItem(Player player) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty()) {
            return main.split(1);
        }
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty()) {
                return st.split(1);
            }
        }
        return ItemStack.EMPTY;
    }
}
