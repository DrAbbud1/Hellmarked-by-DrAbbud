package com.drabbud.infernalplus.event;

import com.drabbud.infernalplus.attachment.IPAttachments;
import com.drabbud.infernalplus.attachment.InfernalData;
import com.drabbud.infernalplus.config.IPConfig;
import com.drabbud.infernalplus.modifier.IModifier;
import com.drabbud.infernalplus.modifier.IPModifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InfernalSelector {

    /** ¿Esta entidad es candidata a volverse infernal? */
    public static boolean isCandidate(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return false;
        if (!(mob instanceof Enemy)) return false; // solo hostiles

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String idStr = id.toString();

        if (IPConfig.WHITELIST_MODE.get()) {
            return IPConfig.ENTITY_WHITELIST.get().contains(idStr);
        }
        return !IPConfig.ENTITY_BLACKLIST.get().contains(idStr);
    }

    /** Tira el dado y, si procede, asigna modificadores. Devuelve true si se volvió infernal. */
    public static boolean roll(LivingEntity entity, RandomSource rng) {
        if (entity.hasData(IPAttachments.INFERNAL.get())
                && entity.getData(IPAttachments.INFERNAL.get()).isInfernal()) {
            return false; // ya procesado
        }
        if (!isCandidate(entity)) return false;

        // la amenaza del jugador cercano sube la probabilidad de aparición
        double threat = com.drabbud.infernalplus.awareness.AwarenessTracker.threatNear(entity, 32.0);
        double chance = IPConfig.SPAWN_CHANCE.get() * threat;
        if (rng.nextDouble() >= chance) return false;

        // --- tier estilo Elite / Ultra / Infernal con tiradas anidadas ---
        int tier = 0; // Elite
        int min = IPConfig.MIN_MODIFIERS.get();
        int max = Math.max(min, IPConfig.MAX_MODIFIERS.get());
        int count = min + rng.nextInt(max - min + 1);

        if (rng.nextInt(Math.max(1, IPConfig.ULTRA_RARITY.get())) == 0) {
            tier = 1; // Ultra
            count += 3 + rng.nextInt(2);
            if (rng.nextInt(Math.max(1, IPConfig.INFERNAL_RARITY.get())) == 0) {
                tier = 2; // Infernal
                count += 3 + rng.nextInt(2);
            }
        }

        List<IModifier> chosen = pick(count, rng);
        if (chosen.isEmpty()) return false;

        List<ResourceLocation> ids = new ArrayList<>();
        for (IModifier m : chosen) {
            ids.add(IPModifiers.REGISTRY.getKey(m));
        }
        InfernalData data = new InfernalData(ids, tier);
        entity.setData(IPAttachments.INFERNAL.get(), data);

        // escalado de vida: por número de modificadores y, si procede, por amenaza
        double strengthMult = IPConfig.AWARENESS_AFFECTS_STRENGTH.get() ? threat : 1.0;
        double healthMult = 1.0 + IPConfig.HEALTH_PER_MODIFIER.get() * chosen.size() * strengthMult;
        var maxHealthAttr = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            ResourceLocation hid = ResourceLocation.fromNamespaceAndPath(
                    com.drabbud.infernalplus.InfernalPlus.MODID, "infernal_health");
            if (maxHealthAttr.getModifier(hid) == null) {
                maxHealthAttr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        hid, healthMult - 1.0,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        for (IModifier m : data.resolve()) {
            m.onApply(entity);
        }
        entity.setHealth(entity.getMaxHealth());
        return true;
    }

    /** Selección ponderada sin repetir y respetando incompatibilidades. */
    private static List<IModifier> pick(int count, RandomSource rng) {
        List<IModifier> pool = new ArrayList<>(IPModifiers.REGISTRY.stream().toList());
        List<IModifier> result = new ArrayList<>();
        Set<String> blocked = new HashSet<>();

        while (result.size() < count && !pool.isEmpty()) {
            int totalWeight = pool.stream().mapToInt(IModifier::weight).sum();
            if (totalWeight <= 0) break;
            int r = rng.nextInt(totalWeight);
            IModifier picked = null;
            for (IModifier m : pool) {
                r -= m.weight();
                if (r < 0) { picked = m; break; }
            }
            if (picked == null) break;

            pool.remove(picked);
            if (blocked.contains(picked.id())) continue;

            result.add(picked);
            blocked.add(picked.id());
            for (String inc : picked.incompatibleWith()) blocked.add(inc);
        }
        return result;
    }

    private InfernalSelector() {}
}
