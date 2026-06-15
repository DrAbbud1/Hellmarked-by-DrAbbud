package com.drabbud.infernalplus.awareness;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.config.IPConfig;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

/**
 * Calcula qué tan "descuidado" está un jugador, devolviendo un factor de amenaza
 * en [1.0 .. maxThreat]. 1.0 = totalmente alerta; valores mayores = más peligro.
 *
 * Factores que suben la amenaza (configurables):
 *  - Noche / oscuridad: el jugador en luz baja o de noche es más vulnerable.
 *  - Insomnio: ticks sin dormir (mismo contador que usan los Phantoms).
 *  - Inactividad: llevar muchos ticks sin moverse (AFK o distraído).
 *
 * El "backstab" (ataque por la espalda) no se calcula aquí porque depende del ángulo
 * mob→jugador en el momento del golpe; se maneja en el handler de daño.
 */
public final class AwarenessTracker {

    /** Actualiza el contador de inactividad del jugador. Llamar cada tick de servidor. */
    public static void tick(ServerPlayer player) {
        AwarenessData data = player.getData(IPAttachments.AWARENESS.get());
        double dx = player.getX() - data.lastX;
        double dy = player.getY() - data.lastY;
        double dz = player.getZ() - data.lastZ;
        double movedSqr = dx * dx + dy * dy + dz * dz;

        if (movedSqr < 0.0025) { // < 0.05 bloques de movimiento
            data.idleTicks = Math.min(data.idleTicks + 1, 24000);
        } else {
            data.idleTicks = 0;
        }
        data.lastX = player.getX();
        data.lastY = player.getY();
        data.lastZ = player.getZ();
        player.setData(IPAttachments.AWARENESS.get(), data);
    }

    /**
     * Factor de amenaza para un jugador concreto. Multiplica probabilidad de spawn
     * infernal y, opcionalmente, la fuerza de los mobs.
     */
    public static double threatFor(Player player) {
        if (!IPConfig.AWARENESS_ENABLED.get()) return 1.0;
        double threat = 1.0;

        // --- noche / oscuridad ---
        if (IPConfig.AWARENESS_DARKNESS.get()) {
            int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
            boolean night = !player.level().isDay();
            if (light <= 7) threat += IPConfig.AWARENESS_DARK_BONUS.get();
            else if (night) threat += IPConfig.AWARENESS_DARK_BONUS.get() * 0.5;
        }

        // --- insomnio (reutiliza la estadística de tiempo sin descansar) ---
        if (IPConfig.AWARENESS_INSOMNIA.get() && player instanceof ServerPlayer sp) {
            int sinceRest = sp.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            // a partir de ~1 día (24000 ticks) empieza a contar; tope a las 3 noches
            double nights = Math.min(Math.max(sinceRest - 24000, 0) / 24000.0, 3.0);
            threat += nights * IPConfig.AWARENESS_INSOMNIA_BONUS.get();
        }

        // --- inactividad / distracción ---
        if (IPConfig.AWARENESS_IDLE.get() && player instanceof ServerPlayer sp) {
            int idle = sp.getData(IPAttachments.AWARENESS.get()).idleTicks;
            // tras ~10 s quieto empieza a subir, tope a ~60 s
            if (idle > 200) {
                double f = Math.min((idle - 200) / 1000.0, 1.0);
                threat += f * IPConfig.AWARENESS_IDLE_BONUS.get();
            }
        }

        return Math.min(threat, IPConfig.AWARENESS_MAX.get());
    }

    /** Promedia la amenaza de los jugadores cercanos a un mob (radio fijo). */
    public static double threatNear(LivingEntity mob, double radius) {
        Player p = mob.level().getNearestPlayer(mob, radius);
        if (p == null) return 1.0;
        return threatFor(p);
    }

    private AwarenessTracker() {}
}
