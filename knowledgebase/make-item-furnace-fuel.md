# Make An Item Furnace Fuel

Use NeoForge's furnace fuel data map for fixed burn times.

## Working Data Map

Create or merge:

```text
src/main/resources/data/neoforge/data_maps/item/furnace_fuels.json
```

```json
{
  "values": {
    "yourmod:your_item": {
      "burn_time": 100
    }
  }
}
```

`burn_time` is measured in ticks. In Minecraft `26.1.2` / NeoForge `26.1.2.61-beta`, vanilla leaf litter uses `100`.

## When To Use This

- Use this for static item fuel values.
- Use `Item#getBurnTime` only when burn time depends on stack data.
- Avoid `FurnaceFuelBurnTimeEvent` for your own fixed-value item; NeoForge's event docs point custom items to item burn time or the fuel data map.
