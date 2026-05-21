package me.mar.flinttool;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarsFlintTool.MODID);

    public static final DeferredItem<FlintToolItem> FLINT_TOOL = ITEMS.registerItem("flint_tool", FlintToolItem::new, Item.Properties::new);

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
