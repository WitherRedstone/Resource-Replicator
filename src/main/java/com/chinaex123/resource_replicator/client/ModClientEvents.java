package com.chinaex123.resource_replicator.client;

import com.chinaex123.resource_replicator.ResourceReplicator;
import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.client.renderer.ItemReplicatorRenderer;
import com.chinaex123.resource_replicator.client.renderer.FluidReplicatorRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = ResourceReplicator.MOD_ID)
public class ModClientEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModClientEvents.class);

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("========== 注册渲染器事件被触发！ =========="); // 调试输出
        LOGGER.info("注册物品复制机渲染器: {}", ModBlockEntities.ITEM_REPLICATOR.get());
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_REPLICATOR.get(), context -> {
            LOGGER.info("创建物品复制机渲染器实例");
            return new ItemReplicatorRenderer(context);
        });

        LOGGER.info("注册流体复制机渲染器: {}", ModBlockEntities.FLUID_REPLICATOR.get());
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_REPLICATOR.get(), context -> {
            LOGGER.info("创建流体复制机渲染器实例");
            return new FluidReplicatorRenderer(context);
        });
    }
}
