package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Hace al mob más grande. El atributo SCALE (1.20.5+) ajusta automáticamente
 * el modelo, la hitbox, el alcance y el knockback. Sube también vida y un poco de daño
 * para que el tamaño se sienta amenazante.
 */
public class GiantModifier implements IModifier {
    public String id() { return "giant"; }
    public int weight() { return 8; }

    public void onApply(LivingEntity e) {
        // +60% de tamaño
        ModifierHelpers.addAttribute(e, Attributes.SCALE, "giant_scale", 0.6,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        // vida y daño acordes al tamaño
        ModifierHelpers.addAttribute(e, Attributes.MAX_HEALTH, "giant_health", 1.5,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        ModifierHelpers.addAttribute(e, Attributes.ATTACK_DAMAGE, "giant_damage", 0.5,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        // un gigante pesado resiste mejor el empuje
        ModifierHelpers.addAttribute(e, Attributes.KNOCKBACK_RESISTANCE, "giant_kbr", 0.5,
                AttributeModifier.Operation.ADD_VALUE);
        e.setHealth(e.getMaxHealth());
    }
}
