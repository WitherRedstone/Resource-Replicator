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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 物品复制机渲染器类
 * 负责在客户端渲染物品复制机中显示的物品（四个侧面）
 */
public class ItemReplicatorRenderer implements BlockEntityRenderer<@NotNull ItemReplicatorBlockEntity, @NotNull ItemReplicatorRenderState> {
    private static final float BLOCK_Z_OFFSET = 0.20F; // 方块的 Z 轴偏移量（使方块稍微突出表面）
    private static final float ITEM_Z_OFFSET = 0.02F; // 物品的 Z 轴偏移量（使物品贴在表面）
    private static final float BLOCK_SCALE = 0.8F; // 方块的缩放比例
    private static final float ITEM_SCALE = 0.65F; // 物品的缩放比例

    /**
     * 构造函数
     */
    public ItemReplicatorRenderer(BlockEntityRendererProvider.Context context) {
        // 不需要额外初始化
    }

    /**
     * 创建渲染状态对象
     */
    @Override
    public ItemReplicatorRenderState createRenderState() {
        return new ItemReplicatorRenderState();
    }

    /**
     * 从方块实体提取渲染状态数据
     */
    @Override
    public void extractRenderState(ItemReplicatorBlockEntity blockEntity, ItemReplicatorRenderState state, float partialTick,
                                   @NotNull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        // 调用父类方法初始化基本状态（blockPos, blockState, blockEntityType, lightCoords）
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

        // 直接从 items 数组获取输入槽物品（索引 0）
        state.displayedItem = blockEntity.items[0];
        // 设置是否有物品的标志
        state.hasItem = !state.displayedItem.isEmpty();
    }

    /**
     * 提交几何体到渲染管线
     */
    @Override
    public void submit(ItemReplicatorRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraState) {
        // 如果没有物品，跳过渲染
        if (!state.hasItem) {
            return;
        }

        ItemStack displayedItem = state.displayedItem;
        // 判断是否为方块物品（方块和物品的渲染方式不同）
        boolean isBlock = displayedItem.getItem() instanceof BlockItem;
        // 定义四个水平方向的数组
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        // 遍历四个方向，在每个面上渲染物品
        for (Direction dir : directions) {
            // 保存当前姿态变换
            poseStack.pushPose();
            // 根据是方块还是物品选择 Z 轴偏移量
            float zOffset = isBlock ? BLOCK_Z_OFFSET : ITEM_Z_OFFSET;

            // 根据方向设置不同的变换
            switch (dir) {
                case NORTH -> {
                    // 北面：Z 轴正方向
                    poseStack.translate(0.5, 0.5, zOffset);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0));
                }
                case EAST -> {
                    // 东面：X 轴正方向，旋转 90 度
                    poseStack.translate(1.0 - zOffset, 0.5, 0.5);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                }
                case SOUTH -> {
                    // 南面：Z 轴负方向，旋转 180 度
                    poseStack.translate(0.5, 0.5, 1.0 - zOffset);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                }
                case WEST -> {
                    // 西面：X 轴负方向，旋转 -90 度
                    poseStack.translate(zOffset, 0.5, 0.5);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
                }
            }

            // 根据是方块还是物品选择缩放比例
            float scale = isBlock ? BLOCK_SCALE : ITEM_SCALE;
            poseStack.scale(scale, scale, scale);

            // 创建物品堆渲染状态
            var itemRenderState = new ItemStackRenderState();
            // 获取物品模型解析器
            var itemModelResolver = Minecraft.getInstance().getItemModelResolver();
            // 更新物品模型的渲染状态
            itemModelResolver.updateForTopItem(itemRenderState, displayedItem, ItemDisplayContext.FIXED, null, null, 0);

            // 使用 ItemStackRenderState.submit 进行渲染
            itemRenderState.submit(poseStack, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

            // 恢复之前的姿态变换
            poseStack.popPose();
        }
    }
}
