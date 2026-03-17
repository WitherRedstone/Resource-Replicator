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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
// 导入 IntelliJ 的可空注解
import org.jetbrains.annotations.Nullable;

/**
 * 流体复制机渲染器类
 * 负责在客户端渲染流体复制机中的流体
 */
public class FluidReplicatorRenderer implements BlockEntityRenderer<@NotNull FluidReplicatorBlockEntity, FluidReplicatorRenderer.@NotNull FluidReplicatorRenderState> {

    /**
     * 构造函数
     */
    public FluidReplicatorRenderer(BlockEntityRendererProvider.Context context) {
        // 不需要额外初始化
    }

    /**
     * 创建渲染状态对象
     */
    @Override
    public FluidReplicatorRenderState createRenderState() {
        return new FluidReplicatorRenderState();
    }

    /**
     * 从方块实体提取渲染状态数据
     */
    @Override
    public void extractRenderState(FluidReplicatorBlockEntity blockEntity, FluidReplicatorRenderState renderState,
                                   float partialTick, @NotNull Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        // 初始化基础渲染状态
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);

        // 获取世界实例
        var level = blockEntity.getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }

        // 获取方块位置
        var pos = blockEntity.getBlockPos();
        if (!level.isLoaded(pos)) {
            return;
        }

        // 获取输入槽的流体
        var inputFluid = blockEntity.getInputFluid();

        // 如果流体不为空且有数量，准备渲染
        if (inputFluid != null && !inputFluid.isEmpty() && inputFluid.getAmount() > 0) {
            renderState.fluidStack = inputFluid;

            try {
                renderState.sprite = getFluidTexture(inputFluid);
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

    /**
     * 提交几何体到渲染管线
     */
    @Override
    public void submit(FluidReplicatorRenderState renderState, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        if (!renderState.pendingRender || renderState.sprite == null) {
            return;
        }

        // 推入姿态栈（保存当前变换）
        poseStack.pushPose();

        // 平移到流体渲染的起始位置（方块内部，缩小显示）
        poseStack.translate(0.25, 0.25, 0.25);

        // 缩放为 0.5x0.5x0.5 的小正方体（使流体看起来在方块内部）
        poseStack.scale(0.5F, 0.5F, 0.5F);

        // 创建半透明实体渲染类型，使用流体的纹理图集
        var renderType = RenderTypes.entityTranslucent(renderState.sprite.atlasLocation());

        // 提交自定义几何体到渲染管线
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (matrix, vertexConsumer) -> {
            var tempPoseStack = new PoseStack();
            tempPoseStack.last().pose().set(matrix.pose());
            tempPoseStack.last().normal().set(matrix.normal());

            // 提取颜色分量（将整数颜色拆分为 RGB 分量）
            float red = (renderState.fluidColor >> 16 & 0xFF) / 255.0F;
            float green = (renderState.fluidColor >> 8 & 0xFF) / 255.0F;
            float blue = (renderState.fluidColor & 0xFF) / 255.0F;
            float alpha = 0.8F;

            // 渲染实心立方体（带纹理和颜色）
            renderSolidCubeNew(tempPoseStack, vertexConsumer, renderState.sprite, red, green, blue, alpha, renderState.packedLight);
        });

        // 弹出姿态栈（恢复之前的变换）
        poseStack.popPose();
    }

    /**
     * 获取渲染距离
     */
    @Override
    public int getViewDistance() {
        return 64;
    }

    /**
     * 获取流体的颜色
     */
    private int getColor(Fluid fluid) {
        if (fluid == null) {
            return 0xFFFFFFFF;
        }

        try {
            var fluidType = fluid.getFluidType();
            if (fluidType != null) {
                return IClientFluidTypeExtensions.of(fluid).getTintColor();
            }
        } catch (Exception e) {
            // 忽略异常，返回默认白色
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
        var blocksAtlasLocation = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
        var atlas = textureManager.getTexture(blocksAtlasLocation);

        if (atlas instanceof TextureAtlas textureAtlas) {
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

        // 前面（Z 正方向）
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);

        // 后面（Z 负方向）
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);

        // 左面（X 负方向）
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);

        // 右面（X 正方向）
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);

        // 上面（Y 正方向）
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);

        // 下面（Y 负方向）
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
    }

    /**
     * 自定义渲染状态类
     * 存储流体复制机渲染所需的所有数据
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
