package com.drabbud.infernalplus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class IPConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue MIN_MODIFIERS;
    public static final ModConfigSpec.IntValue MAX_MODIFIERS;
    public static final ModConfigSpec.DoubleValue HEALTH_PER_MODIFIER;
    public static final ModConfigSpec.BooleanValue AFFECT_BOSSES;
    public static final ModConfigSpec.IntValue ULTRA_RARITY;
    public static final ModConfigSpec.IntValue INFERNAL_RARITY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_WHITELIST;
    public static final ModConfigSpec.BooleanValue WHITELIST_MODE;

    public static final ModConfigSpec.DoubleValue KAMIKAZE_POWER;
    public static final ModConfigSpec.IntValue KAMIKAZE_FUSE_TICKS;
    public static final ModConfigSpec.BooleanValue KAMIKAZE_INSTANT;
    public static final ModConfigSpec.DoubleValue KAMIKAZE_DIRECT_DAMAGE;

    // awareness
    public static final ModConfigSpec.BooleanValue AWARENESS_ENABLED;
    public static final ModConfigSpec.DoubleValue AWARENESS_MAX;
    public static final ModConfigSpec.BooleanValue AWARENESS_DARKNESS;
    public static final ModConfigSpec.DoubleValue AWARENESS_DARK_BONUS;
    public static final ModConfigSpec.BooleanValue AWARENESS_INSOMNIA;
    public static final ModConfigSpec.DoubleValue AWARENESS_INSOMNIA_BONUS;
    public static final ModConfigSpec.BooleanValue AWARENESS_IDLE;
    public static final ModConfigSpec.DoubleValue AWARENESS_IDLE_BONUS;
    public static final ModConfigSpec.BooleanValue AWARENESS_BACKSTAB;
    public static final ModConfigSpec.DoubleValue AWARENESS_BACKSTAB_BONUS;
    public static final ModConfigSpec.BooleanValue AWARENESS_AFFECTS_STRENGTH;

    // visual
    public static final ModConfigSpec.BooleanValue GLOW_ENABLED;

    // loot / apotheosis
    public static final ModConfigSpec.BooleanValue APOTH_LOOT_ENABLED;
    public static final ModConfigSpec.DoubleValue APOTH_DROP_CHANCE_ELITE;
    public static final ModConfigSpec.DoubleValue APOTH_DROP_CHANCE_ULTRA;
    public static final ModConfigSpec.DoubleValue APOTH_DROP_CHANCE_INFERNAL;

    // nombres meme
    public static final ModConfigSpec.BooleanValue MEME_NAMES_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MEME_CUSTOM_NAMES;

    // xp bonus
    public static final ModConfigSpec.BooleanValue XP_BONUS_ENABLED;
    public static final ModConfigSpec.DoubleValue XP_PER_MODIFIER;

    // corazones de vida (drop)
    public static final ModConfigSpec.IntValue LIFE_HEART_MAX;
    public static final ModConfigSpec.DoubleValue LIFE_HEART_DROP_PER_MOD;

    // cristal de armadura (drop)
    public static final ModConfigSpec.IntValue ARMOR_CRYSTAL_MAX;
    public static final ModConfigSpec.DoubleValue ARMOR_CRYSTAL_DROP_PER_MOD;
    public static final ModConfigSpec.IntValue DAMAGE_CRYSTAL_MAX;
    public static final ModConfigSpec.IntValue SPEED_CRYSTAL_MAX;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("spawning");
        SPAWN_CHANCE = b.comment("Probabilidad (0.0-1.0) de que un mob hostil se vuelva infernal al spawnear.")
                .defineInRange("spawnChance", 0.05, 0.0, 1.0);
        MIN_MODIFIERS = b.comment("Mínimo de modificadores por mob infernal.")
                .defineInRange("minModifiers", 1, 1, 20);
        MAX_MODIFIERS = b.comment("Máximo de modificadores por mob infernal.")
                .defineInRange("maxModifiers", 3, 1, 20);
        AFFECT_BOSSES = b.comment("Si los jefes (Ender Dragon, Wither, etc.) pueden volverse infernales.")
                .define("affectBosses", false);
        ULTRA_RARITY = b.comment("1 entre N infernales sube a tier Ultra (más modificadores). Menor = más Ultras.")
                .defineInRange("ultraRarity", 7, 1, 1000);
        INFERNAL_RARITY = b.comment("1 entre N Ultras sube a tier Infernal (el más fuerte). Menor = más Infernales.")
                .defineInRange("infernalRarity", 7, 1, 1000);
        b.pop();

        b.push("scaling");
        HEALTH_PER_MODIFIER = b.comment("Multiplicador de vida extra por cada modificador (acumulativo).")
                .defineInRange("healthPerModifier", 0.75, 0.0, 10.0);
        b.pop();

        b.push("filtering");
        WHITELIST_MODE = b.comment("Si true, SOLO las entidades en entityWhitelist pueden volverse infernales.")
                .define("whitelistMode", false);
        ENTITY_BLACKLIST = b.comment("IDs de entidad que NUNCA se vuelven infernales. Ej: \"minecraft:bat\".")
                .defineListAllowEmpty("entityBlacklist", List.of("minecraft:bat", "minecraft:wither", "minecraft:ender_dragon"),
                        () -> "minecraft:pig", o -> o instanceof String s && s.contains(":"));
        ENTITY_WHITELIST = b.comment("IDs de entidad permitidas cuando whitelistMode = true.")
                .defineListAllowEmpty("entityWhitelist", List.of(),
                        () -> "minecraft:zombie", o -> o instanceof String s && s.contains(":"));
        b.pop();

        b.push("kamikaze");
        KAMIKAZE_POWER = b.comment("Potencia de la explosión del modificador kamikaze. 6.0 = como TNT (instakill sin buena armadura).")
                .defineInRange("power", 6.0, 1.0, 20.0);
        KAMIKAZE_FUSE_TICKS = b.comment("Ticks de fusible tras entrar en rango antes de detonar (20 = 1s). Da margen para esquivar.")
                .defineInRange("fuseTicks", 30, 1, 200);
        KAMIKAZE_INSTANT = b.comment("Si true, detona al instante sin fusible (instakill sin aviso). NO recomendado: se siente injusto.")
                .define("instant", false);
        KAMIKAZE_DIRECT_DAMAGE = b.comment("Daño directo garantizado en el centro de la explosión (decrece con la distancia). Más difícil de mitigar que la explosión vanilla, para que el kamikaze sea una amenaza real con armadura late-game. 20 = 10 corazones a quemarropa.")
                .defineInRange("directDamage", 20.0, 0.0, 200.0);
        b.pop();

        b.push("awareness");
        AWARENESS_ENABLED = b.comment("Activa la dificultad dinámica: los mobs se vuelven más peligrosos cuando el jugador está descuidado.")
                .define("enabled", true);
        AWARENESS_MAX = b.comment("Factor de amenaza máximo. 2.5 = hasta 2.5x probabilidad de infernal (y fuerza si affectsStrength=true).")
                .defineInRange("maxThreat", 2.5, 1.0, 10.0);
        AWARENESS_AFFECTS_STRENGTH = b.comment("Si true, la amenaza también escala vida/daño de los infernales, no solo su probabilidad de spawn.")
                .define("affectsStrength", true);

        AWARENESS_DARKNESS = b.comment("De noche o en luz baja (<=7) el jugador es más vulnerable.")
                .define("darkness", true);
        AWARENESS_DARK_BONUS = b.comment("Amenaza extra por oscuridad total.")
                .defineInRange("darknessBonus", 0.5, 0.0, 5.0);

        AWARENESS_INSOMNIA = b.comment("No dormir (insomnio) aumenta el peligro, igual que con los Phantoms.")
                .define("insomnia", true);
        AWARENESS_INSOMNIA_BONUS = b.comment("Amenaza extra por cada noche sin dormir (hasta 3).")
                .defineInRange("insomniaBonus", 0.35, 0.0, 5.0);

        AWARENESS_IDLE = b.comment("Estar quieto mucho rato (AFK / distraído) aumenta el peligro.")
                .define("idle", true);
        AWARENESS_IDLE_BONUS = b.comment("Amenaza extra por inactividad prolongada.")
                .defineInRange("idleBonus", 0.4, 0.0, 5.0);

        AWARENESS_BACKSTAB = b.comment("Los infernales hacen daño extra al golpearte por la espalda (te pillan distraído).")
                .define("backstab", true);
        AWARENESS_BACKSTAB_BONUS = b.comment("Multiplicador de daño en un golpe por la espalda. 1.5 = +50%.")
                .defineInRange("backstabMultiplier", 1.5, 1.0, 5.0);
        b.pop();

        b.push("apotheosis_loot");
        APOTH_LOOT_ENABLED = b.comment("Si Apotheosis está instalado, los infernales sueltan equipo con afijos. Apotheosis escala la rareza por World Tier. DESACTIVADO por defecto: actívalo solo cuando hayas ajustado las loot tables en data/infernalplus/loot_table/affix/gear/ para tu version de Apotheosis.")
                .define("enabled", false);
        APOTH_DROP_CHANCE_ELITE = b.comment("Probabilidad (0-1) de soltar un objeto con afijo para mobs Elite.")
                .defineInRange("dropChanceElite", 0.25, 0.0, 1.0);
        APOTH_DROP_CHANCE_ULTRA = b.comment("Probabilidad para mobs Ultra.")
                .defineInRange("dropChanceUltra", 0.6, 0.0, 1.0);
        APOTH_DROP_CHANCE_INFERNAL = b.comment("Probabilidad para mobs Infernal (1.0 = garantizado).")
                .defineInRange("dropChanceInfernal", 1.0, 0.0, 1.0);
        b.pop();

        b.push("meme_names");
        MEME_NAMES_ENABLED = b.comment("Los infernales de tier alto (Ultra/Infernal) reciben un nombre meme aleatorio (ej. 'Rey Jochis Supremo').")
                .define("enabled", true);
        MEME_CUSTOM_NAMES = b.comment("Apodos personalizados que se añaden al pool de nombres meme. Pon aquí los nombres de tus amigos o lo que quieras.")
                .defineListAllowEmpty("customNames", List.of("Jochis", "Abbud"),
                        () -> "Pana", o -> o instanceof String);
        b.pop();

        b.push("xp_bonus");
        XP_BONUS_ENABLED = b.comment("Los infernales sueltan más XP cuantos más modificadores tengan.")
                .define("enabled", true);
        XP_PER_MODIFIER = b.comment("XP extra por cada modificador (como fracción de la base). 0.4 => un mob de 10 mods da 5x la XP normal.")
                .defineInRange("xpPerModifier", 0.4, 0.0, 10.0);
        b.pop();

        b.push("life_heart");
        LIFE_HEART_MAX = b.comment("Máximo de corazones de vida que un jugador puede consumir (cada uno = +2 HP).")
                .defineInRange("maxHearts", 10, 1, 100);
        LIFE_HEART_DROP_PER_MOD = b.comment("Probabilidad de soltar un corazón por cada modificador del infernal. 0.03 => un mob de 10 mods tiene ~30% de soltar uno.")
                .defineInRange("dropChancePerModifier", 0.03, 0.0, 1.0);
        b.pop();

        b.push("armor_crystal");
        ARMOR_CRYSTAL_MAX = b.comment("Máximo de cristales de armadura (+1 cada uno) por pieza de equipo.")
                .defineInRange("maxPerItem", 10, 1, 100);
        ARMOR_CRYSTAL_DROP_PER_MOD = b.comment("Probabilidad de soltar un cristal por cada modificador del infernal.")
                .defineInRange("dropChancePerModifier", 0.025, 0.0, 1.0);
        DAMAGE_CRYSTAL_MAX = b.comment("Máximo de cristales de daño (+1 de daño cada uno) por arma.")
                .defineInRange("maxDamagePerItem", 10, 1, 100);
        SPEED_CRYSTAL_MAX = b.comment("Máximo de cristales de velocidad (+0.2 velocidad de ataque cada uno) por arma.")
                .defineInRange("maxSpeedPerItem", 10, 1, 100);
        b.pop();

        ModifierWeights.define(b);

        b.push("visual");
        GLOW_ENABLED = b.comment("Los infernales tienen contorno brillante (glow) visible a distancia y a través de paredes.")
                .define("glowEnabled", true);
        b.pop();

        SPEC = b.build();
    }

    private IPConfig() {}
}
