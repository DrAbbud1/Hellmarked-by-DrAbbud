package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;

/** Se cura una fracción del daño que inflige. */
public class LifestealModifier implements IModifier {
    public String id() { return "lifesteal"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        self.heal(Math.min(4.0f, self.getMaxHealth() * 0.1f));
    }
}
