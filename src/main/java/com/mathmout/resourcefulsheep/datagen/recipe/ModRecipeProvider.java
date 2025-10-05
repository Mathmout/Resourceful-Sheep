package com.mathmout.resourcefulsheep.datagen.recipe;

import com.mathmout.resourcefulsheep.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput pRecipeOutput) {

        // Recipe for Sheep Scanner
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SHEEP_SCANNER.get())
                .pattern("IGI")
                .pattern("IRI")
                .pattern("IDI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS)
                .define('R', Items.REDSTONE)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(pRecipeOutput);

        // Recipe for Lasso.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LASSO.get())
                .pattern("SLS")
                .pattern("LIL")
                .pattern("SLS")
                .define('S', Items.STRING)
                .define('L', Items.LEAD)
                .define('I', Items.SLIME_BLOCK)
                .unlockedBy("has_string", has(Items.STRING))
                .save(pRecipeOutput);
    }
}
