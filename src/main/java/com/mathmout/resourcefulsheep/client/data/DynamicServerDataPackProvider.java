package com.mathmout.resourcefulsheep.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.config.sheeptypes.SheepTypeData;
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
        generateSpawningData();
        generateSheepTags();
        generateWoolBlockTag();
        generateWoolLootTables();
        generateCentrifugeLootTables();
    }

    private void generateCentrifugeLootTables() {
        for (CentrifugeTier tier : CentrifugeTier.values()) {
            String prefix = tier.name().toLowerCase() + "_centrifuge_";

            List<String> blockNames = new ArrayList<>(List.of(
                    prefix + "casing",
                    prefix + "item_in_port",
                    prefix + "item_out_port",
                    prefix + "energy_port"
            ));

            if (tier != CentrifugeTier.BASIC) {
                blockNames.add(prefix + "fluid_port");
            }

            for (String blockName : blockNames) {
                JsonObject lootTable = new JsonObject();
                lootTable.addProperty("type", "minecraft:block");

                JsonArray pools = new JsonArray();
                JsonObject pool = new JsonObject();
                pool.addProperty("rolls", 1);

                // L'item à dropper (le bloc lui-même)
                JsonArray entries = new JsonArray();
                JsonObject entry = new JsonObject();
                entry.addProperty("type", "minecraft:item");
                entry.addProperty("name", ResourcefulSheepMod.MOD_ID + ":" + blockName);
                entries.add(entry);
                pool.add("entries", entries);

                // Condition : survit aux explosions (comportement vanilla classique)
                JsonArray conditions = new JsonArray();
                JsonObject condition = new JsonObject();
                condition.addProperty("condition", "minecraft:survives_explosion");
                conditions.add(condition);
                pool.add("conditions", conditions);

                pools.add(pool);
                lootTable.add("pools", pools);

                // Le chemin d'une Loot Table de bloc est toujours data/mod_id/loot_tables/blocks/nom_du_bloc.json
                String path = "data/" + ResourcefulSheepMod.MOD_ID + "/loot_table/blocks/" + blockName + ".json";
                resourceCache.put(path, GSON.toJson(lootTable).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private void generateWoolLootTables() {
        for (String variantId : ConfigSheepTypeManager.getSheepVariant().keySet()) {
            String blockId = ResourcefulSheepMod.MOD_ID + ":" + variantId + "_wool";

            JsonObject root = new JsonObject();
            root.addProperty("type", "minecraft:block");

            JsonArray pools = new JsonArray();
            JsonObject pool = new JsonObject();
            pool.addProperty("rolls", 1);

            JsonArray entries = new JsonArray();
            JsonObject entry = new JsonObject();
            entry.addProperty("type", "minecraft:item");
            entry.addProperty("name", blockId);

            JsonArray functions = new JsonArray();
            JsonObject copyStateFunc = new JsonObject();
            copyStateFunc.addProperty("function", "minecraft:copy_state");
            copyStateFunc.addProperty("block", blockId);

            JsonArray propertiesArray = new JsonArray();
            propertiesArray.add("color"); // C'est le nom de la propriété dans ton ResourcefulWoolBlock
            copyStateFunc.add("properties", propertiesArray);

            functions.add(copyStateFunc);
            entry.add("functions", functions);

            entries.add(entry);
            pool.add("entries", entries);

            JsonArray conditions = new JsonArray();
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:survives_explosion");
            conditions.add(condition);
            pool.add("conditions", conditions);

            pools.add(pool);
            root.add("pools", pools);

            String path = "data/" + ResourcefulSheepMod.MOD_ID + "/loot_table/blocks/" + variantId + "_wool.json";
            resourceCache.put(path, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        }
    }

    private void generateWoolBlockTag() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", false);

        JsonArray values = new JsonArray();
        for (String variantId : ConfigSheepTypeManager.getSheepVariant().keySet()) {
            JsonObject value = new JsonObject();
            value.addProperty("id", ResourcefulSheepMod.MOD_ID + ":" + variantId + "_wool");
            value.addProperty("required", false);
            values.add(value);
        }
        root.add("values", values);

        String path = "data/minecraft/tags/block/wool.json";
        resourceCache.put(path, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
    }

    private void generateSheepTags() {
        JsonObject globalRoot = new JsonObject();
        globalRoot.addProperty("replace", false);
        JsonArray globalValues = new JsonArray();

        for (SheepTypeData sheepTypeData : ConfigSheepTypeManager.getSheepTypes()) {
            String specificTagName = sheepTypeData.SheepName() + "_sheep";

            JsonObject specificRoot = new JsonObject();
            specificRoot.addProperty("replace", false);
            JsonArray specificValues = new JsonArray();

            for (SheepTypeData.TierData tierData : sheepTypeData.SheepTier()) {
                String fullEntityId = ResourcefulSheepMod.MOD_ID + ":" + sheepTypeData.SheepName() + "_tier_" + tierData.Tier();

                JsonObject value = new JsonObject();
                value.addProperty("id", fullEntityId);
                value.addProperty("required", false);
                specificValues.add(value);
            }

            specificRoot.add("values", specificValues);
            String specificPath = "data/" + ResourcefulSheepMod.MOD_ID + "/tags/entity_type/" + specificTagName + ".json";
            resourceCache.put(specificPath, GSON.toJson(specificRoot).getBytes(StandardCharsets.UTF_8));

            JsonObject globalValue = new JsonObject();
            globalValue.addProperty("id", "#" + ResourcefulSheepMod.MOD_ID + ":" + specificTagName);
            globalValue.addProperty("required", false);
            globalValues.add(globalValue);
        }

        globalRoot.add("values", globalValues);
        String globalPath = "data/" + ResourcefulSheepMod.MOD_ID + "/tags/entity_type/all_sheep.json";
        resourceCache.put(globalPath, GSON.toJson(globalRoot).getBytes(StandardCharsets.UTF_8));
    }

    private void generateSpawningData() {
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
        if (packType == PackType.SERVER_DATA && location.getNamespace().equals(ResourcefulSheepMod.MOD_ID) || location.getNamespace().equals("minecraft")) {
            String path = "data/" + location.getNamespace() + "/" + location.getPath();
            if (resourceCache.containsKey(path)) {
                return () -> new ByteArrayInputStream(resourceCache.get(path));
            }
        }
        return null;
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA && namespace.equals(ResourcefulSheepMod.MOD_ID) || namespace.equals("minecraft")) {
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
        return Set.of(ResourcefulSheepMod.MOD_ID, "minecraft");
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