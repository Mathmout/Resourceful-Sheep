package com.mathmout.resourcefulsheep.client.data;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
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
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ClientEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientEvents.class);

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicClientPack()));
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicServerPack()));
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
                Component.literal("Resourceful Sheep Dynamic Client Resources"),
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

    private static Pack createDynamicServerPack() {
        var locationInfo = new PackLocationInfo(
                ResourcefulSheepMod.MOD_ID + "_dynamic_server",
                Component.literal("Resourceful Sheep Dynamic Server Data"),
                PackSource.DEFAULT,
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