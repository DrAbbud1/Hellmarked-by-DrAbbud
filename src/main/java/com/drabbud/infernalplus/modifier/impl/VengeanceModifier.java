package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Devuelve parte del daño recibido al atacante. Usa un guard estático de reentrada para
 * que dos mobs con Vengeance no entren en un ping-pong de daño (A daña a B, el daño a B
 * dispara su Vengeance que daña a A, etc.). El daño reflejado nunca se vuelve a reflejar.
 */
public class VengeanceModifier implements IModifier {

    // marca de "estamos dentro de un reflejo de daño" para evitar reentrada en cadena
    private static final ThreadLocal<Boolean> REFLECTING = ThreadLocal.withInitial(() -> false);

    public String id() { return "vengeance"; }

    public void onTakeDamage(LivingEntity self, LivingDamageEvent.Post event) {
        if (REFLECTING.get()) return; // este daño ya es un reflejo: no reflejar otra vez
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != self) {
            REFLECTING.set(true);
            try {
                attacker.hurt(self.damageSources().thorns(self), event.getNewDamage() * 0.5f);
            } finally {
                REFLECTING.set(false);
            }
        }
    }
}
