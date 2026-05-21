package me.mar.foodspoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record SpoilageRule(TagKey<Item> tag, Optional<SpoilageProfile> profile) {
    private static final Codec<TagKey<Item>> ITEM_TAG_CODEC = Identifier.CODEC
            .xmap(id -> TagKey.create(Registries.ITEM, id), TagKey::location);

    public static final Codec<SpoilageRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ITEM_TAG_CODEC.fieldOf("tag").forGetter(SpoilageRule::tag),
                    SpoilageProfile.CODEC.optionalFieldOf("profile").forGetter(SpoilageRule::profile))
            .apply(instance, SpoilageRule::new));
}
