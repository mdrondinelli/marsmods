package me.mar.dryingrack;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarsDryingRack.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarsDryingRack.MODID);

    public static final DeferredHolder<Block, DryingRackBlock> DRYING_RACK = BLOCKS.registerBlock(
            DryingRackBlock.NAME,
            DryingRackBlock::new,
            () -> Block.Properties.of()
                    .sound(SoundType.WOOD)
                    .strength(1.0f)
                    .noOcclusion());

    public static final DeferredHolder<Item, BlockItem> DRYING_RACK_ITEM = ITEMS.registerItem(
            DryingRackBlock.NAME,
            props -> new BlockItem(DRYING_RACK.get(), props));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ModBlockEntities.register(modBus);
    }
}
