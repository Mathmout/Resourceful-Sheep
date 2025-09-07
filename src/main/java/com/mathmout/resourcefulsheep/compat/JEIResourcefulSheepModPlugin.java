package com.mathmout.resourcefulsheep.compat;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepMutation;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIResourcefulSheepModPlugin implements IModPlugin {
    public static final RecipeType<SheepMutation> MUTATION_TYPE =
            new RecipeType<>(SheepMutationCategory.UID, SheepMutation.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SheepMutationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SheepMutation> mutations = ConfigSheepMutationManager.getSheepMutations();
        registration.addRecipes(MUTATION_TYPE, mutations);
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        IModPlugin.super.onRuntimeAvailable(jeiRuntime);
    }
}