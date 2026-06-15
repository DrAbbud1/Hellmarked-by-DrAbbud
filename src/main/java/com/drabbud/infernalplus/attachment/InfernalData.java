package com.drabbud.infernalplus.attachment;

import com.drabbud.infernalplus.modifier.IModifier;
import com.drabbud.infernalplus.modifier.IPModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estado por-entidad: lista de modificadores activos + mapa de cooldowns.
 * Es mutable en runtime y se serializa con CODEC.
 */
public final class InfernalData {

    public static final InfernalData EMPTY = new InfernalData(List.of(), 0);

    public static final Codec<InfernalData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("modifiers")
                    .forGetter(d -> d.modifierIds),
            Codec.INT.optionalFieldOf("tier", 0).forGetter(d -> d.tier)
    ).apply(inst, InfernalData::new));

    /** 0 = Elite, 1 = Ultra, 2 = Infernal. */
    private final int tier;
    private final List<ResourceLocation> modifierIds;
    private transient List<IModifier> resolved;
    private final Map<String, Integer> cooldowns = new HashMap<>();
    private final Map<String, Integer> counters = new HashMap<>();

    public InfernalData(List<ResourceLocation> modifierIds, int tier) {
        this.modifierIds = new ArrayList<>(modifierIds);
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    public boolean isInfernal() {
        return !modifierIds.isEmpty();
    }

    public List<ResourceLocation> ids() {
        return modifierIds;
    }

    /** Resuelve perezosamente las instancias desde el registry. */
    public List<IModifier> resolve() {
        if (resolved == null) {
            resolved = new ArrayList<>();
            for (ResourceLocation id : modifierIds) {
                IModifier m = IPModifiers.REGISTRY.get(id);
                if (m != null) resolved.add(m);
            }
        }
        return resolved;
    }

    public boolean has(IModifier m) {
        return resolve().contains(m);
    }

    // --- cooldowns (no se serializan; se reinician al recargar el chunk) ---
    public boolean ready(String key) {
        return cooldowns.getOrDefault(key, 0) <= 0;
    }

    public void setCooldown(String key, int ticks) {
        cooldowns.put(key, ticks);
    }

    public void tickCooldowns() {
        cooldowns.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
    }

    // --- contadores genéricos (p.ej. fusible de kamikaze); no se serializan ---
    public int getCounter(String key) {
        return counters.getOrDefault(key, 0);
    }

    public void setCounter(String key, int value) {
        counters.put(key, value);
    }
}
