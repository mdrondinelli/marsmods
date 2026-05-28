# Roadmap

Gameplay design ideas captured during playtesting. Not yet implemented except where noted.

## Leather gate — bellows progression

Current: cow → instant leather → bellows → iron. Side-progression feels too cheap relative to the kiln chain that precedes it.

**MVP (Option A): rawhide + drying rack**

- Cow drops `rawhide` instead of leather.
- Drying rack (existing `marsfoodspoilage:drying_rack`) accepts rawhide → leather over time.
- Reuses existing block. Minimal new content.

**Later expansions if A still too easy:**

- Option B — multi-step tan chain: rawhide → soak (cauldron water) → scrape (flint tool) → dry → leather.
- Option C — tannin ingredient: oak bark from log stripping, used alongside rawhide on the rack.

**Don't design for Energized Power automation yet.** Pipe support comes for free once rawhide/leather are tagged.

## Wheat / bread trivialization

Bread already spoils (Lever 2 — done), but wheat-and-go pattern still trivializes hunger because flour-to-bread is instant in the crafting grid. Need to anchor baking to a station.

**Lever 1 — multi-step bake chain**

Wheat → flour (via quern) → dough (flour + water bottle, returns glass bottle) → bread (smelt in kiln or furnace).

Quern is a **block**, not an item. Rationale: an item version doesn't solve the trivialization — it just lets you carry the grinding step in your pocket. The whole point is anchoring bake to a base.

Quern design:
- Recipe: 4 cobble + 1 smooth_stone (center) + 1 stick. Pre-iron tier, post-kiln.
- One input slot, one output slot, no fuel. Time per grain ~80–100 ticks ("leave it running" cadence, not slot-machine).
- GUI like furnace. Grinding sfx loop + particle dust while ticking.
- Hopper-feedable (don't gate automation — let watermill/EP differentiate on other axes).
- Durability optional later: block self-destructs after N grinds, needs recraft. Stones wear down — fits reality. Add if economy feels flat.

**Lever 3 — fresh vs preserved tier (later)**

- Fresh bread: high hunger+saturation, spoils fast.
- Hardtack (twice-baked, or flour + salt + bake): low hunger, no spoil. Travel ration.

Gives wheat two purposes. Mirrors army hardtack / ship biscuit.

## Automation path for grinding (post-MVP)

In order:

1. **Watermill grinder** — same block-entity pattern as quern. Place adjacent to flowing water, no GUI, auto-pulls from neighbor inventory, ~2× quern rate.
2. **Windmill grinder** — sky-access required, weather-sensitive (storms boost output?). Skip until watermill validates the pattern.
3. **Energized Power integration via tags** — no EP-specific recipes. Mirror NeoForge common tag conventions: `#c:foods/flour`, `#c:foods/dough`, etc. EP pulverizer recipes that produce flour will already feed our chain; our dough recipe will accept any tagged flour. Tag handshake, not hard dependency.

## Tag conventions

Use NeoForge `#c:...` common tags for any item that another mod might also produce (flour, dough, tin, leather alternatives). Don't author cross-mod-specific recipes — let tag overlap do the work. Existing example in repo: `marsbellows/data/marsbellows/melting_points/tin.json` uses `#c:ores/tin` and `#c:raw_materials/tin` to pick up EnergizedPower tin without naming the mod.

## Small ideas / parking lot

- Cobble → stone might become a crafting recipe (uncook, not melt) instead of kiln smelting. Removes one of the kiln's most-used early recipes — reconsider if kiln feels underused as a result.
- Grinding sfx and dust particles on quern reuse the audio-texture lessons from kiln boost (sensory feedback = perceived depth).
