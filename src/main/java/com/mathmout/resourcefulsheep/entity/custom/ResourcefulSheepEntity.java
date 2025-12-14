package com.mathmout.resourcefulsheep.entity.custom;

import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ResourcefulSheepEntity extends Sheep {
    public ResourcefulSheepEntity(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean isShearable(@org.jetbrains.annotations.Nullable Player player, @NotNull ItemStack item, @NotNull Level level, @NotNull BlockPos pos) {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, @NotNull Level world, @NotNull BlockPos pos) {
        List<ItemStack> vanillaDrops = super.onSheared(player, item, world, pos);
        List<ItemStack> drops = new ArrayList<>(vanillaDrops);

        if (!world.isClientSide) {
            SheepVariantData variantData = ConfigSheepTypeManager.getSheepVariant()
                    .get(BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath());

            if (variantData != null) {
                Item droppedItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(variantData.DroppedItem));

                if (droppedItem != Items.AIR) {
                    int count = ThreadLocalRandom.current().nextInt(variantData.MinDrops, variantData.MaxDrops + 1);
                    drops.add(new ItemStack(droppedItem, count));
                }
            }
        }
        return drops;
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
            if ((mutation.MomId().equals(momId) && mutation.DadId().equals(dadId)) ||
                (mutation.MomId().equals(dadId) && mutation.DadId().equals(momId))) {
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
            cumulativeChance += mutation.Chance();
            if (randomValue < cumulativeChance) {
                childId = mutation.ChildId();
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

    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
        return 10F;
    }
}