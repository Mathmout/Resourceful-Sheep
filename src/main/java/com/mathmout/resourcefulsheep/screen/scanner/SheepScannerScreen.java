package com.mathmout.resourcefulsheep.screen.scanner;

import com.mathmout.resourcefulsheep.Config;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.jei.JEIUtilitiesMethodes;
import com.mathmout.resourcefulsheep.screen.DNAScreenRenderer;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SheepScannerScreen extends AbstractContainerScreen<SheepScannerMenu> {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/sheep_scanner_background.png");

    private static final ResourceLocation WIDGETS =
            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");

    private float scrollOffset = 0;
    private static final int SCROLL_Y_START = 190;
    private static final int SCROLL_Y_END = 230;

    public SheepScannerScreen(SheepScannerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 262;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        // Désactive les titres d'inventaire par défaut, car ce n'est pas un coffre
        this.titleLabelX = 1000;
        this.inventoryLabelX = 1000;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Background
        guiGraphics.blit(
                BACKGROUND,
                x, y,
                176, 262, // Taille dessinée sur l'écran
                0.0F, 0.0F,    // Coordonnées de départ (U, V)
                176, 262,      // Taille de la zone lue sur le fichier image
                512, 512              // Taille totale de ton fichier sheep_scanner_background.png
        );

        // Energy
        if (Config.SHEEP_SCANNER_CONSUMPTION.get() != 0) {
            int stored = menu.getEnergy();
            int max = menu.getMaxEnergy();
            int barHeight = 144;
            int scaledHeight = Math.min((int) (((float) stored / max) * barHeight), barHeight);
            int yOffset = barHeight - scaledHeight;

            int barWidth = 16;

                // La méthode complète qui empêche l'étirement à 157 pixels :
                guiGraphics.blit(
                        WIDGETS,
                        x + 152, y + 40,    // Coordonnées X, Y sur ton écran
                        16, barHeight,            // La taille que ça doit faire sur l'écran (max 16x144)
                        17, 80,            // Coordonnées U, V où commencer à lire dans widgets.png
                        barWidth, barHeight,      // La taille de la zone à lire dans widgets.png
                        256, 256                  // La taille totale et réelle de ton fichier widgets.png
                );

                guiGraphics.blit(
                        WIDGETS,
                        x + 152, y + 40 + yOffset,
                        16, scaledHeight,
                        0, 80 + yOffset,
                        barWidth, scaledHeight,
                        256, 256
                );
            }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Energy tooltip
        if (Config.SHEEP_SCANNER_CONSUMPTION.get() != 0) {
            if (isHovering(152, 40, 16, 144, mouseX, mouseY)) {
                DNAScreenRenderer.renderEnergyValue(guiGraphics, font, menu.getMaxEnergy(), menu.getEnergy(), mouseX, mouseY);
            }
        }


        // --- RECTANGLE 1 : Titre ---
        guiGraphics.drawCenteredString(font, Component.literal("Sheep Scanner").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD), x + imageWidth / 2, y + 43, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, Component.literal("Scan result").withStyle(ChatFormatting.GRAY), x + imageWidth / 2, y + 53, 0xFFFFFF);

        String sheepId = menu.getScannedSheepId();
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(sheepId);

        if (variant != null) {

            // --- RECTANGLE 2 : Rendu 3D ---
            JEIUtilitiesMethodes.drawEntity(guiGraphics, sheepId, x + imageWidth / 2, y + 130, 40);

            // --- RECTANGLE 3 : Informations ---
            String sheepName = TexteUtils.stringToText(variant.Name() + " Sheep");
            Component nameComponent = Component.literal(sheepName).withStyle(ChatFormatting.BLUE);

            int textWidth = font.width(nameComponent);
            int maxWidth = 90;
            float scale = 1.0f;

            guiGraphics.pose().pushPose();

            // Si le texte est plus grand que le cadre, on calcule le ratio de réduction
            if (textWidth > maxWidth) {
                scale = (float) maxWidth / textWidth;
            }

            // On translate le point de départ au centre voulu
            guiGraphics.pose().translate(x + imageWidth / 2.0f, y + 150, 0.0f);
            guiGraphics.pose().scale(scale, scale, 1.0f); // On applique l'échelle

            // Le texte est maintenant dessiné en (0, 0) par rapport à notre nouvelle origine
            guiGraphics.drawCenteredString(font, nameComponent, 0, 0, 0xFFFFFF);

            guiGraphics.pose().popPose();

            guiGraphics.drawCenteredString(font, Component.literal("Tier: ").withStyle(ChatFormatting.RED)
                    .append(Component.literal(String.valueOf(variant.Tier())).withStyle(ChatFormatting.LIGHT_PURPLE)), x + imageWidth / 2, y + 160, 0xFFFFFF);

            // --- RECTANGLE 4 : Liste des Drops ---
            guiGraphics.drawCenteredString(font, Component.literal("Drops").withStyle(ChatFormatting.GOLD), x + imageWidth / 2, y + 180, 0xFFFFFF);

            List<SheepVariantData.DroppedItems> drops = variant.DroppedItems();
            if (drops != null && !drops.isEmpty()) {
                int itemX = x + 50;
                // On applique le décalage du scroll sur le Y de départ (+2 pour un léger padding)
                int itemY = y + SCROLL_Y_START + 2 - (int) this.scrollOffset;

                Lighting.setupFor3DItems();

                // On prépare une variable pour l'infobulle
                ItemStack hoveredStack = null;

                // 1. ON ACTIVE LA COUPURE STRICTE (Scissor)
                guiGraphics.enableScissor(x + 41, y + SCROLL_Y_START, x + 136, y + SCROLL_Y_END);

                for (SheepVariantData.DroppedItems drop : drops) {
                    ResourceLocation itemLoc = ResourceLocation.tryParse(drop.ItemId());
                    if (itemLoc != null) {
                        Item item = BuiltInRegistries.ITEM.get(itemLoc);
                        if (item != Items.AIR) {
                            ItemStack stack = new ItemStack(item);
                            String amountText = drop.MinDrops() == drop.MaxDrops() ?
                                    String.valueOf(drop.MinDrops()) : drop.MinDrops() + " - " + drop.MaxDrops();

                            // Le jeu ne dessinera QUE les pixels compris dans la zone de Scissor
                            guiGraphics.renderItem(stack, itemX, itemY);
                            guiGraphics.drawCenteredString(font, Component.literal(amountText).withStyle(ChatFormatting.DARK_AQUA), x + imageWidth / 2, itemY + 4, 0xFFFFFF);

                            // Détection du survol : on s'assure que la souris est bien dans la zone VISIBLE
                            if (isHovering(itemX - x, itemY - y, 16, 16, mouseX, mouseY)) {
                                if (mouseY >= y + SCROLL_Y_START && mouseY <= y + SCROLL_Y_END) {
                                    hoveredStack = stack;
                                }
                            }

                            // On espace pour la ligne suivante
                            itemY += 18;
                        }
                    }
                }

                // 2. ON DÉSACTIVE LA COUPURE
                guiGraphics.disableScissor();

                // 3. On dessine l'infobulle (Tooltip) librement, par-dessus tout le reste !
                if (hoveredStack != null) {
                    guiGraphics.renderTooltip(font, hoveredStack, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        String sheepId = menu.getScannedSheepId();
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(sheepId);

        if (variant != null && variant.DroppedItems() != null) {
            List<SheepVariantData.DroppedItems> drops = variant.DroppedItems();
            int maxVisibleHeight = SCROLL_Y_END - SCROLL_Y_START;
            int totalListHeight = drops.size() * 18; // 18 pixels d'espacement par item

            // Si la liste est plus grande que la zone, on active le scroll
            if (totalListHeight > maxVisibleHeight) {
                int x = (width - imageWidth) / 2;
                int y = (height - imageHeight) / 2;

                // On vérifie que la souris est bien dans la zone du rectangle
                if (mouseX >= x + 41 && mouseX <= x + 136 && mouseY >= y + SCROLL_Y_START && mouseY <= y + SCROLL_Y_END) {
                    float scrollSpeed = 12.0f; // Vitesse de défilement
                    this.scrollOffset -= (float) (scrollY * scrollSpeed);

                    float maxScroll = totalListHeight - maxVisibleHeight;
                    if (this.scrollOffset < 0) this.scrollOffset = 0;
                    if (this.scrollOffset > maxScroll) this.scrollOffset = maxScroll;

                    return true; // On indique au jeu qu'on a intercepté le scroll
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}