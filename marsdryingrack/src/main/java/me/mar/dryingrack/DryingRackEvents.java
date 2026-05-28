package me.mar.dryingrack;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public final class DryingRackEvents {
    private DryingRackEvents() {}

    /**
     * Fired before an item is placed on a drying rack. Cancel to veto the placement.
     */
    public static class PlaceCheck extends Event implements ICancellableEvent {
        private final ServerLevel level;
        private final BlockPos pos;
        private final ItemStack stack;

        public PlaceCheck(ServerLevel level, BlockPos pos, ItemStack stack) {
            this.level = level;
            this.pos = pos;
            this.stack = stack;
        }

        public ServerLevel getLevel() { return level; }
        public BlockPos getPos() { return pos; }
        public ItemStack getStack() { return stack; }
    }

    /**
     * Fired after an item enters a drying rack — either placed by a player or
     * produced by a completed drying recipe.
     */
    public static class Enter extends Event {
        private final ServerLevel level;
        private final BlockPos pos;
        private final ItemStack stack;

        public Enter(ServerLevel level, BlockPos pos, ItemStack stack) {
            this.level = level;
            this.pos = pos;
            this.stack = stack;
        }

        public ServerLevel getLevel() { return level; }
        public BlockPos getPos() { return pos; }
        public ItemStack getStack() { return stack; }
    }

    /**
     * Fired before an item leaves a drying rack — by player removal or block break.
     */
    public static class Exit extends Event {
        private final ServerLevel level;
        private final BlockPos pos;
        private final ItemStack stack;

        public Exit(ServerLevel level, BlockPos pos, ItemStack stack) {
            this.level = level;
            this.pos = pos;
            this.stack = stack;
        }

        public ServerLevel getLevel() { return level; }
        public BlockPos getPos() { return pos; }
        public ItemStack getStack() { return stack; }
    }
}
