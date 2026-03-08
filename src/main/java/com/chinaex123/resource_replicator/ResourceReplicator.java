package com.chinaex123.resource_replicator;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.compat.Mekanism.ChemicalReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.block.compat.Mekanism.CompatMekBlocks;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
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

        // 模组兼容 - 通用机械
        if (ModList.get().isLoaded("mekanism")) {
            LOGGER.info("Mekanism mod detected, registering compatibility...");
            CompatMekBlocks.register(modEventBus);
        }
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

        // Mekanism 化学品复制机（仅当 Mekanism 加载时）
        if (net.neoforged.fml.ModList.get().isLoaded("mekanism")) {
            try {
                // 通过反射获取 Mekanism 的能力类
                Class<?> mekCapabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
                Object chemicalField = mekCapabilitiesClass.getDeclaredField("CHEMICAL").get(null);
                Object blockCapability = chemicalField.getClass().getMethod("block").invoke(chemicalField);

                // 获取 ChemicalReplicatorBlockEntity 类
                Class<?> entityClass = Class.forName("com.chinaex123.resource_replicator.block.compat.Mekanism.ChemicalReplicatorBlockEntity");

                // 注册能力 - 修正泛型参数
                @SuppressWarnings("unchecked")
                var capability = (net.neoforged.neoforge.capabilities.BlockCapability<mekanism.api.chemical.IChemicalHandler, net.minecraft.core.Direction>) blockCapability;

                var blockEntityType = ModBlockEntities.CHEMICAL_REPLICATOR.get();
                if (blockEntityType == null) {
                    LOGGER.warn("Chemical replicator block entity type is null, skipping capability registration");
                    return;
                }

                event.registerBlockEntity(
                        capability,
                        blockEntityType,
                        (blockEntity, side) -> {
                            // 检查是否是 ChemicalReplicatorBlockEntity 实例
                            if (blockEntity instanceof ChemicalReplicatorBlockEntity chemicalBE) {
                                var handler = chemicalBE.getChemicalHandler(side);
                                if (handler != null) {
                                    return (mekanism.api.chemical.IChemicalHandler) handler;
                                }
                            }
                            return null;
                        }
                );
                LOGGER.info("已注册 Mekanism 化学物质处理能力");
            } catch (Exception e) {
                LOGGER.error("注册 Mekanism 化学物质能力失败：{}", e.getMessage(), e);
            }
        }
    }
}
