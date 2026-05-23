package me.mar.campfires;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class CampfireEvents {
    private final Map<ServerLevel, PriorityQueue<ExpiryEntry>> expiryQueues = new WeakHashMap<>();
    private final Map<ServerLevel, Set<ChunkPos>> pendingChunkLoads = new WeakHashMap<>();

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && isLitCampfire(event.getPlacedBlock())) {
            ensureExpiry(level, event.getPos(), event.getPlacedBlock(), false);
            queuePositionCheck(level, event.getPos());
        }
    }

    @SubscribeEvent
    public void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) {
            return;
        }

        BlockState state = level.getBlockState(event.getPos());
        if (isCampfire(state)) {
            queuePositionCheck(level, event.getPos());
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            pendingChunkLoads.computeIfAbsent(level, unused -> new HashSet<>()).add(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long gameTime = level.getGameTime();
        processPendingChunkLoads(level);
        processDueExpiries(level, gameTime);
        if (gameTime % MarsCampfiresConfig.SCAN_INTERVAL_TICKS.get() == 0) {
            scanLoadedCampfires(level);
        }
    }

    private void processPendingChunkLoads(ServerLevel level) {
        Set<ChunkPos> chunks = pendingChunkLoads.get(level);
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        Iterator<ChunkPos> iterator = chunks.iterator();
        while (iterator.hasNext()) {
            ChunkPos chunkPos = iterator.next();
            if (level.hasChunk(chunkPos.x(), chunkPos.z())) {
                scanChunk(level, level.getChunk(chunkPos.x(), chunkPos.z()));
                iterator.remove();
            }
        }

        if (chunks.isEmpty()) {
            pendingChunkLoads.remove(level);
        }
    }

    private void processDueExpiries(ServerLevel level, long gameTime) {
        PriorityQueue<ExpiryEntry> queue = expiryQueues.get(level);
        if (queue == null) {
            return;
        }

        while (!queue.isEmpty() && queue.peek().expiresAt <= gameTime) {
            ExpiryEntry entry = queue.poll();
            ChunkPos chunkPos = ChunkPos.containing(entry.pos);
            if (!level.hasChunk(chunkPos.x(), chunkPos.z())) {
                continue;
            }

            BlockState state = level.getBlockState(entry.pos);
            if (!isLitCampfire(state)) {
                clearExpiry(level, entry.pos);
                continue;
            }

            if (entry.checkOnly) {
                ensureExpiry(level, entry.pos, state, true);
                continue;
            }

            Long storedExpiry = existingExpiry(level, entry.pos);
            if (storedExpiry == null || !storedExpiry.equals(entry.expiresAt)) {
                continue;
            }

            extinguish(level, entry.pos, state);
        }

        if (queue.isEmpty()) {
            expiryQueues.remove(level);
        }
    }

    private void scanLoadedCampfires(ServerLevel level) {
        Map<ChunkPos, LevelChunk> chunks = new HashMap<>();
        PriorityQueue<ExpiryEntry> queue = expiryQueues.get(level);
        if (queue != null) {
            for (ExpiryEntry entry : queue) {
                ChunkPos chunkPos = ChunkPos.containing(entry.pos);
                if (level.hasChunk(chunkPos.x(), chunkPos.z())) {
                    chunks.computeIfAbsent(chunkPos, pos -> level.getChunk(pos.x(), pos.z()));
                }
            }
        }

        for (LevelChunk chunk : chunks.values()) {
            scanChunk(level, chunk);
        }
    }

    private void scanChunk(ServerLevel level, LevelChunk chunk) {
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            BlockPos pos = blockEntity.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!isCampfire(state)) {
                continue;
            }

            if (!isLitCampfire(state)) {
                clearExpiry(blockEntity);
                continue;
            }

            if (level.isRainingAt(pos)) {
                extinguish(level, pos, state);
            } else {
                ensureExpiry(level, pos, state, true);
            }
        }
    }

    private void queuePositionCheck(ServerLevel level, BlockPos pos) {
        long checkAt = level.getGameTime() + 1L;
        expiryQueues.computeIfAbsent(level, unused -> new PriorityQueue<>())
                .add(new ExpiryEntry(pos.immutable(), checkAt, true));
    }

    private void ensureExpiry(ServerLevel level, BlockPos pos, BlockState state, boolean checkExisting) {
        if (!isLitCampfire(state)) {
            return;
        }
        if (level.isRainingAt(pos)) {
            extinguish(level, pos, state);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        if (checkExisting) {
            Long existing = existingExpiry(blockEntity);
            if (existing != null) {
                if (existing <= level.getGameTime()) {
                    extinguish(level, pos, state);
                } else {
                    queueExpiry(level, pos, existing);
                }
                return;
            }
        }

        long expiresAt = level.getGameTime() + randomBurnDuration(level.getRandom());
        blockEntity.setData(ModAttachments.CAMPFIRE_EXPIRES_AT, expiresAt);
        blockEntity.setChanged();
        queueExpiry(level, pos, expiresAt);
    }

    private int randomBurnDuration(RandomSource random) {
        int min = MarsCampfiresConfig.MIN_BURN_TICKS.get();
        int max = Math.max(min, MarsCampfiresConfig.MAX_BURN_TICKS.get());
        return min + random.nextInt(max - min + 1);
    }

    private void queueExpiry(ServerLevel level, BlockPos pos, long expiresAt) {
        expiryQueues.computeIfAbsent(level, unused -> new PriorityQueue<>())
                .add(new ExpiryEntry(pos.immutable(), expiresAt, false));
    }

    private void extinguish(ServerLevel level, BlockPos pos, BlockState state) {
        CampfireBlock.dowse(null, level, pos, state);
        clearExpiry(level, pos);
        level.setBlock(pos, state.setValue(CampfireBlock.LIT, false), 3);
    }

    private void clearExpiry(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            clearExpiry(blockEntity);
        }
    }

    private void clearExpiry(BlockEntity blockEntity) {
        if (blockEntity.hasData(ModAttachments.CAMPFIRE_EXPIRES_AT)) {
            blockEntity.removeData(ModAttachments.CAMPFIRE_EXPIRES_AT);
            blockEntity.setChanged();
        }
    }

    private Long existingExpiry(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null ? null : existingExpiry(blockEntity);
    }

    private Long existingExpiry(BlockEntity blockEntity) {
        Long expiry = blockEntity.getExistingDataOrNull(ModAttachments.CAMPFIRE_EXPIRES_AT);
        return expiry == null || expiry < 0L ? null : expiry;
    }

    private static boolean isCampfire(BlockState state) {
        return state.getBlock() instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT);
    }

    private static boolean isLitCampfire(BlockState state) {
        return isCampfire(state) && state.getValue(CampfireBlock.LIT);
    }

    private record ExpiryEntry(BlockPos pos, long expiresAt, boolean checkOnly) implements Comparable<ExpiryEntry> {
        @Override
        public int compareTo(ExpiryEntry other) {
            return Long.compare(expiresAt, other.expiresAt);
        }
    }
}
