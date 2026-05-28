package me.mar.foodspoilage;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarsFoodSpoilage.MODID);

    public static final DeferredHolder<Item, Item> DRIED_BEEF = ITEMS.registerItem(
            "dried_beef",
            props -> new Item(props.food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.8f).build())));

    public static final DeferredHolder<Item, Item> DRIED_MUTTON = ITEMS.registerItem(
            "dried_mutton",
            props -> new Item(props.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.8f).build())));

    public static final DeferredHolder<Item, Item> DRIED_PORKCHOP = ITEMS.registerItem(
            "dried_porkchop",
            props -> new Item(props.food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.8f).build())));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
