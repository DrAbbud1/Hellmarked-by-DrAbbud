package com.drabbud.infernalplus.event;

import com.drabbud.infernalplus.InfernalPlus;
import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.attachment.InfernalData;
import com.drabbud.infernalplus.config.IPConfig;
import com.drabbud.infernalplus.modifier.IModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = InfernalPlus.MODID)
public final class InfernalEventHandler {

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        if (!IPConfig.AFFECT_BOSSES.get()
                && (living instanceof EnderDragon || living instanceof WitherBoss)) {
            return;
        }
        InfernalSelector.roll(living, living.getRandom());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living.level().isClientSide()) return;

        InfernalData data = data(living);
        if (!data.isInfernal()) return;

        data.tickCooldowns();
        for (IModifier m : data.resolve()) m.onTick(living);
    }

    @SubscribeEvent
    public static void onHurtTarget(LivingIncomingDamageEvent event) {
        // El atacante es infernal -> aplica efectos ofensivos a la víctima
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            InfernalData ad = data(attacker);
            if (ad.isInfernal() && event.getEntity() instanceof LivingEntity victim) {
                for (IModifier m : ad.resolve()) m.onHurtTarget(attacker, victim);

                // backstab: si pilla a la víctima por la espalda, daño extra
                if (IPConfig.AWARENESS_ENABLED.get() && IPConfig.AWARENESS_BACKSTAB.get()
                        && victim instanceof net.minecraft.world.entity.player.Player) {
                    if (isBehind(victim, attacker)) {
                        event.setAmount(event.getAmount()
                                * IPConfig.AWARENESS_BACKSTAB_BONUS.get().floatValue());
                    }
                }
            }
        }
        // La víctima es infernal -> efectos defensivos / modificación de daño entrante
        if (event.getEntity() instanceof LivingEntity self) {
            InfernalData sd = data(self);
            if (sd.isInfernal()) {
                for (IModifier m : sd.resolve()) m.onIncomingDamage(self, event);
            }
        }
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        LivingEntity self = event.getEntity();
        InfernalData sd = data(self);
        if (sd.isInfernal()) {
            for (IModifier m : sd.resolve()) m.onTakeDamage(self, event);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity self = event.getEntity();
        if (self.level().isClientSide()) return;
        InfernalData sd = data(self);
        if (sd.isInfernal()) {
            for (IModifier m : sd.resolve()) m.onDeath(self);
            // loot con afijos de Apotheosis (si está instalado)
            com.drabbud.infernalplus.compat.ApotheosisLoot.dropFor(self, sd);
        }
    }

    /** Tick de jugadores: actualiza el rastreo de inactividad para awareness. */
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.drabbud.infernalplus.awareness.AwarenessTracker.tick(sp);
        }
    }

    /** ¿'other' está detrás de 'target' respecto a su dirección de mirada? */
    private static boolean isBehind(LivingEntity target, LivingEntity other) {
        net.minecraft.world.phys.Vec3 look = target.getLookAngle().normalize();
        net.minecraft.world.phys.Vec3 toOther = other.position().subtract(target.position()).normalize();
        // producto punto < 0 => el atacante está en el hemisferio trasero
        return look.dot(toOther) < -0.2;
    }

    private static InfernalData data(LivingEntity e) {
        return e.getData(IPAttachments.INFERNAL.get());
    }

    private InfernalEventHandler() {}
}
