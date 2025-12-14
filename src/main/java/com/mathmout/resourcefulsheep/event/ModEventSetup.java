package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.worldgen.modifier.AddSpawnIfSheepPresentModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Optional;

import static net.minecraft.world.level.block.Blocks.BEDROCK;

public class ModEventSetup {

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS)) {
            event.register(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
                    ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "add_spawn_if_sheep_present"),
                    () -> AddSpawnIfSheepPresentModifier.CODEC);
        }
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) -> event.register(entityType.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                ModEventSetup::checkResourcefulSheepSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE));
    }

    public static boolean checkResourcefulSheepSpawnRules(EntityType<? extends Animal> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {

        // 1. On récupère la config tout de suite
        Optional<SheepSpawningData> spawningDataOpt = ConfigSheepSpawningManager.getSpawningDataFor(entityType);
        if (spawningDataOpt.isEmpty()) {
            return false;
        }
        SheepSpawningData spawningData = spawningDataOpt.get();

        if (level.getBlockState(pos.below()).is(BEDROCK)) {
            return false;
        }

        if (spawningData.RequireSeeSky()) {
            int highestY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
            if (pos.getY() < highestY) {
                return false;
            }
        }
        int nearbySheep = level.getEntitiesOfClass(ResourcefulSheepEntity.class,
                new AABB(pos).inflate(spawningData.densityRadius()),
                e -> e.getType() == entityType).size();

        if (nearbySheep > spawningData.maxNearby()) {
            return false;
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                event.put(entityType.get(), Sheep.createAttributes().build()));

    }
}