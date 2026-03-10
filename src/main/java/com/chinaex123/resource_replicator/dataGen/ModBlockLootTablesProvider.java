package com.chinaex123.resource_replicator.dataGen;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.compat.Mekanism.CompatMekBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Stream;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    /**
     * 生成方块战利品表
     * <p>
     * 此方法用于定义模组中各个方块被破坏时的掉落物规则。
     * 目前仅配置了运输箱方块的基础掉落规则。
     */
    @Override
    protected void generate() {

        // ======================= 物品资源复制机 =======================
        dropSelf(ModBlocks.ITEM_REPLICATOR_Tier1.get());
        dropSelf(ModBlocks.ITEM_REPLICATOR_Tier2.get());
        dropSelf(ModBlocks.ITEM_REPLICATOR_Tier3.get());
        dropSelf(ModBlocks.ITEM_REPLICATOR_Tier4.get());
        dropSelf(ModBlocks.ITEM_REPLICATOR_Tier5.get());

        // ======================= 流体资源复制机 =======================
        dropSelf(ModBlocks.FLUID_REPLICATOR_Tier1.get());
        dropSelf(ModBlocks.FLUID_REPLICATOR_Tier2.get());
        dropSelf(ModBlocks.FLUID_REPLICATOR_Tier3.get());
        dropSelf(ModBlocks.FLUID_REPLICATOR_Tier4.get());
        dropSelf(ModBlocks.FLUID_REPLICATOR_Tier5.get());

        // ======================= 化学品资源复制机 =======================
        dropSelf(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier1.get());
        dropSelf(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier2.get());
        dropSelf(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier3.get());
        dropSelf(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier4.get());
        dropSelf(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier5.get());
    }


    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        Stream<Block> modBlocks = ModBlocks.BLOCK_REGISTER.getEntries().stream().map(Holder::value);
        Stream<Block> compatBlocks = CompatMekBlocks.BLOCKS.getEntries().stream().map(Holder::value);
        return Stream.concat(modBlocks, compatBlocks)::iterator;
    }
}
