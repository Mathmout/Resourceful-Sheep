package com.mathmout.resourcefulsheep.screen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DNASequencerScreen extends AbstractContainerScreen<DNASequencerMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/background.png");

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");

    public DNASequencerScreen(DNASequencerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private float scrollOffset = 0;
    private static final int CARD_HEIGHT = 40;
    private static final int CARD_WIDTH = 64;
    private static final int VISIBLE_HEIGHT = 150;
    private boolean isHoveringPanel = false;

    private final Map<String, LivingEntity> entityCache = new HashMap<>();

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int panelX = x - 83;

        // Fond
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);

        // DNA
        guiGraphics.blit(WIDGETS, x - 83, y, 176, 0, 80 , 176);

        this.isHoveringPanel = mouseX >= panelX && mouseX < panelX + 80 &&
                mouseY >= y && mouseY < y + 176;

        renderDnaList(guiGraphics, x - 75, y + 8);

        // Slot
        guiGraphics.blit(WIDGETS, x + (imageWidth - this.font.width(this.title)) / 2, y + 34, 0, 0, 18, 18);        // In
        guiGraphics.blit(WIDGETS, x + (imageWidth + this.font.width(this.title)) / 2 - 18, y + 34, 19, 0, 18, 18);  // Out

        // Flèche
        guiGraphics.blit(WIDGETS, x + imageWidth / 2 - 11, y + 36, 38, 0, 22, 15);

        // Flèche pleine
        if(menu.isCrafting()) {
            int progressWidth = menu.getScaledProgress();
            guiGraphics.blit(WIDGETS, x + imageWidth / 2 - 11, y + 35, 61, 0, progressWidth, 15);
        }

        // Energy
        guiGraphics.blit(WIDGETS, x + 153, y + 13, 12, 19, 11, 60);

        int stored = menu.getEnergy();
        int max = menu.getMaxEnergy();
        if (max > 0) {
            int barHeight = 60;
            int scaledHeight = (int) (((float) stored / max) * barHeight);
            int yOffset = barHeight - scaledHeight;
            guiGraphics.blit(WIDGETS, x + 153, y + 13 + yOffset, 0, 19 + yOffset, 11, scaledHeight);
        }
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int textWidth = this.font.width(this.title);
        guiGraphics.drawString(this.font, this.title, (this.imageWidth - textWidth) / 2, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if(isHovering(153, 13, 11, 60, mouseX, mouseY)) {

            guiGraphics.renderTooltip(font,
                    Component.literal(String.valueOf(menu.getEnergy())).withStyle(ChatFormatting.DARK_RED)
                            .append(Component.literal(" / ").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(String.valueOf(menu.getMaxEnergy())).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" FE").withStyle(ChatFormatting.GOLD)),
                    mouseX, mouseY);
        }
    }

    private void renderDnaList(GuiGraphics guiGraphics, int panelX, int panelY) {
        List<String> rawDnaList = menu.getBlockEntity().getStoredDna();

        if (rawDnaList == null || rawDnaList.isEmpty()){
            String noDnaText = "No DNA Stored";
            float textScale = 0.8f;
            int textWidth = font.width(noDnaText);

            guiGraphics.pose().pushPose();
            float textX = panelX + 40 - textWidth / 2f;
            float textY = panelY + ((float) VISIBLE_HEIGHT / 2);

            guiGraphics.pose().translate(textX, textY, 0);
            guiGraphics.pose().scale(textScale, textScale, 1.0f);
            guiGraphics.drawString(font, noDnaText, 0, 0, 0xc6c6c6, false);
            guiGraphics.pose().popPose();
            return;
        }

        // Tri alphabétique
        List<String> sortedDnaList = new ArrayList<>(rawDnaList);
        sortedDnaList.sort((id1, id2) -> {
            LivingEntity e1 = getCachedEntity(id1);
            LivingEntity e2 = getCachedEntity(id2);

            String name1 = (e1 != null) ? e1.getName().getString() : id1;
            String name2 = (e2 != null) ? e2.getName().getString() : id2;

            return name1.compareToIgnoreCase(name2);
        });

        guiGraphics.enableScissor(panelX, panelY, panelX + CARD_WIDTH, panelY + VISIBLE_HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, - scrollOffset, 0);

        int currentY = panelY + 2;

        for (String entityId : sortedDnaList) {
            LivingEntity entity = getCachedEntity(entityId);
            if (entity != null) {

                guiGraphics.blit(WIDGETS, panelX, currentY, 111, 0, CARD_WIDTH, CARD_HEIGHT);
                float width = entity.getBbWidth();
                float height = entity.getBbHeight();

                float scale = getScale(height, width, entity);

                float scaledHeight = height * scale;

                float entityY = currentY + (CARD_HEIGHT + scaledHeight) / 2;
                float entityX = panelX + (CARD_WIDTH / 4f);

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
                String fullName = entity.getName().getString();
                String[] words = fullName.split(" ");

                float textCenterX = panelX + (CARD_WIDTH * 0.75f);
                float textBaseY = currentY + (CARD_HEIGHT / 2.0f) - 4;
                int textColor = 0xFFFFFF;

                guiGraphics.pose().pushPose();

                if (words.length > 1) { // Multi lignes
                    float multiLineScale = 0.6f;
                    guiGraphics.pose().scale(multiLineScale, multiLineScale, 1.0f);
                    float startY = (textBaseY / multiLineScale) - (words.length * 4);

                    for (int i = 0; i < words.length; i++) {
                        String word = words[i];
                        int wordWidth = font.width(word);
                        guiGraphics.drawString(font, word, (textCenterX / multiLineScale) - (wordWidth / 2.0f), startY + (i * 10), textColor, false);
                    }
                } else { // Mono ligne
                    int wordWidth = font.width(fullName);
                    float maxAvailableWidth = (CARD_WIDTH / 2.0f) - 8;
                    float singleScale = (wordWidth * 0.7f > maxAvailableWidth) ? (maxAvailableWidth / wordWidth) : 0.7f;

                    guiGraphics.pose().scale(singleScale, singleScale, 1.0f);
                    guiGraphics.drawString(font, fullName, (textCenterX / singleScale) - (wordWidth / 2.0f), textBaseY / singleScale, textColor, false);
                }
                guiGraphics.pose().popPose();
                currentY += CARD_HEIGHT + 2;
            }
        }        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isHoveringPanel) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        List<String> dnaList = menu.getBlockEntity().getStoredDna();
        if (dnaList.isEmpty()) return false;

        int totalListHeight = dnaList.size() * (CARD_HEIGHT + 2);

        if (totalListHeight <= VISIBLE_HEIGHT) return false;

        float scrollSpeed = 15.0f;
        this.scrollOffset -= (float) (scrollY * scrollSpeed);

        float maxScroll = totalListHeight - VISIBLE_HEIGHT + 2;
        if (this.scrollOffset < 0) this.scrollOffset = 0;
        if (this.scrollOffset > maxScroll) this.scrollOffset = maxScroll;

        return true;
    }

    private LivingEntity getCachedEntity(String entityId) {
        if (entityCache.containsKey(entityId)) {
            return entityCache.get(entityId);
        }
        EntityType<?> type = EntityType.byString(entityId).orElse(null);
        if (type != null && this.minecraft != null && this.minecraft.level != null) {
            Entity entity = type.create(this.minecraft.level);
            if (entity instanceof Mob mob) {
                entityCache.put(entityId, mob);
                return mob;
            }
        }
        return null;
    }
}
