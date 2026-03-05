package com.chinaex123.resource_replicator.item;

import com.chinaex123.resource_replicator.ResourceReplicator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS_REGISTER =
            DeferredRegister.createItems(ResourceReplicator.MOD_ID);

    // 向指定事件总线注册所有物品
    public static void register(IEventBus eventBus) {
        ITEMS_REGISTER.register(eventBus);
    }
}
