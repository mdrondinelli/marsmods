package me.mar.hemp;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarsHemp.MODID);

    public static final DeferredItem<Item> PLANT_FIBER =
            ITEMS.registerItem("plant_fiber", Item::new, Item.Properties::new);

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
