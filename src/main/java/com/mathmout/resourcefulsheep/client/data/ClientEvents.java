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
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ClientEvents {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicClientPack()));
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicServerPack()));
        }
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