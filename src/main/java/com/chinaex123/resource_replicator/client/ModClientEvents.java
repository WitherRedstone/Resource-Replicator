package com.chinaex123.resource_replicator.client;

import com.chinaex123.resource_replicator.ResourceReplicator;
import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.client.renderer.ItemReplicatorRenderer;
import com.chinaex123.resource_replicator.client.renderer.FluidReplicatorRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ResourceReplicator.MOD_ID)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_REPLICATOR.get(), ItemReplicatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_REPLICATOR.get(), FluidReplicatorRenderer::new);
    }
}
