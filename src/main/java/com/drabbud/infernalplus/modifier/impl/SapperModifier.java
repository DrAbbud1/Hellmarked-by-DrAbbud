package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Drena experiencia/energía: fatiga minera al jugador. */
public class SapperModifier implements IModifier {
    public String id() { return "sapper"; }
    public void onHurtTarget(LivingEntity self, LivingEntity target) {
        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 2));
        }
    }
}
