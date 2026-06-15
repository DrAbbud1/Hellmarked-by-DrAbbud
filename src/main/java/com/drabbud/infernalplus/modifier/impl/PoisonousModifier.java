package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Envenena. */
public class PoisonousModifier implements IModifier {
    public String id() { return "poisonous"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
    }
}
