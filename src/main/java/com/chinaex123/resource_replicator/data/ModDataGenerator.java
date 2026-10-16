package com.chinaex123.resource_replicator.data;

import com.chinaex123.resource_replicator.ResourceReplicator;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = ResourceReplicator.MOD_ID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.add(RecipeProvider.asBootstrap(ModRecipesProvider::new));
        event.add(Registries.LOOT_TABLE, new ModLootProvider());
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModBlockTagsProvider::new);
    }
}
