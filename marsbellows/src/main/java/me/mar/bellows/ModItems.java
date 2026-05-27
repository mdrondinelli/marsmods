package me.mar.bellows;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarsBellows.MODID);

    public static final DeferredItem<Item> BELLOWS =
            ITEMS.registerItem("bellows", BellowsItem::new, () -> new Item.Properties().durability(64).useCooldown(5.0F));

    public static final DeferredHolder<Item, BlockItem> KILN =
            ITEMS.registerItem(KilnBlock.NAME, props -> new BlockItem(ModBlocks.KILN.get(), props));

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
