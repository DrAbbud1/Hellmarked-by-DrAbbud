package com.drabbud.infernalplus.naming;

import com.drabbud.infernalplus.config.IPConfig;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera nombres meme aleatorios para los infernales de tier alto.
 * Combina un título + un apodo + (a veces) un sufijo. Las piezas son genéricas/meme,
 * sin nombres de personas reales. El usuario puede añadir nombres propios vía config
 * (IPConfig.MEME_CUSTOM_NAMES), que entran al pool de apodos.
 */
public final class MemeNameGenerator {

    // títulos al frente
    private static final String[] TITLES = {
            "Rey", "Don", "Lord", "Jefe", "Maestro", "El", "Capo", "Patrón",
            "Sir", "Big", "Gran", "Comandante", "Supremo", "San", "Doctor"
    };

    // apodos coloquiales / meme / tiktok (es-MX + gamer)
    private static final String[] NICKS = {
            "Jochis", "Compa", "Crack", "Pana", "Wey", "Chad", "Gigachad", "Sigma",
            "Pibe", "Morro", "Chamaco", "Broki", "Homie", "Cabrón", "Mostro",
            "Goat", "NPC", "Tryhard", "Sweat", "Noob", "Pro", "Lobo", "Tigre",
            "Chido", "Perrón", "Mero", "Fren", "Bro", "Manito", "Chacal"
    };

    // sufijos opcionales
    private static final String[] SUFFIXES = {
            "Supremo", "del Caos", "3000", "OG", "Maximus", "Based", "GG",
            "Sigma", "Prime", "Ultra", "Definitivo", "Legendario", "del Mal",
            "9000", "X", "Jr", "Sénior", "Cósmico", "Infinito", "Turbo"
    };

    public static String generate(RandomSource rng) {
        // pool de apodos = los de base + los que el usuario puso en config
        List<String> nicks = new ArrayList<>(List.of(NICKS));
        List<? extends String> custom = IPConfig.MEME_CUSTOM_NAMES.get();
        if (custom != null) {
            for (String s : custom) {
                if (s != null && !s.isBlank()) nicks.add(s.trim());
            }
        }

        String title = TITLES[rng.nextInt(TITLES.length)];
        String nick = nicks.get(rng.nextInt(nicks.size()));

        // ~60% de las veces añade sufijo
        if (rng.nextInt(100) < 60) {
            String suffix = SUFFIXES[rng.nextInt(SUFFIXES.length)];
            return title + " " + nick + " " + suffix;
        }
        return title + " " + nick;
    }

    private MemeNameGenerator() {}
}
