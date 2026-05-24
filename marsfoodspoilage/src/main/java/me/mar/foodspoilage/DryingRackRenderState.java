package me.mar.foodspoilage;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class DryingRackRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public final ItemStackRenderState[] items = {
            new ItemStackRenderState(),
            new ItemStackRenderState()
    };
}
