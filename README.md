# Infernal Plus (NeoForge 1.21.1)

Mod que aplica modificadores aleatorios estilo **Infernal Mobs** a TODAS las
entidades hostiles, vanilla y de otros mods (cualquier mob que implemente `Enemy`).

## Cómo funciona
- Al spawnear un hostil se tira un dado (`spawnChance`, default 5%).
- Si pasa, recibe entre `minModifiers` y `maxModifiers` modificadores (selección ponderada, sin incompatibles).
- Su vida se escala según el nº de modificadores (`healthPerModifier`).
- Los datos se guardan por-entidad con un Attachment y persisten al guardar/cargar.

## Configuración
Archivo: `config/infernalplus-common.toml`
- `spawnChance` (0.0–1.0)
- `minModifiers` / `maxModifiers`
- `healthPerModifier`
- `affectBosses` (Wither / Ender Dragon)
- `whitelistMode` + `entityWhitelist` / `entityBlacklist`

## Compilar Proyecto
```
./gradlew build
```
El .jar queda en `build/libs/`.

## Modificadores incluidos (25)
tough, sprint, rust, fiery, poisonous, withering, weakness, slowness,
blinding, hunger, thief, knockback, vengeance, regen, lifesteal,
explosive, ghastly, webber, bulwark, sapper, giant, swift, griefer, kamikaze, stalker

### Notas de los nuevos
- **giant**: usa el atributo `SCALE` (1.20.5+) para agrandar el mob ~60%; la hitbox,
  el alcance y el knockback se ajustan solos. Sube vida, daño y resistencia al empuje.
- **swift**: velocidad muy superior a `sprint`. Incompatible con `sprint` y `slowness`.
- **griefer**: rompe bloques frente a él mientras persigue a un objetivo, con cooldown.
  Respeta la gamerule `mobGriefing` y NO rompe bedrock, obsidiana, líquidos ni
  bloques con block entity (cofres, hornos...). Si quieres desactivar destrozos,
  pon `/gamerule mobGriefing false`.
- **kamikaze**: si un jugador entra a menos de 2 bloques, el mob empieza a cargar
  (silbido + humo/llamas) y tras un fusible breve explota con daño masivo. Esquivable
  si te alejas durante el fusible. Configurable en la sección `[kamikaze]`:
  `power` (6.0 = TNT), `fuseTicks` (30 = 1.5s), e `instant` (true = detonación
  inmediata sin aviso, instakill puro — desactivado por defecto por ser injusto).
- **stalker**: siempre invisible. Solo se revela 1 segundo (20 ticks) cuando ataca o
  cuando recibe daño; la proximidad NO lo delata, así que un stalker quieto a tu lado
  sigue invisible. Incompatible con `kamikaze` (sería un instakill sin aviso visible).

## Extender
Crea una clase que implemente `IModifier` y regístrala en `IPModifiers`.
Otros mods pueden registrar en el registry `infernalplus:modifiers`.

## Dificultad dinámica (awareness)
Los infernales se vuelven más probables y más fuertes cuando el jugador está descuidado.
Sección `[awareness]` del config:
- **darkness**: de noche o en luz baja (<=7) sube la amenaza.
- **insomnia**: noches sin dormir suben la amenaza (usa el mismo contador que los Phantoms).
- **idle**: estar quieto mucho rato (AFK/distraído) sube la amenaza.
- **backstab**: los infernales hacen daño extra (x1.5 por defecto) si te golpean por la espalda.
- **affectsStrength**: si true, la amenaza también escala vida/daño, no solo la probabilidad de spawn.
- **maxThreat**: tope del multiplicador (2.5 por defecto).

## Sistema de tiers
Cada infernal tiene un tier estilo Apotheosis:
- **Elite** (base): minModifiers–maxModifiers modificadores.
- **Ultra** (1/`ultraRarity`): +3-4 modificadores.
- **Infernal** (1/`infernalRarity` sobre Ultra): +3-4 más.

## Compatibilidad con Apotheosis (opcional)
Si Apotheosis está instalado, los infernales sueltan equipo con afijos según su tier.
Apotheosis escala la rareza del afijo por tu World Tier automáticamente.
- Es soft dependency: sin Apotheosis, el mod funciona igual (no suelta este loot).
- Sección `[apotheosis_loot]`: probabilidad de drop por tier.
- IMPORTANTE: las loot tables en `data/infernalplus/loot_table/affix/gear/*.json` son
  PLANTILLAS. Debes ajustarlas para que referencien la generación de afijos de tu versión
  de Apotheosis (las rutas/funciones de su API de loot cambian entre versiones).

## Comandos (requieren OP / nivel 2)
- `/infernal spawn <mob> [tier] [count]` — aparece mob(s) infernal(es) en tu posición.
  - `tier`: 0=Elite, 1=Ultra, 2=Infernal. Si se omite, tier aleatorio.
  - `count`: cuántos (1-50). Ej: `/infernal spawn minecraft:zombie 2` = un zombie Infernal.
- `/infernal clear [radius]` — elimina todos los infernales en el radio (default 32).
- `/infernal info` — muestra tier, modificadores y vida del mob al que apuntas.

## Efectos visuales (v1.2.5)
- **Nombre con tier**: cada infernal lleva un nombre con color por tier, visible al apuntarlo
  (o en el tooltip de Jade si lo tienes). ⚡ Infernal (rojo), ✦ Ultra (dorado), ❖ Elite (amarillo).
- **Aura de partículas por tier**: Elite suelta humo tenue, Ultra llamas, Infernal fuego de alma
  + destellos de alma. Cuanto mayor el tier, más densas y frecuentes las partículas.

## Nombres meme (v1.2.6)
Todos los infernales reciben un nombre meme aleatorio combinando
título + apodo + sufijo, ej: "⚡ Infernal Rey Jochis Supremo Zombie".
- Mezcla es-MX + gamer + memes (chad, gigachad, sigma, GG, basado, etc.).
- Sin nombres de personas reales por defecto.
- Sección `[meme_names]` del config: `enabled` (on/off) y `customNames` (lista editable
  para añadir apodos propios o nombres de tus amigos al pool).

## Interruptor (v1.2.8)
- `/infernal toggle off` — pausa los spawns nuevos Y elimina todos los infernales cargados.
- `/infernal toggle on` — reactiva los spawns.
- `/infernal toggle status` — muestra si está activado o no.
El estado es en memoria (vuelve a 'activado' al reiniciar el server). El comando `/infernal spawn`
sigue funcionando aunque esté pausado (es comando de admin explícito).

## XP escalada (v1.2.8)
Los infernales sueltan más experiencia cuantos más modificadores tengan.
- Fórmula: xp = base × (1 + xpPerModifier × nº_modificadores).
- Por defecto un mob de ~10 modificadores (Infernal cargado) da ~5x la XP normal.
- Sección `[xp_bonus]`: `enabled` y `xpPerModifier` (sube/baja la recompensa).

## Corazón Infernal (v1.3.0) — Fase 1
Ítem que sueltan los infernales (probabilidad según nº de modificadores).
- Clic derecho para consumirlo: +2 HP (1 corazón) de vida máxima permanente, estilo Terraria.
- Tope configurable (default 10 corazones = +20 HP) en `[life_heart]`.
- El bonus persiste al morir y reconectar.
- La textura `assets/infernalplus/textures/item/life_heart.png` es un placeholder 16x16;
  reemplázala por tu propio diseño cuando quieras.

## Cristal Infernal (v1.3.1) — Fase 2
Ítem que sueltan los infernales (probabilidad según nº de modificadores).
- Sostén el cristal y haz CLIC DERECHO sobre una pieza de equipo en tu inventario
  (armadura, espada o escudo) para darle +1 de armadura permanente.
- Tope configurable (default 10 por pieza) en `[armor_crystal]`.
- El bonus se guarda en el propio ítem (data component) y se aplica al equiparlo.
- Textura placeholder en `assets/infernalplus/textures/item/armor_crystal.png`; reemplázala.

## Cristales de arma (v1.3.3)
Ahora hay TRES cristales, cada uno con su bonus (clic derecho sobre el equipo):
- **Cristal Infernal** (armadura): +1 armadura, en cualquier equipo.
- **Cristal de Daño** (rojo): +1 daño de ataque, solo en armas.
- **Cristal de Velocidad** (cian): +0.2 velocidad de ataque, solo en armas.
Cada uno acumula con su propio tope, configurable en `[armor_crystal]`
(maxPerItem, maxDamagePerItem, maxSpeedPerItem). Los tres pueden combinarse en una
misma arma. Los infernales sueltan uno de los tres al azar.

## Pesos de modificadores configurables (v1.3.4)
Sección `[modifier_weights]` del config: controla qué tan común es cada modificador.
- Formato: lista de "id=peso", ej. "kamikaze=4", "tough=14".
- Mayor peso = más común. Peso 0 = DESACTIVADO (ese modificador nunca aparece).
- Si un modificador no está en la lista, usa su peso por defecto.
- Permite afinar rareza (hacer kamikaze/stalker raros, comunes los básicos) y apagar
  los que no quieras sin recompilar.

## Auditoría y correcciones (v1.4.0)
- **Robo de objetos (thief)**: el ítem robado ya NUNCA se destruye. El mob lo equipa y
  lo usa contra ti si puede; si no, lo suelta al suelo. Funciona con ítems vanilla y modded.
- **Compatibilidad modded**: los infernales ahora afectan a mobs hostiles de otros mods,
  no solo a los que implementan la interfaz Enemy de vanilla (detección por Monster/MobCategory).
- **Daño kamikaze**: además de la explosión vanilla (mitigable con armadura late-game),
  ahora aplica daño directo garantizado que decrece con la distancia. Configurable en
  `[kamikaze] directDamage`. Amenaza real sin one-shots injustos.
- **Glow**: los infernales tienen contorno brillante visible a distancia y a través de
  paredes. Desactivable en `[visual] glowEnabled`.
- **Atributos**: auditados; todos usan operaciones aditivas, ninguno reemplaza la base.

## Correcciones 1.4.1 (fixes reales de los bugs persistentes)
- **Armadura/cristales (causa raíz real)**: modificar el componente ATTRIBUTE_MODIFIERS de la
  armadura vanilla rompía su armadura base. AHORA el cristal NO toca el ítem: solo guarda un
  contador, y el bonus se aplica al JUGADOR según el equipo que lleva (CrystalBonusHandler,
  recalculado cada 10 ticks con addTransientModifier). Imposible romper la armadura base.
  Nota: el bonus ya no se ve en el tooltip del ítem (está en el jugador al equiparlo).
- **Robo de objetos (causa raíz real)**: al equipar el ítem robado, el dropChance del mob era 0
  por defecto, así que el ítem se perdía al morir el mob. Ahora se fuerza dropChance a 100%:
  matas al ladrón y recuperas tu objeto. Si no puede equiparlo, lo suelta al instante.


## Robo simplificado (v1.4.2)
El robo ahora MUEVE el ítem de las manos del jugador al suelo, en un solo paso.
- No se equipa en el mob (eso causaba pérdidas por dropChance).
- No se copia (eso causaría duplicación de ítems).
- Imposible perder o duplicar el objeto. Funciona con ítems vanilla y modded.


## Comandos renombrados (v1.4.4)
El comando principal ahora es /hellmarked (antes /infernal):
- /hellmarked spawn <mob> [tier] [cantidad]
- /hellmarked info
- /hellmarked clear [radio]
- /hellmarked toggle on|off|status
El antiguo /infernal sigue funcionando como alias.
