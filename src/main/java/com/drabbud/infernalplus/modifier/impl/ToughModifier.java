package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** +vida máxima y cura al aplicar. */
public class ToughModifier implements IModifier {
    public String id() { return "tough"; }
    public int weight() { return 14; }
    public void onApply(LivingEntity e) {
        ModifierHelpers.addAttribute(e, Attributes.MAX_HEALTH, "tough", 2.5,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        e.setHealth(e.getMaxHealth());
    }
}
