package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record DryingRecipesData(List<DryingRecipe> recipes) {
    public static final DryingRecipesData EMPTY = new DryingRecipesData(List.of());

    public static final Codec<DryingRecipesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    DryingRecipe.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(DryingRecipesData::recipes))
            .apply(instance, DryingRecipesData::new));
}
