package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Reduce todo el daño entrante en 40%. */
public class BulwarkModifier implements IModifier {
    public String id() { return "bulwark"; }
    public int weight() { return 9; }
    public void onIncomingDamage(LivingEntity self, LivingIncomingDamageEvent event) {
        event.setAmount(event.getAmount() * 0.6f);
    }
}
