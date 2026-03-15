package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidReplicatorRenderer implements BlockEntityRenderer<FluidReplicatorBlockEntity, FluidReplicatorRenderer.FluidReplicatorRenderState> {

    public FluidReplicatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FluidReplicatorRenderState createRenderState() {
        return new FluidReplicatorRenderState();
    }

    @Override
    public void submit(FluidReplicatorRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        // 新的渲染系统不使用传统的 MultiBufferSource
        // 这里暂时留空，因为流体渲染逻辑已经在别处处理了
    }

    /**
     * 渲染流体复制机中的流体
     * 
     * @param entity 流体复制机方块实体，包含要渲染的流体数据
     * @param partialTick 部分刻时间，用于平滑动画插值（目前未使用）
     * @param poseStack 姿态栈，用于管理模型视图矩阵的变换
     * @param bufferSource 缓冲区源，提供渲染顶点数据的缓冲对象
     * @param packedLight 打包的光照值，包含天空光和方块光的亮度信息
     * @param packedOverlay 打包的覆盖层值，用于处理叠加纹理（目前未使用）
     */
    public void render(FluidReplicatorBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = entity.getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }

        BlockPos pos = entity.getBlockPos();
        if (!level.isLoaded(pos)) {
            return;
        }

        // 先尝试获取输入槽的流体
        FluidStack fluidStack = entity.getInputFluid();

        // 如果输入槽为空，尝试其他方法获取流体
        if (fluidStack == null || fluidStack.isEmpty()) {
            return;
        }

        if (fluidStack.getAmount() <= 0) {
            return;
        }

        var fluid = fluidStack.getFluid();

        // 获取流体纹理
        TextureAtlasSprite sprite = getFluidTexture(fluidStack);
        if (sprite == null) {
            return;
        }

        // 获取流体颜色
        int color = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);
        float alpha = 0.8F; // 稍微透明一点看起来更好
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        poseStack.pushPose();

        // 移动到方块中心并缩小 - 让流体显示在方块内部
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.translate(-0.5, -0.5, -0.5);

        // 使用实体半透明渲染类型，带纹理
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.entityTranslucent(sprite.atlasLocation()));

        // 渲染一个完整的立方体
        renderSolidCube(poseStack, buffer, sprite, red, green, blue, alpha, packedLight);

        poseStack.popPose();
    }

    /**
     * 使用渲染状态渲染流体
     */
    private void renderFluid(FluidReplicatorRenderState renderState, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (renderState.sprite == null) {
            return;
        }

        poseStack.pushPose();

        // 移动到方块中心并缩小 - 让流体显示在方块内部
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.translate(-0.5, -0.5, -0.5);

        // 使用实体半透明渲染类型，带纹理
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.entityTranslucent(renderState.sprite.atlasLocation()));

        // 渲染一个完整的立方体
        renderSolidCube(poseStack, buffer, renderState.sprite, renderState.red, renderState.green, 
                       renderState.blue, renderState.alpha, packedLight);

        poseStack.popPose();
    }

    /**
     * 获取流体的纹理图集精灵
     * 
     * @param fluidStack 包含流体信息的流体堆对象
     * @return TextureAtlasSprite 流体的纹理图集精灵，如果纹理不存在则返回 null
     */
    private TextureAtlasSprite getFluidTexture(FluidStack fluidStack) {
        var fluid = fluidStack.getFluid();
        var stillTexture = IClientFluidTypeExtensions.of(fluid).getStillTexture(fluidStack);

        // 直接从 Minecraft 的纹理管理器获取纹理图集
        var textureManager = Minecraft.getInstance().getTextureManager();
        var atlas = textureManager.getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);

        if (atlas instanceof net.minecraft.client.renderer.texture.TextureAtlas textureAtlas) {
            return textureAtlas.getSprite(stillTexture);
        }

        return null;
    }

    /**
     * 使用纹理渲染实心立方体
     * 
     * @param poseStack 姿态栈，提供模型视图矩阵的变换信息
     * @param buffer 顶点缓冲区，用于收集待渲染的顶点数据
     * @param sprite 纹理图集精灵，提供 UV 坐标和纹理信息
     * @param red 红色分量（0.0-1.0）
     * @param green 绿色分量（0.0-1.0）
     * @param blue 蓝色分量（0.0-1.0）
     * @param alpha 透明度分量（0.0-1.0），0.0 为完全透明，1.0 为完全不透明
     * @param light 打包的光照值，低 16 位为 U 方向光照，高 16 位为 V 方向光照
     */
    private void renderSolidCube(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                                 TextureAtlasSprite sprite, float red, float green, float blue, float alpha,
                                 int light) {
        var matrix = poseStack.last().pose();

        // 定义单位立方体的八个顶点坐标
        float x0 = 0.0F;
        float y0 = 0.0F;
        float z0 = 0.0F;
        float x1 = 1.0F;
        float y1 = 1.0F;
        float z1 = 1.0F;

        // 获取纹理的 UV 坐标范围
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // 解析打包的光照值为两个分量
        int uv2u = light & 0xFFFF;
        int uv2v = (light >> 16) & 0xFFFF;

        // 前面 (Z = z1) - 面向南
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);

        // 后面 (Z = z0) - 面向北
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);

        // 左面 (X = x0) - 面向西
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);

        // 右面 (X = x1) - 面向东
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);

        // 顶面 (Y = y1) - 向上
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);

        // 底面 (Y = y0) - 向下
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
    }

    /**
     * 自定义渲染状态类
     */
    public static class FluidReplicatorRenderState extends BlockEntityRenderState {
        public FluidStack fluidStack = FluidStack.EMPTY;
        public TextureAtlasSprite sprite = null;
        public float red = 1.0F;
        public float green = 1.0F;
        public float blue = 1.0F;
        public float alpha = 0.8F;
        public int packedLight = 15728880;

        public FluidReplicatorRenderState() {
            super();
        }
    }
}