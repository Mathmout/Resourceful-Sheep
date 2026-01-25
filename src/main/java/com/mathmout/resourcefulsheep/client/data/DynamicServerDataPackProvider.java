package com.mathmout.resourcefulsheep.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DynamicServerDataPackProvider implements PackResources {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, byte[]> resourceCache = new HashMap<>();
    private final PackLocationInfo locationInfo;

    public DynamicServerDataPackProvider(PackLocationInfo locationInfo) {
        this.locationInfo = locationInfo;
        generateResources();
    }

    private void generateResources() {
        for (SheepSpawningData rule : ConfigSheepSpawningManager.getSheepSpawning()) {
            JsonObject spawner = new JsonObject();
            spawner.addProperty("type", ResourcefulSheepMod.MOD_ID + ":" + rule.sheepId());
            spawner.addProperty("weight", 8);
            spawner.addProperty("minCount", rule.minCount());
            spawner.addProperty("maxCount", rule.maxCount());

            JsonArray spawners = new JsonArray();
            spawners.add(spawner);

            if (rule.Biomes().isEmpty()) {
                JsonObject json = new JsonObject();
                json.addProperty("type", "resourceful_sheep:add_spawn_if_sheep_present");
                json.add("spawners", spawners);

                String path = "data/" + ResourcefulSheepMod.MOD_ID + "/neoforge/biome_modifier/" + rule.sheepId() + ".json";
                resourceCache.put(path, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
            }
            else {
                List<String> tags = new ArrayList<>();
                List<String> biomeIds = new ArrayList<>();

                for (String biome : rule.Biomes()) {
                    if (biome.startsWith("#")) {
                        tags.add(biome);
                    } else {
                        biomeIds.add(biome);
                    }
                }
                // Générer un fichier pour la liste des ID
                if (!biomeIds.isEmpty()) {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "neoforge:add_spawns");

                    JsonArray biomeList = new JsonArray();
                    biomeIds.forEach(biomeList::add);

                    json.add("biomes", biomeList);
                    json.add("spawners", spawners);

                    String path = "data/" + ResourcefulSheepMod.MOD_ID + "/neoforge/biome_modifier/" + rule.sheepId() + "_ids.json";
                    resourceCache.put(path, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
                }

                // Générer un fichier pour chaque Tag
                for (int i = 0; i < tags.size(); i++) {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "neoforge:add_spawns");
                    json.addProperty("biomes", tags.get(i));
                    json.add("spawners", spawners);

                    String path = "data/" + ResourcefulSheepMod.MOD_ID + "/neoforge/biome_modifier/" + rule.sheepId() + "_tag_" + i + ".json";
                    resourceCache.put(path, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(@NotNull String @NotNull ... elements) {
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull ResourceLocation location) {
        if (packType == PackType.SERVER_DATA && location.getNamespace().equals(ResourcefulSheepMod.MOD_ID)) {
            String path = "data/" + location.getNamespace() + "/" + location.getPath();
            if (resourceCache.containsKey(path)) {
                return () -> new ByteArrayInputStream(resourceCache.get(path));
            }
        }
        return null;
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA && namespace.equals(ResourcefulSheepMod.MOD_ID)) {
            String prefix = "data/" + namespace + "/" + path;
            for (Map.Entry<String, byte[]> entry : resourceCache.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(
                            namespace,
                            entry.getKey().substring(("data/" + namespace + "/").length())
                    );
                    resourceOutput.accept(resourceLocation, () -> new ByteArrayInputStream(entry.getValue()));
                }
            }
        }
    }

    @NotNull
    @Override
    public Set<String> getNamespaces(@NotNull PackType type) {
        return Set.of(ResourcefulSheepMod.MOD_ID);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> deserializer) {
        return null;
    }

    @NotNull
    @Override
    public PackLocationInfo location() {
        return this.locationInfo;
    }

    @Override
    public void close() {
    }
}