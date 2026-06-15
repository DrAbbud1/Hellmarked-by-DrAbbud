package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Explota al morir. */
public class ExplosiveModifier implements IModifier {
    public String id() { return "explosive"; }
    public int weight() { return 7; }
    public void onDeath(LivingEntity self) {
        Level level = self.level();
        if (!level.isClientSide()) {
            level.explode(self, self.getX(), self.getY(), self.getZ(), 2.5f, Level.ExplosionInteraction.MOB);
        }
    }
}
