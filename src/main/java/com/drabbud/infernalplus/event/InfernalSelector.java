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

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String idStr = id.toString();

        // El filtro de listas tiene prioridad y funciona igual para vanilla y modded.
        if (IPConfig.WHITELIST_MODE.get()) {
            return IPConfig.ENTITY_WHITELIST.get().contains(idStr);
        }
        if (IPConfig.ENTITY_BLACKLIST.get().contains(idStr)) return false;

        // En modo whitelist ya devolvió arriba. En modo normal, decidir hostilidad de forma
        // genérica para cubrir mobs de otros mods que NO implementan la interfaz Enemy.
        return isHostile(mob);
    }

    /**
     * Hostilidad robusta y compatible con mods. No depende solo de 'instanceof Enemy'
     * (que muchos mobs modded no implementan). Considera varias señales:
     *  - implementa la interfaz Enemy de vanilla, o
     *  - su MobCategory es MONSTER (la mayoría de hostiles modded la declaran), o
     *  - es un Monster por jerarquía de clase.
     */
    private static boolean isHostile(Mob mob) {
        if (mob instanceof Enemy) return true;
        if (mob instanceof net.minecraft.world.entity.monster.Monster) return true;
        var category = mob.getType().getCategory();
        return category == net.minecraft.world.entity.MobCategory.MONSTER;
    }

    /** Tira el dado y, si procede, asigna modificadores. Devuelve true si se volvió infernal. */
    public static boolean roll(LivingEntity entity, RandomSource rng) {
        if (!InfernalState.isEnabled()) return false; // mod pausado
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

        applyInfernal(entity, chosen, tier, threat);
        return true;
    }

    /**
     * Fuerza a una entidad a ser infernal con una lista de modificadores y tier dados.
     * Reutilizado por el spawn natural y por el comando /infernal spawn.
     */
    public static void applyInfernal(LivingEntity entity, List<IModifier> chosen, int tier, double threat) {
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

        // nombre con color por tier; customNameVisible=false => MC lo muestra solo al apuntar de cerca
        applyTierName(entity, tier);

        // indicador visible a distancia: efecto glowing permanente (contorno brillante que
        // atraviesa paredes). Se puede desactivar en config. Complementa el aura de partículas.
        if (IPConfig.GLOW_ENABLED.get()) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.GLOWING,
                    -1, 0, false, false, false)); // duración -1 = permanente, sin partículas ni icono
        }
    }

    /** Pone el nombre del mob con el color de su tier. Visible solo al apuntar (comportamiento vanilla). */
    private static void applyTierName(LivingEntity entity, int tier) {
        String label;
        net.minecraft.ChatFormatting color;
        switch (tier) {
            case 2 -> { label = "\u26A1 Infernal"; color = net.minecraft.ChatFormatting.DARK_RED; }
            case 1 -> { label = "\u2726 Ultra";    color = net.minecraft.ChatFormatting.GOLD; }
            default -> { label = "\u2756 Elite";    color = net.minecraft.ChatFormatting.YELLOW; }
        }
        String base = entity.getType().getDescription().getString();

        // nombre meme para todos los tiers (se ve gracioso en el mensaje de muerte)
        String middle = "";
        if (IPConfig.MEME_NAMES_ENABLED.get()) {
            middle = com.drabbud.infernalplus.naming.MemeNameGenerator.generate(entity.getRandom()) + " ";
        }

        // resultado: "⚡ Infernal Rey Jochis Supremo Zombie"
        net.minecraft.network.chat.Component name = net.minecraft.network.chat.Component
                .literal(label + " " + middle + base)
                .withStyle(color);
        entity.setCustomName(name);
        entity.setCustomNameVisible(false);
    }

    /** Selecciona 'count' modificadores aleatorios (público para el comando). */
    public static List<IModifier> pickRandom(int count, RandomSource rng) {
        return pick(count, rng);
    }

    /** Selección ponderada sin repetir y respetando incompatibilidades. */
    private static List<IModifier> pick(int count, RandomSource rng) {
        // excluir de entrada los modificadores con peso 0 (desactivados en config)
        List<IModifier> pool = new ArrayList<>(IPModifiers.REGISTRY.stream()
                .filter(m -> com.drabbud.infernalplus.config.ModifierWeights.weightOf(m) > 0)
                .toList());
        List<IModifier> result = new ArrayList<>();
        Set<String> blocked = new HashSet<>();

        while (result.size() < count && !pool.isEmpty()) {
            int totalWeight = pool.stream()
                    .mapToInt(com.drabbud.infernalplus.config.ModifierWeights::weightOf).sum();
            if (totalWeight <= 0) break;
            int r = rng.nextInt(totalWeight);
            IModifier picked = null;
            for (IModifier m : pool) {
                r -= com.drabbud.infernalplus.config.ModifierWeights.weightOf(m);
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
