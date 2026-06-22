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

            // aviso terrorífico: capa de sonidos al empezar la carga
            if (charge == 1) {
                double x = e.getX(), y = e.getY(), z = e.getZ();
                // capa grave/presagio: rugido del Ender Dragon, tono bajado
                level.playSound(null, x, y, z,
                        SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.4f, 0.6f);
                // capa aguda/alarma: chillido de Ghast
                level.playSound(null, x, y, z,
                        SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 1.2f, 0.7f);
                // capa "va a explotar": silbido de creeper agudo
                level.playSound(null, x, y, z,
                        SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.3f, 1.2f);
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

        // 1) Explosión vanilla: efecto visual, sonido y destrucción de bloques.
        level.explode(e, e.getX(), e.getY(), e.getZ(), power, Level.ExplosionInteraction.MOB);

        // 2) Daño directo garantizado a entidades cercanas. La explosión vanilla se mitiga
        //    casi por completo con armadura/protección de late-game, así que añadimos un
        //    componente de daño que escala con la cercanía y es más difícil de anular,
        //    pero decrece con la distancia para evitar one-shots injustos a quien se aleja.
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            double radius = power; // alcance del daño directo = potencia
            double radiusSqr = radius * radius;
            double baseDamage = IPConfig.KAMIKAZE_DIRECT_DAMAGE.get();
            var box = e.getBoundingBox().inflate(radius);
            for (LivingEntity victim : sl.getEntitiesOfClass(LivingEntity.class, box)) {
                if (victim == e) continue;
                double distSqr = victim.distanceToSqr(e);
                if (distSqr > radiusSqr) continue;
                // factor lineal 1.0 (pegado) -> 0.0 (borde del radio)
                double falloff = 1.0 - Math.sqrt(distSqr) / radius;
                if (falloff <= 0) continue;
                float dmg = (float) (baseDamage * falloff);
                victim.hurt(level.damageSources().explosion(e, e), dmg);
            }
        }

        e.discard();
    }
}
