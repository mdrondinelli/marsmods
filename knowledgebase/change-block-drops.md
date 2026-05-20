# Change Block Drops

Use `BlockDropsEvent` when drops already reached the drop event and need suppression or replacement.

## Working API

```java
@SubscribeEvent
public void modifyPrimitiveDrops(BlockDropsEvent event) {
    if (event.getState().is(BlockTags.LOGS) && event.getTool().isEmpty()) {
        event.getDrops().clear();
        event.setDroppedExperience(0);
    }
}
```

## Project Examples

Hand-broken logs:

- target: `BlockTags.LOGS`
- tool: empty stack
- action: clear drops and XP

Stone with flint:

- do not handle in `BlockDropsEvent`
- use `PlayerEvent.HarvestCheck` to allow flint to harvest stone
- let vanilla stone loot drop cobblestone

## When To Use This

- Suppress drops after a valid block break.
- Replace drops when vanilla/datapack loot cannot express the behavior.
- Clear XP when suppressing or replacing drops and XP would be wrong.

## What Not To Do

- Do not use drop replacement when harvest logic is the actual missing piece.
- Do not override vanilla/datapack loot if allowing the correct harvest path is enough.
- Do not leave XP behind when the intended behavior is "no drops" unless XP is explicitly desired.
