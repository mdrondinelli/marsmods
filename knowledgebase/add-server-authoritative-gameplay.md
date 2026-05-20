# Add Server-Authoritative Gameplay

Use this when adding persistent state, gameplay rules, configs, networking, or player/block behavior.

## Server Authority

Minecraft gameplay state should live server-side. Client code should mostly present state or send input requests.

Good server-side locations:

- NeoForge common event handlers
- server configs
- attachments/capabilities
- datapack reload listeners
- block/entity/server-level state

## Configs

Separate:

- server gameplay config
- client cosmetic config

Gameplay-affecting values should be server authoritative and synced if clients need to display them.

Expose real balance knobs where useful:

- timings
- multipliers
- toggles
- compatibility behavior

## Attachments And Capabilities

Use modern attachment/capability-style systems for persistent custom data.

Good uses:

- player progression
- custom sleep state
- fatigue
- ritual affinity
- machine metadata

## Networking

Use packets only when needed:

- GUI sync
- visual effects
- client interaction requests
- custom state sync

## What Not To Do

- Do not modify authoritative gameplay state only on the client.
- Do not use giant static managers or global UUID maps when attachments fit.
- Do not spam packets for constantly changing large data.
- Do not put gameplay logic in rendering classes.
