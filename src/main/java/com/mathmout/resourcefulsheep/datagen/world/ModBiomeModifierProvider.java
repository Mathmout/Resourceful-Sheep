package com.mathmout.resourcefulsheep.datagen.world;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModBiomeModifierProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifierProvider::bootstrap);

    public ModBiomeModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ResourcefulSheepMod.MOD_ID));
    }

    private static void bootstrap(BootstrapContext<BiomeModifier> context) {
        ConfigSheepSpawningManager.init();
        List<SheepSpawningData> spawningRules = ConfigSheepSpawningManager.getSheepSpawning();
        HolderGetter<Biome> biomeLookup = context.lookup(Registries.BIOME);

        for (SheepSpawningData rule : spawningRules) {
            ResourceLocation entityRl = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, rule.sheepId());
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityRl);

            MobSpawnSettings.SpawnerData spawner = new MobSpawnSettings.SpawnerData(
                    entityType,
                    8,
                    rule.minCount(),
                    rule.maxCount()
            );

            ResourceKey<BiomeModifier> modifierKey = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, entityRl);

            // Case 2: Explicit biome spawning
            if (!rule.biomes().isEmpty()) {
                HolderSet<Biome> biomeHolderSet = HolderSet.direct(rule.biomes().stream()
                        .map(s -> ResourceKey.create(Registries.BIOME, ResourceLocation.parse(s)))
                        .map(biomeLookup::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));

                context.register(modifierKey, new BiomeModifiers.AddSpawnsBiomeModifier(biomeHolderSet, List.of(spawner)));
            }
        }
    }
}