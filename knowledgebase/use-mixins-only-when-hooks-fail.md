# Use Mixins Only When Hooks Fail

Use this when deciding whether to alter vanilla behavior directly.

## Rule

Prefer NeoForge events, registries, capabilities, attachments, datapacks, and tags first.

Use mixins only when:

- vanilla behavior genuinely must change
- no event or hook exists
- datapacks/tags/configs cannot express the behavior

## If A Mixin Is Necessary

- keep injection narrow
- isolate it clearly
- document intent and target behavior
- avoid local-variable fragility
- verify after Minecraft/NeoForge updates

## What Not To Do

- Do not use broad invasive injections for convenience.
- Do not use fragile local captures unless unavoidable.
- Do not stack overlapping redirects.
- Do not use mixins when an event like `BreakSpeed`, `HarvestCheck`, `BlockDropsEvent`, or `ModifyRecipeJsonsEvent` solves the task.
