package com.mathmout.resourcefulsheep.screen.centrifuge;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.screen.ScreenRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/centrifuge_background.png");

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");

    public CentrifugeScreen(CentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 240;
        this.imageHeight = 213;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);

        int nbProcess = menu.getTier().getParallelProcesses();
        int outCols = (menu.getTier() == CentrifugeTier.BASIC) ? 3 : nbProcess;
        int nbFluidTank = menu.getTier().getNbFluidTank();
        int availableWidth = imageWidth - 3 - (nbFluidTank * 22) - 22;

        // --- GÉOMÉTRIE (Espacement uniquement entre les slots) ---
        int maxGap = 13;
        int maxPossibleGap = (nbProcess > 1) ? (availableWidth - (nbProcess * 18)) / (nbProcess - 1) : 0;
        int actualGap = (nbProcess == 1) ? 0 : Math.min(maxGap, maxPossibleGap);

        int totalInputWidth = (nbProcess * 18) + (Math.max(0, nbProcess - 1) * actualGap);
        int inputStartX = x + 22 + (availableWidth - totalInputWidth) / 2;

        int totalOutputWidth = outCols * 18;
        int outputStartX = x + 22 + (availableWidth - totalOutputWidth) / 2;

        // Y Fixes
        int inputY = y + 25;
        int arrowY = y + 50;
        int outputY = y + 68;

        // --- ENERGIE ---
        if (menu.getTier().getEnergyConsumption() > 0) {
            guiGraphics.blit(WIDGETS, x + 10, y + 34, 12, 19, 11, 60);
            int stored = menu.getEnergy();
            int max = menu.getMaxEnergy();
            if (max > 0) {
                int scaledHeight = Math.min((int) (((float) stored / max) * 60), 60);
                int yOffset = 60 - scaledHeight;
                guiGraphics.blit(WIDGETS, x + 10, y + 34 + yOffset, 0, 19 + yOffset, 11, scaledHeight);
            }
        }

        // --- TANKS ---
        FluidTank[] tanks = menu.getFluidTanks();
        int tankY = y + 33;
        for (int i = 0 ; i < tanks.length ; i++) {
            int tankX = x + imageWidth - 3 - (tanks.length * 22) + (i * 22);
            guiGraphics.blit(WIDGETS, tankX + 2, tankY + 2, 79, 39, 14, 58);
            if (!tanks[i].isEmpty()) {
                renderFluid(guiGraphics, tanks[i].getFluid(), tanks[i].getCapacity(), tankX + 2, tankY + 2);
            }
            guiGraphics.blit(WIDGETS, tankX, tankY, 94, 39, 18, 62);
        }

        // --- SLOTS D'ENTRÉE ET FLÈCHES ---
        for (int i = 0; i < nbProcess; i++) {
            int slotX = inputStartX + (i * (18 + actualGap));

            guiGraphics.blit(WIDGETS, slotX, inputY, 94, 19, 18, 18);
            guiGraphics.blit(WIDGETS, slotX + 4, arrowY, 46, 17, 9, 12); // Flèche vide

            int progress = menu.getProcessTimer(i);
            int maxProgress = menu.getMaxProgress();
            if (progress > 0 && maxProgress > 0) {
                int scaledProgress = progress * 12 / maxProgress;
                guiGraphics.blit(WIDGETS, slotX + 4, arrowY, 56, 17, 9, scaledProgress);
            }
        }

        // --- GRILLE BLEUE DES SORTIES ---
        if (menu.getTier() == CentrifugeTier.BASIC) {
            guiGraphics.blit(WIDGETS, outputStartX, outputY, 56, 141, 18, 18); // Gauche
            guiGraphics.blit(WIDGETS, outputStartX + 18, outputY, 75, 141, 18, 18); // Milieu
            guiGraphics.blit(WIDGETS, outputStartX + 36, outputY, 94, 141, 18, 18); // Droite
        } else {
            for (int col = 0; col < nbProcess; col++) {
                int px = outputStartX + (col * 18);

                // Ligne du Haut
                if (col == 0) guiGraphics.blit(WIDGETS, px, outputY, 56, 103, 18, 18);
                else if (col == nbProcess - 1) guiGraphics.blit(WIDGETS, px, outputY, 94, 103, 18, 18);
                else guiGraphics.blit(WIDGETS, px, outputY, 75, 103, 18, 18);

                // Ligne du Bas
                if (col == 0) guiGraphics.blit(WIDGETS, px, outputY + 18, 56, 122, 18, 18);
                else if (col == nbProcess - 1) guiGraphics.blit(WIDGETS, px, outputY + 18, 94, 122, 18, 18);
                else guiGraphics.blit(WIDGETS, px, outputY + 18, 75, 122, 18, 18);
            }
        }
    }

    private void renderFluid(GuiGraphics guiGraphics, FluidStack fluidStack, int capacity, int x, int y) {
        if (fluidStack.isEmpty() || capacity == 0) return;

        int width = 14;
        int height = 58;

        IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidExt.getStillTexture(fluidStack);

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int color = fluidExt.getTintColor(fluidStack);

        int fluidHeight = (int) (((float) fluidStack.getAmount() / capacity) * height);
        if (fluidHeight <= 0) return;
        int startY = y + height - fluidHeight;

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int fillY = 0; fillY < fluidHeight; fillY += 16) {
            int drawHeight = Math.min(16, fluidHeight - fillY);
            int drawY = startY + fluidHeight - fillY - drawHeight;

            for (int fillX = 0; fillX < width; fillX += 16) {
                int drawWidth = Math.min(16, width - fillX);
                int drawX = x + fillX;

                float u0 = sprite.getU0();
                float u1 = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * ((float) drawWidth / 16f);
                float v0 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * ((16f - drawHeight) / 16f);
                float v1 = sprite.getV1();

                bufferbuilder.addVertex(matrix, drawX, drawY + drawHeight, 0).setColor(r, g, b, a).setUv(u0, v1);
                bufferbuilder.addVertex(matrix, drawX + drawWidth, drawY + drawHeight, 0).setColor(r, g, b, a).setUv(u1, v1);
                bufferbuilder.addVertex(matrix, drawX + drawWidth, drawY, 0).setColor(r, g, b, a).setUv(u1, v0);
                bufferbuilder.addVertex(matrix, drawX, drawY, 0).setColor(r, g, b, a).setUv(u0, v0);
            }
        }
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(10, 34, 11, 60, mouseX, mouseY) && menu.getTier().getEnergyConsumption() > 0) {
            ScreenRenderer.renderEnergyValue(guiGraphics, font, menu.getMaxEnergy(), menu.getEnergy(), mouseX, mouseY);
        }

        int nbProcess = menu.getTier().getParallelProcesses();
        int nbTanks = menu.getTier().getNbFluidTank();
        int availableWidth = imageWidth - 3 - (nbTanks * 22) - 22;
        int maxGap = 13;
        int maxPossibleGap = (nbProcess > 1) ? (availableWidth - (nbProcess * 18)) / (nbProcess - 1) : 0;
        int actualGap = (nbProcess == 1) ? 0 : Math.min(maxGap, maxPossibleGap);
        int totalInputWidth = (nbProcess * 18) + (Math.max(0, nbProcess - 1) * actualGap);
        int inputStartX = 22 + (availableWidth - totalInputWidth) / 2;

        // Tooltip Flèches
        for (int i = 0; i < nbProcess; i++) {
            int arrowX = inputStartX + (i * (18 + actualGap)) + 4;
            if (isHovering(arrowX, 46, 9, 12, mouseX, mouseY)) {
                ScreenRenderer.renderProgressTooltip(guiGraphics, font, menu.getProcessTimer(i), menu.getMaxProgress(), mouseX, mouseY);
            }
        }

        // Tooltip Fluides
        FluidTank[] tanks = menu.getFluidTanks();
        for (int i = 0; i < tanks.length; i++) {
            int localTankX = imageWidth - 3 - (tanks.length * 22) + (i * 22);

            if (isHovering(localTankX, 33, 18, 62, mouseX, mouseY)) {
                List<Component> tooltip = new ArrayList<>();
                if (tanks[i].isEmpty()) {
                    tooltip.add(Component.literal("Empty").withStyle(ChatFormatting.RED));
                    tooltip.add(Component.empty()
                            .append(Component.literal("0").withStyle(ChatFormatting.RED))
                            .append(Component.literal(" / ").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(String.valueOf(tanks[i].getCapacity())).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" mB").withStyle(ChatFormatting.GOLD)));
                } else {
                    tooltip.add(tanks[i].getFluid().getHoverName());
                    tooltip.add(Component.empty()
                            .append(Component.literal(String.valueOf(tanks[i].getFluidAmount())).withStyle(ChatFormatting.DARK_AQUA))
                            .append(Component.literal(" / ").withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(String.valueOf(tanks[i].getCapacity())).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" mB").withStyle(ChatFormatting.GREEN)));
                }
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 5, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8 + 39, this.imageHeight - 94, 4210752, false);
    }
}