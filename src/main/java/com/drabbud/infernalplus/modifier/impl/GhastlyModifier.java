package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Inmune a daño a distancia (proyectiles). */
public class GhastlyModifier implements IModifier {
    public String id() { return "ghastly"; }
    public int weight() { return 6; }
    public void onIncomingDamage(LivingEntity self, LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (source.getDirectEntity() instanceof Projectile || source.getEntity() instanceof Projectile) {
            event.setCanceled(true);
        }
    }
}
