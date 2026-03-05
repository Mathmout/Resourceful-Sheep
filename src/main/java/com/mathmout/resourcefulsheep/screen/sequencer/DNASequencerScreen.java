package com.mathmout.resourcefulsheep.screen.sequencer;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.screen.DNAScreenRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DNASequencerScreen extends AbstractContainerScreen<DNASequencerMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/dna_sequencer_background.png");

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");

    public DNASequencerScreen(DNASequencerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private float scrollOffset = 0;
    private static final int CARD_HEIGHT = 40;
    private static final int VISIBLE_HEIGHT = 150;
    private boolean isHoveringPanel = false;

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

        DNAScreenRenderer.renderDnaList(guiGraphics, x - 75, y + 8, font, scrollOffset, this.menu.getStoredDna());

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
            int scaledHeight = Math.min((int) (((float) stored / max) * barHeight), barHeight);
            int yOffset = barHeight - scaledHeight;
            guiGraphics.blit(WIDGETS, x + 153, y + 13 + yOffset, 0, 19 + yOffset, 11, scaledHeight);
        }
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isHoveringPanel) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        List<String> dnaList = this.menu.getStoredDna();
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

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return mouseX < guiLeft - 83 || mouseY < guiTop || mouseX >= guiLeft + this.imageWidth || mouseY >= guiTop + this.imageHeight;
    }
}
