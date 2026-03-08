package com.chinaex123.resource_replicator.dataGen;

import com.chinaex123.resource_replicator.block.ModBlocks;
import com.chinaex123.resource_replicator.block.compat.Mekanism.CompatMekBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {

        // ======================= 物品资源复制机 =======================
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier1.get())
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
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier2.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier1)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier2", has(ModBlocks.ITEM_REPLICATOR_Tier2.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier3.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier2)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier3", has(ModBlocks.ITEM_REPLICATOR_Tier3.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier4.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier3)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier4", has(ModBlocks.ITEM_REPLICATOR_Tier4.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_REPLICATOR_Tier5.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.ITEM_REPLICATOR_Tier4)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_item_replicator_tier5", has(ModBlocks.ITEM_REPLICATOR_Tier5.get()))
                .save(recipeOutput);

        // ======================= 流体资源复制机 =======================
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier1.get())
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
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier2.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier1)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier2", has(ModBlocks.FLUID_REPLICATOR_Tier1.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier3.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier2)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier3", has(ModBlocks.FLUID_REPLICATOR_Tier2.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier4.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier3)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier4", has(ModBlocks.FLUID_REPLICATOR_Tier3.get()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLUID_REPLICATOR_Tier5.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', ModBlocks.FLUID_REPLICATOR_Tier4)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_fluid_replicator_tier5", has(ModBlocks.FLUID_REPLICATOR_Tier4.get()))
                .save(recipeOutput);

        // ======================= 化学品资源复制机 =======================
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CompatMekBlocks.CHEMICAL_REPLICATOR_Tier1.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', MekanismItems.POLONIUM_PELLET)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Items.DRAGON_BREATH)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_chemical_replicator_tier1_1", has(MekanismItems.POLONIUM_PELLET))
                .unlockedBy("has_chemical_replicator_tier1_2", has(Tags.Items.INGOTS_NETHERITE))
                .unlockedBy("has_chemical_replicator_tier1_3", has(Items.DRAGON_BREATH))
                .save(recipeOutput.withConditions(modLoaded("mekanism")));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CompatMekBlocks.CHEMICAL_REPLICATOR_Tier2.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', CompatMekBlocks.CHEMICAL_REPLICATOR_Tier1.get())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', MekanismItems.PLUTONIUM_PELLET)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_chemical_replicator_tier2_1", has(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier1.get()))
                .unlockedBy("has_chemical_replicator_tier2_2", has(MekanismItems.PLUTONIUM_PELLET.get()))
                .save(recipeOutput.withConditions(modLoaded("mekanism")));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CompatMekBlocks.CHEMICAL_REPLICATOR_Tier3.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', CompatMekBlocks.CHEMICAL_REPLICATOR_Tier2.get())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', MekanismItems.PLUTONIUM_PELLET)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_chemical_replicator_tier3", has(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier2.get()))
                .save(recipeOutput.withConditions(modLoaded("mekanism")));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CompatMekBlocks.CHEMICAL_REPLICATOR_Tier4.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', CompatMekBlocks.CHEMICAL_REPLICATOR_Tier3.get())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', MekanismItems.PLUTONIUM_PELLET)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_chemical_replicator_tier4", has(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier3.get()))
                .save(recipeOutput.withConditions(modLoaded("mekanism")));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CompatMekBlocks.CHEMICAL_REPLICATOR_Tier5.get())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', CompatMekBlocks.CHEMICAL_REPLICATOR_Tier4.get())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', MekanismItems.PLUTONIUM_PELLET)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_chemical_replicator_tier5", has(CompatMekBlocks.CHEMICAL_REPLICATOR_Tier4.get()))
                .save(recipeOutput.withConditions(modLoaded("mekanism")));
    }
}
