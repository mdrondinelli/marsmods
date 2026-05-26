package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.LeafLitterSleepSupport;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

public class LeafLitterSleepMixinsCommon {}

@Mixin(LeafLitterBlock.class)
abstract class LeafLitterSleepMixinsCommonLeafLitterBlock {
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()
            && LeafLitterSleepSupport.getSleepDirection(level, pos, state).isPresent();
    }

    public void setBedOccupied(BlockState state, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
    }

    public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        return LeafLitterSleepSupport.getBedDirection(level, pos, state);
    }
}
