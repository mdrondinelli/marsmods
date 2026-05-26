package me.mar.wildcrops;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class WildCropPatchFeature extends Feature<SimpleBlockConfiguration> {
    public WildCropPatchFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int attempts = Mth.nextInt(random, 3, 5);
        int placed = 0;

        for (int i = 0; i < attempts; i++) {
            int x = origin.getX() + Mth.nextInt(random, -3, 3);
            int z = origin.getZ() + Mth.nextInt(random, -3, 3);
            BlockPos cropPos = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);

            if (tryPlaceCrop(level, random, cropPos, context.config())) {
                placed++;
            }
        }

        return placed > 0;
    }

    private static boolean tryPlaceCrop(WorldGenLevel level, RandomSource random, BlockPos cropPos, SimpleBlockConfiguration config) {
        BlockPos soilPos = cropPos.below();
        BlockState crop = config.toPlace().getOptionalState(level, random, cropPos);
        BlockState originalSoil = level.getBlockState(soilPos);

        if (crop == null || !canReplace(level.getBlockState(cropPos)) || !canSupportPatch(originalSoil)) {
            return false;
        }
        level.setBlock(soilPos, Blocks.ROOTED_DIRT.defaultBlockState(), 2);
        if (!crop.canSurvive(level, cropPos)) {
            level.setBlock(soilPos, originalSoil, 2);
            return false;
        }

        if (!level.setBlock(cropPos, crop, 2)) {
            level.setBlock(soilPos, originalSoil, 2);
            return false;
        }
        return level.getBlockState(cropPos).is(crop.getBlock());
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir()
                || state.is(BlockTags.REPLACEABLE)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS);
    }

    private static boolean canSupportPatch(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.ROOTED_DIRT);
    }
}
