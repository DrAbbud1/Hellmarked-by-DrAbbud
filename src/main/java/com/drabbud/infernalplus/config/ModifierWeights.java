package com.drabbud.infernalplus.config;

import com.drabbud.infernalplus.modifier.IModifier;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pesos configurables por modificador. El peso controla qué tan común es un modificador
 * en la selección ponderada. Peso 0 = desactivado (nunca aparece).
 *
 * Se define como una lista de "id=peso" en el config para no tener que listar 25 campos
 * fijos. Si un modificador no aparece en la lista, usa su peso por defecto del código.
 */
public final class ModifierWeights {

    private static ModConfigSpec.ConfigValue<List<? extends String>> WEIGHTS;
    private static Map<String, Integer> cache = null;

    /** Registra el campo en el builder del config. Llamar dentro de la construcción del SPEC. */
    public static void define(ModConfigSpec.Builder b) {
        b.push("modifier_weights");
        WEIGHTS = b.comment(
                        "Peso de cada modificador como \"id=peso\". Mayor peso = más común.",
                        "Peso 0 = desactivado (ese modificador nunca aparecerá).",
                        "Si un modificador no está listado, usa su peso por defecto.",
                        "Ejemplo: \"kamikaze=2\" lo hace más raro; \"fiery=0\" lo desactiva.")
                .defineListAllowEmpty("weights", defaultList(),
                        () -> "fiery=10", o -> o instanceof String s && s.contains("="));
        b.pop();
    }

    /** Lista por defecto: todos los modificadores con un peso de referencia. */
    private static List<String> defaultList() {
        return List.of(
                "tough=14", "sprint=10", "rust=10", "fiery=10", "poisonous=10",
                "withering=10", "weakness=10", "slowness=10", "blinding=10", "hunger=10",
                "thief=6", "knockback=10", "vengeance=10", "regen=10", "lifesteal=10",
                "explosive=7", "ghastly=6", "webber=7", "bulwark=9", "sapper=10",
                "giant=8", "swift=7", "griefer=5", "kamikaze=4", "stalker=6"
        );
    }

    /** Peso efectivo de un modificador: del config si está, si no su valor por defecto del código. */
    public static int weightOf(IModifier m) {
        if (cache == null) buildCache();
        Integer override = cache.get(m.id());
        return override != null ? override : m.weight();
    }

    private static void buildCache() {
        cache = new HashMap<>();
        if (WEIGHTS == null) return;
        for (String entry : WEIGHTS.get()) {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            String id = entry.substring(0, eq).trim();
            try {
                int w = Integer.parseInt(entry.substring(eq + 1).trim());
                cache.put(id, Math.max(0, w));
            } catch (NumberFormatException ignored) {}
        }
    }

    /** Invalida la caché (al recargar el config). */
    public static void invalidate() {
        cache = null;
    }

    private ModifierWeights() {}
}
