package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record SpoiledEffectsRule(TagKey<Item> tag, SpoiledFoodEffects effects) {
    private static final Codec<TagKey<Item>> ITEM_TAG_CODEC = Identifier.CODEC
            .xmap(id -> TagKey.create(Registries.ITEM, id), TagKey::location);

    public static final Codec<SpoiledEffectsRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ITEM_TAG_CODEC.fieldOf("tag").forGetter(SpoiledEffectsRule::tag),
                    SpoiledFoodEffects.CODEC.fieldOf("effects").forGetter(SpoiledEffectsRule::effects))
            .apply(instance, SpoiledEffectsRule::new));
}
