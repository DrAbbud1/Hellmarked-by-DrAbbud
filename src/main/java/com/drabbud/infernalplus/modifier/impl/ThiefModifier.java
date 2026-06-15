package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Roba un ítem del inventario del jugador al golpear (con cooldown) y lo hace desaparecer. */
public class ThiefModifier implements IModifier {
    public String id() { return "thief"; }
    public int weight() { return 6; }

    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        if (!(target instanceof Player player)) return;
        var data = self.getData(com.drabbud.infernalplus.attachment.IPAttachments.INFERNAL.get());
        if (!data.ready("thief")) return;

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty()) {
                inv.removeItem(i, 1);
                data.setCooldown("thief", 200);
                break;
            }
        }
    }
}
