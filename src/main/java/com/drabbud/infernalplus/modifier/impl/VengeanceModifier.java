package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Devuelve parte del daño recibido al atacante. */
public class VengeanceModifier implements IModifier {
    public String id() { return "vengeance"; }
    public void onTakeDamage(LivingEntity self, LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != self) {
            attacker.hurt(self.damageSources().thorns(self), event.getNewDamage() * 0.5f);
        }
    }
}
