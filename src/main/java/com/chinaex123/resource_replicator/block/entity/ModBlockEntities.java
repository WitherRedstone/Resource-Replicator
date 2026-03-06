package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.ResourceReplicator;
import com.chinaex123.resource_replicator.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
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

    public static void register(IEventBus bus){
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
