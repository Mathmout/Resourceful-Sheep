package com.mathmout.resourcefulsheep.config.mutations;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            LOGGER.error("[ResourcefulSheep] Failed to create config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void loadSheepMutations() {
        SHEEP_MUTATIONS.clear();
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path)) {
                    SheepMutation data = GSON.fromJson(reader, SheepMutation.class);
                    if (data != null && data.MomId() != null && data.DadId() != null && data.ChildId() != null) {
                        SHEEP_MUTATIONS.add(data);
                    }
                } catch (IOException e) {
                    LOGGER.error("[ResourcefulSheep] Failed to read sheep mutation data from file: {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to list sheep mutation configurations in: {}", CONFIG_DIR, e);
        }
        LOGGER.info("[ResourcefulSheep] Loaded {} sheep mutations.", SHEEP_MUTATIONS.size());
    }

    private static void createDefaultIfEmpty() {
        try (Stream<Path> stream = Files.list(CONFIG_DIR)) {
            if (stream.findAny().isEmpty()) {
                LOGGER.info("[ResourcefulSheep] No config files found in {}. Creating default configurations...", CONFIG_DIR);
                for (SheepMutation defaultMutation : DefaultSheepMutations.getDefaults()) {
                    String fileName = defaultMutation.ChildId() + ".json";
                    saveSheepMutation(fileName, defaultMutation);
                    LOGGER.info("[ResourcefulSheep] Created default config file: {}", fileName);
                }
            }
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to check if config directory is empty: {}", CONFIG_DIR, e);
        }
    }

    public static void saveSheepMutation(String fileName, SheepMutation data) {
        Path filePath = CONFIG_DIR.resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Failed to write sheep mutation data to file: {}", filePath, e);
        }
    }

    public static List<SheepMutation> getSheepMutations() {
        return SHEEP_MUTATIONS;
    }

    public static void validateConfig() {
        LOGGER.info("[ResourcefulSheep] Validating Sheep Mutations...");

        // Map pour regrouper les mutations par paire de parents (ordre ignoré)
        Map<String, List<SheepMutation>> mutationsByParents = new HashMap<>();

        for (SheepMutation mutation : SHEEP_MUTATIONS) {
            // Maman
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse("resourceful_sheep:" + mutation.MomId()))) {
                LOGGER.warn("[ResourcefulSheep] Config Warning SheepMutation: MomId '{}' not found in Entity Registry.", mutation.MomId());
            }
            // Papa
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse("resourceful_sheep:" + mutation.DadId()))) {
                LOGGER.warn("[ResourcefulSheep] Config Warning SheepMutation: DadId '{}' not found in Entity Registry.", mutation.DadId());
            }
            // Enfant
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse("resourceful_sheep:" + mutation.ChildId()))) {
                LOGGER.warn("[ResourcefulSheep] Config Warning SheepMutation: ChildId '{}' not found in Entity Registry.", mutation.ChildId());
            }

            // Création d'une clé unique pour la paire de parents (triée par ordre alphabétique)
            String mom = mutation.MomId();
            String dad = mutation.DadId();
            String parentKey = (mom.compareTo(dad) <= 0) ? (mom + " and " + dad) : (dad + " and " + mom);

            // Ajout de la mutation dans le groupe correspondant
            mutationsByParents.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(mutation);
        }

        // Vérification des chances totales pour chaque groupe de parents
        for (Map.Entry<String, List<SheepMutation>> entry : mutationsByParents.entrySet()) {
            int totalChance = 0;
            List<String> childFiles = new ArrayList<>();

            for (SheepMutation sheepMutation : entry.getValue()) {
                totalChance += sheepMutation.Chance();
                childFiles.add(sheepMutation.ChildId());
            }

            if (totalChance > 100) {
                LOGGER.warn("[ResourcefulSheep] Config Warning: The total mutation chance for parents '{}' is {}, which exceeds 100. Affected children: {}",
                        entry.getKey(),
                        totalChance,
                        String.join(", ", childFiles));
            }
        }
        LOGGER.info("[ResourcefulSheep] Sheep Mutations Config Validation Complete.");
    }
}