package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.client.data.DynamicResourceProvider;
import com.mathmout.resourcefulsheep.client.data.DynamicSheepTextureGenerator;
import com.mathmout.resourcefulsheep.client.renderer.ResourcefulSheepRenderer;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.ModItems;
import com.mathmout.resourcefulsheep.item.custom.SuspiciousSpawnEgg;
import com.mathmout.resourcefulsheep.screen.sequencer.DNASequencerScreen;
import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import com.mathmout.resourcefulsheep.screen.splicer.DNASplicerScreen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@EventBusSubscriber(modid = ResourcefulSheepMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModEvents.class);

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {

            List<String> possibleIds;
            if (stack.has(ModDataComponents.SUSPICIOUS_EGG_DATA.get())) {
                CompoundTag data = stack.get(ModDataComponents.SUSPICIOUS_EGG_DATA.get());
                if (data != null && data.contains("mom_id") && data.contains("dad_id")) {
                    String mom = data.getString("mom_id");
                    String dad = data.getString("dad_id");
                    possibleIds = SuspiciousSpawnEgg.getPossibleResults(mom, dad);
                } else {
                    possibleIds = SuspiciousSpawnEgg.getAllSpawnEggIds();
                }
            } else {
                possibleIds = SuspiciousSpawnEgg.getAllSpawnEggIds();
            }

            if (possibleIds.isEmpty()) return 0xFF808080;

            long time = System.currentTimeMillis();
            double speed = 2000.0;
            double progress = (time % (possibleIds.size() * speed)) / speed;

            int currentIndex = (int) progress;
            int nextIndex = (currentIndex + 1) % possibleIds.size();
            float factor = (float) (progress - currentIndex);

            String idA = possibleIds.get(currentIndex);
            String idB = possibleIds.get(nextIndex);

            int colorA = getSpawnEggColor(idA, tintIndex);
            int colorB = getSpawnEggColor(idB, tintIndex);

            return blendColors(colorA, colorB, factor);

        }, ModItems.SUSPICIOUS_SPAWN_EGG.get());
    }

    private static int getSpawnEggColor(String entityId, int tintIndex) {
        EntityType<?> type = EntityType.byString(entityId).orElse(null);
        if (type != null) {
            SpawnEggItem egg = SpawnEggItem.byId(type);
            if (egg != null) {
                return egg.getColor(tintIndex);
            }
        }
        return 0xFFFFFFFF;
    }

    private static int blendColors(int color1, int color2, float ratio) {
        float r1 = (color1 >> 16 & 255);
        float g1 = (color1 >> 8 & 255);
        float b1 = (color1 & 255);

        float r2 = (color2 >> 16 & 255);
        float g2 = (color2 >> 8 & 255);
        float b2 = (color2 & 255);

        float r = r1 + (r2 - r1) * ratio;
        float g = g1 + (g2 - g1) * ratio;
        float b = b1 + (b2 - b1) * ratio;

        return (255 << 24) | ((int) r << 16) | ((int) g << 8) | (int) b;
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.DNA_SEQUENCER_MENU.get(), DNASequencerScreen::new);
        event.register(ModMenuTypes.DNA_SPLICER_MENU.get(), DNASplicerScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                EntityRenderers.register(entityType.get(), ResourcefulSheepRenderer::new));
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicClientPack()));
        }
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new PreparableReloadListener() {
            @Override
            public @NotNull CompletableFuture<Void> reload(
                    @NotNull PreparationBarrier preparationBarrier,
                    @NotNull ResourceManager resourceManager,
                    @NotNull ProfilerFiller preparationsProfiler,
                    @NotNull ProfilerFiller reloadProfiler,
                    @NotNull Executor backgroundExecutor,
                    @NotNull Executor gameExecutor) {
                LOGGER.info("Dynamic textures reload listener: Starting reload...");
                return CompletableFuture.runAsync(() -> LOGGER.info("Dynamic textures reload listener: Prepare stage running."), backgroundExecutor).thenCompose(preparationBarrier::wait).thenRunAsync(() -> {
                    LOGGER.info("Dynamic textures reload listener: Apply stage starting...");
                    try {
                        DynamicSheepTextureGenerator.clear();
                        new DynamicSheepTextureGenerator().generateAllTextures(resourceManager);
                    } catch (Exception e) {
                        LOGGER.error("Failed to run dynamic texture generator", e);
                    }
                    LOGGER.info("Dynamic textures reload listener: Apply stage finished.");
                }, gameExecutor);
            }
        });
    }

    private static Pack createDynamicClientPack() {
        var locationInfo = new PackLocationInfo(
                ResourcefulSheepMod.MOD_ID + "_dynamic_client",
                Component.literal("Resourceful Sheep Name Pack"),
                PackSource.DEFAULT,
                Optional.empty()
        );

        Pack.ResourcesSupplier resourcesSupplier = new Pack.ResourcesSupplier() {
            @Override
            public @NotNull PackResources openPrimary(@NotNull PackLocationInfo info) {
                return new DynamicResourceProvider(info);
            }

            @Override
            public @NotNull PackResources openFull(@NotNull PackLocationInfo info, Pack.@NotNull Metadata meta) {
                return new DynamicResourceProvider(info);
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