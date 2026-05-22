package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record SpoilageRulesData(List<SpoilageRule> spoilage, List<SpoiledEffectsRule> spoiledEffects) {
    public static final SpoilageRulesData EMPTY = new SpoilageRulesData(List.of(), List.of());

    public static final Codec<SpoilageRulesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    SpoilageRule.CODEC.listOf().optionalFieldOf("spoilage", List.of()).forGetter(SpoilageRulesData::spoilage),
                    SpoiledEffectsRule.CODEC.listOf().optionalFieldOf("spoiled_effects", List.of()).forGetter(SpoilageRulesData::spoiledEffects))
            .apply(instance, SpoilageRulesData::new));
}
