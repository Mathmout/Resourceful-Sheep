package com.mathmout.resourcefulsheep.screen.splicer;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.dnacrossbreeding.ConfigDNACrossbreedingManager;
import com.mathmout.resourcefulsheep.config.dnacrossbreeding.SheepCrossbreeding;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.network.SetDnaParentPayload;
import com.mathmout.resourcefulsheep.screen.DNAScreenRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
        import java.util.List;

public class DNASplicerScreen extends AbstractContainerScreen<DNASplicerMenu> {

    private float scrollOffset = 0;
    private static final int CARD_HEIGHT = 40;
    private static final int CARD_WIDTH = 60;
    private static final int VISIBLE_HEIGHT = 150;
    private boolean isHoveringPanel = false;

    private String draggingDna = null;
    private boolean isDragging = false;
    private int tickCounter = 0;
    private int currentResultIndex = 0;
    private final List<String> possibleResults = new ArrayList<>();

    private String lastParent1 = "";
    private String lastParent2 = "";

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/dna_splicer_background.png");

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");


    public DNASplicerScreen(DNASplicerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 304;
    }

    @Override
    protected void init() {
        super.init();
        updatePossibleResults();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tickCounter++;

        // Animation du cycle des résultats
        if (tickCounter % 40 == 0) {
            if (!possibleResults.isEmpty()) {
                currentResultIndex = (currentResultIndex + 1) % possibleResults.size();
            }
        }

        // Optimisation : On ne met à jour la liste QUE si les parents ont changé
        String currentP1 = menu.getParent1();
        String currentP2 = menu.getParent2();

        if (!currentP1.equals(lastParent1) || !currentP2.equals(lastParent2)) {
            updatePossibleResults();
            lastParent1 = currentP1;
            lastParent2 = currentP2;
            currentResultIndex = 0;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int panelX = x - 83;

        // Fond
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 512, 512);

        // DNA
        guiGraphics.blit(WIDGETS, x - 83, y, 176, 0, 80 , 176);

        // Cards vide
        int space = (imageWidth - 3 * CARD_WIDTH) / 4 ;
        guiGraphics.blit(WIDGETS, x + space + 10, y + 41 - CARD_HEIGHT / 2, 113,41, CARD_WIDTH, CARD_HEIGHT);
        guiGraphics.blit(WIDGETS, x + 2 * space + CARD_WIDTH, y + 41 - CARD_HEIGHT / 2, 113,41, CARD_WIDTH, CARD_HEIGHT);
        guiGraphics.blit(WIDGETS, x + 3 * space + 2 * CARD_WIDTH - 10, y + 41 - CARD_HEIGHT / 2, 113,41, CARD_WIDTH, CARD_HEIGHT);

        // Cards pleine
        renderDnaCard(guiGraphics, x + space + 10, y + 41 - CARD_HEIGHT / 2, menu.getParent1(), "green");
        renderDnaCard(guiGraphics, x + 2 * space + CARD_WIDTH, y + 41 - CARD_HEIGHT / 2, menu.getParent2(), "green");
        if (!possibleResults.isEmpty()) {
            renderDnaCard(guiGraphics, x + 3 * space + 2 * CARD_WIDTH - 10, y + 41 - CARD_HEIGHT / 2, possibleResults.get(currentResultIndex), "blue");
        }

        // Slot
        guiGraphics.blit(WIDGETS, x + 3 * (space + CARD_WIDTH) - 10 + (imageWidth - (3 * (space + CARD_WIDTH) - 10)) / 2 - 9, y + 41 - 18 / 2, 84,0, 18, 18);

        // Flèche
        int arrowX = x + 2 * space + 2 * CARD_WIDTH + (3 * space + 2 * CARD_WIDTH - 10 - (2 * space + 2 * CARD_WIDTH)) / 2 - 6;
        guiGraphics.blit(WIDGETS, arrowX, y + 41 - 4, 33, 19, 12, 9);

        int progressWidth = menu.getScaledProgress() - 9;
        if(menu.isCrafting()) {
            if (progressWidth > 0)
                guiGraphics.blit(WIDGETS, arrowX, y + 41 - 4 - 1, 33, 30, progressWidth, 10);
        }

        // Plus
        int plusX = x + space + 10 + CARD_WIDTH + (2 * space + CARD_WIDTH - (space + 10 + CARD_WIDTH)) / 2 - 4;
        guiGraphics.blit(WIDGETS, plusX, y + 41 - 4, 24, 19, 9, 9);
        if(menu.isCrafting()) {
            progressWidth = Math.min(9, menu.getScaledProgress());
            guiGraphics.blit(WIDGETS, plusX, y + 35, 24, 30, progressWidth, 10);
        }

        // Energy
        guiGraphics.blit(WIDGETS, x + (space + 10) / 2 - 5, y + 13, 12, 19, 11, 60);

        int stored = menu.getEnergy();
        int max = menu.getMaxEnergy();
        if (max > 0) {
            int barHeight = 60;
            int scaledHeight = Math.min((int) (((float) stored / max) * barHeight), barHeight);
            int yOffset = barHeight - scaledHeight;
            guiGraphics.blit(WIDGETS, x + (space + 10) / 2 - 5, y + 13 + yOffset, 0, 19 + yOffset, 11, scaledHeight);
        }

        this.isHoveringPanel = mouseX >= panelX && mouseX < panelX + 80 && mouseY >= y && mouseY < y + 176;

        DNAScreenRenderer.renderDnaList(guiGraphics, x - 75, y + 8, font, scrollOffset, this.menu.getNeighborDnaList());
    }

    private void renderDnaCard(GuiGraphics guiGraphics, int x, int y, String dnaId, String color) {
        if (dnaId != null && !dnaId.isEmpty()) {
            LivingEntity entity = DNAScreenRenderer.getEntity(dnaId);
            if (entity != null) {
                DNAScreenRenderer.renderCard(guiGraphics, x, y, entity, dnaId, font, color);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isDragging && draggingDna != null) {
            // On dessine la carte sous la souris (centrée sur le curseur)
            // On désactive le depth test pour être sûr que ça passe au dessus de tout
            RenderSystem.disableDepthTest();
            guiGraphics.pose().translate(0, 0, 200f);
            renderDnaCard(guiGraphics, mouseX - CARD_WIDTH / 2, mouseY - CARD_HEIGHT / 2, draggingDna, "red");
            RenderSystem.enableDepthTest();
        }

        if(isHovering(((imageWidth - 3 * CARD_WIDTH) / 4 + 10) / 2 - 5, 13, 11, 60, mouseX, mouseY)) {

            guiGraphics.renderTooltip(font,
                    Component.literal(String.valueOf(menu.getEnergy())).withStyle(ChatFormatting.DARK_RED)
                            .append(Component.literal(" / ").withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(String.valueOf(menu.getMaxEnergy())).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" FE").withStyle(ChatFormatting.GOLD)),
                    mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Clic Gauche
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            // 1. Vérifier si on clique dans la liste
            if (mouseX >= x - 83 && mouseX < x && mouseY >= y && mouseY < y + 176) {
                String clickedDna = getDnaAtPosition(mouseX, mouseY);
                if (clickedDna != null) {
                    this.draggingDna = clickedDna;
                    this.isDragging = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDragging && draggingDna != null) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            int space = (imageWidth - 3 * CARD_WIDTH) / 4;
            int slotY = y + 41 - CARD_HEIGHT / 2;

            // Slot 1 Bounds
            int s1x = x + space + 10;
            if (mouseX >= s1x && mouseX <= s1x + CARD_WIDTH && mouseY >= slotY && mouseY <= slotY + CARD_HEIGHT) {
                // DROP SUR SLOT 1
                PacketDistributor.sendToServer(new SetDnaParentPayload(menu.blockEntity.getBlockPos(), 1, draggingDna));
            }

            // Slot 2 Bounds
            int s2x = x + 2 * space + CARD_WIDTH;
            if (mouseX >= s2x && mouseX <= s2x + CARD_WIDTH && mouseY >= slotY && mouseY <= slotY + CARD_HEIGHT) {
                // DROP SUR SLOT 2
                PacketDistributor.sendToServer(new SetDnaParentPayload(menu.blockEntity.getBlockPos(), 2, draggingDna));
            }

            // Reset Drag
            isDragging = false;
            draggingDna = null;
            return true;
        }

        isDragging = false;
        draggingDna = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private String getDnaAtPosition(double mouseX, double mouseY) {
        List<String> entitiesList = this.menu.getNeighborDnaList();
        if (entitiesList == null || entitiesList.isEmpty()) return null;

        // Tri alphabétique
        List<String> sortedentitiesList = new ArrayList<>(entitiesList);
        sortedentitiesList.sort((id1, id2) -> {
            LivingEntity e1 = DNAScreenRenderer.getEntity(id1);
            LivingEntity e2 = DNAScreenRenderer.getEntity(id2);

            String name1 = DNAScreenRenderer.getDisplayName(id1, e1);
            String name2 = DNAScreenRenderer.getDisplayName(id2, e2);

            return name1.compareToIgnoreCase(name2);
        });

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int listX = x - 75;
        int listY = y + 8;

        double relativeY = mouseY - listY + scrollOffset;
        if (mouseX < listX || mouseX > listX + CARD_WIDTH) return null;
        if (mouseY < listY || mouseY > listY + VISIBLE_HEIGHT) return null;

        int index = (int) (relativeY / (CARD_HEIGHT + 2));

        if (index >= 0 && index < sortedentitiesList.size()) {
            return sortedentitiesList.get(index);
        }
        return null;
    }

    private void updatePossibleResults() {
        possibleResults.clear();
        String p1 = menu.getParent1();
        String p2 = menu.getParent2();

        if (p1.isEmpty() || p2.isEmpty()) return;

        // Crossbreeding
        for (SheepCrossbreeding recipe : ConfigDNACrossbreedingManager.getSheepCrossbreeding()) {
            if ((recipe.MomId().equals(p1) && recipe.DadId().equals(p2)) ||
                    (recipe.MomId().equals(p2) && recipe.DadId().equals(p1))) {
                addIdSafe(recipe.ChildId());
                for (String failId : recipe.ResultsIfFail()) {
                    addIdSafe(failId);
                }
            }
        }

        // Mutation
        String p1Clean = p1.replace(ResourcefulSheepMod.MOD_ID + ":", "");
        String p2Clean = p2.replace(ResourcefulSheepMod.MOD_ID + ":", "");

        for (SheepMutation mutation : ConfigSheepMutationManager.getSheepMutations()) {
            if ((mutation.MomId().equals(p1Clean) && mutation.DadId().equals(p2Clean)) ||
                    (mutation.MomId().equals(p2Clean) && mutation.DadId().equals(p1Clean))) {
                addIdSafe(ResourcefulSheepMod.MOD_ID + ":" + mutation.ChildId());
            }
        }

        // Si aucune recette trouvée et pour le 50/50
        addIdSafe(p1);
        addIdSafe(p2);
    }

    private void addIdSafe(String id) {
        if (id == null || id.isEmpty()) return;

        String fullId = id;
        if (!id.contains(":")) {
            fullId = ResourcefulSheepMod.MOD_ID + ":" + id;
        }

        if (!possibleResults.contains(fullId)) {
            possibleResults.add(fullId);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isHoveringPanel) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        List<String> dnaList = this.menu.getNeighborDnaList();
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
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int textWidth = this.font.width(this.title);
        guiGraphics.drawString(this.font, this.title, (this.imageWidth - textWidth) / 2, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX + 63, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return mouseX < guiLeft - 83 || mouseY < guiTop || mouseX >= guiLeft + this.imageWidth || mouseY >= guiTop + this.imageHeight;
    }
}