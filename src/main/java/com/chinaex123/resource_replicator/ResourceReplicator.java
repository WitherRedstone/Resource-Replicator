package com.chinaex123.resource_replicator;

import com.chinaex123.resource_replicator.init.RRBlocks;
import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.init.RRBlockEntities;
import com.chinaex123.resource_replicator.config.RRServerConfig;
import com.chinaex123.resource_replicator.init.RRItems;
import com.chinaex123.resource_replicator.network.FluidSyncPacket;
import com.chinaex123.resource_replicator.network.ItemReplicatorSyncPacket;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(ResourceReplicator.MOD_ID)
public class ResourceReplicator {
    public static final String MOD_ID = "resource_replicator";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ResourceReplicator(IEventBus modEventBus, ModContainer modContainer) {
        RRCreativeTabs.register(modEventBus);
        RRBlocks.register(modEventBus);
        RRItems.register(modEventBus);
        RRBlockEntities.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerPayloadHandlers);

        modContainer.registerConfig(ModConfig.Type.COMMON, RRServerConfig.CONFIG_SPEC);
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                FluidSyncPacket.TYPE,
                FluidSyncPacket.CODEC,
                FluidSyncPacket::handle
        );

        registrar.playToClient(
                ItemReplicatorSyncPacket.TYPE,
                ItemReplicatorSyncPacket.CODEC,
                ItemReplicatorSyncPacket::handle
        );
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 为物品复制机注册物品处理能力
        event.registerBlockEntity(Capabilities.Item.BLOCK,
                RRBlockEntities.ITEM_REPLICATOR.get(),
                ItemReplicatorBlockEntity::getItemHandler);
        // 为物品复制机注册能量处理能力
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                RRBlockEntities.ITEM_REPLICATOR.get(),
                ItemReplicatorBlockEntity::getEnergyHandler);

        // 为流体复制机注册流体处理能力
        event.registerBlockEntity(Capabilities.Fluid.BLOCK,
                RRBlockEntities.FLUID_REPLICATOR.get(),
                FluidReplicatorBlockEntity::getFluidHandler);
        // 为流体复制机注册能量处理能力
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                RRBlockEntities.FLUID_REPLICATOR.get(),
                FluidReplicatorBlockEntity::getEnergyHandler);
    }
}
