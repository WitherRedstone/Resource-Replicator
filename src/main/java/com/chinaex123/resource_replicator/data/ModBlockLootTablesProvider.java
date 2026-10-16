package com.chinaex123.resource_replicator.data;

import com.chinaex123.resource_replicator.init.RRBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    protected void generate() {

        // ======================= 物品资源复制机 =======================
        dropSelf(RRBlocks.ITEM_REPLICATOR_Tier1.get());
        dropSelf(RRBlocks.ITEM_REPLICATOR_Tier2.get());
        dropSelf(RRBlocks.ITEM_REPLICATOR_Tier3.get());
        dropSelf(RRBlocks.ITEM_REPLICATOR_Tier4.get());
        dropSelf(RRBlocks.ITEM_REPLICATOR_Tier5.get());

        // ======================= 流体资源复制机 =======================
        dropSelf(RRBlocks.FLUID_REPLICATOR_Tier1.get());
        dropSelf(RRBlocks.FLUID_REPLICATOR_Tier2.get());
        dropSelf(RRBlocks.FLUID_REPLICATOR_Tier3.get());
        dropSelf(RRBlocks.FLUID_REPLICATOR_Tier4.get());
        dropSelf(RRBlocks.FLUID_REPLICATOR_Tier5.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return RRBlocks.BLOCK_REGISTER.getEntries().stream().map(Holder::value)::iterator;
    }
}
