package com.mathmout.resourcefulsheep.client.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.config.sheeptypes.SheepTypeData;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicTexturesGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTexturesGenerator.class);
    public static final Map<ResourceLocation, byte[]> DYNAMIC_SHEEP_TEXTURES = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, byte[]> DYNAMIC_WOOL_TEXTURES = new ConcurrentHashMap<>();

    private static volatile boolean isGenerated = false;
    private static volatile Thread generatingThread = null;
    private static final Object LOCK = new Object();

    private static final int MAX_TIER_INCREMENT = 6; // 60% maximum coverage
    private static final double BODY_INTENSITY_FACTOR = 0.45;
    private static final double TIER_INCREMENT = 0.10; // 10% increase per tier

    private static final ResourceLocation SHEEP_BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
            "textures/entity/sheep/template/sheep.png");
    private static final ResourceLocation SHEEP_FUR_BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
            "textures/entity/sheep/template/sheep_fur.png");

    private static final List<Rectangle> BODY_REGIONS = List.of(
            new Rectangle(28, 14, 28, 16),
            new Rectangle(34, 8, 16, 6)
    );

    private static final List<Rectangle> FUR_REGIONS = List.of(
            new Rectangle(28, 14, 28, 16),
            new Rectangle(34, 8, 16, 6),
            new Rectangle(0, 6, 24, 6),
            new Rectangle(6, 0, 12, 6)
    );

    public static void clear() {
        synchronized(LOCK) {
            DYNAMIC_SHEEP_TEXTURES.clear();
            DYNAMIC_WOOL_TEXTURES.clear();
            isGenerated = false;
            generatingThread = null;
        }
    }

    public static void triggerGeneration() {
        // 1. Si tout est déjà prêt, on passe directement.
        if (isGenerated) return;

        // 2. L'ASTUCE MAGIQUE : Si le thread qui demande une texture est CELUI qui
        // est actuellement en train de les générer, on le laisse passer sans rien
        // bloquer pour qu'il puisse récupérer ses images de base (évite la boucle infinie).
        if (Thread.currentThread() == generatingThread) return;

        // 3. Les AUTRES threads (qui chargent les textures du jeu) vont s'entasser ici et ATTENDRE.
        synchronized(LOCK) {
            // Quand la porte s'ouvre enfin, on vérifie si le premier thread a fini le travail.
            if (isGenerated) return;

            // On verrouille officiellement cette tâche pour CE thread précis.
            generatingThread = Thread.currentThread();

            ResourceManager rm = Minecraft.getInstance().getResourceManager();
            try {
                new DynamicTexturesGenerator().generateAllTextures(rm);
                isGenerated = true; // On marque comme terminé pour libérer tous les autres threads !
            } catch (Exception e) {
                LOGGER.error("[ResourcefulSheep] Erreur lors de la génération dynamique", e);
            } finally {
                generatingThread = null; // On rend les clés
            }
        }
    }

    public void generateAllTextures(ResourceManager resourceManager) {
        LOGGER.info("[ResourcefulSheep] Starting dynamic sheep texture generation...");

        Optional<Resource> sheepBaseResource = resourceManager.getResource(SHEEP_BASE_TEXTURE);
        Optional<Resource> furBaseResource = resourceManager.getResource(SHEEP_FUR_BASE_TEXTURE);

        if (sheepBaseResource.isEmpty() || furBaseResource.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("[ResourcefulSheep] Could not load base sheep templates! Aborting texture generation.");
            }
            return;
        }

        try (InputStream sheepBaseStream = sheepBaseResource.get().open();
             InputStream furBaseStream = furBaseResource.get().open()) {

            BufferedImage sheepBaseImage = ImageIO.read(sheepBaseStream);
            BufferedImage furBaseImage = ImageIO.read(furBaseStream);

            // Chargement des 16 laines de base
            Map<String, BufferedImage> woolBaseImages = new HashMap<>();
            for (DyeColor color : DyeColor.values()) {
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/block/wools/" + color.getName() + "_wool.png");
                Optional<Resource> res = resourceManager.getResource(loc);
                if (res.isPresent()) {
                    try (InputStream is = res.get().open()) {
                        woolBaseImages.put(color.getName(), ImageIO.read(is));
                    } catch (IOException e) {
                        LOGGER.warn("[ResourcefulSheep] Error reading wool texture: {}", loc);
                    }
                } else {
                    LOGGER.warn("[ResourcefulSheep] Missing base wool texture in your folder: {}", loc);
                }
            }

            for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
                try {
                    List<ResourceLocation> sources = getVariantSourceTextures(variant);
                    generateTexturesForVariant(variant, resourceManager, sheepBaseImage, furBaseImage, woolBaseImages, sources);
                } catch (IOException e) {
                    LOGGER.error("[ResourcefulSheep] Failed to generate textures for sheep variant: {}", variant.Id(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("[ResourcefulSheep] Error processing base sheep templates.", e);
        }
        LOGGER.info("[ResourcefulSheep] Dynamic sheep texture generation finished. Generated {} textures.", DYNAMIC_SHEEP_TEXTURES.size());
    }

    private void generateTexturesForVariant(SheepVariantData variant, ResourceManager resourceManager, BufferedImage sheepBaseImage, BufferedImage furBaseImage, Map<String, BufferedImage> woolBaseImages, List<ResourceLocation> itemKeys) throws IOException {
        if (itemKeys == null || itemKeys.isEmpty()) {
            LOGGER.warn("[ResourcefulSheep] No suitable item/block textures found for sheep name: {}", variant.Name());
        }

        Map<Integer, Integer> combinedPalette = new HashMap<>();

        for (ResourceLocation itemKey : itemKeys) {
            // On récupère TOUTES les images associées à cet item (top, side, bottom, etc.)
            List<BufferedImage> sourceTextures = loadBufferedImages(resourceManager, itemKey);

            for (BufferedImage sourceTexture : sourceTextures) {
                int itemTint = -1;
                try {
                    Item item = BuiltInRegistries.ITEM.get(itemKey);
                    if (item != Items.AIR) {
                        itemTint = Minecraft.getInstance().getItemColors().getColor(new ItemStack(item), 0);
                    }
                } catch (Exception ignored) {

                }
                Map<Integer, Integer> texturePalette = analyzeTexture(sourceTexture, itemTint);
                texturePalette.forEach((color, count) -> combinedPalette.merge(color, count, Integer::sum));
            }
        }

        if (combinedPalette.isEmpty()) {
            LOGGER.warn("[ResourcefulSheep] Combined color palette for {} is empty (all source textures failed). Skipping.", variant.Name());
        }

        List<Map.Entry<Integer, Integer>> weightedColorPalette = new ArrayList<>(combinedPalette.entrySet());

        int bodyPixelsTotal = calculateTotalPixels(BODY_REGIONS);
        int furPixelsTotal = calculateTotalPixels(FUR_REGIONS);

        SheepTypeData sheepType = ConfigSheepTypeManager.getSheepTypes().stream()
                .filter(st -> st.SheepName().equals(variant.Name()))
                .findFirst()
                .orElse(null);
        if (sheepType == null) return;

        double maxTier = sheepType.SheepTier().stream().mapToInt(SheepTypeData.TierData::Tier).max().orElse(1);
        int tier = variant.Tier();

        double coverage;
        if (maxTier > MAX_TIER_INCREMENT) {
            coverage = (MAX_TIER_INCREMENT / 10.0) * (tier / maxTier);
        } else {
            coverage = tier * TIER_INCREMENT;
        }

        int bodyPixelsToAdd = (int) (bodyPixelsTotal * coverage * BODY_INTENSITY_FACTOR);
        int furPixelsToAdd = (int) (furPixelsTotal * coverage);

        // Moutons
        BufferedImage sheepImage = copyImage(sheepBaseImage);
        BufferedImage furImage = copyImage(furBaseImage);

        addRandomPixels(sheepImage, BODY_REGIONS, weightedColorPalette, bodyPixelsToAdd);
        addRandomPixels(furImage, FUR_REGIONS, weightedColorPalette, furPixelsToAdd);

        ResourceLocation sheepTextureLocation = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                "textures/entity/sheep/" + variant.Id() + ".png");
        DYNAMIC_SHEEP_TEXTURES.put(sheepTextureLocation, bufferedImageToPngBytes(sheepImage));

        ResourceLocation furTextureLocation = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                "textures/entity/sheep/" + variant.Id() + "_fur.png");
        DYNAMIC_SHEEP_TEXTURES.put(furTextureLocation, bufferedImageToPngBytes(furImage));

        // Laines
        int woolPixelsToAdd = (int) (256 * coverage);
        List<Rectangle> woolRegion = List.of(new Rectangle(0, 0, 16, 16));

        for (Map.Entry<String, BufferedImage> entry : woolBaseImages.entrySet()) {
            String colorName = entry.getKey();
            BufferedImage woolImage = copyImage(entry.getValue());

            // On tamponne les pixels colorés sur toute la surface de la laine
            addRandomPixels(woolImage, woolRegion, weightedColorPalette, woolPixelsToAdd);

            ResourceLocation woolLoc = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                    "textures/block/" + variant.Id() + "_wool_" + colorName + ".png");
            DYNAMIC_WOOL_TEXTURES.put(woolLoc, bufferedImageToPngBytes(woolImage));
        }
    }

    private List<ResourceLocation> getVariantSourceTextures(SheepVariantData variant) {
        Set<ResourceLocation> uniqueItems = new HashSet<>();

        if (variant.DroppedItems() != null) {
            for (SheepVariantData.DroppedItems dropData : variant.DroppedItems()) {
                String rawId = dropData.ItemId();

                // TAG
                if (rawId.startsWith("#")) {
                    try {
                        ResourceLocation tagLoc = ResourceLocation.parse(rawId.substring(1));
                        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);

                        var tagResult = BuiltInRegistries.ITEM.getTag(tagKey);

                        if (tagResult.isPresent()) {
                            List<Holder<Item>> itemsInTag = tagResult.get().stream().toList();
                            if (!itemsInTag.isEmpty()) {
                                for (Holder<Item> itemHolder : itemsInTag) {
                                    ResourceLocation itemLoc = BuiltInRegistries.ITEM.getKey(itemHolder.value());
                                    uniqueItems.add(itemLoc);
                                }
                            } else {
                                LOGGER.debug("[ResourcefulSheep] Tag {} is empty or not yet bound during texture generation.", rawId);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.debug("[ResourcefulSheep] Invalid tag found in sheep config texture generation: {}", rawId);
                    }
                }
                // ITEM
                else {
                    ResourceLocation loc = ResourceLocation.tryParse(rawId);
                    if (loc != null) {
                        uniqueItems.add(loc);
                    }
                }
            }
        }
        return new ArrayList<>(uniqueItems);
    }

    private List<BufferedImage> loadBufferedImages(ResourceManager resourceManager, ResourceLocation itemKey) {
        List<BufferedImage> images = new ArrayList<>();
        Set<ResourceLocation> texturePathsToLoad = new HashSet<>();

        // On commence par chercher le modèle de l'item
        ResourceLocation currentModelLoc = ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), "models/item/" + itemKey.getPath() + ".json");

        int depth = 0;
        // Boucle pour remonter les "parents" (limite de 10 pour éviter les boucles infinies)
        while (depth < 10) {
            depth++;
            Optional<Resource> modelRes = resourceManager.getResource(currentModelLoc);

            // Fallback, si l'item n'a pas de modèle, on tente direct en tant que bloc
            if (modelRes.isEmpty() && depth == 1) {
                currentModelLoc = ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), "models/block/" + itemKey.getPath() + ".json");
                modelRes = resourceManager.getResource(currentModelLoc);
            }

            if (modelRes.isPresent()) {
                try (InputStream is = modelRes.get().open()) {
                    JsonObject modelJson = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();

                    if (modelJson.has("textures")) {
                        JsonObject textures = modelJson.getAsJsonObject("textures");
                        for (String key : textures.keySet()) {
                            String texPath = textures.get(key).getAsString();

                            // On ignore les variables qui pointent vers d'autres variables (ex: "#all")
                            if (texPath.startsWith("#")) continue;

                            try {
                                ResourceLocation texLoc = ResourceLocation.parse(texPath);
                                texturePathsToLoad.add(ResourceLocation.fromNamespaceAndPath(texLoc.getNamespace(), "textures/" + texLoc.getPath() + ".png"));
                            } catch (Exception e) {
                                LOGGER.warn("[ResourcefulSheep] Path ignored : {}", texPath);
                            }
                        }
                        // On a trouvé les textures, on arrête de chercher !
                        break;
                    }
                    // S'il n'y a pas de textures, on regarde le parent et on recommence.
                    else if (modelJson.has("parent")) {
                        String parentPath = modelJson.get("parent").getAsString();
                        ResourceLocation parentLoc = ResourceLocation.parse(parentPath);
                        // On modifie le fichier actuel pour aller lire le parent au prochain tour de boucle
                        currentModelLoc = ResourceLocation.fromNamespaceAndPath(parentLoc.getNamespace(), "models/" + parentLoc.getPath() + ".json");
                    }
                    else {
                        // pas de textures, ni de parent on arrete.
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.warn("[ResourcefulSheep] JSON reading error for {}", currentModelLoc);
                    break;
                }
            } else {
                break; // Fichier introuvable
            }
        }

        // Fallback si le JSON ne nous a vraiment rien donné
        if (texturePathsToLoad.isEmpty()) {
            String name = itemKey.getPath();
            texturePathsToLoad.add(ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), "textures/item/" + name + ".png"));
            texturePathsToLoad.add(ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), "textures/block/" + name + ".png"));
        }

        // On charge toutes les images PNG trouvées !
        for (ResourceLocation loc : texturePathsToLoad) {
            if (loc.getPath().contains("missingno")) continue;
            Optional<Resource> resource = resourceManager.getResource(loc);
            if (resource.isPresent()) {
                try (InputStream inputStream = resource.get().open()) {
                    images.add(ImageIO.read(inputStream));
                } catch (IOException e) {
                    LOGGER.warn("[ResourcefulSheep] Cannot read the image : {}", loc);
                }
            }
        }
        return images;
    }

    private byte[] bufferedImageToPngBytes(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", stream);
            return stream.toByteArray();
        }
    }

    // Modifie la signature pour accepter tintColor
    private Map<Integer, Integer> analyzeTexture(BufferedImage image, int tintColor) {
        Map<Integer, Integer> colorCounts = new HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();

        // On décompose la couleur de teinte (Tint)
        // Si tintColor est -1 (pas de teinte), on considère que c'est du blanc (pas de changement)
        int tRed = (tintColor >> 16) & 0xFF;
        int tGreen = (tintColor >> 8) & 0xFF;
        int tBlue = (tintColor) & 0xFF;
        boolean hasTint = (tintColor != -1 && tintColor != 0xFFFFFF);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                // Ignore fully transparent pixels
                if (alpha > 10) {
                    // Si on a une teinte, on l'applique sur le pixel gris
                    if (hasTint) {
                        int r = (pixel >> 16) & 0xFF;
                        int g = (pixel >> 8) & 0xFF;
                        int b = (pixel) & 0xFF;

                        // Formule de multiplication des couleurs (Blend Multiply)
                        r = (r * tRed) / 255;
                        g = (g * tGreen) / 255;
                        b = (b * tBlue) / 255;

                        // On recompose le pixel
                        pixel = (alpha << 24) | (r << 16) | (g << 8) | b;
                    }
                    colorCounts.merge(pixel, 1, Integer::sum);
                }
            }
        }
        return colorCounts;
    }


    private int calculateTotalPixels(List<Rectangle> regions) {
        return regions.stream().mapToInt(r -> r.width * r.height).sum();
    }

    private void addRandomPixels(BufferedImage image, List<Rectangle> regions, List<Map.Entry<Integer, Integer>> weightedColorPalette, int totalPixelsToAdd) {
        if (weightedColorPalette.isEmpty() || totalPixelsToAdd <= 0) {
            return;
        }

        long totalWeight = weightedColorPalette.stream().mapToLong(Map.Entry::getValue).sum();
        int totalRegionPixels = calculateTotalPixels(regions);

        for (int i = 0; i < totalPixelsToAdd; i++) {
            Rectangle region = selectWeightedRandomRegion(regions, totalRegionPixels);
            int color = selectWeightedRandomColor(weightedColorPalette, totalWeight);
            int x = region.x + (int) (Math.random() * region.width);
            int y = region.y + (int) (Math.random() * region.height);
            // Force pixel to be opaque
            image.setRGB(x, y, color | 0xFF000000);
        }
    }

    private Rectangle selectWeightedRandomRegion(List<Rectangle> regions, int totalPixels) {
        int randomValue = (int) (Math.random() * totalPixels);
        int currentPixels = 0;
        for (Rectangle region : regions) {
            currentPixels += region.width * region.height;
            if (randomValue < currentPixels) {
                return region;
            }
        }
        return regions.getLast(); // Fallback
    }

    private int selectWeightedRandomColor(List<Map.Entry<Integer, Integer>> weightedColorPalette, long totalWeight) {
        long randomValue = (long) (Math.random() * totalWeight);
        long currentWeight = 0;
        for (Map.Entry<Integer, Integer> entry : weightedColorPalette) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                return entry.getKey();
            }
        }
        return weightedColorPalette.getLast().getKey(); // Fallback
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }
}