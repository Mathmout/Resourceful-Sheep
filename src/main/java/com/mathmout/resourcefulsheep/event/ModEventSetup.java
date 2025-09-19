package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.slf4j.Logger;

public class ModEventSetup {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) -> event.register(entityType.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                ModEventSetup::checkResourcefulSheepSpawnRules, // Using our custom rule
                RegisterSpawnPlacementsEvent.Operation.REPLACE));
    }

    public static boolean checkResourcefulSheepSpawnRules(EntityType<? extends Animal> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        LOGGER.info("Attempting spawn for {} at {} in biome {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType), pos, level.getBiome(pos).unwrapKey().map(k -> k.location().toString()).orElse("?"));

        // Check for nearby sheep to limit density
        int nearbySheep = level.getEntitiesOfClass(ResourcefulSheepEntity.class, new AABB(pos).inflate(256), e -> e.getType() == entityType).size();
        if (nearbySheep > 1 ) {
            LOGGER.info(" -> Failed: Too many sheep nearby.");
            return false;
        }

        boolean canSpawn = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);

        if (!canSpawn) {
            LOGGER.info(" -> Failed: Block below is not sturdy.");
        }
        return canSpawn;
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                event.put(entityType.get(), Sheep.createAttributes().build()));

    }
}
