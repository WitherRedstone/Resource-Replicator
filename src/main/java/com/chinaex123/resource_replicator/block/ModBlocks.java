package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.ResourceReplicator;
import com.chinaex123.resource_replicator.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    // 创建方块注册器实例
    public static final DeferredRegister.Blocks BLOCK_REGISTER =
            DeferredRegister.createBlocks(ResourceReplicator.MOD_ID);

    // ======================= 物品资源复制机 =======================
    public static final DeferredBlock<@NotNull ItemReplicatorBlock> ITEM_REPLICATOR_Tier1 =
            registerBlock("item_replicator_tier1", 
                    properties -> new ItemReplicatorBlock(properties, 1),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull ItemReplicatorBlock> ITEM_REPLICATOR_Tier2 =
            registerBlock("item_replicator_tier2", 
                    properties -> new ItemReplicatorBlock(properties, 2),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull ItemReplicatorBlock> ITEM_REPLICATOR_Tier3 =
            registerBlock("item_replicator_tier3", 
                    properties -> new ItemReplicatorBlock(properties, 3),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull ItemReplicatorBlock> ITEM_REPLICATOR_Tier4 =
            registerBlock("item_replicator_tier4", 
                    properties -> new ItemReplicatorBlock(properties, 4),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull ItemReplicatorBlock> ITEM_REPLICATOR_Tier5 =
            registerBlock("item_replicator_tier5", 
                    properties -> new ItemReplicatorBlock(properties, 5),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());

    // ======================= 流体资源复制机 =======================
    public static final DeferredBlock<@NotNull FluidReplicatorBlock> FLUID_REPLICATOR_Tier1 =
            registerBlock("fluid_replicator_tier1", 
                    properties -> new FluidReplicatorBlock(properties, 1),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull FluidReplicatorBlock> FLUID_REPLICATOR_Tier2 =
            registerBlock("fluid_replicator_tier2", 
                    properties -> new FluidReplicatorBlock(properties, 2),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull FluidReplicatorBlock> FLUID_REPLICATOR_Tier3 =
            registerBlock("fluid_replicator_tier3", 
                    properties -> new FluidReplicatorBlock(properties, 3),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull FluidReplicatorBlock> FLUID_REPLICATOR_Tier4 =
            registerBlock("fluid_replicator_tier4", 
                    properties -> new FluidReplicatorBlock(properties, 4),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());
    public static final DeferredBlock<@NotNull FluidReplicatorBlock> FLUID_REPLICATOR_Tier5 =
            registerBlock("fluid_replicator_tier5", 
                    properties -> new FluidReplicatorBlock(properties, 5),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .destroyTime(2.5f)
                            .explosionResistance(6.0f)
                            .requiresCorrectToolForDrops());

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, 
            Function<BlockBehaviour.Properties, T> func,
            Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = BLOCK_REGISTER.registerBlock(name, func, properties);
        ModItems.ITEMS_REGISTER.registerSimpleBlockItem(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, 
            java.util.function.Function<BlockBehaviour.Properties, T> func, 
            boolean shouldRegisterItem) {
        DeferredBlock<T> block = BLOCK_REGISTER.registerBlock(name, func);
        if (shouldRegisterItem) {
            ModItems.ITEMS_REGISTER.registerSimpleBlockItem(block);
        }
        return block;
    }

    // 向指定事件总线注册所有物品
    public static void register(IEventBus eventBus) {
        BLOCK_REGISTER.register(eventBus);
    }
}
