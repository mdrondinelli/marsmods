package me.mar.bellows;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BellowsItem extends Item {
    private static final int COOLDOWN_TICKS = 15;

    public BellowsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof KilnBlockEntity kiln) || !kiln.canApplyBellowsBoost()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            kiln.applyBellowsBoost();
            serverLevel.playSound(null, context.getClickedPos(), ModSoundEvents.BELLOWS_USE.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
            if (player != null) {
                player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
                stack.hurtAndBreak(1, serverLevel, player, brokenItem -> player.onEquippedItemBroken(brokenItem, context.getHand().asEquipmentSlot()));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
