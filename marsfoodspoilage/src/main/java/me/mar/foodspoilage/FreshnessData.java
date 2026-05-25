package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FreshnessData(long lastUpdateTick, long remainingFreshTicks, long staleThresholdTicks, float spoilageRate) {
    public FreshnessData {
        spoilageRate = Math.max(0.0f, spoilageRate);
    }

    public static final Codec<FreshnessData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("last_update_tick").forGetter(FreshnessData::lastUpdateTick),
            Codec.LONG.fieldOf("remaining_fresh_ticks").forGetter(FreshnessData::remainingFreshTicks),
            Codec.LONG.fieldOf("stale_threshold_ticks").forGetter(FreshnessData::staleThresholdTicks),
            Codec.FLOAT.optionalFieldOf("spoilage_rate", 1.0f).forGetter(FreshnessData::spoilageRate))
            .apply(instance, FreshnessData::new));

    public static final StreamCodec<ByteBuf, FreshnessData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, FreshnessData::lastUpdateTick,
            ByteBufCodecs.VAR_LONG, FreshnessData::remainingFreshTicks,
            ByteBufCodecs.VAR_LONG, FreshnessData::staleThresholdTicks,
            ByteBufCodecs.FLOAT, FreshnessData::spoilageRate,
            FreshnessData::new);

    public FreshnessData updatedTo(long currentTick) {
        long elapsed = Math.max(0, currentTick - this.lastUpdateTick);
        long effective = Math.round(elapsed * this.spoilageRate);
        long remaining = Math.max(0, this.remainingFreshTicks - effective);
        return new FreshnessData(currentTick, remaining, this.staleThresholdTicks, this.spoilageRate);
    }

    public FreshnessData withRate(float rate) {
        return new FreshnessData(lastUpdateTick, remainingFreshTicks, staleThresholdTicks, rate);
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
