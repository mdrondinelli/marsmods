package me.mar.torches.worldgen;

// Disabled: missing configured_feature/placed_feature/biome_modifier data JSONs.
// See knowledgebase/marstorches-worldgen.md for planned StructurePlaceEvent approach.

import me.mar.torches.block.MarsTorchBlock;
import me.mar.torches.block.MarsWallTorchBlock;
import me.mar.torches.config.TorchConfig;
import me.mar.torches.registry.TorchRegistry;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TorchFeature extends Feature<NoneFeatureConfiguration> {

    public static final String NAME = "replace_all_feature";

    public TorchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        MutableBlockPos replacePos = new MutableBlockPos();
        int initialBurnTime = MarsTorchBlock.getInitialBurnTime();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < context.level().getHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    replacePos.set(context.origin().getX(), 0, context.origin().getZ()).move(x, y, z);
                    if (context.level().getBlockState(replacePos).getBlock() == Blocks.TORCH) {
                        context.level().setBlock(replacePos, TorchRegistry.TORCH_BLOCK.get().defaultBlockState()
                                .setValue(MarsTorchBlock.getLitState(), MarsTorchBlock.LIT)
                                .setValue(MarsTorchBlock.getBurnTime(), initialBurnTime), 3);
                        context.level().scheduleTick(replacePos, context.level().getBlockState(replacePos).getBlock(), TorchConfig.TORCH_BURNOUT_TIME.get());
                    } else if (context.level().getBlockState(replacePos).getBlock() == Blocks.WALL_TORCH) {
                        context.level().setBlock(replacePos, TorchRegistry.TORCH_WALL_BLOCK.get().defaultBlockState()
                                .setValue(MarsWallTorchBlock.getLitState(), MarsTorchBlock.LIT)
                                .setValue(MarsWallTorchBlock.getBurnTime(), initialBurnTime)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.level().getBlockState(replacePos).getValue(BlockStateProperties.HORIZONTAL_FACING)), 3);
                        context.level().scheduleTick(replacePos, context.level().getBlockState(replacePos).getBlock(), TorchConfig.TORCH_BURNOUT_TIME.get());
                    }
                }
            }
        }
        return true;
    }
}
