package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.client.data.DynamicResourceProvider;
import com.mathmout.resourcefulsheep.client.data.DynamicSheepTextureGenerator;
import com.mathmout.resourcefulsheep.client.renderer.ResourcefulSheepRenderer;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
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
                Component.literal("Resourceful Sheep Resource Pack"),
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