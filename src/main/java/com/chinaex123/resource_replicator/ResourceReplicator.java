package com.chinaex123.resource_replicator;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.item.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

@Mod(ResourceReplicator.MOD_ID)
public class ResourceReplicator {
    public static final String MOD_ID = "resource_replicator";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ResourceReplicator(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerCapabilities); // 能力注册事件
        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.CONFIG_SPEC);

        ModCreativeTabs.register(modEventBus); // 注册自定义创造模式物品栏
        ModBlocks.register(modEventBus); // 注册方块
        ModItems.register(modEventBus); // 注册物品
        ModBlockEntities.register(modEventBus); // 注册方块实体

        // 启用 NeoForge 的牛奶流体支持
        if (ServerConfig.isEnableMilkFluid()) {
            try {
                net.neoforged.neoforge.common.NeoForgeMod.enableMilkFluid();
                LOGGER.info("[Resource Replicator] 牛奶流体支持已启用");
            } catch (Exception e) {
                LOGGER.warn("[Resource Replicator] 无法启用牛奶流体支持", e);
            }
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 为物品复制机注册物品处理能力
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                ModBlockEntities.ITEM_REPLICATOR.get(),
                ItemReplicatorBlockEntity::getItemHandler);

        // 为物品复制机注册能量处理能力
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.ITEM_REPLICATOR.get(),
                ItemReplicatorBlockEntity::getEnergyHandler);

        // 为流体复制机注册流体处理能力
        event.registerBlockEntity(Capabilities.Fluid.BLOCK,
                ModBlockEntities.FLUID_REPLICATOR.get(),
                FluidReplicatorBlockEntity::getFluidHandler);

        // 为流体复制机注册能量处理能力
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.FLUID_REPLICATOR.get(),
                FluidReplicatorBlockEntity::getEnergyHandler);
    }
}
