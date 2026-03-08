package com.chinaex123.resource_replicator;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.compat.Mekanism.CompatMekBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ResourceReplicator.MOD_ID);

    public static final Supplier<CreativeModeTab> RESOURCE_REPLICATOR_TAB =
            CREATIVE_MODE_TAB.register("resource_replicator_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ITEM_REPLICATOR_Tier5.get()))
                    .title(Component.translatable("itemGroup.resource_replicator_tab"))
                    .displayItems((parameters, output) -> {

                        // ======================= 物品资源复制机 =======================
                        output.accept(ModBlocks.ITEM_REPLICATOR_Tier1.get());
                        output.accept(ModBlocks.ITEM_REPLICATOR_Tier2.get());
                        output.accept(ModBlocks.ITEM_REPLICATOR_Tier3.get());
                        output.accept(ModBlocks.ITEM_REPLICATOR_Tier4.get());
                        output.accept(ModBlocks.ITEM_REPLICATOR_Tier5.get());

                        // ======================= 流体资源复制机 =======================
                        output.accept(ModBlocks.FLUID_REPLICATOR_Tier1.get());
                        output.accept(ModBlocks.FLUID_REPLICATOR_Tier2.get());
                        output.accept(ModBlocks.FLUID_REPLICATOR_Tier3.get());
                        output.accept(ModBlocks.FLUID_REPLICATOR_Tier4.get());
                        output.accept(ModBlocks.FLUID_REPLICATOR_Tier5.get());

                        // ======================= 化学品资源复制机 =======================
                        // 加载 通用机械 时添加
                        if (ModList.get().isLoaded("mekanism")) {
                            output.accept(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier1.get());
                            output.accept(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier2.get());
                            output.accept(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier3.get());
                            output.accept(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier4.get());
                            output.accept(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier5.get());
                        }

                    })
                    .build());

    // 注册到NeoForge事件总线里
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
