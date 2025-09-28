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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicSheepTextureGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSheepTextureGenerator.class);
    public static final Map<ResourceLocation, byte[]> DYNAMIC_TEXTURES = new ConcurrentHashMap<>();

    private static final double MIN_COVERAGE = 0.05; // 5% coverage for Tier 1
    private static final double MAX_COVERAGE = 0.80; // 80% maximum coverage
    private static final double BODY_INTENSITY_FACTOR = 0.45;

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

            Map<String, ResourceLocation> sourceTextureCache = findSourceTextures();

            for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
                try {
                    generateTexturesForVariant(variant, resourceManager, sheepBaseImage, furBaseImage, sourceTextureCache.get(variant.Resource));
                } catch (IOException e) {
                    LOGGER.error("Failed to generate textures for sheep variant: {}", variant.Id, e);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Error processing base sheep templates.", e);
        }

        LOGGER.info("Dynamic sheep texture generation finished. Generated {} textures.", DYNAMIC_TEXTURES.size());
    }

    private void generateTexturesForVariant(SheepVariantData variant, ResourceManager resourceManager, BufferedImage sheepBaseImage, BufferedImage furBaseImage, ResourceLocation itemTextureLocation) throws IOException {
        if (itemTextureLocation == null) {
            LOGGER.warn("No suitable item/block texture found for sheep resource: {}", variant.Resource);
            return;
        }

        ResourceLocation actualItemTextureLocation = ResourceLocation.fromNamespaceAndPath(itemTextureLocation.getNamespace(), "textures/item/" + itemTextureLocation.getPath() + ".png");

        Optional<Resource> textureResource = resourceManager.getResource(actualItemTextureLocation);
        if (textureResource.isEmpty()) {
            LOGGER.warn("Could not find texture resource for: {}", actualItemTextureLocation);
            return;
        }

        try (InputStream inputStream = textureResource.get().open()) {
            BufferedImage sourceTexture = ImageIO.read(inputStream);
            Map<Integer, Integer> colorPalette = analyzeTexture(sourceTexture);
            if (colorPalette.isEmpty()) {
                LOGGER.warn("Color palette for {} is empty. Skipping texture generation.", actualItemTextureLocation);
                return;
            }

            List<Map.Entry<Integer, Integer>> weightedColorPalette = new java.util.ArrayList<>(colorPalette.entrySet());

            int bodyPixelsTotal = calculateTotalPixels(BODY_REGIONS);
            int furPixelsTotal = calculateTotalPixels(FUR_REGIONS);

            SheepTypeData sheepType = ConfigSheepTypeManager.getSheepTypes().stream().filter(st -> st.Resource().equals(variant.Resource)).findFirst().orElse(null);
            if (sheepType == null) return;

            int maxTier = sheepType.SheepTier().stream().mapToInt(SheepTypeData.TierData::Tier).max().orElse(1);
            int minTier = sheepType.SheepTier().stream().mapToInt(SheepTypeData.TierData::Tier).min().orElse(1);

            int tier = variant.Tier;
            double coverage = MIN_COVERAGE;
            if (maxTier > minTier) {
                coverage = MIN_COVERAGE + (MAX_COVERAGE - MIN_COVERAGE) * (double) (tier - minTier) / (maxTier - minTier);
            } else if (maxTier > 1) {
                coverage = MIN_COVERAGE + (MAX_COVERAGE - MIN_COVERAGE) * (double) (tier - 1) / (maxTier - 1);
            }

            int bodyPixelsToAdd = (int) (bodyPixelsTotal * coverage * BODY_INTENSITY_FACTOR);
            int furPixelsToAdd = (int) (furPixelsTotal * coverage);

            BufferedImage sheepImage = copyImage(sheepBaseImage);
            BufferedImage furImage = copyImage(furBaseImage);

            addRandomPixels(sheepImage, BODY_REGIONS, weightedColorPalette, bodyPixelsToAdd);
            addRandomPixels(furImage, FUR_REGIONS, weightedColorPalette, furPixelsToAdd);

            ResourceLocation sheepTextureLocation = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/entity/sheep/" + variant.Id + ".png");
            DYNAMIC_TEXTURES.put(sheepTextureLocation, bufferedImageToPngBytes(sheepImage));

            ResourceLocation furTextureLocation = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/entity/sheep/" + variant.Id + "_fur.png");
            DYNAMIC_TEXTURES.put(furTextureLocation, bufferedImageToPngBytes(furImage));
        }
    }

    private Map<String, ResourceLocation> findSourceTextures() {
        Map<String, ResourceLocation> cache = new java.util.HashMap<>();
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
        Map<Integer, Integer> colorCounts = new java.util.HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                // Check if pixel is not transparent
                if ((pixel >> 24) != 0x00) {
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
            image.setRGB(x, y, 0xFF000000 | (color & 0x00FFFFFF));
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
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    private ResourceLocation getSourceTextureLocation(SheepTypeData sheepType) {
        ResourceLocation lastItemRL = null;
        ResourceLocation firstBlockRL = null;

        for (SheepTypeData.TierData tierData : sheepType.SheepTier()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tierData.DroppedItem()));
            ResourceLocation currentRL = BuiltInRegistries.ITEM.getKey(item);

            if (item instanceof BlockItem) {
                if (firstBlockRL == null) {
                    firstBlockRL = currentRL;
                }
            } else {
                lastItemRL = currentRL;
            }
        }
        return lastItemRL != null ? lastItemRL : firstBlockRL;
    }
}