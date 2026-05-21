# Port An Architectury Mod To NeoForge-Only

Use this when converting a multi-platform Architectury mod (common + neoforge + fabric modules) to a single NeoForge-only mod using `net.neoforged.moddev`.

## Module Collapse

Merge all three modules into one flat source set:

- `sleepingoverhaul-common/src/main/java/` → `src/main/java/`
- `sleepingoverhaul-neoforge/src/main/java/` → `src/main/java/` (merge, don't overwrite)
- Drop `sleepingoverhaul-fabric/` entirely

## Build System

Replace the multi-module Architectury Gradle setup with `net.neoforged.moddev`. Copy `build.gradle`, `settings.gradle`, `gradle.properties`, and the Gradle wrapper from a working moddev project (e.g. `marsflinttool/`) and adjust `mod_id`, `mod_version`, `minecraft_version`, `neo_version`.

Remove:
- `architectury { ... }` Gradle blocks
- `dev.architectury.*` dependency declarations
- `architectury.common.json` — delete it; causes load errors if present

## Access Widener → Access Transformer

Fabric uses `.accesswidener`. NeoForge uses Access Transformers.

Widener format:
```
accessWidener  v2  named
accessible class net/minecraft/world/effect/HungerMobEffect
```

AT format (`src/main/resources/META-INF/accesstransformer.cfg`):
```
public net.minecraft.world.effect.HungerMobEffect
```

Enable in `build.gradle`:
```gradle
neoForge {
    accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')
}
```

## Mixin JSON

Update `"compatibilityLevel"` in all mixin JSON files to match the Java version. MC 26.x = `"JAVA_25"`. MC 1.21.x = `"JAVA_21"`.

## IModPlatform Abstraction

Architectury mods often have an `IModPlatform` interface abstracting Fabric/NeoForge differences. Keep it — removing it requires changing all callers for no benefit. The NeoForge impl (`ModPlatformForge`) becomes the only implementation.

## Imports To Remove

- All `dev.architectury.*`
- All `net.fabricmc.*`
- Any `@ExpectPlatform` annotations — remove the annotation and keep the NeoForge impl directly

## What Not To Do

- Do not keep Architectury in the dependency tree "just in case" — it will conflict.
- Do not copy the multi-module Gradle settings — use a single module.
- Do not forget to delete `architectury.common.json`.
