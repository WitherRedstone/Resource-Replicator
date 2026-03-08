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
    private static final float BLOCK_Z_OFFSET = -0.24f; // 方块的偏移

    // 为不同物品类型定义不同的缩放
    private static final float ITEM_SCALE = 0.6f;
    private static final float BLOCK_SCALE = 0.95f;

    /**
     * 渲染物品复制机中的物品
     * <p>
     * 此方法在客户端每帧被调用，用于在物品复制机方块的四个侧面渲染显示的物品。
     * 渲染流程如下：
     * </p>
     * <ul>
     *     <li><strong>物品检查</strong>：获取输入槽的物品，如果为空则不渲染</li>
     *     <li><strong>类型判断</strong>：区分方块类物品（BlockItem）和普通物品，使用不同的偏移量和缩放比例</li>
     *     <li><strong>四面渲染</strong>在北、东、南、西四个方向各渲染一次物品，确保从任何角度都能看到</li>
     *     <li><strong>坐标变换</strong>：根据方向平移到对应面的中心，并旋转到正确的朝向</li>
     *     <li><strong>物品渲染</strong>：使用 FIXED 模式渲染物品，适合固定在方块表面的显示方式</li>
     * </ul>
     * 
     * @param blockEntity 物品复制机方块实体，包含要渲染的物品数据
     * @param partialTick 部分刻时间，用于平滑动画插值（目前未使用）
     * @param poseStack 姿态栈，用于管理模型视图矩阵的变换
     * @param bufferSource 缓冲区源，提供渲染顶点数据的缓冲对象
     * @param packedLight 打包的光照值，包含天空光和方块光的亮度信息
     * @param packedOverlay 打包的覆盖层值，用于处理叠加纹理（目前未使用）
     */
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

    /**
     * 判断是否应该在屏幕外渲染此方块实体
     */
    @Override
    public boolean shouldRenderOffScreen(ItemReplicatorBlockEntity p_173568_) {
        return true;
    }
}
