package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;

/** Empuje fuerte al golpear. */
public class KnockbackModifier implements IModifier {
    public String id() { return "knockback"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        double dx = target.getX() - self.getX();
        double dz = target.getZ() - self.getZ();
        target.knockback(1.6, -dx, -dz);
    }
}
