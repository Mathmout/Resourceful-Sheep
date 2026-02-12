package com.mathmout.resourcefulsheep.config.dnacrossbreeding;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigDNACrossbreedingManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("resourceful_sheep/sheep_crossbreeding");
    private static final List<SheepCrossbreeding> SHEEP_CROSSBREEDING = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDNACrossbreedingManager.class);

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            createDefaultIfEmpty();
            loadSheepCrossbreeding();
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void loadSheepCrossbreeding() {
        SHEEP_CROSSBREEDING.clear();
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path)) {
                    SheepCrossbreeding data = GSON.fromJson(reader, SheepCrossbreeding.class);
                    if (data != null && data.MomId() != null && data.DadId() != null && data.ChildId() != null && data.ResultsIfFail() != null) {
                        SHEEP_CROSSBREEDING.add(data);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read sheep crossbreeding data from file: {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list sheep crossbreeding configurations in: {}", CONFIG_DIR, e);
        }
        LOGGER.info("Loaded {} sheep crossbreeding.", SHEEP_CROSSBREEDING.size());
    }

    private static void createDefaultIfEmpty() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            if (stream.findAny().isEmpty()) {
                LOGGER.info("No config files found in {}. Creating default configurations...", CONFIG_DIR);
                for (SheepCrossbreeding defaultsheepCrossbreeding : DefaultDNACrossbreeding.getDefaults()) {
                    String fileName = defaultsheepCrossbreeding.ChildId().split(":")[1] + ".json";
                    saveSheepCrossbreeding(fileName, defaultsheepCrossbreeding);
                    LOGGER.info("Created default config file: {}", fileName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to check if config directory is empty: {}", CONFIG_DIR, e);
        }
    }

    public static void saveSheepCrossbreeding(String fileName, SheepCrossbreeding data) {
        Path filePath = CONFIG_DIR.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write sheep crossbreeding data to file: {}", filePath, e);
        }
    }

    public static void validateConfig() {
        LOGGER.info("Validating DNA Crossbreeding IDs...");

        for (SheepCrossbreeding recipe : SHEEP_CROSSBREEDING) {
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(recipe.MomId()))) {
                LOGGER.warn("Config Warning [DNA Crossbreeding]: MomId '{}' not found in Entity Registry.", recipe.MomId());
            }

            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(recipe.DadId()))) {
                LOGGER.warn("Config Warning [DNA Crossbreeding]: DadId '{}' not found in Entity Registry.", recipe.DadId());
            }

            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(recipe.ChildId()))) {
                LOGGER.warn("Config Warning [DNA Crossbreeding]: ChildId '{}' not found in Entity Registry.", recipe.ChildId());
            }

            for (String failId : recipe.ResultsIfFail()) {
                if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(failId))) {
                    LOGGER.warn("Config Warning [DNA Crossbreeding]: ResultsIfFail Item '{}' not found in Entity Registry.", failId);
                }
            }
        }
        LOGGER.info("DNA Crossbreeding Config Validation Complete.");
    }

    public static List<SheepCrossbreeding> getSheepCrossbreeding() {
        return SHEEP_CROSSBREEDING;
    }
}
