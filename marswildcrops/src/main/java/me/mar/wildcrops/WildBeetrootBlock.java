package me.mar.wildcrops;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WildBeetrootBlock extends BeetrootBlock {
    public WildBeetrootBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.ROOTED_DIRT);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSufficientLight(level, pos) && this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    @Override
    protected Item getBaseSeedId() {
        return Items.BEETROOT_SEEDS;
    }
}
