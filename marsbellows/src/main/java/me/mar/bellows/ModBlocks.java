package me.mar.bellows;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarsBellows.MODID);

    public static final DeferredHolder<Block, KilnBlock> KILN = BLOCKS.registerBlock(
            KilnBlock.NAME,
            KilnBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.BRICKS)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(KilnBlock.LIT) ? 13 : 0));

    private ModBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
