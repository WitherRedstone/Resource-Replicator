package com.chinaex123.resource_replicator;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import com.chinaex123.resource_replicator.block.enumTier.ItemReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(ResourceReplicator.MOD_ID)
public class ResourceReplicator {
    public static final String MOD_ID = "resource_replicator";

    public ResourceReplicator(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerCapabilities); // 能力注册事件
        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.CONFIG_SPEC);

        ModCreativeTabs.register(modEventBus); // 注册自定义创造模式物品栏
        ModBlocks.register(modEventBus); // 注册方块
        ModItems.register(modEventBus); // 注册物品
        ModBlockEntities.register(modEventBus); // 注册方块实体
        //NeoForge.EVENT_BUS.register(TooltipEventHandler.class); // 注册工具提示事件处理器
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 为物品复制机注册物品处理能力
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ITEM_REPLICATOR.get(),
                ItemReplicatorBlockEntity::getItemHandler);

        // 为流体复制机注册流体处理能力
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FLUID_REPLICATOR.get(),
                FluidReplicatorBlockEntity::getFluidHandler);
    }
}
