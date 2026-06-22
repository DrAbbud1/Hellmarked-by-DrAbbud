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

        // aura de partículas según el tier (más intensa cuanto mayor el tier)
        com.drabbud.infernalplus.client.InfernalAura.tick(living, data.tier());
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
            com.drabbud.infernalplus.compat.ApotheosisLoot.dropFor(self, sd, event.getSource());

            // drop del corazón de vida: probabilidad escalada por número de modificadores
            int mods = sd.resolve().size();
            double heartChance = IPConfig.LIFE_HEART_DROP_PER_MOD.get() * mods;
            if (self.getRandom().nextDouble() < heartChance) {
                self.spawnAtLocation(
                        new net.minecraft.world.item.ItemStack(
                                com.drabbud.infernalplus.item.IPItems.LIFE_HEART.get()), 0.0F);
            }

            // drop de cristales: cada tipo escalado por número de modificadores
            double crystalChance = IPConfig.ARMOR_CRYSTAL_DROP_PER_MOD.get() * mods;
            if (self.getRandom().nextDouble() < crystalChance) {
                // elegir aleatoriamente qué cristal cae (armadura/daño/velocidad)
                int pick = self.getRandom().nextInt(3);
                net.minecraft.world.item.Item crystal = switch (pick) {
                    case 1 -> com.drabbud.infernalplus.item.IPItems.DAMAGE_CRYSTAL.get();
                    case 2 -> com.drabbud.infernalplus.item.IPItems.SPEED_CRYSTAL.get();
                    default -> com.drabbud.infernalplus.item.IPItems.ARMOR_CRYSTAL.get();
                };
                self.spawnAtLocation(new net.minecraft.world.item.ItemStack(crystal), 0.0F);
            }
        }
    }

    /** Tick de jugadores: rastreo de inactividad (awareness) y bonus de cristales del equipo. */
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.drabbud.infernalplus.awareness.AwarenessTracker.tick(sp);
            // recalcular el bonus de cristales según el equipo, espaciado para rendimiento
            if (sp.tickCount % 10 == 0) {
                com.drabbud.infernalplus.item.CrystalBonusHandler.apply(sp);
            }
        }
    }

    /** ¿'other' está detrás de 'target' respecto a su dirección de mirada? */
    private static boolean isBehind(LivingEntity target, LivingEntity other) {
        net.minecraft.world.phys.Vec3 look = target.getLookAngle().normalize();
        net.minecraft.world.phys.Vec3 toOther = other.position().subtract(target.position()).normalize();
        // producto punto < 0 => el atacante está en el hemisferio trasero
        return look.dot(toOther) < -0.2;
    }

    @SubscribeEvent
    public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        com.drabbud.infernalplus.command.InfernalCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onExperienceDrop(net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent event) {
        LivingEntity self = event.getEntity();
        InfernalData sd = data(self);
        if (!sd.isInfernal()) return;
        if (!IPConfig.XP_BONUS_ENABLED.get()) return;

        int mods = sd.resolve().size();
        // xp final = base * (1 + xpPerModifier * numModificadores)
        double mult = 1.0 + IPConfig.XP_PER_MODIFIER.get() * mods;
        int original = event.getOriginalExperience();
        int boosted = (int) Math.round(original * mult);
        event.setDroppedExperience(boosted);
    }

    /** Reaplica el bonus de vida de los corazones al entrar al mundo (el atributo no persiste solo). */
    @SubscribeEvent
    public static void onPlayerLogin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player p) {
            int hearts = p.getData(IPAttachments.LIFE_HEARTS.get());
            com.drabbud.infernalplus.item.LifeHeartItem.applyHealthBonus(p, hearts);
        }
    }

    /** Reaplica el bonus tras respawnear (muerte). */
    @SubscribeEvent
    public static void onPlayerRespawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player p) {
            int hearts = p.getData(IPAttachments.LIFE_HEARTS.get());
            com.drabbud.infernalplus.item.LifeHeartItem.applyHealthBonus(p, hearts);
        }
    }

    private static InfernalData data(LivingEntity e) {
        return e.getData(IPAttachments.INFERNAL.get());
    }

    private InfernalEventHandler() {}
}
