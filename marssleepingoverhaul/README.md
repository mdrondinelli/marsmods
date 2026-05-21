# Sleeping Overhaul 2 — MC 26.x NeoForge Port

Fork of [SleepingOverhaul2](https://github.com/CosmicDan-Minecraft/SleepingOverhaul2) by CosmicDan, ported to Minecraft 26.1.2 / NeoForge 26.x.

## Changes from upstream

- **Platform:** Architectury multi-platform (Fabric + NeoForge) → NeoForge-only flat source set
- **MC version:** 1.21.4 → 26.1.2
- **Build system:** Architectury Gradle → `net.neoforged.moddev`
- **Sleep API:** Rewrote all `BedBlock` method calls to use `BedRule` (MC 26.x environment attributes API)
- **Time skip suppression:** Switched to `EventHooks.onSleepFinished` returning `null` for `ClockAdjustment`
- **`BedSleepingProblem`:** Updated for record type (MC 26.x); `NOT_POSSIBLE_NOW` → `OTHER_PROBLEM`
- **Access Widener → Access Transformer:** Converted `HungerMobEffect` widener to AT format
- **Mixin compat level:** `JAVA_21` → `JAVA_25`
- **New config option:** `bedRest.bedRestAllowDaytime` — allows the Sleep button to work at any time of day

## License

LGPL-3.0, same as upstream. See [LICENSE.md](LICENSE.md).
