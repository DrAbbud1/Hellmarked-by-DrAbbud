package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Más rápido. */
public class SprintModifier implements IModifier {
    public String id() { return "sprint"; }
    public String[] incompatibleWith() { return new String[]{"slowness"}; }
    public void onApply(LivingEntity e) {
        ModifierHelpers.addAttribute(e, Attributes.MOVEMENT_SPEED, "sprint", 0.45,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
