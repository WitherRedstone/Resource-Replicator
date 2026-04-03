package com.chinaex123.resource_replicator.dataGen;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.ResourceReplicator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ResourceReplicator.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // 镐挖掘
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                // ======================= 物品资源复制机 =======================
                .add(ModBlocks.ITEM_REPLICATOR_Tier1.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier2.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier3.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier4.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier5.get())
                // ======================= 流体资源复制机 =======================
                .add(ModBlocks.FLUID_REPLICATOR_Tier1.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier2.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier3.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier4.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier5.get());

        // 需要铁等级的工具
        tag(BlockTags.NEEDS_IRON_TOOL)
                // ======================= 物品资源复制机 =======================
                .add(ModBlocks.ITEM_REPLICATOR_Tier1.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier2.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier3.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier4.get())
                .add(ModBlocks.ITEM_REPLICATOR_Tier5.get())
                // ======================= 流体资源复制机 =======================
                .add(ModBlocks.FLUID_REPLICATOR_Tier1.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier2.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier3.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier4.get())
                .add(ModBlocks.FLUID_REPLICATOR_Tier5.get());
    }
}
