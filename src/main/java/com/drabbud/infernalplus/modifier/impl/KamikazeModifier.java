package com.drabbud.infernalplus.modifier.impl;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.config.IPConfig;
import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Cuando un jugador entra a menos de 2 bloques, el mob comienza a cargar (aviso visual/sonoro)
 * y tras un breve fusible explota. La explosión inflige daño masivo (instakill sin armadura
 * de diamante/netherita) y revienta bloques. Es esquivable: si te alejas durante el fusible,
 * la carga se cancela.
 */
public class KamikazeModifier implements IModifier {
    public String id() { return "kamikaze"; }
    public int weight() { return 4; }

    private static final double TRIGGER_DIST_SQR = 2.0 * 2.0;

    public void onTick(LivingEntity e) {
        Level level = e.level();
        if (level.isClientSide()) return;

        var data = e.getData(IPAttachments.INFERNAL.get());
        Player target = level.getNearestPlayer(e, 8.0);
        boolean inRange = target != null
                && !target.isCreative() && !target.isSpectator()
                && e.distanceToSqr(target) <= TRIGGER_DIST_SQR;

        int fuse = IPConfig.KAMIKAZE_FUSE_TICKS.get();

        if (IPConfig.KAMIKAZE_INSTANT.get()) {
            // modo instantáneo: detona en cuanto hay un jugador en rango
            if (inRange) { detonate(e, level); }
            return;
        }

        if (inRange) {
            int charge = data.getCounter("kamikaze") + 1;
            data.setCounter("kamikaze", charge);

            // aviso: silbido al empezar y humo cada tick
            if (charge == 1) {
                level.playSound(null, e.getX(), e.getY(), e.getZ(),
                        SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.2f, 0.8f);
            }
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE,
                        e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(),
                        4, 0.2, 0.2, 0.2, 0.01);
                sl.sendParticles(ParticleTypes.FLAME,
                        e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(),
                        2, 0.2, 0.2, 0.2, 0.01);
            }

            if (charge >= fuse) {
                detonate(e, level);
            }
        } else {
            // salió del rango: la carga se enfría
            if (data.getCounter("kamikaze") > 0) {
                data.setCounter("kamikaze", 0);
            }
        }
    }

    private void detonate(LivingEntity e, Level level) {
        float power = IPConfig.KAMIKAZE_POWER.get().floatValue();
        level.explode(e, e.getX(), e.getY(), e.getZ(), power, Level.ExplosionInteraction.MOB);
        e.discard();
    }
}
