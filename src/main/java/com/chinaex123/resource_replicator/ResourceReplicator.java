package com.chinaex123.resource_replicator;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ResourceReplicator.MOD_ID)
public class ResourceReplicator {
    // 在公共位置定义模组ID，供所有地方引用
    public static final String MOD_ID = "resource_replicator";

    public ResourceReplicator(IEventBus modEventBus, ModContainer modContainer) {
        //modEventBus.addListener(this::registerCapabilities); // 能力注册事件
        //modEventBus.addListener(ShippingBoxNetworking::register); // 注册网络数据包处理器

        ModCreativeTabs.register(modEventBus); // 注册自定义创造模式物品栏
        ModBlocks.register(modEventBus); // 注册方块
        ModItems.register(modEventBus); // 注册物品
        ModBlockEntities.register(modEventBus); // 注册方块实体
        //NeoForge.EVENT_BUS.register(TooltipEventHandler.class); // 注册工具提示事件处理器
    }
}
