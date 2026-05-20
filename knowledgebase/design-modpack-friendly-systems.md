# Design Modpack-Friendly Systems

Use this when deciding whether behavior belongs in Java, datapacks, tags, or pack scripts.

## Preferred Layering

```text
Datapacks
  -> Tags
  -> KubeJS / scripting integration
  -> Small focused Java mods
```

Java should expose hooks, systems, or components. Content and progression should stay data-driven where practical so packs can rebalance without source edits.

## Project Direction

Current pack direction:

```text
primitive survival
  -> mechanical infrastructure
  -> industrial systems
  -> arcane/high-tech fusion
```

Primitive mechanics should evolve rather than become obsolete:

- torches -> lighting infrastructure
- food preservation -> refrigeration
- manual metallurgy -> industrial refining
- sleep -> ritual/dream/infrastructure mechanic

## Good Java Responsibilities

- server-authoritative hooks
- event handlers
- registries and codecs
- capability/attachment-backed state
- integration points for datapacks and KubeJS

## Good Data Responsibilities

- recipes
- loot tables
- tags
- advancements
- worldgen
- balance values where reloadable data is viable

## What Not To Do

- Do not build giant all-in-one gameplay classes.
- Do not hardcode progression lists in Java when tags/data can express them.
- Do not assume direct mod dependencies or load order unless gated.
- Do not optimize for cleverness over stability and interoperability.

## Priority Order

1. correctness
2. stability
3. interoperability
4. reloadability
5. performance
6. configurability
7. polish
