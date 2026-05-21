# Design Sleep Or Time Systems

Use this when designing sleep, rest, fatigue, dream, or time-acceleration mechanics.

## Known Reference

Sleeping Overhaul 2 is useful prior art.

Key detail:

- it accelerates actual server ticking during sleep
- it does not simply skip time

## Useful Concepts

- distinguish "resting in bed" from "actually sleeping"
- make sleep state multiplayer-aware
- expose a configurable sleep pipeline
- connect sleep to progression, infrastructure, ritual, dream, or recovery systems

## Risk

Whole-server tick acceleration can destabilize modpack balance. Accelerating real ticks means other mods' machines, entities, farms, and world systems may all run faster.

## What Not To Do

- Do not add arbitrary punishment or constant micromanagement.
- Do not interrupt building/play loops with fatigue too often.
- Do not skip time blindly if other systems depend on real tick progression.
- Do not make sleep logic client-authoritative.

---

## MC 26.x Sleep API

### BedRule (replaces BedBlock methods)

`BedBlock.canSetSpawn()`, `bedWorks()`, `natural()` all removed. All bed behavior is now in a `BedRule` record retrieved from the level:

```java
BedRule rule = level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, pos);
rule.explodes()         // should the bed explode in this dimension?
rule.canSleep(Level)    // is it nighttime / valid for sleep?
rule.canSetSpawn(Level) // can this bed be used as a respawn anchor?
rule.asProblem()        // returns the BedSleepingProblem for this failure case
```

Wrap these in mixins, not `BedBlock` methods.

### Suppress time skip on sleep

`EventHooks.onSleepFinished(ServerLevel, ClockAdjustment)` — return `null` to suppress all clock advancement. Called from `ServerLevel.tick()` when enough players sleep. Correct hook for any custom sleep action (timelapse, nothing, etc.).

### BedSleepingProblem is a record, not an enum

`NOT_POSSIBLE_NOW` is gone. Daytime / wrong environment → `OTHER_PROBLEM` (message = null). Physical blocks each have their own constant (`TOO_FAR_AWAY`, `OBSTRUCTED`, `NOT_SAFE`).

### Per-tick continue-sleep check

`Player.tick()` calls:

```java
EventHooks.canEntityContinueSleeping(player,
    !BedRule.canSleep(level) ? BedSleepingProblem.OTHER_PROBLEM : null)
```

Returning false kicks the player out of bed. Intercept `OTHER_PROBLEM` at this call site to allow daytime sleep.

### reallySleeping pattern (multiplayer bed-rest)

To distinguish "resting in bed" from "actually pressed Sleep" in multiplayer, add a per-player `reallySleeping` boolean via Mixin on `Player`. Override `SleepStatus.update()` to substitute `reallySleeping` for `isSleeping()` — otherwise bed-resting players count toward `playersSleepingPercentage` and trigger the sleep threshold unintentionally.

See `marssleepingoverhaul2` `BedRestMixinsCommon.java` for a working implementation.
