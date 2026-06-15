package com.drabbud.infernalplus.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Contrato de un modificador infernal. Cada modificador es un singleton sin estado;
 * el estado por-entidad (cooldowns, etc.) se guarda en el InfernalData attachment.
 */
public interface IModifier {

    /** Id corto y único, p.ej. "fiery". Se usa para serializar y para la traducción. */
    String id();

    /** Peso de selección. Mayor = más probable de aparecer. */
    default int weight() { return 10; }

    /** Modificadores incompatibles con este (por id). No se combinan en el mismo mob. */
    default String[] incompatibleWith() { return new String[0]; }

    /**
     * Se llama una vez cuando el mob recibe el modificador. Buen lugar para aplicar
     * AttributeModifiers permanentes (vida, velocidad, daño...).
     */
    default void onApply(LivingEntity entity) {}

    /** Tick del lado servidor mientras el mob viva. */
    default void onTick(LivingEntity entity) {}

    /** El mob infernal está a punto de golpear a su objetivo. */
    default void onHurtTarget(LivingEntity self, LivingEntity target) {}

    /** El mob infernal va a recibir daño (puede modificar el monto). */
    default void onIncomingDamage(LivingEntity self, LivingIncomingDamageEvent event) {}

    /** El mob infernal recibió daño efectivo. */
    default void onTakeDamage(LivingEntity self, LivingDamageEvent.Post event) {}

    /** El mob infernal muere. */
    default void onDeath(LivingEntity self) {}
}
