package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Pega más fuerte. */
public class RustModifier implements IModifier {
    public String id() { return "rust"; }
    public String[] incompatibleWith() { return new String[]{"weakness"}; }
    public void onApply(LivingEntity e) {
        ModifierHelpers.addAttribute(e, Attributes.ATTACK_DAMAGE, "rust", 1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
