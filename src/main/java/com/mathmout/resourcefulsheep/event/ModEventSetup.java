package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public class ModEventSetup {

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) -> event.register(entityType.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModEventSetup::checkResourcefulSheepSpawnRules, // Using our custom rule
                RegisterSpawnPlacementsEvent.Operation.REPLACE));
    }

    public static boolean checkResourcefulSheepSpawnRules(EntityType<? extends Animal> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // Check if the block below is a solid surface to stand on, then perform default mob spawn checks (light level, space, etc.)
        if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
             return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
        }
        return false;
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                event.put(entityType.get(), Sheep.createAttributes().build()));

    }
}