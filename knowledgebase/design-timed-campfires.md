# Design Timed Campfires

Use this when implementing block state that expires after elapsed game time, including while chunks are unloaded.

## Absolute Expiry

Store an absolute server `gameTime` expiry on the campfire block entity instead of decrementing a timer:

- On placement or lighting, write `expiresAt = level.getGameTime() + randomDuration`.
- While the chunk is loaded, queue the position by `expiresAt` and extinguish when due.
- On `ChunkEvent.Load`, defer work to the next `LevelTickEvent.Post`, then scan that chunk's block entities. If a lit campfire has an expired timestamp, extinguish it immediately.

This makes unloaded time count without force-loading chunks.

## Hook Shape

`ChunkEvent.Load` on the server fires before the `LevelChunk` is fully promoted. Do not read/write the level there. Store the `ChunkPos` and process it on the next level tick after `level.hasChunk(x, z)` is true.

For vanilla campfires, use a persisted block entity attachment for the expiry timestamp. Call `BlockEntity#setChanged()` after writing or removing the attachment.

## Rain

Rain extinguishing only needs loaded-chunk reconciliation:

- On periodic scans of known loaded campfire chunks, call `level.isRainingAt(pos)`.
- If true and the campfire is lit, call `CampfireBlock.dowse(...)`, clear expiry, and set `CampfireBlock.LIT` to false.

Do not try to reconstruct historical rain for unloaded chunks unless you also store weather intervals globally.
