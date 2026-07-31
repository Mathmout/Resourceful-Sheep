package com.mathmout.resourcefulsheep.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DynamicResourceProvider implements PackResources {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, byte[]> resourceCache = new HashMap<>();
    private final PackLocationInfo locationInfo;

    public DynamicResourceProvider(PackLocationInfo locationInfo) {
        this.locationInfo = locationInfo;
        generateLangFile();
        generateSpawnEggModels();
        generateWoolModels();
        generateCentrifugeModels();
    }

    private void generateLangFile() {
        JsonObject langJson = new JsonObject();

        // Traductions statics.

        // Items
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".lasso", "Lasso");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".sheep_scanner", "Sheep Scanner");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".iron_syringe", "Iron Syringe");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".diamond_syringe", "Diamond Syringe");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".netherite_syringe", "Netherite Syringe");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".suspicious_spawn_egg", "Suspicious Spawn Egg");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".resourceful_wool", "§lResourceful Wool");

        // Créatif menu
        langJson.addProperty("creativetab." + ResourcefulSheepMod.MOD_ID, "Resourceful Sheep");

        // JEI
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_mutations", "Sheep Mutations");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_spawning", "Sheep Spawning");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_dropping", "Sheep Dropping");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_feeding", "Sheep Feeding");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_eating", "Sheep Eating");
        langJson.addProperty("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_cross_breeding", "Sheep Cross Breeding");
        langJson.addProperty("jei." + ResourcefulSheepMod.MOD_ID + ".diamond_template.desc", "This template can be purchased from a Journeyman Shepherd villager, or found randomly in chests inside Shepherd houses in villages.");

        // Smithing Template
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".smithing_template.diamond_upgrade.applies_to", "Iron Syringe");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".smithing_template.diamond_upgrade.ingredients", "Diamond");
        langJson.addProperty("upgrade." + ResourcefulSheepMod.MOD_ID + ".diamond_upgrade", "Diamond Upgrade");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".smithing_template.diamond_upgrade.base_slot_description", "Add iron syringe");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".smithing_template.diamond_upgrade.additions_slot_description", "Add diamond block");
        langJson.addProperty("item." + ResourcefulSheepMod.MOD_ID + ".diamond_upgrade_smithing_template", "Smithing Template");
        
        // Block
        langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + ".dna_sequencer", "DNA Sequencer");
        langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + ".dna_splicer", "DNA Splicer");

        // Moutons
        langJson.addProperty("entity." + ResourcefulSheepMod.MOD_ID + ".resourceful_sheep", "§lResourceful Sheep");

        for (CentrifugeTier tier : CentrifugeTier.values()) {
            String prefix = tier.name().toLowerCase() + "_centrifuge_";
            langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + "." + prefix + "controller", "Centrifuge Controller");
            langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + "." + prefix + "casing", "Centrifuge Casing");
            langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + "." + prefix + "item_in_port", "Centrifuge Item Input Port");
            langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + "." + prefix + "item_out_port", "Centrifuge Item Output Port");
            langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + "." + prefix + "energy_port", "Centrifuge Energy Port");
            if (tier != CentrifugeTier.BASIC) {
                langJson.addProperty("block." + ResourcefulSheepMod.MOD_ID + "." + prefix + "fluid_port", "Centrifuge Fluid Port");
            }
        }

        resourceCache.put(
                "assets/" + ResourcefulSheepMod.MOD_ID + "/lang/en_us.json",
                GSON.toJson(langJson).getBytes(StandardCharsets.UTF_8)
        );
    }

    private void generateSpawnEggModels() {
        JsonObject modelObject = new JsonObject();
        modelObject.addProperty("parent", "minecraft:item/template_spawn_egg");
        byte[] modelBytes = GSON.toJson(modelObject).getBytes(StandardCharsets.UTF_8);

        for (String variantId : ConfigSheepTypeManager.getSheepVariant().keySet()) {
            String path = "assets/" + ResourcefulSheepMod.MOD_ID + "/models/item/" + variantId + "_spawn_egg.json";
            resourceCache.put(path, modelBytes);
        }
    }

    private void generateWoolModels() {
        for (String variantId : ConfigSheepTypeManager.getSheepVariant().keySet()) {
            JsonObject blockStateRoot = new JsonObject();
            JsonObject variantsObject = new JsonObject();

            // Le modèle principal de l'Item dans l'inventaire
            JsonObject mainItemModelRoot = new JsonObject();
            mainItemModelRoot.addProperty("parent", ResourcefulSheepMod.MOD_ID + ":block/" + variantId + "_wool_white");
            JsonArray overrides = new JsonArray();

            // Appel de la sous-méthode pour chaque couleur
            for (DyeColor color : DyeColor.values()) {
                generateSingleWoolColorModel(variantId, color, variantsObject, overrides);
            }

            // Sauvegarde finale du BlockState
            blockStateRoot.add("variants", variantsObject);
            String blockStatePath = "assets/" + ResourcefulSheepMod.MOD_ID + "/blockstates/" + variantId + "_wool.json";
            resourceCache.put(blockStatePath, GSON.toJson(blockStateRoot).getBytes(StandardCharsets.UTF_8));

            // Sauvegarde finale du Modèle Item principal
            mainItemModelRoot.add("overrides", overrides);
            String mainItemModelPath = "assets/" + ResourcefulSheepMod.MOD_ID + "/models/item/" + variantId + "_wool.json";
            resourceCache.put(mainItemModelPath, GSON.toJson(mainItemModelRoot).getBytes(StandardCharsets.UTF_8));
        }
    }

    private void generateSingleWoolColorModel(String variantId, DyeColor color, JsonObject variantsObject, JsonArray overrides) {
        String colorName = color.getName();

        // Le Block Model
        JsonObject blockModelRoot = new JsonObject();
        blockModelRoot.addProperty("parent", "minecraft:block/cube_all");
        JsonObject texturesObject = new JsonObject();
        texturesObject.addProperty("all", ResourcefulSheepMod.MOD_ID + ":block/" + variantId + "_wool_" + colorName);
        blockModelRoot.add("textures", texturesObject);

        String blockModelPath = "assets/" + ResourcefulSheepMod.MOD_ID + "/models/block/" + variantId + "_wool_" + colorName + ".json";
        resourceCache.put(blockModelPath, GSON.toJson(blockModelRoot).getBytes(StandardCharsets.UTF_8));

        // Lien dans le BlockState
        JsonObject modelPointer = new JsonObject();
        modelPointer.addProperty("model", ResourcefulSheepMod.MOD_ID + ":block/" + variantId + "_wool_" + colorName);
        variantsObject.add("color=" + colorName, modelPointer);

        // On crée un sous-modèle Item pour cette couleur
        JsonObject subItemModel = new JsonObject();
        subItemModel.addProperty("parent", ResourcefulSheepMod.MOD_ID + ":block/" + variantId + "_wool_" + colorName);
        String subItemModelPath = "assets/" + ResourcefulSheepMod.MOD_ID + "/models/item/" + variantId + "_wool_" + colorName + ".json";
        resourceCache.put(subItemModelPath, GSON.toJson(subItemModel).getBytes(StandardCharsets.UTF_8));

        // Override dans le modèle principal (sauf pour le blanc)
        if (color != DyeColor.WHITE) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();

            predicate.addProperty("resourceful_sheep:color", (float) color.getId());
            override.add("predicate", predicate);
            override.addProperty("model", ResourcefulSheepMod.MOD_ID + ":item/" + variantId + "_wool_" + colorName);

            overrides.add(override);
        }
    }

    private void generateCentrifugeModels() {
        for (CentrifugeTier tier : CentrifugeTier.values()) {
            String prefix = tier.name().toLowerCase() + "_centrifuge_";

           List<String> blockNames = new ArrayList<>(List.of(
                    prefix + "casing",
                    prefix + "controller",
                    prefix + "item_in_port",
                    prefix + "item_out_port",
                    prefix + "energy_port"
            ));

            if (tier != CentrifugeTier.BASIC) {
                blockNames.add(prefix + "fluid_port");
            }

            for (String blockName : blockNames) {
                // 1. Modèle du bloc
                JsonObject blockModel = getCentrifugeModel(blockName, prefix);

                resourceCache.put("assets/" + ResourcefulSheepMod.MOD_ID + "/models/block/" + blockName + ".json",
                        GSON.toJson(blockModel).getBytes(StandardCharsets.UTF_8));

                // 2. BlockState (Gestion des directions)
                JsonObject blockState = new JsonObject();
                JsonObject variants = new JsonObject();
                String modelRef = ResourcefulSheepMod.MOD_ID + ":block/" + blockName;

                if (blockName.endsWith("casing")) {
                    // Le casing n'a AUCUNE orientation (1 seule variante)
                    JsonObject variant = new JsonObject();
                    variant.addProperty("model", modelRef);
                    variants.add("", variant); // "" signifie l'état par défaut

                } else if (blockName.contains("port")) {
                    // Les ports ont 6 directions
                    for (Direction dir : Direction.values()) {
                        JsonObject variant = new JsonObject();
                        variant.addProperty("model", modelRef);

                        // Rotations pour que la texture du port suive la direction
                        switch (dir) {
                            case UP -> variant.addProperty("x", -90);
                            case DOWN -> variant.addProperty("x", 90);
                            case SOUTH -> variant.addProperty("y", 180);
                            case WEST -> variant.addProperty("y", 270);
                            case EAST -> variant.addProperty("y", 90);
                            case NORTH -> {} // Défaut, pas de rotation
                        }
                        variants.add("facing=" + dir.getName(), variant);
                    }

                } else if (blockName.endsWith("controller")) {
                    // Le contrôleur a 4 directions horizontales
                    JsonObject north = new JsonObject(); north.addProperty("model", modelRef);
                    JsonObject south = new JsonObject(); south.addProperty("model", modelRef); south.addProperty("y", 180);
                    JsonObject east = new JsonObject(); east.addProperty("model", modelRef); east.addProperty("y", 90);
                    JsonObject west = new JsonObject(); west.addProperty("model", modelRef); west.addProperty("y", 270);

                    variants.add("facing=north", north);
                    variants.add("facing=south", south);
                    variants.add("facing=east", east);
                    variants.add("facing=west", west);
                }

                blockState.add("variants", variants);
                resourceCache.put("assets/" + ResourcefulSheepMod.MOD_ID + "/blockstates/" + blockName + ".json",
                        GSON.toJson(blockState).getBytes(StandardCharsets.UTF_8));

                // 3. Modèle de l'Item
                JsonObject itemModel = new JsonObject();
                itemModel.addProperty("parent", modelRef);

                resourceCache.put("assets/" + ResourcefulSheepMod.MOD_ID + "/models/item/" + blockName + ".json",
                        GSON.toJson(itemModel).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static @NotNull JsonObject getCentrifugeModel(String blockName, String prefix) {
        JsonObject blockModel = new JsonObject();
        JsonObject textures = new JsonObject();
        String texturePath = ResourcefulSheepMod.MOD_ID + ":block/" + blockName;

        if (blockName.endsWith("controller")) {
            blockModel.addProperty("parent", "minecraft:block/orientable");
            textures.addProperty("front", texturePath + "_front");
            textures.addProperty("side", ResourcefulSheepMod.MOD_ID + ":block/" + prefix + "casing");
            textures.addProperty("top", ResourcefulSheepMod.MOD_ID + ":block/" + prefix + "casing");
            textures.addProperty("bottom", ResourcefulSheepMod.MOD_ID + ":block/" + prefix + "casing");
        } else {
            blockModel.addProperty("parent", "minecraft:block/cube_all");
            textures.addProperty("all", texturePath);
        }
        blockModel.add("textures", textures);
        return blockModel;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NotNull ... elements) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull ResourceLocation location) {
        if (packType == PackType.CLIENT_RESOURCES && location.getNamespace().equals(ResourcefulSheepMod.MOD_ID)) {

            // LE PIÈGE : Si Minecraft demande une texture, on s'assure qu'elles existent !
            if (location.getPath().startsWith("textures")) {
                DynamicTexturesGenerator.triggerGeneration();
            }

            String path = packType.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath();
            if (resourceCache.containsKey(path)) {
                return () -> new ByteArrayInputStream(resourceCache.get(path));
            }

            // Moutons
            byte[] sheepTextureBytes = DynamicTexturesGenerator.DYNAMIC_SHEEP_TEXTURES.get(location);
            if (sheepTextureBytes != null) {
                return () -> new ByteArrayInputStream(sheepTextureBytes);
            }

            // Laines
            byte[] woolTextureBytes = DynamicTexturesGenerator.DYNAMIC_WOOL_TEXTURES.get(location);
            if (woolTextureBytes != null) {
                return () -> new ByteArrayInputStream(woolTextureBytes);
            }
        }
        return null;
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, PackResources.@NotNull ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES && namespace.equals(ResourcefulSheepMod.MOD_ID)) {

            // LE PIÈGE : Idem lors du scan des dossiers par Minecraft
            if (path.startsWith("textures")) {
                DynamicTexturesGenerator.triggerGeneration();
            }

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

            // Moutons
            for (Map.Entry<ResourceLocation, byte[]> entry : DynamicTexturesGenerator.DYNAMIC_SHEEP_TEXTURES.entrySet()) {
                if (entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(path)) {
                    resourceOutput.accept(entry.getKey(), () -> new ByteArrayInputStream(entry.getValue()));
                }
            }

            // Laines
            for (Map.Entry<ResourceLocation, byte[]> entry : DynamicTexturesGenerator.DYNAMIC_WOOL_TEXTURES.entrySet()) {
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