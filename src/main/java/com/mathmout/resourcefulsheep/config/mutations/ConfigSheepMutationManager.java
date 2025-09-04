package com.mathmout.resourcefulsheep.config.mutations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mathmout.resourcefulsheep.entity.custom.SheepMutation;
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

public class ConfigSheepMutationManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("resourceful_sheep/sheep_mutations");
    private static final List<SheepMutation> SHEEP_MUTATIONS = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSheepMutationManager.class);

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            createDefaultIfEmpty();
            loadSheepMutations();
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void loadSheepMutations() {
        SHEEP_MUTATIONS.clear();
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path)) {
                    SheepMutation data = GSON.fromJson(reader, SheepMutation.class);
                    if (data != null && data.MomId != null && data.DadId != null && data.Child != null) {
                        SHEEP_MUTATIONS.add(data);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read sheep mutation data from file: {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list sheep mutation configurations in: {}", CONFIG_DIR, e);
        }
        LOGGER.info("Loaded {} sheep mutations.", SHEEP_MUTATIONS.size());
    }

    private static void createDefaultIfEmpty() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            if (stream.findAny().isEmpty()) {
                LOGGER.info("No config files found in {}. Creating default configurations...", CONFIG_DIR);
                for (SheepMutation defaultMutation : DefaultSheepMutations.getDefaults()) {
                    String fileName = defaultMutation.MomId + "_x_" + defaultMutation.DadId + "_to_" + defaultMutation.Child + ".json";
                    saveSheepMutation(fileName, defaultMutation);
                    LOGGER.info("Created default config file: {}", fileName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to check if config directory is empty: {}", CONFIG_DIR, e);
        }
    }

    public static void saveSheepMutation(String fileName, SheepMutation data) {
        Path filePath = CONFIG_DIR.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write sheep mutation data to file: {}", filePath, e);
        }
    }

    public static List<SheepMutation> getSheepMutations() {
        return SHEEP_MUTATIONS;
    }
}
