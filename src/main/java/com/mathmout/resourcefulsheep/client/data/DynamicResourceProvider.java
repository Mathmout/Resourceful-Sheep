package com.mathmout.resourcefulsheep.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamicResourceProvider implements PackResources {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, byte[]> resourceCache = new HashMap<>();
    private final PackLocationInfo locationInfo;

    public DynamicResourceProvider(PackLocationInfo locationInfo) {
        this.locationInfo = locationInfo;
        generateResources();
    }

    private void generateResources() {
        generateLangFile();
        generateModelFiles();
    }

    private void generateLangFile() {
        JsonObject langJson = new JsonObject();

        // Traductions statics.

        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".lasso", "Lasso");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".sheep_scanner", "Sheep Scanner");
        langJson.addProperty("creativetab." + ResourcefulSheepMod.MOD_ID, "Resourceful Sheep");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".mutation", "Sheep Mutations");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_spawning", "Sheep Spawning");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_dropping", "Sheep Dropping");

        // Traductions dynamiques.

        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
            String entityKey = "entity." + ResourcefulSheepMod.MOD_ID + "." + variant.Id;
            langJson.addProperty(entityKey, "§lResourceful Sheep");
        }
        resourceCache.put(
                "assets/" + ResourcefulSheepMod.MOD_ID + "/lang/en_us.json",
                GSON.toJson(langJson).getBytes(StandardCharsets.UTF_8)
        );
    }

    private void generateModelFiles() {
        JsonObject modelObject = new JsonObject();
        modelObject.addProperty("parent", "minecraft:item/template_spawn_egg");
        byte[] modelBytes = GSON.toJson(modelObject).getBytes(StandardCharsets.UTF_8);

        for (String variantId : ConfigSheepTypeManager.getSheepVariant().keySet()) {
            String path = "assets/" + ResourcefulSheepMod.MOD_ID + "/models/item/" + variantId + "_spawn_egg.json";
            resourceCache.put(path, modelBytes);
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NotNull ... elements) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull ResourceLocation location) {
        if (packType == PackType.CLIENT_RESOURCES && location.getNamespace().equals(ResourcefulSheepMod.MOD_ID)) {
            String path = packType.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath();
            if (resourceCache.containsKey(path)) {
                return () -> new ByteArrayInputStream(resourceCache.get(path));
            }

            byte[] textureBytes = DynamicSheepTextureGenerator.DYNAMIC_TEXTURES.get(location);
            if (textureBytes != null) {
                return () -> new ByteArrayInputStream(textureBytes);
            }
        }
        return null;
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, PackResources.@NotNull ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES && namespace.equals(ResourcefulSheepMod.MOD_ID)) {
            String prefix = "assets/" + namespace + "/" + path;
            for (Map.Entry<String, byte[]> entry : resourceCache.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    ResourceLocation resourcelocation = ResourceLocation.fromNamespaceAndPath(
                            namespace,
                            entry.getKey().substring(("assets/" + namespace + "/").length())
                    );
                    resourceOutput.accept(resourcelocation, () -> new ByteArrayInputStream(entry.getValue()));
                }
            }

            for (Map.Entry<ResourceLocation, byte[]> entry : DynamicSheepTextureGenerator.DYNAMIC_TEXTURES.entrySet()) {
                if (entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(path)) {
                    resourceOutput.accept(entry.getKey(), () -> new ByteArrayInputStream(entry.getValue()));
                }
            }
        }
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        return Set.of(ResourcefulSheepMod.MOD_ID);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> deserializer) {
        return null;
    }

    @Override
    public @NotNull PackLocationInfo location() {
        return this.locationInfo;
    }

    @Override
    public void close() {
    }
}
