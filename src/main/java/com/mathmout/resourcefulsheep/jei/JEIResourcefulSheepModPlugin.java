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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;


import java.util.*;

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
    public static final RecipeType<SheepEatingRecipeWrapper> EATING_TYPE =
            new RecipeType<>(SheepEatingCategory.UID, SheepEatingRecipeWrapper.class);

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
        registration.addRecipeCategories(new SheepEatingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Mutation Recipes
        List<SheepMutation> mutations = ConfigSheepMutationManager.getSheepMutations();
        registration.addRecipes(MUTATION_TYPE, mutations);

        // Spawning Recipes
        List<SheepSpawningData> spawns = ConfigSheepSpawningManager.getSheepSpawning();
        List<SheepSpawningRecipeWrapper> wrappedSpawns = new ArrayList<>();

        for (SheepSpawningData spawnData : spawns) {
            if (spawnData.Biomes().isEmpty()) {
                wrappedSpawns.add(new SheepSpawningRecipeWrapper(spawnData, List.of()));
            } else {
                Set<String> uniqueBiomes = new LinkedHashSet<>();
                for (String biome : spawnData.Biomes()) {
                    // TAG
                    if (biome.startsWith("#")) {
                        try {
                            ClientLevel level = Minecraft.getInstance().level;
                            if (level != null) {
                                ResourceLocation tagLoc = ResourceLocation.tryParse(biome.substring(1));
                                if (tagLoc != null) {
                                    TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, tagLoc);
                                    Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
                                    Optional<HolderSet.Named<Biome>> tagWrapper = biomeRegistry.getTag(tagKey);
                                    if (tagWrapper.isPresent()) {
                                        for (Holder<Biome> biomeHolder : tagWrapper.get()) {
                                            Optional<ResourceKey<Biome>> keyOpt = biomeHolder.unwrapKey();
                                            keyOpt.ifPresent(k -> uniqueBiomes.add(k.location().toString()));
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {

                        }
                        // ID
                    } else {
                        uniqueBiomes.add(biome);
                    }
                }
                List<String> resolvedBiomes = new ArrayList<>(uniqueBiomes);
                if (resolvedBiomes.isEmpty() && !spawnData.Biomes().isEmpty()) {
                    resolvedBiomes.addAll(spawnData.Biomes());
                }
                final int biomesPerPage = 7;
                for (int i = 0; i < resolvedBiomes.size(); i += biomesPerPage) {
                    int end = Math.min(i + biomesPerPage, resolvedBiomes.size());
                    List<String> pageBiomes = resolvedBiomes.subList(i, end);
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

        // Eating Recipes
        registration.addRecipes(EATING_TYPE, getWrappedEatingRecipes(variants));
    }

    private List<SheepEatingRecipeWrapper> getWrappedEatingRecipes(List<SheepVariantData> variants) {
        List<SheepEatingRecipeWrapper> wrappedRecipes = new ArrayList<>();
        final int eatingPairsPerPage = 4;

        for (SheepVariantData variant : variants) {
            List<Map.Entry<String, String>> entries = getEntriesForVariant(variant);

            for (int i = 0; i < entries.size(); i += eatingPairsPerPage) {
                int end = Math.min(i + eatingPairsPerPage, entries.size());
                List<Map.Entry<String, String>> pageEntries = entries.subList(i, end);
                wrappedRecipes.add(new SheepEatingRecipeWrapper(variant, pageEntries));
        }
    }
    return wrappedRecipes;
}
    private List<Map.Entry<String, String>> getEntriesForVariant(SheepVariantData variant) {
        Map<String, String> map = variant.EtableBocksMap();
        if (map == null || map.isEmpty()) {
            return List.of(
                    Map.entry("minecraft:grass_block", "minecraft:dirt"),
                    Map.entry("minecraft:short_grass", "minecraft:air"),
                    Map.entry("minecraft:fern", "minecraft:air")
            );
        }
        return new ArrayList<>(map.entrySet());
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        IModPlugin.super.onRuntimeAvailable(jeiRuntime);
    }
}