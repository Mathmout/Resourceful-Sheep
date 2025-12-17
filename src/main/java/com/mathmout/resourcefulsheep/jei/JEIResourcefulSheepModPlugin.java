package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIResourcefulSheepModPlugin implements IModPlugin {
    public static final RecipeType<SheepMutation> MUTATION_TYPE =
            new RecipeType<>(SheepMutationCategory.UID, SheepMutation.class);
    public static final RecipeType<SheepSpawningRecipeWrapper> SPAWNING_TYPE =
            new RecipeType<>(SheepSpawningCategory.UID, SheepSpawningRecipeWrapper.class);
    public static final RecipeType<SheepVariantData> DROPPING_TYPE =
            new RecipeType<>(SheepDroppingCategory.UID, SheepVariantData.class);
    public static final RecipeType<SheepVariantData> FEEDING_TYPE =
            new RecipeType<>(SheepFeedingCategory.UID, SheepVariantData.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SheepMutationCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SheepSpawningCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SheepDroppingCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SheepFeedingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Mutation Recipes
        List<SheepMutation> mutations = ConfigSheepMutationManager.getSheepMutations();
        registration.addRecipes(MUTATION_TYPE, mutations);

        // Spawning Recipes
        List<SheepSpawningData> spawns = ConfigSheepSpawningManager.getSheepSpawning();
        List<SheepSpawningRecipeWrapper> wrappedSpawns = new ArrayList<>();
        final int biomesPerPage = 5;

        for (SheepSpawningData spawnData : spawns) {
            if (spawnData.Biomes().isEmpty()) {
                wrappedSpawns.add(new SheepSpawningRecipeWrapper(spawnData, List.of()));
            } else {
                for (int i = 0; i < spawnData.Biomes().size(); i += biomesPerPage) {
                    int end = Math.min(i + biomesPerPage, spawnData.Biomes().size());
                    List<String> pageBiomes = spawnData.Biomes().subList(i, end);
                    wrappedSpawns.add(new SheepSpawningRecipeWrapper(spawnData, pageBiomes));
                }
            }
        }
        registration.addRecipes(SPAWNING_TYPE, wrappedSpawns);

        // Dropping Recipes
        List<SheepVariantData> variants = new ArrayList<>(ConfigSheepTypeManager.getSheepVariant().values());
        registration.addRecipes(DROPPING_TYPE, variants);

        // Feeding Recipes
        registration.addRecipes(FEEDING_TYPE, variants);
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        IModPlugin.super.onRuntimeAvailable(jeiRuntime);
    }
}
