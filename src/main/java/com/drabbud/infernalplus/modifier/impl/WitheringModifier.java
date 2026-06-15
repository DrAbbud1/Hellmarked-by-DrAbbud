package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Marchita. */
public class WitheringModifier implements IModifier {
    public String id() { return "withering"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
    }
}
