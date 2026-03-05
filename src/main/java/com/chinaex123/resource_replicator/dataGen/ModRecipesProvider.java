//package com.chinaex123.resource_replicator.dataGen;
//
//import com.chinaex123.cobblestone_generator.CobblestoneGenerator;
//import com.chinaex123.cobblestone_generator.block.ModBlocks;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.data.PackOutput;
//import net.minecraft.data.recipes.*;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.neoforged.neoforge.common.Tags;
//import net.neoforged.neoforge.common.conditions.IConditionBuilder;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.concurrent.CompletableFuture;
//
//public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
//    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
//        super(output, registries);
//    }
//
//    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
//
//        // 石原石刷石机
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
//                        ModBlocks.STONE_COBBLEGEN.get())
//                .pattern("BBB")
//                .pattern("CAD")
//                .pattern("BBB")
//                .define('A', Items.COBBLESTONE)
//                .define('B', Items.STONE)
//                .define('C', Items.WATER_BUCKET)
//                .define('D', Items.LAVA_BUCKET)
//                .unlockedBy("has_stone_cobblegen_water", has(Items.WATER_BUCKET))
//                .unlockedBy("has_stone_cobblegen_lava", has(Items.LAVA_BUCKET))
//                .save(recipeOutput);
//    }
//}
