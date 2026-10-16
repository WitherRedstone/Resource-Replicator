package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.client.renderer.state.ItemReplicatorRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * 物品复制机渲染器类。
 * <p>
 * 负责在客户端渲染物品复制机中显示的物品，
 * 将输入槽物品渲染在方块四个水平侧面上。
 */
public class ItemReplicatorRenderer implements BlockEntityRenderer<@NotNull ItemReplicatorBlockEntity, @NotNull ItemReplicatorRenderState> {

    /**
     * 方块的 Z 轴偏移量（使方块稍微突出表面）。
     */
    private static final float BLOCK_Z_OFFSET = 0.20F;

    /**
     * 物品的 Z 轴偏移量（使物品贴在表面）。
     */
    private static final float ITEM_Z_OFFSET = 0.02F;

    /**
     * 方块的缩放比例。
     */
    private static final float BLOCK_SCALE = 0.8F;

    /**
     * 物品的缩放比例。
     */
    private static final float ITEM_SCALE = 0.65F;

    /**
     * 构造函数。
     *
     * @param context 方块实体渲染器上下文
     */
    public ItemReplicatorRenderer(BlockEntityRendererProvider.Context context) { }

    /**
     * 创建渲染状态对象。
     *
     * @return 物品复制机渲染状态
     */
    @Override
    public @NonNull ItemReplicatorRenderState createRenderState() {
        return new ItemReplicatorRenderState();
    }

    /**
     * 从方块实体提取渲染状态数据。
     * <p>
     * 读取输入槽（索引 0）物品，并设置是否有物品的标志。
     *
     * @param blockEntity      方块实体
     * @param state            渲染状态
     * @param partialTick      部分 tick 插值
     * @param cameraPos        相机位置
     * @param crumblingOverlay 破坏进度覆盖层
     */
    @Override
    public void extractRenderState(ItemReplicatorBlockEntity blockEntity, ItemReplicatorRenderState state, float partialTick,
                                   @NotNull Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

        // 直接从 items 数组获取输入槽物品
        state.displayedItem = blockEntity.items[0];
        state.hasItem = !state.displayedItem.isEmpty();
    }

    /**
     * 提交几何体到渲染管线。
     * <p>
     * 将输入槽物品渲染在方块的四个水平侧面（北、东、南、西），
     * 方块物品与普通物品使用不同的偏移与缩放。
     *
     * @param state      渲染状态
     * @param poseStack  姿态栈
     * @param collector  提交节点收集器
     * @param cameraState 相机渲染状态
     */
    @Override
    public void submit(ItemReplicatorRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector collector, @NotNull CameraRenderState cameraState) {
        // 如果没有物品，跳过渲染
        if (!state.hasItem) {
            return;
        }

        ItemStack displayedItem = state.displayedItem;
        // 判断是否为方块物品
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
                    poseStack.translate(0.5, 0.5, zOffset);
                    poseStack.mulPose(new Transformation(null, Axis.YP.rotationDegrees(0), null, null));
                }
                case EAST -> {
                    poseStack.translate(1.0 - zOffset, 0.5, 0.5);
                    poseStack.mulPose(new Transformation(null, Axis.YP.rotationDegrees(90), null, null));
                }
                case SOUTH -> {
                    poseStack.translate(0.5, 0.5, 1.0 - zOffset);
                    poseStack.mulPose(new Transformation(null, Axis.YP.rotationDegrees(180), null, null));
                }
                case WEST -> {
                    poseStack.translate(zOffset, 0.5, 0.5);
                    poseStack.mulPose(new Transformation(null, Axis.YP.rotationDegrees(-90), null, null));
                }
            }

            // 根据是方块还是物品选择缩放比例
            float scale = isBlock ? BLOCK_SCALE : ITEM_SCALE;
            poseStack.scale(scale, scale, scale);

            // 创建物品堆渲染状态
            var itemRenderState = new ItemStackRenderState();
            var itemModelResolver = Minecraft.getInstance().getItemModelResolver();

            itemModelResolver.updateForTopItem(itemRenderState, displayedItem, ItemDisplayContext.FIXED, null, null, 0);
            itemRenderState.submit(poseStack, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

            // 恢复之前的姿态变换
            poseStack.popPose();
        }
    }
}