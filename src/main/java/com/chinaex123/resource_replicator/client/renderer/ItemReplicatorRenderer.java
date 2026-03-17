package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.client.renderer.state.ItemReplicatorRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ItemReplicatorRenderer implements BlockEntityRenderer<ItemReplicatorBlockEntity, ItemReplicatorRenderState> {
    private static final float BLOCK_Z_OFFSET = 0.20F;
    private static final float ITEM_Z_OFFSET = 0.02F;
    private static final float BLOCK_SCALE = 0.8F;
    private static final float ITEM_SCALE = 0.65F;

    public ItemReplicatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ItemReplicatorRenderState createRenderState() {
        return new ItemReplicatorRenderState();
    }

    @Override
    public void extractRenderState(ItemReplicatorBlockEntity blockEntity, ItemReplicatorRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        // 调用父类方法初始化基本状态（blockPos, blockState, blockEntityType, lightCoords）
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

        // 直接从 items 数组获取输入槽物品（索引 0）
        state.displayedItem = blockEntity.items[0];
        state.hasItem = !state.displayedItem.isEmpty();
    }

    @Override
    public void submit(ItemReplicatorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!state.hasItem) {
            return;
        }

        ItemStack displayedItem = state.displayedItem;
        boolean isBlock = displayedItem.getItem() instanceof BlockItem;
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        for (Direction dir : directions) {
            poseStack.pushPose();
            float zOffset = isBlock ? BLOCK_Z_OFFSET : ITEM_Z_OFFSET;

            switch (dir) {
                case NORTH -> {
                    poseStack.translate(0.5, 0.5, zOffset);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0));
                }
                case EAST -> {
                    poseStack.translate(1.0 - zOffset, 0.5, 0.5);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                }
                case SOUTH -> {
                    poseStack.translate(0.5, 0.5, 1.0 - zOffset);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                }
                case WEST -> {
                    poseStack.translate(zOffset, 0.5, 0.5);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
                }
            }

            float scale = isBlock ? BLOCK_SCALE : ITEM_SCALE;
            poseStack.scale(scale, scale, scale);

            // 创建 ItemStackRenderState 并渲染
            var itemRenderState = new ItemStackRenderState();
            var itemModelResolver = Minecraft.getInstance().getItemModelResolver();
            itemModelResolver.updateForTopItem(itemRenderState, displayedItem, ItemDisplayContext.FIXED, null, null, 0);

            // 使用 ItemStackRenderState.submit 进行渲染
            itemRenderState.submit(poseStack, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }
    }
}
