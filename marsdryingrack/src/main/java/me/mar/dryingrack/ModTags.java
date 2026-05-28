package me.mar.dryingrack;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    private ModTags() {}

    public static final class Items {
        private Items() {}

        public static final TagKey<Item> DRYABLE = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MarsDryingRack.MODID, "dryable"));
    }
}
