package com.drabbud.infernalplus.compat;

import com.drabbud.infernalplus.InfernalPlus;
import com.drabbud.infernalplus.attachment.InfernalData;
import com.drabbud.infernalplus.config.IPConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.fml.ModList;

import java.util.List;

/**
 * Compatibilidad OPCIONAL con Apotheosis. No referencia ninguna clase de Apotheosis,
 * por lo que el mod compila y funciona aunque Apotheosis no esté presente.
 *
 * Estrategia robusta: cuando un mob infernal muere, tiramos de una loot table de gear
 * con afijos que Apotheosis aporta vía datapack. Apotheosis se encarga de escalar la
 * rareza del afijo según el World Tier del jugador, así que aquí solo elegimos QUÉ tabla
 * usar según el tier del mob (Elite/Ultra/Infernal) y con qué probabilidad.
 *
 * Si la tabla configurada no existe (Apotheosis ausente o cambió de rutas), no se suelta
 * nada y no se lanza ninguna excepción.
 */
public final class ApotheosisLoot {

    private static Boolean apotheosisPresent;

    private static boolean isApotheosisLoaded() {
        if (apotheosisPresent == null) {
            apotheosisPresent = ModList.get() != null && ModList.get().isLoaded("apotheosis");
        }
        return apotheosisPresent;
    }

    public static void dropFor(LivingEntity mob, InfernalData data) {
        if (!IPConfig.APOTH_LOOT_ENABLED.get()) return;
        if (!isApotheosisLoaded()) return;
        if (!(mob.level() instanceof ServerLevel level)) return;

        double chance = switch (data.tier()) {
            case 2 -> IPConfig.APOTH_DROP_CHANCE_INFERNAL.get();
            case 1 -> IPConfig.APOTH_DROP_CHANCE_ULTRA.get();
            default -> IPConfig.APOTH_DROP_CHANCE_ELITE.get();
        };
        if (mob.getRandom().nextDouble() >= chance) return;

        ResourceLocation tableId = tableForTier(data.tier());
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, tableId);

        LootTable table;
        try {
            table = level.getServer().reloadableRegistries().getLootTable(key);
        } catch (Exception e) {
            InfernalPlus.LOGGER.debug("Tabla de loot Apotheosis no disponible: {}", tableId);
            return;
        }
        if (table == LootTable.EMPTY) return;

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, nearestPlayer(mob))
                .create(LootContextParamSets.ENTITY);

        List<ItemStack> loot = table.getRandomItems(params);
        for (ItemStack stack : loot) {
            if (!stack.isEmpty()) {
                // API change: spawnAtLocation(ItemStack, float) is used in mappings
                mob.spawnAtLocation(stack, 0.0F);
            }
        }
    }

    private static net.minecraft.server.level.ServerPlayer nearestPlayer(LivingEntity mob) {
        var p = mob.level().getNearestPlayer(mob, 32.0);
        return p instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null;
    }

    /** Ruta de la loot table de Apotheosis a usar según tier; configurable a futuro. */
    private static ResourceLocation tableForTier(int tier) {
        String path = switch (tier) {
            case 2 -> "affix/gear/infernal";
            case 1 -> "affix/gear/ultra";
            default -> "affix/gear/elite";
        };
        // las tablas reales viven bajo el namespace de este mod; el usuario las provee
        // como datapack que delega en Apotheosis. Default apunta a nuestro namespace.
        return ResourceLocation.fromNamespaceAndPath(InfernalPlus.MODID, path);
    }

    private ApotheosisLoot() {}
}
