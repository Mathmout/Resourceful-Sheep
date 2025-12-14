package com.mathmout.resourcefulsheep.config.spawning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigSheepSpawningManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("resourceful_sheep/sheep_spawning");
    private static final List<SheepSpawningData> SHEEP_SPAWNING = new ArrayList<>();
    private static Map<String, SheepSpawningData> sheepSpawningMap = Map.of();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSheepSpawningManager.class);

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            createDefaultIfEmpty();
            loadSheepSpawning();
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void loadSheepSpawning() {
        SHEEP_SPAWNING.clear();
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path)) {
                    SheepSpawningData data = GSON.fromJson(reader, SheepSpawningData.class);
                    if (data != null && data.sheepId() != null) {
                        SHEEP_SPAWNING.add(data);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read sheep spawning data from file: {}", path, e);
                }
            });
            sheepSpawningMap = SHEEP_SPAWNING.stream().collect(Collectors.toMap(SheepSpawningData::sheepId, Function.identity()));
        } catch (IOException e) {
            LOGGER.error("Failed to list sheep spawning configurations in: {}", CONFIG_DIR, e);
        }
        LOGGER.info("Loaded {} sheep spawning configs.", SHEEP_SPAWNING.size());
    }

    private static void createDefaultIfEmpty() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            if (stream.findAny().isEmpty()) {
                LOGGER.info("No config files found in {}. Creating default configurations...", CONFIG_DIR);
                for (SheepSpawningData defaultSpawning : DefaultSheepSpawning.getDefaults()) {
                    String fileName = defaultSpawning.sheepId() + ".json";
                    saveSheepSpawning(fileName, defaultSpawning);
                    LOGGER.info("Created default config file: {}", fileName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to check if config directory is empty: {}", CONFIG_DIR, e);
        }
    }

    public static void saveSheepSpawning(String fileName, SheepSpawningData data) {
        Path filePath = CONFIG_DIR.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write sheep spawning data to file: {}", filePath, e);
        }
    }

    public static List<SheepSpawningData> getSheepSpawning() {
        return SHEEP_SPAWNING;
    }

    public static Optional<SheepSpawningData> getSpawningDataFor(EntityType<?> entityType) {
        return Optional.ofNullable(sheepSpawningMap.get(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath()));
    }
}