package me.mar.torches.block;

import java.util.function.ToIntFunction;

import me.mar.torches.config.TorchConfig;
import me.mar.torches.registry.TorchRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class MarsTorchBlock extends net.minecraft.world.level.block.TorchBlock {

    public static final String NAME = "torch";

    public static final int TICK_INTERVAL = 1200;

    // Fixed max = config upper bound (240 min). Avoids dynamic property size from config.
    protected static final IntegerProperty BURNTIME = IntegerProperty.create("burntime", 0, 240);
    protected static final IntegerProperty LITSTATE = IntegerProperty.create("litstate", 0, 2);

    public static final int LIT = 2;
    public static final int SMOLDERING = 1;
    public static final int UNLIT = 0;

    public MarsTorchBlock(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
        registerDefaultState(stateDefinition.any().setValue(LITSTATE, 0).setValue(BURNTIME, 0));
    }

    public static int getInitialBurnTime() {
        return TorchConfig.TORCH_BURNOUT_TIME.get();
    }

    public static boolean shouldBurnOut() {
        return TorchConfig.TORCH_BURNOUT_TIME.get() > 0;
    }

    public static ToIntFunction<BlockState> getLightValueFromState() {
        return (state) -> {
            if (state.getValue(MarsTorchBlock.LITSTATE) == MarsTorchBlock.LIT) {
                return 14;
            } else if (state.getValue(MarsTorchBlock.LITSTATE) == MarsTorchBlock.SMOLDERING) {
                return 12;
            }
            return 0;
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LITSTATE) == LIT || (state.getValue(LITSTATE) == SMOLDERING && level.getRandom().nextInt(2) == 1)) {
            super.animateTick(state, level, pos, random);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() == Items.FLINT_AND_STEEL
                || stack.getItem() == TorchRegistry.MATCHBOX_ITEM.get()
                || TorchConfig.LIGHT_TORCH_ITEMS.get().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            playLightingSound(level, pos);
            if (!player.isCreative() && (stack.getItem() != TorchRegistry.MATCHBOX_ITEM.get() || TorchConfig.MATCHBOX_DURABILITY.get() > 0)) {
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            if (level.isRainingAt(pos)) {
                playExtinguishSound(level, pos);
            } else {
                changeToLit(level, pos, state);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (shouldBurnOut() && state.getValue(LITSTATE) > UNLIT) {
            if (level.isRainingAt(pos)) {
                playExtinguishSound(level, pos);
                changeToUnlit(level, pos, state);
                return;
            }
            int newBurnTime = state.getValue(BURNTIME) - 1;
            int initialBurnTime = getInitialBurnTime();
            if (newBurnTime <= 0) {
                playExtinguishSound(level, pos);
                changeToUnlit(level, pos, state);
                level.updateNeighborsAt(pos, this);
            } else if (state.getValue(LITSTATE) == LIT && (newBurnTime <= initialBurnTime / 10 || newBurnTime <= 1)) {
                changeToSmoldering(level, pos, state, newBurnTime);
                level.updateNeighborsAt(pos, this);
            } else {
                level.setBlock(pos, state.setValue(BURNTIME, newBurnTime), 2);
                level.scheduleTick(pos, this, TICK_INTERVAL);
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            defaultBlockState().updateNeighbourShapes(level, pos, 3);
        }
        super.onPlace(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BURNTIME);
        builder.add(LITSTATE);
    }

    public static IntegerProperty getBurnTime() {
        return BURNTIME;
    }

    public static IntegerProperty getLitState() {
        return LITSTATE;
    }

    public void changeToLit(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TorchRegistry.TORCH_BLOCK.get().defaultBlockState().setValue(LITSTATE, LIT).setValue(BURNTIME, getInitialBurnTime()), 2);
        if (shouldBurnOut()) {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    public void changeToSmoldering(Level level, BlockPos pos, BlockState state, int newBurnTime) {
        if (shouldBurnOut()) {
            level.setBlock(pos, TorchRegistry.TORCH_BLOCK.get().defaultBlockState().setValue(LITSTATE, SMOLDERING).setValue(BURNTIME, newBurnTime), 2);
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    public void changeToUnlit(Level level, BlockPos pos, BlockState state) {
        if (shouldBurnOut()) {
            if (TorchConfig.NO_RELIGHT_ENABLED.get()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                level.setBlock(pos, TorchRegistry.TORCH_BLOCK.get().defaultBlockState(), 2);
                level.scheduleTick(pos, this, TICK_INTERVAL);
            }
        }
    }

    public void playLightingSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    public void playExtinguishSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
