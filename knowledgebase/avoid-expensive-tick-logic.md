# Avoid Expensive Tick Logic

Use this when tempted to poll world state or add ticking systems.

## Biggest Modded Performance Risks

- ticking entities
- chunk scans
- pathfinding spam
- block entity ticking
- excessive allocations
- synchronization overhead

Many small tick costs stack badly in modpacks.

## Prefer

- NeoForge events
- lifecycle hooks
- scheduled updates
- sparse ticking
- cached local computations
- capability/attachment state
- event-driven transitions

## What Not To Do

- Do not scan chunks every tick.
- Do not iterate entities globally every tick.
- Do not create nested ticking structures without a hard reason.
- Do not log from tick paths except carefully gated debug categories.
- Do not use reflection hacks when an event/hook exists.
