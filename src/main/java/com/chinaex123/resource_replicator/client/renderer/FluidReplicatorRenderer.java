package com.chinaex123.resource_replicator.client.renderer;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidReplicatorRenderer implements BlockEntityRenderer<FluidReplicatorBlockEntity> {

    public FluidReplicatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidReplicatorBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = entity.getLevel();
        if (level == null || !level.isClientSide) {
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

        // 使用半透明渲染类型
        com.mojang.blaze3d.vertex.VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());

        // 渲染一个完整的立方体
        renderSolidCube(poseStack, buffer, sprite, red, green, blue, alpha, packedLight);

        poseStack.popPose();
    }

    private TextureAtlasSprite getFluidTexture(FluidStack fluidStack) {
        var fluid = fluidStack.getFluid();
        var stillTexture = IClientFluidTypeExtensions.of(fluid).getStillTexture(fluidStack);

        if (stillTexture == null) {
            return null;
        }

        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
    }

    private void renderSolidCube(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer,
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

        // 注意：所有面都使用逆时针顺序（默认正面）

        // 前面 (Z = z1) - 面向南
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);

        // 后面 (Z = z0) - 面向北
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 0.0F, -1.0F);

        // 左面 (X = x0) - 面向西
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), -1.0F, 0.0F, 0.0F);

        // 右面 (X = x1) - 面向东
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 1.0F, 0.0F, 0.0F);

        // 顶面 (Y = y1) - 向上
        buffer.addVertex(matrix, x0, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y1, z0).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);

        // 底面 (Y = y0) - 向下
        buffer.addVertex(matrix, x0, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v1).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z0).setColor(red, green, blue, alpha).setUv(u1, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
        buffer.addVertex(matrix, x1, y0, z1).setColor(red, green, blue, alpha).setUv(u0, v0).setUv2(uv2u, uv2v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setNormal(poseStack.last(), 0.0F, -1.0F, 0.0F);
    }
}