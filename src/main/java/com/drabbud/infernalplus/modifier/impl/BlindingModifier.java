package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Ciega a jugadores. */
public class BlindingModifier implements IModifier {
    public String id() { return "blinding"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
        }
    }
}
