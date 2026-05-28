package me.mar.dryingrack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackRenderState> {
    private static final float SLOT_X = 8f / 16f;
    private static final float SLOT_Y = 18f / 16f;
    private static final float SLOT_Z = 8f / 16f;
    private static final float SCALE = 0.375f;

    private final ItemModelResolver itemModelResolver;

    public DryingRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public DryingRackRenderState createRenderState() {
        return new DryingRackRenderState();
    }

    @Override
    public void extractRenderState(DryingRackBlockEntity blockEntity, DryingRackRenderState state,
            float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(DryingRackBlock.FACING);
        int seed = (int) blockEntity.getBlockPos().asLong();
        var beItems = blockEntity.getItems();
        state.items = new ArrayList<>();
        for (int i = 0; i < DryingRackBlockEntity.SLOTS; i++) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemState, beItems.get(i), ItemDisplayContext.FIXED,
                    blockEntity.getLevel(), null, seed + i);
            state.items.add(itemState);
        }
    }

    @Override
    public void submit(DryingRackRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        // Rotate everything around the block center to match facing
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - state.facing.toYRot()));
        poseStack.translate(-0.5f, 0f, -0.5f);

        for (int i = 0; i < state.items.size(); i++) {
            ItemStackRenderState itemState = state.items.get(i);
            if (itemState.isEmpty()) continue;
            poseStack.pushPose();
            poseStack.translate(SLOT_X, SLOT_Y, SLOT_Z);
            poseStack.scale(SCALE, SCALE, SCALE);
            itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
