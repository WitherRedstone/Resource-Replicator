package com.chinaex123.resource_replicator.data;

import com.chinaex123.resource_replicator.init.RRBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class ModRecipesProvider extends RecipeProvider {
    public ModRecipesProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.items;

        // ======================= 物品资源复制机 =======================
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.ITEM_REPLICATOR_Tier1.get().asItem())
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
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.ITEM_REPLICATOR_Tier2.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.ITEM_REPLICATOR_Tier1.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier2", has(RRBlocks.ITEM_REPLICATOR_Tier2.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.ITEM_REPLICATOR_Tier3.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.ITEM_REPLICATOR_Tier2.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier3", has(RRBlocks.ITEM_REPLICATOR_Tier3.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.ITEM_REPLICATOR_Tier4.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.ITEM_REPLICATOR_Tier3.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_item_replicator_tier4", has(RRBlocks.ITEM_REPLICATOR_Tier4.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.ITEM_REPLICATOR_Tier5.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.ITEM_REPLICATOR_Tier4.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_item_replicator_tier5", has(RRBlocks.ITEM_REPLICATOR_Tier5.get()))
                .save(output);

        // ======================= 流体资源复制机 =======================
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.FLUID_REPLICATOR_Tier1.get().asItem())
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
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.FLUID_REPLICATOR_Tier2.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.FLUID_REPLICATOR_Tier1.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier2", has(RRBlocks.FLUID_REPLICATOR_Tier1.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.FLUID_REPLICATOR_Tier3.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.FLUID_REPLICATOR_Tier2.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier3", has(RRBlocks.FLUID_REPLICATOR_Tier2.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.FLUID_REPLICATOR_Tier4.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.FLUID_REPLICATOR_Tier3.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .unlockedBy("has_fluid_replicator_tier4", has(RRBlocks.FLUID_REPLICATOR_Tier3.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RRBlocks.FLUID_REPLICATOR_Tier5.get().asItem())
                .pattern("BDB")
                .pattern("CAC")
                .pattern("BDB")
                .define('A', RRBlocks.FLUID_REPLICATOR_Tier4.get().asItem())
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', Tags.Items.NETHER_STARS)
                .define('D', Tags.Items.STORAGE_BLOCKS_NETHERITE)
                .unlockedBy("has_fluid_replicator_tier5", has(RRBlocks.FLUID_REPLICATOR_Tier4.get()))
                .save(output);
    }
}
