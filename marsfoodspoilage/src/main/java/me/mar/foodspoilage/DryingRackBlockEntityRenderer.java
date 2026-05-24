package me.mar.foodspoilage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackRenderState> {
    // Slot X positions in NORTH-facing model space (x=2/16 and x=14/16, y=17/16, z=8/16)
    private static final float[] SLOT_X = { 2f / 16f, 14f / 16f };
    private static final float SLOT_Y = 17f / 16f;
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
        var items = blockEntity.getItems();
        for (int i = 0; i < DryingRackBlockEntity.SLOTS; i++) {
            this.itemModelResolver.updateForTopItem(
                    state.items[i], items.get(i), ItemDisplayContext.FIXED,
                    blockEntity.getLevel(), null, seed + i);
        }
    }

    @Override
    public void submit(DryingRackRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState camera) {
        // Rotate everything around the block center to match facing
        poseStack.pushPose();
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.translate(-0.5f, 0f, -0.5f);

        for (int i = 0; i < DryingRackBlockEntity.SLOTS; i++) {
            ItemStackRenderState itemState = state.items[i];
            if (itemState.isEmpty()) continue;
            poseStack.pushPose();
            poseStack.translate(SLOT_X[i], SLOT_Y, SLOT_Z);
            poseStack.scale(SCALE, SCALE, SCALE);
            itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
