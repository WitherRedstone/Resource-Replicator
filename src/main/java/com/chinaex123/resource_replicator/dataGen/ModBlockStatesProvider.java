//package com.chinaex123.resource_replicator.dataGen;
//
//import com.chinaex123.resource_replicator.ResourceReplicator;
//import com.chinaex123.resource_replicator.block.ModBlocks;
//import net.minecraft.data.PackOutput;
//import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
//import net.neoforged.neoforge.common.data.ExistingFileHelper;
//
//public class ModBlockStatesProvider extends BlockStateProvider {
//    public ModBlockStatesProvider(PackOutput output, ExistingFileHelper exFileHelper) {
//        super(output, ResourceReplicator.MOD_ID, exFileHelper);
//    }
//
//    @Override
//    protected void registerStatesAndModels() {
//
//        // ======================= 物品资源复制机 =======================
//        simpleBlockWithItem(ModBlocks.ITEM_REPLICATOR_Tier1.get(), cubeAll(ModBlocks.ITEM_REPLICATOR_Tier1.get()));
//        simpleBlockWithItem(ModBlocks.ITEM_REPLICATOR_Tier2.get(), cubeAll(ModBlocks.ITEM_REPLICATOR_Tier2.get()));
//        simpleBlockWithItem(ModBlocks.ITEM_REPLICATOR_Tier3.get(), cubeAll(ModBlocks.ITEM_REPLICATOR_Tier3.get()));
//        simpleBlockWithItem(ModBlocks.ITEM_REPLICATOR_Tier4.get(), cubeAll(ModBlocks.ITEM_REPLICATOR_Tier4.get()));
//        simpleBlockWithItem(ModBlocks.ITEM_REPLICATOR_Tier5.get(), cubeAll(ModBlocks.ITEM_REPLICATOR_Tier5.get()));
//
//        // ======================= 流体资源复制机 =======================
//        simpleBlockWithItem(ModBlocks.FLUID_REPLICATOR_Tier1.get(), cubeAll(ModBlocks.FLUID_REPLICATOR_Tier1.get()));
//        simpleBlockWithItem(ModBlocks.FLUID_REPLICATOR_Tier2.get(), cubeAll(ModBlocks.FLUID_REPLICATOR_Tier2.get()));
//        simpleBlockWithItem(ModBlocks.FLUID_REPLICATOR_Tier3.get(), cubeAll(ModBlocks.FLUID_REPLICATOR_Tier3.get()));
//        simpleBlockWithItem(ModBlocks.FLUID_REPLICATOR_Tier4.get(), cubeAll(ModBlocks.FLUID_REPLICATOR_Tier4.get()));
//        simpleBlockWithItem(ModBlocks.FLUID_REPLICATOR_Tier5.get(), cubeAll(ModBlocks.FLUID_REPLICATOR_Tier5.get()));
//    }
//}
