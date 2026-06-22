package com.drabbud.infernalplus.client;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Emite un aura de partículas alrededor de los mobs infernales. La cantidad y el tipo
 * dependen del tier para que los más peligrosos se vean más imponentes.
 * Se ejecuta en servidor con sendParticles (no necesita código de cliente ni red custom).
 */
public final class InfernalAura {

    public static void tick(LivingEntity e, int tier) {
        if (!(e.level() instanceof ServerLevel sl)) return;

        // frecuencia: Elite cada 6 ticks, Ultra cada 4, Infernal cada 2
        int interval = switch (tier) {
            case 2 -> 2;
            case 1 -> 4;
            default -> 6;
        };
        if (e.tickCount % interval != 0) return;

        // cantidad de partículas por emisión, escalada por tier
        int count = switch (tier) {
            case 2 -> 6;
            case 1 -> 3;
            default -> 1;
        };

        // tipo de partícula por tier: Elite humo, Ultra llama, Infernal alma+llama
        ParticleOptions particle = switch (tier) {
            case 2 -> ParticleTypes.SOUL_FIRE_FLAME;
            case 1 -> ParticleTypes.FLAME;
            default -> ParticleTypes.SMOKE;
        };

        double w = e.getBbWidth() * 0.6;
        double h = e.getBbHeight();
        sl.sendParticles(particle,
                e.getX(), e.getY() + h * 0.5, e.getZ(),
                count, w, h * 0.4, w, 0.01);

        // los Infernal sueltan además un destello de alma para mayor presencia
        if (tier == 2) {
            sl.sendParticles(ParticleTypes.SOUL,
                    e.getX(), e.getY() + h * 0.5, e.getZ(),
                    2, w, h * 0.4, w, 0.02);
        }
    }

    private InfernalAura() {}
}
