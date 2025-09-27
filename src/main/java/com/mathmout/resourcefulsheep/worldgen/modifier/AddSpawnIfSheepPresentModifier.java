package com.mathmout.resourcefulsheep.worldgen.modifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AddSpawnIfSheepPresentModifier(List<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {

    public static final MapCodec<AddSpawnIfSheepPresentModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(AddSpawnIfSheepPresentModifier::spawners)
            ).apply(instance, AddSpawnIfSheepPresentModifier::new));

    @Override
    public void modify(@NotNull Holder<Biome> biome, @NotNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        if (phase == Phase.ADD) {
            boolean vanillaSheepPresent = biome.value().getMobSettings()
                    .getMobs(MobCategory.CREATURE).unwrap().stream()
                    .anyMatch(s -> s.type.equals(EntityType.SHEEP));

            if (vanillaSheepPresent) {
                for (MobSpawnSettings.SpawnerData spawner : this.spawners) {
                    builder.getMobSpawnSettings().addSpawn(MobCategory.AMBIENT, spawner);
                }
            }
        }
    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return ModBiomeModifierSerializers.ADD_SPAWN_IF_SHEEP_PRESENT.get();
    }
}
