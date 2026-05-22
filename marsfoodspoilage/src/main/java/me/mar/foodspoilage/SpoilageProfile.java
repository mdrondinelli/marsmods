package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpoilageProfile(long shelfLifeTicks, long staleThresholdTicks) {
    public static final Codec<SpoilageProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("shelf_life_ticks").forGetter(SpoilageProfile::shelfLifeTicks),
            Codec.LONG.fieldOf("stale_threshold_ticks").forGetter(SpoilageProfile::staleThresholdTicks))
            .apply(instance, SpoilageProfile::new));

    public SpoilageProfile {
        shelfLifeTicks = Math.max(1, shelfLifeTicks);
        staleThresholdTicks = Math.max(0, Math.min(staleThresholdTicks, shelfLifeTicks));
    }
}
