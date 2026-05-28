package me.mar.dryingrack;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class DryingRackRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public List<ItemStackRenderState> items = Collections.emptyList();
}
