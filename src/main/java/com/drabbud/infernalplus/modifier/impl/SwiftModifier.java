package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Mucho más rápido que 'sprint'. Pensado para mobs que persiguen sin tregua.
 * Incompatible con slowness y con sprint (no apilar bonos de velocidad).
 */
public class SwiftModifier implements IModifier {
    public String id() { return "swift"; }
    public int weight() { return 7; }
    public String[] incompatibleWith() { return new String[]{"slowness", "sprint"}; }

    public void onApply(LivingEntity e) {
        ModifierHelpers.addAttribute(e, Attributes.MOVEMENT_SPEED, "swift", 0.9,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
