package com.mathmout.resourcefulsheep.datagen.recipe;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
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

        // Recipe for Iron Syringe.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_SYRINGE.get())
                .pattern("I  ")
                .pattern(" G ")
                .pattern("  P")
                .define('P', Items.PISTON)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(pRecipeOutput);

        // Recipe for Diamond Syringe.
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.IRON_SYRINGE.get()),
                        Ingredient.of(Items.DIAMOND_BLOCK),
                        RecipeCategory.MISC,
                        ModItems.DIAMOND_SYRINGE.get())
                .unlocks("has_iron_syringe", has(ModItems.IRON_SYRINGE.get()))
                .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "diamond_syringe_smithing"));

        // Recipe for Netherite Syringe.
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.DIAMOND_SYRINGE.get()),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        RecipeCategory.MISC,
                        ModItems.NETHERITE_SYRINGE.get())
                .unlocks("has_diamond_syringe", has(ModItems.DIAMOND_SYRINGE.get()))
                .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "netherite_syringe_smithing"));

        // Iron Syringe Clearing
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IRON_SYRINGE.get())
                .requires(ModItems.IRON_SYRINGE.get())
                .unlockedBy("has_iron_syringe", has(ModItems.IRON_SYRINGE.get()))
                .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "iron_syringe_clearing"));

        // Diamond Syringe Clearing
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DIAMOND_SYRINGE.get())
                .requires(ModItems.DIAMOND_SYRINGE.get())
                .unlockedBy("has_diamond_syringe", has(ModItems.DIAMOND_SYRINGE.get()))
                .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "diamond_syringe_clearing"));

        // Netherite Syringe Clearing
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.NETHERITE_SYRINGE.get())
                .requires(ModItems.NETHERITE_SYRINGE.get())
                .unlockedBy("has_netherite_syringe", has(ModItems.NETHERITE_SYRINGE.get()))
                .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "netherite_syringe_clearing"));

        // Template duplication recipe
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 2)
                .pattern("IDI")
                .pattern("ISI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('D', Items.DIAMOND)
                .define('S', ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get())
            .unlockedBy("has_template", has(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()))
            .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "diamond_upgrade_template_duplication"));

        // DNA Sequencer Clearing
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.DNA_SEQUENCER.get())
                .requires(ModBlocks.DNA_SEQUENCER.get())
                .unlockedBy("has_netherite_syringe", has(ModBlocks.DNA_SEQUENCER.get()))
                .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "dna_sequencer_clearing"));

        // DNA Sequencer recipe
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DNA_SEQUENCER.get())
                .pattern("IDI")
                .pattern("BRB")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('D', Items.DIAMOND)
                .define('B', Items.GLASS_BOTTLE)
                .define('R', Items.REDSTONE_BLOCK)
            .unlockedBy("has_template", has(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()))
            .save(pRecipeOutput, ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "dna_sequencer_recipe"));

    }
}
