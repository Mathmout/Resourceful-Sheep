package com.mathmout.resourcefulsheep.config.sheeptypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mathmout.resourcefulsheep.Config;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class ConfigSheepTypeManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("resourceful_sheep/sheep_types");
    private static final Map<String, SheepTypeData> SHEEP_TYPES = new HashMap<>();
    private static final Map<String, SheepVariantData> SHEEP_VARIANTS = new HashMap<>();
    private static final Map<String, String> RESOURCE_FILE_MATCHING = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSheepTypeManager.class);

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            createDefaultIfEmpty();
            loadSheepTypes();
            buildVariants();
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to create config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void loadSheepTypes() {
        SHEEP_TYPES.clear();
        RESOURCE_FILE_MATCHING.clear();
        LOGGER.info("[ResourcefulSheep] Validating Sheep Type Data...");

        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path)) {
                    SheepTypeData data = GSON.fromJson(reader, SheepTypeData.class);

                    if (data != null && data.SheepName() != null) {
                        String fileName = path.getFileName().toString();
                        String resourceId = data.SheepName().toLowerCase();

                        if (SHEEP_TYPES.containsKey(resourceId)) {
                            String previousFile = RESOURCE_FILE_MATCHING.get(resourceId);
                            LOGGER.error("[ResourcefulSheep] Config Error SheepType : The ID '{}' is already defined in '{}'. Ignoring file '{}'.", resourceId, previousFile, fileName);
                            return;
                        }

                        if (!fileName.toLowerCase().contains(resourceId)) {
                            LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : File '{}' contains Sheep ID '{}'. Mismatch possible.", fileName, resourceId);
                        }

                        SHEEP_TYPES.put(resourceId, data);
                        RESOURCE_FILE_MATCHING.put(resourceId, fileName);
                    }
                } catch (JsonSyntaxException e) {
                    LOGGER.error("[ResourcefulSheep] JSON SYNTAX ERROR in file '{}': {}", path.getFileName(), e.getMessage());
                } catch (IOException e) {
                    LOGGER.error("[ResourcefulSheep] Failed to read file: {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to list config files", e);
        }
    }

    private static void createDefaultIfEmpty() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            if (stream.findAny().isEmpty()) {
                LOGGER.info("[ResourcefulSheep] No config files found in {}. Creating default configurations...", CONFIG_DIR);
                for (SheepTypeData defaultType : DefaultSheepTypes.getDefaults()) {
                    String fileName = defaultType.SheepName() + "_sheep.json";
                    saveSheepType(fileName, defaultType);
                    LOGGER.info("[ResourcefulSheep] Created default config file: {}", fileName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to check if config directory is empty: {}", CONFIG_DIR, e);
        }
    }

    public static void saveSheepType(String fileName, SheepTypeData data) {
        Path filePath = CONFIG_DIR.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to write sheep type data to file: {}", filePath, e);
        }
    }

    private static void buildVariants() {
        SHEEP_VARIANTS.clear();
        for (SheepTypeData type : SHEEP_TYPES.values()) {
            for (SheepTypeData.TierData tier : type.SheepTier()) {
                String id = type.SheepName().toLowerCase() + "_tier_" + tier.Tier();

                List<SheepVariantData.DroppedItems> variantDrops = new ArrayList<>();
                if (tier.DroppedItems() != null) {
                    for (SheepTypeData.TierData.DroppedItem drop : tier.DroppedItems()) {
                        variantDrops.add(new SheepVariantData.DroppedItems(
                                drop.ItemId(),
                                drop.MinDrops(),
                                drop.MaxDrops()
                        ));
                    }
                }

                SheepVariantData variant = new SheepVariantData(
                        id,
                        type.SheepName().toLowerCase(),
                        tier.Tier(),
                        variantDrops,
                        type.EggColorBackground(),
                        type.EggColorSpotsNTitle(),
                        type.FoodItems(),
                        type.FireImmune(),
                        type.ImmuneEffects(),
                        type.EtableBocksMap()
                );
                SHEEP_VARIANTS.put(id, variant);
            }
        }
        LOGGER.info("[ResourcefulSheep] Loaded {} sheep variants from {} sheep types.", SHEEP_VARIANTS.size(), SHEEP_TYPES.size());
    }

    public static Map<String, SheepVariantData> getSheepVariant() {
        return SHEEP_VARIANTS;
    }

    public static Collection<SheepTypeData> getSheepTypes() {
        return SHEEP_TYPES.values();
    }

    public static void validateConfig() {
        SHEEP_TYPES.forEach((sheepName, data) -> {

            // Food Items
            if (data.FoodItems() != null) {
                for (String item : data.FoodItems()) {
                    ResourceLocation loc = ResourceLocation.tryParse(item.startsWith("#") ? item.substring(1) : item);
                    if (loc == null) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : Invalid tag format '{}'.", item);
                    } else if (!item.startsWith("#") && !BuiltInRegistries.ITEM.containsKey(loc)) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : FoodItem '{}' for '{}' sheep not found in Item Registry.", item, sheepName);
                    }
                }
            }

            // Immune Effects
            if (data.ImmuneEffects() != null) {
                for (String effect : data.ImmuneEffects()) {
                    if (!BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.parse(effect))) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : ImmuneEffect '{}' for '{}' sheep not found in Effect Registry.", effect, sheepName);
                    }
                }
            }

            // Etable Blocks
            if (data.EtableBocksMap() != null) {
                data.EtableBocksMap().forEach((eatenBlockId, replacementBlockId) -> {
                    ResourceLocation locEaten = ResourceLocation.tryParse(eatenBlockId.startsWith("#") ? eatenBlockId.substring(1) : eatenBlockId);

                    if (locEaten == null) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : Invalid Eaten Block format '{}'.", eatenBlockId);
                    } else if (!eatenBlockId.startsWith("#") && !BuiltInRegistries.BLOCK.containsKey(locEaten)) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : EtableBlock key '{}' (eaten) for '{}' sheep not found in Block Registry.", eatenBlockId, sheepName);
                    }
                    if (replacementBlockId.startsWith("#")) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : Replacement block '{}' cannot be a Tag! It must be a specific block.", replacementBlockId);
                    } else if (!BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(replacementBlockId))) {
                        LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : EtableBlock value '{}' (replacement) for '{}' sheep not found in Block Registry.", replacementBlockId, sheepName);
                    }
                });
            }

            // Dropped Items
            if (data.SheepTier() != null) {
                for (SheepTypeData.TierData tier : data.SheepTier()) {
                    if (tier.DroppedItems() != null) {
                        int nbItem = 0;
                        int nbFluid = 0;

                        for (SheepTypeData.TierData.DroppedItem dropData : tier.DroppedItems()) {
                            String itemId = dropData.ItemId();
                            ResourceLocation itemLoc = ResourceLocation.parse(itemId);

                            if (!BuiltInRegistries.ITEM.containsKey(itemLoc) && !BuiltInRegistries.FLUID.containsKey(itemLoc)) {
                                LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : DroppedItem '{}' in Tier {} for {} sheep not found in neither Item Registry nor Fluid Registry.", itemId, tier.Tier(), sheepName);
                            }

                            if (dropData.MaxDrops() < dropData.MinDrops()) {
                                LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : MaxDrops must be bigger than MinDrops in {} sheep tier {}.", sheepName, tier.Tier());
                            }

                            // --- CALCUL DES SLOTS D'ITEMS ---
                            if (BuiltInRegistries.ITEM.containsKey(itemLoc)) {
                                Item item = BuiltInRegistries.ITEM.get(itemLoc);
                                int maxStackSize = item.getDefaultMaxStackSize();

                                // On divise le maxDrops par la taille max du stack et on arrondit au supérieur
                                int slotsNeeded = (int) Math.ceil((double) dropData.MaxDrops() / maxStackSize);
                                nbItem += slotsNeeded;
                            }
                            // --- CALCUL DES TANKS DE FLUIDES ---
                            else if (BuiltInRegistries.FLUID.containsKey(itemLoc)) {
                                int tankCapacity = Config.CENTRIFUGE_ULTIMATE_FLUID_CAPACITY.get();

                                // Pareil pour les fluides : on divise par la capacité max d'un réservoir
                                int tanksNeeded = (int) Math.ceil((double) dropData.MaxDrops() / tankCapacity);
                                nbFluid += tanksNeeded;
                            }
                        }

                        if (nbItem > 13) {
                            LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : The drops for '{}' sheep in tier {} require more than 13 item slots (currently {}). They will not fit in the Centrifuge.", sheepName, tier.Tier(), nbItem);
                        }

                        if (nbFluid > 3) {
                            LOGGER.warn("[ResourcefulSheep] Config Warning SheepType : The drops for '{}' sheep in tier {} require more than 3 fluid tanks (currently {}). They will not fit in the Centrifuge.", sheepName, tier.Tier(), nbFluid);
                        }
                    }
                }
            }
        });
    LOGGER.info("[ResourcefulSheep] Sheep Type Data Validation Complete.");
    }
}