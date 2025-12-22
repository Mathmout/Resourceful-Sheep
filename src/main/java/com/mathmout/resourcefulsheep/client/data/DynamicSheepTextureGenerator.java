package com.mathmout.resourcefulsheep.client.data;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.config.sheeptypes.SheepTypeData;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicSheepTextureGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSheepTextureGenerator.class);
    public static final Map<ResourceLocation, byte[]> DYNAMIC_TEXTURES = new ConcurrentHashMap<>();

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
        DYNAMIC_TEXTURES.clear();
    }

    public void generateAllTextures(ResourceManager resourceManager) {
        LOGGER.info("Starting dynamic sheep texture generation...");

        Optional<Resource> sheepBaseResource = resourceManager.getResource(SHEEP_BASE_TEXTURE);
        Optional<Resource> furBaseResource = resourceManager.getResource(SHEEP_FUR_BASE_TEXTURE);

        if (sheepBaseResource.isEmpty() || furBaseResource.isEmpty()) {
            LOGGER.error("Could not load base sheep templates! Aborting texture generation.");
            return;
        }

        try (InputStream sheepBaseStream = sheepBaseResource.get().open();
             InputStream furBaseStream = furBaseResource.get().open()) {

            BufferedImage sheepBaseImage = ImageIO.read(sheepBaseStream);
            BufferedImage furBaseImage = ImageIO.read(furBaseStream);

            Map<String, List<ResourceLocation>> sourceTextureCache = findSourceTextures();

            for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
                try {
                    List<ResourceLocation> sources = sourceTextureCache.get(variant.Resource());
                    generateTexturesForVariant(variant, resourceManager, sheepBaseImage, furBaseImage, sources);
                } catch (IOException e) {
                    LOGGER.error("Failed to generate textures for sheep variant: {}", variant.Id(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error processing base sheep templates.", e);
        }
        LOGGER.info("Dynamic sheep texture generation finished. Generated {} textures.", DYNAMIC_TEXTURES.size());
    }

    private void generateTexturesForVariant(SheepVariantData variant, ResourceManager resourceManager, BufferedImage sheepBaseImage, BufferedImage furBaseImage, List<ResourceLocation> itemKeys) throws IOException {
        if (itemKeys == null || itemKeys.isEmpty()) {
            LOGGER.warn("No suitable item/block textures found for sheep resource: {}", variant.Resource());
            return;
        }

        // Map combinée pour stocker les couleurs de TOUS les items
        Map<Integer, Integer> combinedPalette = new HashMap<>();

        // On boucle sur chaque item (block ou item) associé à ce type de mouton
        for (ResourceLocation itemKey : itemKeys) {
            BufferedImage sourceTexture = loadBufferedImage(resourceManager, itemKey);

            if (sourceTexture != null) {
                Map<Integer, Integer> texturePalette = analyzeTexture(sourceTexture);
                // On fusionne la palette de cet item dans la palette globale
                texturePalette.forEach((color, count) -> combinedPalette.merge(color, count, Integer::sum));
            }
        }

        if (combinedPalette.isEmpty()) {
            LOGGER.warn("Combined color palette for {} is empty (all source textures failed). Skipping.", variant.Resource());
            return;
        }

        List<Map.Entry<Integer, Integer>> weightedColorPalette = new ArrayList<>(combinedPalette.entrySet());

        int bodyPixelsTotal = calculateTotalPixels(BODY_REGIONS);
        int furPixelsTotal = calculateTotalPixels(FUR_REGIONS);

        SheepTypeData sheepType = ConfigSheepTypeManager.getSheepTypes().stream()
                .filter(st -> st.Resource().equals(variant.Resource()))
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

        BufferedImage sheepImage = copyImage(sheepBaseImage);
        BufferedImage furImage = copyImage(furBaseImage);

        addRandomPixels(sheepImage, BODY_REGIONS, weightedColorPalette, bodyPixelsToAdd);
        addRandomPixels(furImage, FUR_REGIONS, weightedColorPalette, furPixelsToAdd);

        ResourceLocation sheepTextureLocation = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                "textures/entity/sheep/" + variant.Id() + ".png");
        DYNAMIC_TEXTURES.put(sheepTextureLocation, bufferedImageToPngBytes(sheepImage));

        ResourceLocation furTextureLocation = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                "textures/entity/sheep/" + variant.Id() + "_fur.png");
        DYNAMIC_TEXTURES.put(furTextureLocation, bufferedImageToPngBytes(furImage));
    }

    private BufferedImage loadBufferedImage(ResourceManager resourceManager, ResourceLocation itemKey) {
        Item item = BuiltInRegistries.ITEM.get(itemKey);
        // Try primary path first
        String primaryPath = item instanceof BlockItem ? "textures/block/" : "textures/item/";
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), primaryPath + itemKey.getPath() + ".png");

        Optional<Resource> res = resourceManager.getResource(loc);

        // If fail, try secondary path
        if (res.isEmpty()) {
            String secondaryPath = item instanceof BlockItem ? "textures/item/" : "textures/block/";
            loc = ResourceLocation.fromNamespaceAndPath(itemKey.getNamespace(), secondaryPath + itemKey.getPath() + ".png");
            res = resourceManager.getResource(loc);
        }

        if (res.isPresent()) {
            try (InputStream is = res.get().open()) {
                return ImageIO.read(is);
            } catch (IOException e) {
                LOGGER.warn("Failed to read texture stream for: {}", loc);
            }
        } else {
            LOGGER.debug("Could not find texture resource for: {}", itemKey);
        }
        return null;
    }

    private Map<String, List<ResourceLocation>> findSourceTextures() {
        Map<String, List<ResourceLocation>> cache = new HashMap<>();
        for (SheepTypeData sheepType : ConfigSheepTypeManager.getSheepTypes()) {
            cache.put(sheepType.Resource(), getSourceTextureLocation(sheepType));
        }
        return cache;
    }

    private byte[] bufferedImageToPngBytes(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", stream);
            return stream.toByteArray();
        }
    }

    private Map<Integer, Integer> analyzeTexture(BufferedImage image) {
        Map<Integer, Integer> colorCounts = new HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                // Ignore fully transparent pixels
                if (alpha > 10) {
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

    private List<ResourceLocation> getSourceTextureLocation(SheepTypeData sheepType) {
        Set<ResourceLocation> locs = new HashSet<>();

        // Find the last item in the tier list that is NOT a block
        if (sheepType.SheepTier() != null) {
            for (SheepTypeData.TierData tierData : sheepType.SheepTier()) {
                if (tierData.DroppedItems() != null) {
                    for (SheepTypeData.TierData.DroppedItems DroppedItems : tierData.DroppedItems()) {
                        ResourceLocation item = ResourceLocation.parse(DroppedItems.ItemId());
                        locs.add(item);
                    }
                }
            }
        }
        return new ArrayList<>(locs);
    }
}