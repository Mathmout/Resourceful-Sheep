package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.client.data.DynamicResourceProvider;
import com.mathmout.resourcefulsheep.client.renderer.ResourcefulSheepRenderer;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.ModItems;
import com.mathmout.resourcefulsheep.item.custom.ResourcefulWoolItem;
import com.mathmout.resourcefulsheep.item.custom.SuspiciousSpawnEgg;
import com.mathmout.resourcefulsheep.screen.centrifuge.CentrifugeScreen;
import com.mathmout.resourcefulsheep.screen.scanner.SheepScannerScreen;
import com.mathmout.resourcefulsheep.screen.sequencer.DNASequencerScreen;
import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import com.mathmout.resourcefulsheep.screen.splicer.DNASplicerScreen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

// Lancement du jeu
@EventBusSubscriber(modid = ResourcefulSheepMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    // ---------------------------------------------------------
    // 1. COULEURS ET RENDUS
    // ---------------------------------------------------------

    // Enregistre les couleurs dynamiques (arc-en-ciel) pour les œufs de spawn suspects
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

    // Lie les entités (les moutons) à leurs modèles 3D visuels
    @SubscribeEvent
    public static void registerEntityRenderers(final FMLClientSetupEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                EntityRenderers.register(entityType.get(), ResourcefulSheepRenderer::new));
    }

    // ---------------------------------------------------------
    // 2. INTERFACES GRAPHIQUES (GUI)
    // ---------------------------------------------------------

    // Associe les écrans (GUI) à leurs conteneurs (Menus)
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.DNA_SEQUENCER_MENU.get(), DNASequencerScreen::new);
        event.register(ModMenuTypes.DNA_SPLICER_MENU.get(), DNASplicerScreen::new);
        event.register(ModMenuTypes.SHEEP_SCANNER_MENU.get(), SheepScannerScreen::new);
        event.register(ModMenuTypes.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
    }

    // ---------------------------------------------------------
    // 3. GÉNÉRATION DYNAMIQUE (TEXTURES & MODÈLES)
    // ---------------------------------------------------------

    // Injecte notre pack de ressources généré dynamiquement dans le jeu
    @SubscribeEvent
    public static void registerDynamicPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addRepositorySource((packConsumer) -> packConsumer.accept(createDynamicClientPack()));
        }
    }

    // Force le jeu à générer les textures puis à charger les modèles de laine colorée
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (String variantId : ConfigSheepTypeManager.getSheepVariant().keySet()) {
            String safeVariantId = variantId.toLowerCase();
            for (DyeColor color : DyeColor.values()) {
                if (color != DyeColor.WHITE) {
                    event.register(new ModelResourceLocation(
                            ResourceLocation.fromNamespaceAndPath(
                                    ResourcefulSheepMod.MOD_ID,
                                    "item/" + safeVariantId + "_wool_" + color.getName()
                            ),
                            "standalone"
                    ));
                }
            }
        }
    }

    // Permet aux items de laine de lire leur propriété "color" pour afficher la bonne texture dans l'inventaire
    @SubscribeEvent
    public static void registerWoolColorProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (DeferredItem<ResourcefulWoolItem> woolItem : ModItems.RESOURCEFUL_WOOLS) {
                ItemProperties.register(woolItem.get(),
                        ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "color"),
                        (stack, level, entity, seed) -> {
                            BlockItemStateProperties stateProps = stack.get(DataComponents.BLOCK_STATE);
                            if (stateProps != null) {
                                String colorStr = stateProps.properties().get("color");
                                if (colorStr != null) {
                                    return DyeColor.byName(colorStr, DyeColor.WHITE).getId();
                                }
                            }
                            return 0.0f; // Par défaut, on affiche le blanc
                        });
            }
        });
    }

    // ---------------------------------------------------------
    // 4. MÉTHODES UTILITAIRES
    // ---------------------------------------------------------

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