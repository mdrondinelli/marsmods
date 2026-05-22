package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FreshnessData(long lastUpdateTick, long remainingFreshTicks, long initialFreshTicks, long staleThresholdTicks) {
    public static final Codec<FreshnessData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("last_update_tick").forGetter(FreshnessData::lastUpdateTick),
            Codec.LONG.fieldOf("remaining_fresh_ticks").forGetter(FreshnessData::remainingFreshTicks),
            Codec.LONG.fieldOf("initial_fresh_ticks").forGetter(FreshnessData::initialFreshTicks),
            Codec.LONG.fieldOf("stale_threshold_ticks").forGetter(FreshnessData::staleThresholdTicks))
            .apply(instance, FreshnessData::new));

    public static final StreamCodec<ByteBuf, FreshnessData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, FreshnessData::lastUpdateTick,
            ByteBufCodecs.VAR_LONG, FreshnessData::remainingFreshTicks,
            ByteBufCodecs.VAR_LONG, FreshnessData::initialFreshTicks,
            ByteBufCodecs.VAR_LONG, FreshnessData::staleThresholdTicks,
            FreshnessData::new);

    public FreshnessData updatedTo(long currentTick) {
        long elapsed = Math.max(0, currentTick - this.lastUpdateTick);
        long remaining = Math.max(0, this.remainingFreshTicks - elapsed);
        return new FreshnessData(currentTick, remaining, this.initialFreshTicks, this.staleThresholdTicks);
    }

    public SpoilageState state() {
        if (this.remainingFreshTicks <= 0) {
            return SpoilageState.SPOILED;
        }
        if (this.remainingFreshTicks <= this.staleThresholdTicks) {
            return SpoilageState.STALE;
        }
        return SpoilageState.FRESH;
    }
}
