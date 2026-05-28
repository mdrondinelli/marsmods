package me.mar.balancedhunger;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ExhaustionHandlers {
    private ExhaustionHandlers() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.isCanceled()) return;
        Player player = event.getPlayer();
        if (player == null || player.getAbilities().instabuild || player.isSpectator()) return;
        applyDelta(player, BalancedHungerConfig.BLOCK_BREAK_EXHAUSTION.get().floatValue(), VanillaExhaustion.BLOCK_BREAK);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.getAbilities().instabuild || player.isSpectator()) return;

        float movement = classifyMovement(player);
        if (movement > 0F) {
            player.causeFoodExhaustion(movement);
        }

        float idle = BalancedHungerConfig.IDLE_PER_TICK.get().floatValue();
        if (idle > 0F) {
            player.causeFoodExhaustion(idle);
        }
    }

    private static float classifyMovement(Player player) {
        boolean hasMoveInput = player.xxa != 0F || player.zza != 0F;
        boolean jumping = player.isJumping();
        boolean shift = player.isShiftKeyDown();

        if (player.isSwimming()) {
            if (jumping) return BalancedHungerConfig.SWIM_UP_PER_TICK.get().floatValue();
            if (shift) return BalancedHungerConfig.SWIM_DOWN_PER_TICK.get().floatValue();
            return BalancedHungerConfig.SWIM_HORIZONTAL_PER_TICK.get().floatValue();
        }

        if (player.onClimbable()) {
            if (jumping) return BalancedHungerConfig.CLIMB_UP_PER_TICK.get().floatValue();
            if (shift) return 0F;
            return BalancedHungerConfig.CLIMB_DOWN_PER_TICK.get().floatValue();
        }

        if (player.isInWater()) {
            if (jumping) return BalancedHungerConfig.WADE_UP_PER_TICK.get().floatValue();
            if (shift) return BalancedHungerConfig.WADE_DOWN_PER_TICK.get().floatValue();
            if (hasMoveInput) return BalancedHungerConfig.WADE_HORIZONTAL_PER_TICK.get().floatValue();
            return 0F;
        }

        if (player.onGround()) {
            if (player.isSprinting()) return BalancedHungerConfig.SPRINT_PER_TICK.get().floatValue();
            if (player.isCrouching()) return hasMoveInput ? BalancedHungerConfig.CROUCH_PER_TICK.get().floatValue() : 0F;
            return hasMoveInput ? BalancedHungerConfig.WALK_PER_TICK.get().floatValue() : 0F;
        }

        return 0F;
    }

    private static void applyDelta(Player player, float target, float vanilla) {
        float extra = target - vanilla;
        if (extra > 0F) player.causeFoodExhaustion(extra);
    }
}
