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
