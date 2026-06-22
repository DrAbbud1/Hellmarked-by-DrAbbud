# Hellmarked by DrAbbud (NeoForge 1.21.1)

Convierte a los mobs hostiles en amenazas impredecibles estilo Infernal Mobs: modificadores
aleatorios, sistema de tiers, dificultad dinámica y recompensas que valen el riesgo.
Funciona con mobs vanilla y de otros mods.

> Inspirado en Infernal Mobs de AtomicStryker. Mod independiente con código propio.
> El mod ID interno es `infernalplus` (no se cambia para no romper datos guardados).

## Cómo funciona
- Al aparecer un mob hostil se tira un dado (`spawnChance`, default 5%).
- Si pasa, se vuelve "Hellmarked": recibe modificadores, más vida y un nombre único.
- El tier (Elite/Ultra/Infernal) decide cuántos modificadores y cuánto peligro.
- Los datos se guardan por-entidad y persisten al guardar/cargar.

## Sistema de tiers
- **Elite** (base): pocos modificadores.
- **Ultra** (1/`ultraRarity`): +3-4 modificadores.
- **Infernal** (1/`infernalRarity` sobre Ultra): +3-4 más. El más peligroso.

## Modificadores (25+)
tough, sprint, rust, fiery, poisonous, withering, weakness, slowness, blinding, hunger,
thief, knockback, vengeance, regen, lifesteal, explosive, ghastly, webber, bulwark, sapper,
giant, swift, griefer, kamikaze, stalker.

Cada modificador tiene un peso configurable en `[modifier_weights]` (peso 0 = desactivado).

## Dificultad dinámica (awareness)
Los Hellmarked se vuelven más probables y fuertes cuando el jugador está descuidado:
- **darkness**: de noche o en luz baja.
- **insomnia**: noches sin dormir.
- **idle**: estar quieto mucho rato.
- **backstab**: daño extra si te golpean por la espalda.
Sección `[awareness]` del config.

## Indicadores visuales
- Nombre con color por tier, visible al apuntar (⚡ Infernal rojo, ✦ Ultra dorado, ❖ Elite amarillo).
- Aura de partículas que escala con el tier.
- Glow (contorno brillante) visible a distancia. Desactivable en `[visual]`.

## Nombres meme
Todos los Hellmarked reciben un nombre aleatorio (título + apodo + sufijo), ej:
"⚡ Infernal Rey Jochis Supremo Zombie". Personalizable en `[meme_names] customNames`.

## Recompensas
- **XP escalada**: más experiencia cuantos más modificadores (`[xp_bonus]`).
- **Corazón Hellmarked**: clic derecho para +2 HP de vida máxima permanente (estilo Terraria),
  con tope configurable. El bonus persiste al morir/reconectar.
- **Cristales Hellmarked**: clic derecho sobre el equipo para mejorarlo.
  - Cristal de armadura: +armadura (cualquier equipo).
  - Cristal de daño: +daño de ataque (armas).
  - Cristal de velocidad: +velocidad de ataque (armas).
  El bonus se aplica al jugador según el equipo que lleve, sin romper la armadura base.

## Kamikaze
Algunos Hellmarked explotan al acercarte, con un fusible sonoro en capas (aviso para huir).
Además de la explosión vanilla, aplican daño directo garantizado que decrece con la distancia,
para ser una amenaza real incluso con armadura de late-game. Configurable en `[kamikaze]`.

## Comandos (requieren OP)
- `/hellmarked spawn <mob> [tier] [cantidad]` — invoca Hellmarked.
- `/hellmarked info` — muestra tier, modificadores y vida del mob al que apuntas.
- `/hellmarked clear [radio]` — elimina los Hellmarked cercanos.
- `/hellmarked toggle on|off|status` — pausa/reactiva el sistema (off limpia los existentes).
- El antiguo `/infernal` sigue funcionando como alias.

## Compatibilidad con Apotheosis (opcional)
Si Apotheosis está instalado, los Hellmarked sueltan equipo con afijos según su tier.
Es soft dependency: sin Apotheosis el mod funciona igual.
Las loot tables en `data/infernalplus/loot_table/affix/gear/*.json` son plantillas; ajústalas
a tu versión de Apotheosis.

## Compilar
Requiere JDK 21. En la carpeta del proyecto:
```
./gradlew build      (Linux/Mac)
.\gradlew.bat build  (Windows)
```
El jar sale en `build/libs/HellmarkedByDrAbbud-<version>.jar`.

## Notas
- Las texturas de los ítems (`assets/infernalplus/textures/item/*.png`) son placeholders;
  reemplázalas por tu propio diseño.
- Multijugador: todos los jugadores necesitan el mod instalado (registra un registry propio).
