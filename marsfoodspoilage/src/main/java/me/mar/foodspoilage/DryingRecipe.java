package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public record DryingRecipe(Item input, Item output, int durationTicks) {
    private static final Codec<Item> ITEM_CODEC = Identifier.CODEC
            .xmap(id -> BuiltInRegistries.ITEM.getValue(id),
                  item -> BuiltInRegistries.ITEM.getKey(item));

    public static final Codec<DryingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ITEM_CODEC.fieldOf("input").forGetter(DryingRecipe::input),
                    ITEM_CODEC.fieldOf("output").forGetter(DryingRecipe::output),
                    Codec.INT.fieldOf("duration_ticks").forGetter(DryingRecipe::durationTicks))
            .apply(instance, DryingRecipe::new));
}
