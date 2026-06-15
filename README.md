# Infernal Plus

**Versión del mod:** 1.0.0  
**Minecraft:** 1.21.1  
**Loader:** NeoForge 21.1.115+  
**Java:** 21+

Mod que aplica modificadores aleatorios estilo **Infernal Mobs** a TODAS las entidades hostiles (vanilla y de otros mods). Cada mob puede recibir múltiples modificadores que alteran su comportamiento, vida y peligrosidad.

---

## 📥 Instalación

### Requisitos
- **Minecraft 1.21.1**
- **NeoForge 21.1.115** o superior
- **Java 21+** instalado

### Pasos
1. Descarga `infernalplus-1.0.0.jar`
2. Coloca el JAR en tu carpeta `mods`:
   - **Launcher estándar:** `%appdata%\.minecraft\mods\`
   - **MultiMC/PolyMC/Prism:** carpeta `mods` de tu instancia
3. Inicia Minecraft con el perfil **NeoForge 1.21.1**

---

## 🔧 Cómo funciona
- Al spawnear un hostil se tira un dado (`spawnChance`, default 5%).
- Si pasa, recibe entre `minModifiers` y `maxModifiers` modificadores (selección ponderada, sin incompatibles).
- Su vida se escala según el nº de modificadores (`healthPerModifier`).
- Los datos se guardan por-entidad con un Attachment y persisten al guardar/cargar.

## ⚙️ Configuración
Archivo: `config/infernalplus-common.toml`
- `spawnChance` (0.0–1.0)
- `minModifiers` / `maxModifiers`
- `healthPerModifier`
- `affectBosses` (Wither / Ender Dragon)
- `whitelistMode` + `entityWhitelist` / `entityBlacklist`

## 🛠️ Compilar desde fuente

**Windows:**
```cmd
set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.0.35-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat clean build
```

**Linux/Mac:**
```bash
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
./gradlew clean build
```

El JAR compilado estará en: `build/libs/infernalplus-1.0.0.jar`

## 👹 Modificadores incluidos (25)
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

## 🔗 Extender
Crea una clase que implemente `IModifier` y regístrala en `IPModifiers`.
Otros mods pueden registrar en el registry `infernalplus:modifiers`.

## 📊 Dificultad dinámica (Awareness)
Los infernales se vuelven más probables y más fuertes cuando el jugador está descuidado.
Sección `[awareness]` del config:
- **darkness**: de noche o en luz baja (<=7) sube la amenaza.
- **insomnia**: noches sin dormir suben la amenaza (usa el mismo contador que los Phantoms).
- **idle**: estar quieto mucho rato (AFK/distraído) sube la amenaza.
- **backstab**: los infernales hacen daño extra (x1.5 por defecto) si te golpean por la espalda.
- **affectsStrength**: si true, la amenaza también escala vida/daño, no solo la probabilidad de spawn.
- **maxThreat**: tope del multiplicador (2.5 por defecto).

## 🏆 Sistema de tiers
Cada infernal tiene un tier estilo Apotheosis:
- **Elite** (base): minModifiers–maxModifiers modificadores.
- **Ultra** (1/`ultraRarity`): +3-4 modificadores.
- **Infernal** (1/`infernalRarity` sobre Ultra): +3-4 más.

## 🛡️ Compatibilidad con Apotheosis (opcional)
Si Apotheosis está instalado, los infernales sueltan equipo con afijos según su tier.
Apotheosis escala la rareza del afijo por tu World Tier automáticamente.
- Es soft dependency: sin Apotheosis, el mod funciona igual (no suelta este loot).
- Sección `[apotheosis_loot]`: probabilidad de drop por tier.
- IMPORTANTE: las loot tables en `data/infernalplus/loot_table/affix/gear/*.json` son
  PLANTILLAS. Debes ajustarlas para que referencien la generación de afijos de tu versión
  de Apotheosis (las rutas/funciones de su API de loot cambian entre versiones).
