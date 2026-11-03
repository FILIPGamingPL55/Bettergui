# BetterGUI — Fabric Mod for Minecraft 1.21.11

Feather Client-inspired dark GUI + custom capes.

## Features
| Feature | Details |
|---|---|
| **Wersja MC** | 1.21.11 · Loader 0.19.2 · Fabric API 0.141.3+1.21.11 · Java 21 |
| **Dark Title Screen** | Feather-style dark background, scaled logo, smooth hover buttons |
| **Custom Capes** | 4 built-in capes (Default, Flame, Galaxy, Gradient) — pick in Cosmetics |
| **Cape Selection Screen** | Opens from the title screen → Cosmetics button |
| **No Vanilla Bloat** | Replaces only the title screen; all other screens are unchanged |

## Requirements
- Java 17+
- **Java 21**
- Minecraft 1.21.11 (Fabric Loader ≥ 0.19.2)
- Fabric API 0.141.3+1.21.11

## Build

```bash
./gradlew build
# Output: build/libs/better-gui-1.0.0.jar
```

## Install
1. Copy `better-gui-1.0.0.jar` to your `.minecraft/mods/` folder
2. Make sure Fabric Loader + Fabric API are installed
3. Launch Minecraft 1.20.6

## Adding Custom Capes
1. Create a **64×32 PNG** texture (vanilla Minecraft cape layout)
2. Drop it into `src/main/resources/assets/bettergui/textures/cape/yourname.png`
3. Add `"yourname"` to the `CAPE_NAMES` array in `CapeManager.java`
4. Rebuild

## Cape Texture Layout (64×32)
```
The cape panel occupies pixels (0,0)→(10,16) on the texture.
Matches standard Minecraft cape layout — any vanilla/Optifine cape PNG works.
```

## Project Structure
```
src/main/java/com/bettergui/
├── BetterGuiMod.java              ← entry point
├── cape/
│   └── CapeManager.java           ← cape registry & active selection
├── mixin/
│   ├── TitleScreenMixin.java      ← replaces vanilla title screen
│   └── PlayerSkinMixin.java       ← injects cape into player skin
└── screen/
    ├── CustomTitleScreen.java     ← Feather-style title screen
    ├── CapeSelectionScreen.java   ← Cosmetics / cape picker
    └── widget/
        └── FeatherButton.java     ← dark rounded button with hover anim
```
