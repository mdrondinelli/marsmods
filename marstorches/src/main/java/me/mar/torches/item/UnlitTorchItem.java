package me.mar.torches.item;

import me.mar.torches.registry.TorchRegistry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;

public class UnlitTorchItem extends StandingAndWallBlockItem {

    public static final String NAME = "unlit_torch";

    public UnlitTorchItem(Properties properties, Direction direction) {
        super(TorchRegistry.TORCH_BLOCK.get(), TorchRegistry.TORCH_WALL_BLOCK.get(), direction, properties);
    }
}
