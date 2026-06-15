package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;

/** Incendia al objetivo. */
public class FieryModifier implements IModifier {
    public String id() { return "fiery"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        target.igniteForSeconds(6);
    }
}
