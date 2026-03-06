package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemReplicatorRenderer implements BlockEntityRenderer<ItemReplicatorBlockEntity> {

    public ItemReplicatorRenderer(BlockEntityRendererProvider.Context context) {}

    // 为不同物品类型定义不同的偏移量
    private static final float ITEM_Z_OFFSET = -0.02f; // 物品的偏移
    private static final float BLOCK_Z_OFFSET = -0.20f; // 方块的偏移

    // 为不同物品类型定义不同的缩放
    private static final float ITEM_SCALE = 0.6f;
    private static final float BLOCK_SCALE = 0.8f;

    @Override
    public void render(ItemReplicatorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack displayedItem = blockEntity.getDisplayedItem();

        if (displayedItem.isEmpty()) {
            return;
        }

        // 判断是物品还是方块
        boolean isBlock = displayedItem.getItem() instanceof BlockItem;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 在四个侧面渲染物品
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        for (Direction dir : directions) {
            poseStack.pushPose();

            // 根据物品类型选择偏移量
            float zOffset = isBlock ? BLOCK_Z_OFFSET : ITEM_Z_OFFSET;

            // 移动到对应面的中心，并根据物品类型调整向外偏移
            switch (dir) {
                case NORTH -> {
                    poseStack.translate(0.5, 0.5, -zOffset);
                    poseStack.mulPose(Axis.YP.rotationDegrees(0));
                }
                case EAST -> {
                    poseStack.translate(1 + zOffset, 0.5, 0.5);
                    poseStack.mulPose(Axis.YP.rotationDegrees(270));
                }
                case SOUTH -> {
                    poseStack.translate(0.5, 0.5, 1 + zOffset);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                }
                case WEST -> {
                    poseStack.translate(-zOffset, 0.5, 0.5);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
            }

            // 根据物品类型选择缩放
            float scale = isBlock ? BLOCK_SCALE : ITEM_SCALE;
            poseStack.scale(scale, scale, scale);

            // 使用 FIXED 模式
            itemRenderer.renderStatic(displayedItem, ItemDisplayContext.FIXED,
                    packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, blockEntity.getLevel(), 0);

            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ItemReplicatorBlockEntity p_173568_) {
        return true;
    }
}
