package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Aplica debilidad al objetivo. */
public class WeaknessModifier implements IModifier {
    public String id() { return "weakness"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
    }
}
