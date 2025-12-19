package com.mathmout.resourcefulsheep.config.sheeptypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigSheepTypeManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("resourceful_sheep/sheep_types");
    private static final Map<String, SheepTypeData> SHEEP_TYPES = new HashMap<>();
    private static final Map<String, SheepVariantData> SHEEP_VARIANTS = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSheepTypeManager.class);

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            createDefaultIfEmpty();
            loadSheepTypes();
            buildVariants();
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void loadSheepTypes() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path)) {
                    SheepTypeData data = GSON.fromJson(reader, SheepTypeData.class);
                    if (data != null && data.Resource() != null) {
                        SHEEP_TYPES.put(data.Resource(), data);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read sheep type data from file: {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list sheep type configurations in: {}", CONFIG_DIR, e);
        }
    }

    private static void createDefaultIfEmpty() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            if (stream.findAny().isEmpty()) {
                LOGGER.info("No config files found in {}. Creating default configurations...", CONFIG_DIR);
                for (SheepTypeData defaultType : DefaultSheepTypes.getDefaults()) {
                    String fileName = defaultType.Resource() + "_sheep.json";
                    saveSheepType(fileName, defaultType);
                    LOGGER.info("Created default config file: {}", fileName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to check if config directory is empty: {}", CONFIG_DIR, e);
        }
    }
    public static void saveSheepType(String fileName, SheepTypeData data) {
        Path filePath = CONFIG_DIR.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write sheep type data to file: {}", filePath, e);
        }
    }
    private static void buildVariants() {
        SHEEP_VARIANTS.clear();
        for (SheepTypeData type : SHEEP_TYPES.values()) {
            for (SheepTypeData.TierData tier : type.SheepTier()) {
                String id = type.Resource().toLowerCase() + "_tier_" + tier.Tier();
                SheepVariantData variant = new SheepVariantData(
                        id,
                        type.Resource(),
                        tier.Tier(),
                        tier.DroppedItem(),
                        tier.MinDrops(),
                        tier.MaxDrops(),
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
        LOGGER.info("Loaded {} sheep variants from {} sheep types.", SHEEP_VARIANTS.size(), SHEEP_TYPES.size());
    }

    public static Map<String, SheepVariantData> getSheepVariant() {
        return SHEEP_VARIANTS;
    }

    public static Collection<SheepTypeData> getSheepTypes() {
        return SHEEP_TYPES.values();
    }
}