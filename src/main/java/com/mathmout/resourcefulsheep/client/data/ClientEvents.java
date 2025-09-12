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
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicPack()));
        }
    }

    private static Pack createDynamicPack() {
        var locationInfo = new PackLocationInfo(
                ResourcefulSheepMod.MOD_ID + "_dynamic",
                Component.literal("Resourceful Sheep Dynamic Resources"),
                PackSource.BUILT_IN,
                Optional.empty()
        );

        // The modern Pack.Metadata constructor is not easily accessible for virtual packs.
        // We must use the deprecated constructor as it is the current recommended practice.
        @SuppressWarnings("deprecation")
        var metadata = new Pack.Metadata(
                locationInfo.title(),
                PackCompatibility.COMPATIBLE,
                FeatureFlagSet.of(),
                List.of()
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
                metadata,
                new PackSelectionConfig(true, Pack.Position.TOP, false)
        );
    }
}