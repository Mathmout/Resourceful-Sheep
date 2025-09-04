package com.mathmout.resourcefulsheep.entity.custom;

import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ResourcefulSheepEntity extends Sheep {
    public ResourcefulSheepEntity(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Nullable
    @Override
    public Sheep getBreedOffspring(@NotNull ServerLevel serverLevel, AgeableMob ageableMob) {

        ResourceLocation momIdRL = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        ResourceLocation dadIdRL = BuiltInRegistries.ENTITY_TYPE.getKey(ageableMob.getType());

        String momId = momIdRL.getPath();
        String dadId = dadIdRL.getPath();

        String childId = determineOffspringId(momId, dadId);

        EntityType<ResourcefulSheepEntity> childEntityType = null;
        if (ModEntities.SHEEP_ENTITIES.containsKey(childId)) {
            childEntityType = ModEntities.SHEEP_ENTITIES.get(childId).get();
        }

        if (childEntityType == null) {
             // Fallback to one of the parents if child entity type not found
            if (ModEntities.SHEEP_ENTITIES.containsKey(momId)) {
                childEntityType = ModEntities.SHEEP_ENTITIES.get(momId).get();
            } else {
                return null; // Should not happen
            }
        }
        return childEntityType.create(serverLevel);
    }

    private String determineOffspringId(String momId, String dadId) {

        List<SheepMutation> possibleMutations = new ArrayList<>();

        for (SheepMutation mutation : ConfigSheepMutationManager.getSheepMutations()) {
            if ((mutation.MomId.equals(momId) && mutation.DadId.equals(dadId)) ||
                (mutation.MomId.equals(dadId) && mutation.DadId.equals(momId))) {
                possibleMutations.add(mutation);
            }
        }

        Random random = new Random();
        int randomValue = random.nextInt(100);
        String childId = null;

        int cumulativeChance = 0;
        int i = 0;
        while (childId == null && i < possibleMutations.size()) {
            SheepMutation mutation = possibleMutations.get(i);
            cumulativeChance += mutation.Chance;
            if (randomValue < cumulativeChance) {
                childId = mutation.Child;
            }
            i++;
        }

        if (childId == null) {
            // Mutation failed or no mutation found, 50/50 chance between parents
            if (random.nextBoolean()) {
                childId = momId;
            } else {
                childId = dadId;
            }
        }
        return childId;
    }
}
