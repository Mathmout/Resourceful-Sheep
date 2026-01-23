package com.mathmout.resourcefulsheep.entity.custom;

import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.ResourcefulSheepEatBlockGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class ResourcefulSheepEntity extends Sheep {

    public ResourcefulSheepEntity(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean fireImmune() {
        SheepVariantData variant = getSheepVariantData();
        return variant.FireImmune();
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {

        SheepVariantData variant = getSheepVariantData();

        if (variant != null && variant.ImmuneEffects() != null) {
            String effectId = Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect().value())).toString();
            if (variant.ImmuneEffects().contains(effectId)) {
                return false;
            }
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        List<Goal> goalsToRemove = this.goalSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .filter(goal -> goal instanceof TemptGoal || goal instanceof EatBlockGoal)
                .toList();
        goalsToRemove.forEach(this.goalSelector::removeGoal);

        ResourcefulSheepEatBlockGoal eatBlockGoal = new ResourcefulSheepEatBlockGoal(this);
        this.goalSelector.addGoal(5, eatBlockGoal);
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, this::isFood, false));
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
        return 10F;
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        SheepVariantData variant = getSheepVariantData();
        if (variant != null && variant.FoodItems() != null && !variant.FoodItems().isEmpty()) {
            for (String id : variant.FoodItems()) {
                if (matchesItemOrTag(stack, id)) {
                    return true;
                }
            }
            return false;
        }
        return stack.is(Items.WHEAT);
    }

    private boolean matchesItemOrTag(ItemStack stack, String id) {
        if (id == null || id.isEmpty()) return false;

        // Tags
        if (id.startsWith("#")) {
            ResourceLocation tagLoc = ResourceLocation.tryParse(id.substring(1));
            if (tagLoc != null) {
                TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagLoc);
                return stack.is(tagKey);
            }
        }
        // Items
        else {
            ResourceLocation itemLoc = ResourceLocation.tryParse(id);
            if (itemLoc != null) {
                Item item = BuiltInRegistries.ITEM.get(itemLoc);
                return item != Items.AIR && stack.is(item);
            }
        }
        return false;
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
            SheepVariantData variantData = getSheepVariantData();

            if (variantData != null && variantData.DroppedItems() != null && !variantData.DroppedItems().isEmpty()) {

                for (SheepVariantData.DroppedItems dropData : variantData.DroppedItems()) {
                    String rawId = dropData.ItemId();

                    int count = ThreadLocalRandom.current().nextInt(dropData.MinDrops(), dropData.MaxDrops() + 1);

                    if (count <= 0) continue;

                    // TAG
                    if (rawId.startsWith("#")) {
                        try {
                            ResourceLocation tagLoc = ResourceLocation.parse(rawId.substring(1));
                            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);

                            // On récupère tous les items valides du tag
                            var tagResult = BuiltInRegistries.ITEM.getTag(tagKey);

                            if (tagResult.isPresent()) {
                                List<Item> validItems = tagResult.get().stream()
                                        .map(Holder::value)
                                        .toList();
                                if (!validItems.isEmpty()) {
                                    Map<Item, Integer> itemsToDrop = new HashMap<>();

                                    for (int i = 0; i < count; i++) {
                                        Item randomItem = validItems.get(ThreadLocalRandom.current().nextInt(validItems.size()));
                                        itemsToDrop.put(randomItem, itemsToDrop.getOrDefault(randomItem, 0) + 1);
                                    }

                                    for (Map.Entry<Item, Integer> entry : itemsToDrop.entrySet()) {
                                        drops.add(new ItemStack(entry.getKey(), entry.getValue()));
                                    }
                                }
                            }
                        } catch (Exception ignored) {

                        }
                    }
                    // ID
                    else {
                        Item droppedItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(rawId));
                        if (droppedItem != Items.AIR) {
                            drops.add(new ItemStack(droppedItem, count));
                        }
                    }
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

    public SheepVariantData getSheepVariantData() {
        return ConfigSheepTypeManager.getSheepVariant()
                .get(BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath());
    }
}