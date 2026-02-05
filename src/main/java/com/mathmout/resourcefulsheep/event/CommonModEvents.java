package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import com.mathmout.resourcefulsheep.client.data.DynamicServerDataPackProvider;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.ModItems;
import com.mathmout.resourcefulsheep.item.custom.SheepScannerEnergyStorage;
import com.mathmout.resourcefulsheep.item.custom.Syringe;
import com.mathmout.resourcefulsheep.worldgen.modifier.AddSpawnIfSheepPresentModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.Blocks.BEDROCK;

@EventBusSubscriber(modid = ResourcefulSheepMod.MOD_ID)
public class CommonModEvents {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof Syringe syringe)) return;
        if (stack.has(ModDataComponents.SYRINGE_CONTENT.get())) return; // Déjà pleine

        Entity target = event.getTarget();

        if (!(target instanceof LivingEntity)) {
            LivingEntity parent = findParentEntity(target);
            if (parent != null) {
                InteractionResult result = syringe.tryExtractDna(stack, event.getEntity(), parent, event.getHand());
                if (result.consumesAction()) {
                    event.setCanceled(true);
                    event.setCancellationResult(result);
                }
            }
        }
    }

    private static LivingEntity findParentEntity(Entity target) {
        if (target instanceof PartEntity<?> partEntity) {
            Entity parent = partEntity.getParent();
            if (parent instanceof LivingEntity livingParent) {
                return livingParent;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Enregistrer l'Inventaire (ItemHandler)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, // Le type de capability
                ModBlockEntities.DNA_SEQUENCER_BLOCK_ENTITY.get(), // Le BlockEntity
                (blockEntity, side) -> blockEntity.itemHandler // La variable à renvoyer
        );

        // Enregistrer l'Énergie (EnergyStorage)
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.DNA_SEQUENCER_BLOCK_ENTITY.get(),
                (block_entity, side) -> block_entity.energyStorage
        );

        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (itemStack, context) -> new SheepScannerEnergyStorage(itemStack), // On instancie notre wrapper
                ModItems.SHEEP_SCANNER.get()
        );
    }

    @SubscribeEvent
    public static void ModifyTemplateIcons(FMLCommonSetupEvent event) {
        SmithingTemplateItem netheriteTemplate = (SmithingTemplateItem) Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.asItem();
        List<ResourceLocation> currentIcons = netheriteTemplate.baseSlotEmptyIcons;
        List<ResourceLocation> newIcons = new ArrayList<>(currentIcons);
        newIcons.add(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "item/empty_slot_syringe"));
        netheriteTemplate.baseSlotEmptyIcons = newIcons;
    }

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
                CommonModEvents::checkResourcefulSheepSpawnRules,
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

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicServerPack()));
        }
    }

    private static Pack createDynamicServerPack() {
        var locationInfo = new PackLocationInfo(
                ResourcefulSheepMod.MOD_ID + "_dynamic_server",
                Component.literal("Resourceful Sheep Dynamic Server Data"),
                PackSource.BUILT_IN,
                Optional.empty()
        );

        Pack.ResourcesSupplier resourcesSupplier = new Pack.ResourcesSupplier() {
            @Override
            public @NotNull PackResources openPrimary(@NotNull PackLocationInfo info) {
                return new DynamicServerDataPackProvider(info);
            }

            @Override
            public @NotNull PackResources openFull(@NotNull PackLocationInfo info, Pack.@NotNull Metadata meta) {
                return new DynamicServerDataPackProvider(info);
            }
        };

        return new Pack(
                locationInfo,
                resourcesSupplier,
                new Pack.Metadata(locationInfo.title(), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of()),
                new PackSelectionConfig(true, Pack.Position.TOP, false)
        );
    }
}