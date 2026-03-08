package com.chinaex123.resource_replicator.client;

import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.client.renderer.ItemReplicatorRenderer;
import com.chinaex123.resource_replicator.client.renderer.FluidReplicatorRenderer;
import com.chinaex123.resource_replicator.client.renderer.ChemicalReplicatorRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "resource_replicator")
public class ModClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_REPLICATOR.get(),
                ItemReplicatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_REPLICATOR.get(),
                FluidReplicatorRenderer::new);

        // 注册化学品复制机渲染器（仅在 Mekanism 安装时）
        if (ModList.get().isLoaded("mekanism")) {
            var blockEntityType = ModBlockEntities.CHEMICAL_REPLICATOR.get();
            if (blockEntityType != null) {
                event.registerBlockEntityRenderer(
                        blockEntityType,
                        ChemicalReplicatorRenderer::new
                );
            }
        }
    }
}
