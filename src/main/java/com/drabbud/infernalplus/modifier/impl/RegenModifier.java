package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;

/** Regeneración rápida cuando no está a full vida. */
public class RegenModifier implements IModifier {
    public String id() { return "regen"; }
    public void onTick(LivingEntity e) {
        if (e.tickCount % 20 == 0 && e.getHealth() < e.getMaxHealth()) {
            e.heal(2.0f);
        }
    }
}
