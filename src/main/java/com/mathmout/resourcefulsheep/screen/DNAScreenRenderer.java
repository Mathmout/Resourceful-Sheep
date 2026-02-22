package com.mathmout.resourcefulsheep.screen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Squid;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DNAScreenRenderer {

    private static final int VISIBLE_HEIGHT = 150;
    public static final int CARD_HEIGHT = 40;
    public static final int CARD_WIDTH = 60;

    private static final Map<String, LivingEntity> entityCache = new HashMap<>();

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");


public static void renderDnaList(GuiGraphics guiGraphics, int panelX, int panelY, Font font, float scrollOffset, List<String> entitiesList) {

    if (entitiesList == null || entitiesList.isEmpty()) {
        String[] lines;
        if (entitiesList == null) {
            lines = new String[]{"No DNA", " Sequencer", "Found"};
        } else {
            lines = new String[]{"No DNA Stored"};
        }

        float textScale = 0.8f;
        int lineHeight = 10;

        float totalTextHeight = lines.length * lineHeight * textScale;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(panelX + 40, panelY + (VISIBLE_HEIGHT - totalTextHeight) / 2, 0);
        guiGraphics.pose().scale(textScale, textScale, 1.0f);

        for (int i = 0; i < lines.length; i++) {
            int textWidth = (int) (font.width(lines[i]) / textScale);
            guiGraphics.drawString(font, lines[i], -textWidth / 2, i * lineHeight, 0xc6c6c6, false);
        }
        guiGraphics.pose().popPose();
        return;
    }

        // Tri alphabétique
        List<String> sortedentitiesList = new ArrayList<>(entitiesList);
        sortedentitiesList.sort((id1, id2) -> {
            LivingEntity e1 = getEntity(id1);
            LivingEntity e2 = getEntity(id2);

            String name1 = getDisplayName(id1, e1);
            String name2 = getDisplayName(id2, e2);

            return name1.compareToIgnoreCase(name2);
        });

        guiGraphics.enableScissor(panelX + 2, panelY, panelX + CARD_WIDTH + 2, panelY + VISIBLE_HEIGHT);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, - scrollOffset, 0);

        int currentY = panelY + 2;

        for (String entityId : sortedentitiesList) {
            LivingEntity entity = getEntity(entityId);
            renderCard(guiGraphics, panelX + 2, currentY, entity, entityId, font);
            currentY += CARD_HEIGHT + 2;
        }
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
        Lighting.setupFor3DItems();
    }

    public static void renderCard(GuiGraphics guiGraphics, int x, int y, LivingEntity entity, String entityId, Font font) {

        if (entity == null) return;
        guiGraphics.blit(WIDGETS, x, y, 113, 0, CARD_WIDTH, CARD_HEIGHT);
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();

        float scale = getScale(height, width, entity);
        float scaledHeight = height * scale;

        float entityY = y + (CARD_HEIGHT + scaledHeight) / 2;
        float entityX = x + (CARD_WIDTH / 4f) + 2;

        // Affichage entité
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(entityX, entityY, 50);
        guiGraphics.pose().scale(scale, scale, scale);

        Quaternionf rot = new Quaternionf()
                .rotateZ((float) Math.PI)
                .rotateY((float) Math.toRadians(-40))
                .rotateX((float) Math.toRadians(5));
        guiGraphics.pose().mulPose(rot);

        EntityRenderDispatcher entityRender = Minecraft.getInstance().getEntityRenderDispatcher();
        RenderSystem.setShaderLights(new Vector3f(0f, 1f, 1f), new Vector3f(0f, 0f, 0f));
        entityRender.setRenderShadow(false);
        entityRender.render(entity, 0, 0, 0, 0, 1.0f, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880);
        entityRender.setRenderShadow(true);
        guiGraphics.flush();
        guiGraphics.pose().popPose();

        // Affichage nom
        String displayName = getDisplayName(entityId, entity);

        float maxLineWidth = (CARD_WIDTH / 2.0f) - 8;
        float preferredScale = 0.75f;
        float availableWidthForFont = maxLineWidth / preferredScale;

        String[] words = displayName.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (!currentLine.isEmpty()) {
                if (font.width(currentLine + " " + word) <= availableWidthForFont) {
                    currentLine.append(" ").append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            } else {
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());

        int maxTextWidth = 0;
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, font.width(line));
        }

        float finalScale = preferredScale;
        if (maxTextWidth * preferredScale > maxLineWidth) {
            finalScale = maxLineWidth / maxTextWidth;
        }

        float textCenterX = x + (CARD_WIDTH * 0.75f) + 2;
        float lineHeight = 10.0f;
        float totalBlockHeight = lines.size() * lineHeight * finalScale;
        float startY = (y + (CARD_HEIGHT / 2.0f)) - (totalBlockHeight / 2.0f);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(finalScale, finalScale, 1.0f);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = font.width(line);

            float drawX = (textCenterX / finalScale) - (lineWidth / 2.0f);
            float drawY = (startY / finalScale) + (i * lineHeight);

            guiGraphics.drawString(font, line, drawX, drawY, 0xFFFFFF, false);
        }
        guiGraphics.pose().popPose();

    }

    public static String getDisplayName(String entityId, LivingEntity entity) {
        if (entity == null) return entityId;
        if (entity instanceof ResourcefulSheepEntity) {
            try {
                String path = entityId.contains(":") ? entityId.split(":")[1] : entityId;
                return TexteUtils.StringToText(path).replace("Tier", "Sheep Tier");
            } catch (Exception e) {
                return entity.getName().getString();
            }
        }
        return entity.getName().getString();
    }

    private static float getScale(float height, float width, LivingEntity entity) {
        float ratio = height / width;

        float targetPixelSize;

        // spider = 0,64
        // cave spider = 0,71
        // zombie = 3,25
        // bee = 0,85
        // sheep = 0,69

        if (ratio <= 0.6f) {         // Crepe
            targetPixelSize = 14;
        }else if (ratio >= 1.8f) {   // Perche
            targetPixelSize = 30;
        } else {                     // Cube
            targetPixelSize = 15;
        }

        if (entity.getType() == EntityType.ENDER_DRAGON) {
            targetPixelSize = 45;
        }else if (entity.getType() == EntityType.GHAST) {
            targetPixelSize = 12;
        }else if (entity instanceof Squid) {
            targetPixelSize = 10;
        }

        float maxDim = Math.max(height, width);
        return targetPixelSize / maxDim;
    }

    public static LivingEntity getEntity(String entityId) {
        if (entityCache.containsKey(entityId)) {
            return entityCache.get(entityId);
        }
        EntityType<?> type = EntityType.byString(entityId).orElse(null); // Ca passe par le registry
        if (type != null && Minecraft.getInstance().level != null) {
            Entity entity = type.create(Minecraft.getInstance().level);
            if (entity instanceof Mob mob) {
                entityCache.put(entityId, mob);
                return mob;
            }
        }
        return null;
    }
}