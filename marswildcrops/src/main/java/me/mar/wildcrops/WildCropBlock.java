package me.mar.wildcrops;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WildCropBlock extends CropBlock {
    public static final MapCodec<WildCropBlock> CODEC = simpleCodec(properties -> new WildCropBlock(properties, null));
    private final Item cloneItem;

    public WildCropBlock(BlockBehaviour.Properties properties, Item cloneItem) {
        super(properties);
        this.cloneItem = cloneItem;
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
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
        return this.cloneItem;
    }
}
