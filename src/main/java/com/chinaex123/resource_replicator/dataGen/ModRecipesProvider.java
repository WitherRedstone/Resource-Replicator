package com.chinaex123.resource_replicator.dataGen;

import com.chinaex123.resource_replicator.ResourceReplicator;
import com.chinaex123.resource_replicator.block.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider {
    private final HolderLookup.Provider provider;
    private final RecipeOutput output;

    public ModRecipesProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
        this.provider = provider;
        this.output = output;
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(packOutput, lookupProvider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipesProvider(provider, output);
        }

        @Override
        public @NotNull String getName() {
            return ResourceReplicator.MOD_ID;
        }
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> itemRegistryLookup = this.registries.lookupOrThrow(Registries.ITEM);

        // ======================= 物品资源复制机 =======================
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier1.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', Tags.Items.NETHER_STARS)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Items.DRAGON_BREATH)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier1_1", has(Tags.Items.NETHER_STARS))
                .unlockedBy("has_item_replicator_tier1_2", has(Tags.Items.INGOTS_NETHERITE))
                .unlockedBy("has_item_replicator_tier1_3", has(Items.DRAGON_BREATH))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier2.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier1.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier2", has(ModBlocks.ITEM_REPLICATOR_Tier2.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier3.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier2.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier3", has(ModBlocks.ITEM_REPLICATOR_Tier3.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier4.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier3.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier4", has(ModBlocks.ITEM_REPLICATOR_Tier4.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier5.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier4.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_item_replicator_tier5", has(ModBlocks.ITEM_REPLICATOR_Tier5.get()))
                .save(output);

        // ======================= 流体资源复制机 =======================
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier1.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', Items.SPONGE)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Items.DRAGON_BREATH)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier1_1", has(Items.SPONGE))
                .unlockedBy("has_fluid_replicator_tier1_2", has(Tags.Items.INGOTS_NETHERITE))
                .unlockedBy("has_fluid_replicator_tier1_3", has(Items.DRAGON_BREATH))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier2.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier1.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier2", has(ModBlocks.FLUID_REPLICATOR_Tier1.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier3.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier2.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier3", has(ModBlocks.FLUID_REPLICATOR_Tier2.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier4.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier3.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier4", has(ModBlocks.FLUID_REPLICATOR_Tier3.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier5.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier4.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_fluid_replicator_tier5", has(ModBlocks.FLUID_REPLICATOR_Tier4.get()))
                .save(output);
    }
}
