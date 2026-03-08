package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.compat.Mekanism.ChemicalReplicatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

public class ChemicalReplicatorRenderer implements BlockEntityRenderer<ChemicalReplicatorBlockEntity> {

    public ChemicalReplicatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    /**
     * 渲染化学品复制机中的化学品
     * <p>
     * 此方法在客户端每帧被调用，用于在化学品复制机方块内部渲染一个半透明的彩色立方体来表示化学品。
     * 渲染流程如下：
     * </p>
     * <ul>
     *     <li><strong>环境检查</strong>：确保在客户端且世界和方块已加载</li>
     *     <li><strong>获取化学品</strong>：通过反射获取输入槽的化学品堆，避免编译时依赖 Mekanism API</li>
     *     <li><strong>有效性验证</strong>：检查化学品是否为空、数量是否大于 0</li>
     *     <li><strong>颜色提取</strong>：尝试从化学品中获取颜色表示（通过 getChemicalColorRepresentation 方法），失败则使用默认蓝色</li>
     *     <li><strong>坐标变换</strong>：将立方体缩小到 60% 并居中显示在方块内部</li>
     *     <li><strong>渲染立方体</strong>：使用白色混凝土纹理和提取的颜色渲染半透明立方体</li>
     * </ul>
     * 
     * @param entity 化学品复制机方块实体，包含要渲染的化学品数据
     * @param partialTick 部分刻时间，用于平滑动画插值（目前未使用）
     * @param poseStack 姿态栈，用于管理模型视图矩阵的变换
     * @param bufferSource 缓冲区源，提供渲染顶点数据的缓冲对象
     * @param packedLight 打包的光照值，包含天空光和方块光的亮度信息
     * @param packedOverlay 打包的覆盖层值，用于处理叠加纹理（目前未使用）
     */
    @Override
    public void render(ChemicalReplicatorBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = entity.getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }

        BlockPos pos = entity.getBlockPos();
        if (!level.isLoaded(pos)) {
            return;
        }

        // 先尝试获取输入槽的化学品
        Object chemicalStack = entity.getInputChemical();

        // 如果输入槽为空，尝试其他方法获取化学品
        if (chemicalStack == null) {
            return;
        }

        // 使用反射检查化学品是否为空并获取颜色
        float red = 0.2F;
        float green = 0.6F;
        float blue = 1.0F;

        try {
            var isEmptyMethod = chemicalStack.getClass().getMethod("isEmpty");
            Boolean isEmpty = (Boolean) isEmptyMethod.invoke(chemicalStack);
            if (isEmpty) {
                return;
            }

            var getAmountMethod = chemicalStack.getClass().getMethod("getAmount");
            Long amount = (Long) getAmountMethod.invoke(chemicalStack);
            if (amount <= 0) {
                return;
            }

            // 尝试获取化学品的颜色
            try {
                // 直接在 ChemicalStack 上调用 getChemicalColorRepresentation()
                var getColorMethod = chemicalStack.getClass().getMethod("getChemicalColorRepresentation");
                Integer color = (Integer) getColorMethod.invoke(chemicalStack);

                if (color != null && color != 0) {
                    // 直接使用低 24 位作为 RGB 值（忽略 Alpha）
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;

                    red = (float)r / 255.0F;
                    green = (float)g / 255.0F;
                    blue = (float)b / 255.0F;
                }
            } catch (Exception e) {
                // 获取颜色失败，使用默认蓝色
            }
        } catch (Exception e) {
            return;
        }

        float alpha = 0.5F;

        poseStack.pushPose();

        // 移动到方块中心并缩小 - 让化学品显示在方块内部
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.translate(-0.5, -0.5, -0.5);

        // 获取白色方块纹理
        TextureAtlasSprite whiteSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/white_concrete"));

        if (whiteSprite != null) {
            // 使用半透明渲染类型
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());

            // 使用纹理渲染
            renderSolidCubeWithTexture(poseStack, buffer, whiteSprite, red, green, blue, alpha, 0xF000F0);
        }

        poseStack.popPose();
    }

    /**
     * 使用纹理渲染实心立方体
     * <p>
     * 此方法用于在三维空间中绘制一个带有纹理和颜色的单位立方体（1x1x1）。
     * 通过向顶点缓冲区添加立方体的六个面（每个面由两个三角形组成，共 4 个顶点）来实现渲染。
     * </p>
     * <p>
     * 每个顶点的属性包括：
     * </p>
     * <ul>
     *     <li><strong>位置</strong>：立方体顶点的三维坐标（0.0-1.0）</li>
     *     <li><strong>颜色</strong>：RGBA 颜色值，支持透明度</li>
     *     <li><strong>UV 坐标</strong>：纹理映射坐标，决定纹理在立方体表面的显示区域</li>
     *     <li><strong>光照</strong>：打包的光照值，包含天空光和方块光亮度</li>
     *     <li><strong>法线</strong>：面的法向量，用于光照计算（指向外侧）</li>
     * </ul>
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
    private void renderSolidCubeWithTexture(PoseStack poseStack, VertexConsumer buffer,
                                            TextureAtlasSprite sprite, float red, float green, float blue,
                                            float alpha, int light) {
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
}
