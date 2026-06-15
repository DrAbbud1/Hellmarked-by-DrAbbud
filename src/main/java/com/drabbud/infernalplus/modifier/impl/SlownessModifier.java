package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Ralentiza al objetivo. */
public class SlownessModifier implements IModifier {
    public String id() { return "slowness"; }
    public String[] incompatibleWith() { return new String[]{"sprint"}; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
    }
}
