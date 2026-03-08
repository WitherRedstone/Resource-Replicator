package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.ResourceReplicator;
import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.compat.Mekanism.ChemicalReplicatorBlockEntity;
import com.chinaex123.resource_replicator.block.compat.Mekanism.CompatMekBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ResourceReplicator.MOD_ID);

    // ======================= 物品资源复制机 =======================
    public static final Supplier<BlockEntityType<ItemReplicatorBlockEntity>> ITEM_REPLICATOR =
            BLOCK_ENTITY_TYPES.register("item_replicator", () ->
                    BlockEntityType.Builder.of(ItemReplicatorBlockEntity::new,
                            ModBlocks.ITEM_REPLICATOR_Tier1.get(),
                            ModBlocks.ITEM_REPLICATOR_Tier2.get(),
                            ModBlocks.ITEM_REPLICATOR_Tier3.get(),
                            ModBlocks.ITEM_REPLICATOR_Tier4.get(),
                            ModBlocks.ITEM_REPLICATOR_Tier5.get()).build(null));

    // ======================= 流体资源复制机 =======================
    public static final Supplier<BlockEntityType<FluidReplicatorBlockEntity>> FLUID_REPLICATOR =
            BLOCK_ENTITY_TYPES.register("fluid_replicator", () ->
                    BlockEntityType.Builder.of(FluidReplicatorBlockEntity::new,
                            ModBlocks.FLUID_REPLICATOR_Tier1.get(),
                            ModBlocks.FLUID_REPLICATOR_Tier2.get(),
                            ModBlocks.FLUID_REPLICATOR_Tier3.get(),
                            ModBlocks.FLUID_REPLICATOR_Tier4.get(),
                            ModBlocks.FLUID_REPLICATOR_Tier5.get()).build(null));

    // 化学品复制机方块实体 - 仅在 Mekanism 安装时注册
    public static final Supplier<BlockEntityType<ChemicalReplicatorBlockEntity>> CHEMICAL_REPLICATOR =
            ModList.get().isLoaded("mekanism") ?
                    BLOCK_ENTITY_TYPES.register("chemical_replicator", () ->
                            BlockEntityType.Builder.of(
                                    ChemicalReplicatorBlockEntity::new,
                                    CompatMekBlocks.CHEMICAL_REPLICATOR_Tier1.get(),
                                    CompatMekBlocks.CHEMICAL_REPLICATOR_Tier2.get(),
                                    CompatMekBlocks.CHEMICAL_REPLICATOR_Tier3.get(),
                                    CompatMekBlocks.CHEMICAL_REPLICATOR_Tier4.get(),
                                    CompatMekBlocks.CHEMICAL_REPLICATOR_Tier5.get()
                            ).build(null)
                    ) :
                    () -> null;
    public static void register(IEventBus bus){
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
