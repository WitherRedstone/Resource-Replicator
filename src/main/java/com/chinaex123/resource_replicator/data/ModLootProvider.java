package com.chinaex123.resource_replicator.data;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class ModLootProvider extends LootTableProvider {
    public ModLootProvider() {
        super(Set.of(), List.of(
                new SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK)
        ));
    }
}