package com.chinaex123.resource_replicator.block.compat.Mekanism;

import com.chinaex123.resource_replicator.ResourceReplicator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Mekanism 兼容方块 - 仅在 Mekanism 安装时加载
 */
public class CompatMekBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK, ResourceReplicator.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM, ResourceReplicator.MOD_ID);

    // ======================= 化学品资源复制机 =======================
    public static final Supplier<ChemicalReplicatorBlock> CHEMICAL_REPLICATOR_Tier1 =
            registerChemicalReplicator("chemical_replicator_tier1", 1, Rarity.COMMON);
    public static final Supplier<ChemicalReplicatorBlock> CHEMICAL_REPLICATOR_Tier2 =
            registerChemicalReplicator("chemical_replicator_tier2", 2, Rarity.COMMON);
    public static final Supplier<ChemicalReplicatorBlock> CHEMICAL_REPLICATOR_Tier3 =
            registerChemicalReplicator("chemical_replicator_tier3", 3, Rarity.UNCOMMON);
    public static final Supplier<ChemicalReplicatorBlock> CHEMICAL_REPLICATOR_Tier4 =
            registerChemicalReplicator("chemical_replicator_tier4", 4, Rarity.RARE);
    public static final Supplier<ChemicalReplicatorBlock> CHEMICAL_REPLICATOR_Tier5 =
            registerChemicalReplicator("chemical_replicator_tier5", 5, Rarity.EPIC);

    private static Supplier<ChemicalReplicatorBlock> registerChemicalReplicator(String name, int tier, Rarity rarity) {
        Supplier<ChemicalReplicatorBlock> blockSupplier = BLOCKS.register(name, () ->
                new ChemicalReplicatorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), tier)
        );

        // 同时注册对应的物品，并设置稀有度
        ITEMS.register(name, () -> new BlockItem(blockSupplier.get(), new Item.Properties().rarity(rarity)));

        return blockSupplier;
    }

    /**
     * 注册所有 Mekanism 兼容内容
     */
    public static void register(IEventBus modEventBus) {
        if (!ModList.get().isLoaded("mekanism")) {
            return;
        }

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
