package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FluidReplicatorRenderer implements BlockEntityRenderer<FluidReplicatorBlockEntity, FluidReplicatorRenderer.FluidReplicatorRenderState> {

    public FluidReplicatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FluidReplicatorRenderState createRenderState() {
        return new FluidReplicatorRenderState();
    }

    @Override
    public void extractRenderState(FluidReplicatorBlockEntity blockEntity, FluidReplicatorRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        // 初始化基本状态
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        
        var level = blockEntity.getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }
        
        var pos = blockEntity.getBlockPos();
        if (!level.isLoaded(pos)) {
            return;
        }
        
        var inputFluid = blockEntity.getInputFluid();
        
        if (inputFluid != null && !inputFluid.isEmpty() && inputFluid.getAmount() > 0) {
            renderState.fluidStack = inputFluid;
            
            try {
                var sprite = getFluidTexture(inputFluid);
                renderState.sprite = sprite;
                renderState.fluidColor = getColor(inputFluid.getFluid());
                renderState.pendingRender = true;
            } catch (Exception e) {
                renderState.sprite = null;
                renderState.pendingRender = false;
            }
        } else {
            renderState.fluidStack = FluidStack.EMPTY;
            renderState.sprite = null;
            renderState.pendingRender = false;
        }
    }

    @Override
    public void submit(FluidReplicatorRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!renderState.pendingRender || renderState.sprite == null) {
            return;
        }
        
        // 推入姿态栈
        poseStack.pushPose();
        
        // 平移到流体渲染的起始位置（方块内部）
        poseStack.translate(0.25, 0.25, 0.25);
        
        // 缩放为 0.5x0.5x0.5 的小正方体
        poseStack.scale(0.5F, 0.5F, 0.5F);
        
        var renderType = RenderTypes.entityTranslucent(renderState.sprite.atlasLocation());
        
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (matrix, vertexConsumer) -> {
            var tempPoseStack = new PoseStack();
            tempPoseStack.last().pose().set(matrix.pose());
            tempPoseStack.last().normal().set(matrix.normal());

            // 提取颜色分量
            float red = (renderState.fluidColor >> 16 & 0xFF) / 255.0F;
            float green = (renderState.fluidColor >> 8 & 0xFF) / 255.0F;
            float blue = (renderState.fluidColor & 0xFF) / 255.0F;
            float alpha = 0.8F;
            
            renderSolidCubeNew(tempPoseStack, vertexConsumer, renderState.sprite, red, green, blue, alpha, renderState.packedLight);
        });
        
        // 弹出姿态栈
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    /**
     * 获取流体的颜色
     */
    private int getColor(net.minecraft.world.level.material.Fluid fluid) {
        if (fluid == null) {
            return 0xFFFFFFFF;
        }
        
        try {
            var fluidType = fluid.getFluidType();
            if (fluidType != null) {
                return IClientFluidTypeExtensions.of(fluid).getTintColor();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        
        return 0xFFFFFFFF;
    }

    /**
     * 获取流体的纹理图集精灵
     */
    private TextureAtlasSprite getFluidTexture(FluidStack fluidStack) {
        var fluid = fluidStack.getFluid();
        var stillTexture = IClientFluidTypeExtensions.of(fluid).getStillTexture(fluidStack);

        var textureManager = Minecraft.getInstance().getTextureManager();
        var atlas = textureManager.getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);

        if (atlas instanceof net.minecraft.client.renderer.texture.TextureAtlas textureAtlas) {
            return textureAtlas.getSprite(stillTexture);
        }

        return null;
    }

    /**
     * 使用纹理渲染实心立方体
     */
    private void renderSolidCubeNew(PoseStack poseStack, VertexConsumer buffer,
                                    TextureAtlasSprite sprite, float red, float green, float blue, float alpha,
                                    int light) {
        var matrix = poseStack.last().pose();

        float x0 = 0.0F;
        float y0 = 0.0F;
        float z0 = 0.0F;
        float x1 = 1.0F;
        float y1 = 1.0F;
        float z1 = 1.0F;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        int uv2u = light & 0xFFFF;
        int uv2v = (light >> 16) & 0xFFFF;

        // 前面
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);

        // 后面
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);

        // 左面
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);

        // 右面
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);

        // 上面
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);

        // 下面
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
        public int packedLight = 15728880;
        public int fluidColor = 0xFFFFFFFF;
        public boolean pendingRender = false;

        public FluidReplicatorRenderState() {
            super();
        }
    }
}