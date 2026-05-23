package me.mar.torches.item;

import me.mar.torches.block.MarsTorchBlock;
import me.mar.torches.registry.TorchRegistry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public class LitTorchItem extends StandingAndWallBlockItem {

    public static final String NAME = "lit_torch";

    public LitTorchItem(Properties properties, Direction direction) {
        super(TorchRegistry.TORCH_BLOCK.get(), TorchRegistry.TORCH_WALL_BLOCK.get(), direction, properties);
    }

    @Override
    public BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            return state.setValue(MarsTorchBlock.getLitState(), MarsTorchBlock.LIT)
                        .setValue(MarsTorchBlock.getBurnTime(), MarsTorchBlock.getInitialBurnTime());
        }
        return null;
    }
}
