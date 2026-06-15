package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.entity.projectile.Projectile;

/** Inmune a daño a distancia (proyectiles). */
public class GhastlyModifier implements IModifier {
    public String id() { return "ghastly"; }
    public int weight() { return 6; }
    public void onIncomingDamage(LivingEntity self, LivingIncomingDamageEvent event) {
        var src = event.getSource();
        var direct = src.getDirectEntity();
        if (direct instanceof Projectile) {
            event.setCanceled(true);
        }
    }
}
