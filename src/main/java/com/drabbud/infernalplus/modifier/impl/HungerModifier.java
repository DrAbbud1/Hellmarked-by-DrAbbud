package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Hambre. */
public class HungerModifier implements IModifier {
    public String id() { return "hunger"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));
    }
}
