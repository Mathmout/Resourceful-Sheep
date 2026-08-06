package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeCategory implements IRecipeCategory<SheepVariantData> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "centrifuge");
    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    private final IDrawable energyBar;
    private final IDrawable energyBarBackground;

    public CentrifugeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CENTRIFUGE_CONTROLLERS.get(CentrifugeTier.ULTIMATE).get()));

        IDrawableStatic arrowStatic = guiHelper.createDrawable(WIDGETS, 32, 41, 12, 10);
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic, 40, IDrawableAnimated.StartDirection.LEFT, false);

        this.energyBarBackground = guiHelper.createDrawable(WIDGETS, 12, 19, 11, 60);
        IDrawableStatic energyFull = guiHelper.createDrawable(WIDGETS, 0, 19, 11, 60);
        this.energyBar = guiHelper.createAnimatedDrawable(energyFull, 100, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public @NotNull RecipeType<SheepVariantData> getRecipeType() {
        return JEIResourcefulSheepModPlugin.CENTRIFUGE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe." + ResourcefulSheepMod.MOD_ID + ".centrifuge");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getHeight() {
        return 76;
    }

    @Override
    public int getWidth() {
        return 188;
    }

    private CentrifugeTier getRequiredTier(int items, int fluids) {
        if (items <= 3 && fluids == 0) return CentrifugeTier.BASIC;
        if (items <= 6 && fluids <= 1) return CentrifugeTier.ADVANCED;
        if (items <= 10 && fluids <= 2) return CentrifugeTier.ELITE;
        return CentrifugeTier.ULTIMATE;
    }

    // --- LOGIQUE DE CALCUL DU LAYOUT DYNAMIQUE ---
    private record LayoutData(int inputX, int inputY,
                              int arrowX, int arrowY,
                              int outX, int outY,
                              int tankX, int tankY,
                              int energyX, int energyY,
                              int rows, int totalItems, int totalFluids,
                              CentrifugeTier minTier) {
    }

    private LayoutData calculateLayout(SheepVariantData recipe) {
        int totalItems = 1; // 1 de base pour la laine vanilla
        int totalFluids = 0;

        List<SheepVariantData.DroppedItems> drops = recipe.DroppedItems();
        if (drops != null) {
            for (SheepVariantData.DroppedItems drop : drops) {
                ResourceLocation itemLoc = ResourceLocation.tryParse(drop.ItemId());
                if (itemLoc != null) {
                    if (BuiltInRegistries.FLUID.containsKey(itemLoc)) totalFluids++;
                    else if (BuiltInRegistries.ITEM.containsKey(itemLoc)) totalItems++;
                }
            }
        }

        int tankCount = Math.min(3, totalFluids);
        int tankWidth = tankCount > 0 ? (tankCount * 22) : 0;

        // Fixation à 4 colonnes pour l'inventaire
        int actualItemCols = 4;
        int rows = (int) Math.ceil((double) totalItems / actualItemCols);
        int itemGridWidth = actualItemCols * 18;
        int itemGridHeight = rows * 18;

        // Largeur totale du contenu pour le centrage global (plus besoin de compter la barre d'énergie)
        int contentWidth = 18 + 4 + 9 + 8 + itemGridWidth + (tankCount > 0 ? 8 + tankWidth : 0);
        int startX = (getWidth() - contentWidth) / 2;

        int inputY = (getHeight() - 18) / 2;
        int arrowX = startX + 18 + 3;
        int arrowY = (getHeight() - 12) / 2;
        int outX = arrowX + 9 + 8;
        int outY = (getHeight() - itemGridHeight) / 2;
        int tankX = outX + itemGridWidth + 8;
        int tankY = (getHeight() - 62) / 2;
        int energyX = (startX - 11) / 2;
        int energyY = (getHeight() - 60) / 2;

        return new LayoutData(startX, inputY,
                arrowX, arrowY,
                outX, outY,
                tankX, tankY,
                energyX, energyY,
                rows, totalItems, totalFluids,
                getRequiredTier(totalItems, totalFluids));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepVariantData recipe, @NotNull IFocusGroup focuses) {
        LayoutData layout = calculateLayout(recipe);

        // --- SLOT D'ENTRÉE ---
        ResourceLocation woolId = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.Id() + "_wool");
        Item woolItem = BuiltInRegistries.ITEM.get(woolId);

        builder.addSlot(RecipeIngredientRole.INPUT, layout.inputX, layout.inputY).addItemStacks(JEIUtil.getColoredWools(woolItem));

        // --- SORTIE : LAINE VANILLA ---
        List<ItemStack> vanillaWools = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            vanillaWools.add(new ItemStack(getWoolItemFromColor(color)));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, layout.outX, layout.outY).addItemStacks(vanillaWools);

        // --- MACHINE MINIMALE REQUISE (Indicateur visuel) ---
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, layout.arrowX - 3, layout.arrowY + 16)
                .addItemStack(new ItemStack(ModBlocks.CENTRIFUGE_CONTROLLERS.get(layout.minTier).get()))
                .addRichTooltipCallback((recipeSlotView, tooltip) ->
                        tooltip.add(Component.literal("Minimum required tier.").withStyle(ChatFormatting.GRAY)));

        List<SheepVariantData.DroppedItems> droppedItemsData = recipe.DroppedItems();
        if (droppedItemsData == null || droppedItemsData.isEmpty()) return;

        int itemIndex = 1;
        int fluidIndex = 0;

        for (SheepVariantData.DroppedItems dropData : droppedItemsData) {
            ResourceLocation itemLoc = ResourceLocation.tryParse(dropData.ItemId());
            if (itemLoc == null) continue;

            int average = (dropData.MinDrops() + dropData.MaxDrops()) / 2;
            if (average <= 0) average = 1;

            if (BuiltInRegistries.FLUID.containsKey(itemLoc)) {
                FluidStack fluidStack = new FluidStack(BuiltInRegistries.FLUID.get(itemLoc), average);
                int px = layout.tankX + (fluidIndex * 22) + 2;
                int py = layout.tankY + 2;

                IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, px, py)
                        .addIngredient(NeoForgeTypes.FLUID_STACK, fluidStack)
                        .setFluidRenderer(CentrifugeTier.ULTIMATE.getFluidCapacity(), false, 14, 58);

                addDropTooltip(slot, dropData, true);
                fluidIndex++;
            }
            else if (BuiltInRegistries.ITEM.containsKey(itemLoc)) {
                Item item = BuiltInRegistries.ITEM.get(itemLoc);
                if (item != Items.AIR) {
                    ItemStack stack = new ItemStack(item);
                    stack.setCount(average);

                    int col = itemIndex % 4; // Toujours 4 colonnes
                    int row = itemIndex / 4;
                    int px = layout.outX + (col * 18);
                    int py = layout.outY + (row * 18);

                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, px, py)
                            .addItemStack(stack);

                    addDropTooltip(slot, dropData, false);
                    itemIndex++;
                }
            }
        }
    }

    private void addDropTooltip(IRecipeSlotBuilder slot, SheepVariantData.DroppedItems dropData, boolean isFluid) {
        String unit = isFluid ? " mB" : "";
        if (dropData.MinDrops() == dropData.MaxDrops()) {
            slot.addRichTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(Component.literal("Amount : " + dropData.MinDrops() + unit)
                            .withStyle(ChatFormatting.DARK_AQUA)));
        } else {
            slot.addRichTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(Component.literal("Amount : " + dropData.MinDrops() + " to " + dropData.MaxDrops() + unit)
                            .withStyle(ChatFormatting.DARK_AQUA)));
        }
    }

    @Override
    public void draw(@NotNull SheepVariantData recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        LayoutData layout = calculateLayout(recipe);

        // Dessin du fond vide de la flèche et de la flèche animée
        guiGraphics.blit(WIDGETS, layout.arrowX, layout.arrowY, 33, 19, 12, 9);
        this.arrow.draw(guiGraphics, layout.arrowX, layout.arrowY);

        // Fond du slot d'entrée
        guiGraphics.blit(WIDGETS, layout.inputX - 1, layout.inputY - 1, 94, 19, 18, 18);

        // Energy
        if (layout.totalFluids() < 3) {
            this.energyBarBackground.draw(guiGraphics, layout.energyX, layout.energyY);
            this.energyBar.draw(guiGraphics, layout.energyX, layout.energyY);
        }

        // --- GRILLE DYNAMIQUE DES SORTIES (4 COLONNES) ---
        for (int r = 0; r < layout.rows; r++) {
            for (int c = 0; c < 4; c++) {
                int px = layout.outX + (c * 18) - 1;
                int py = layout.outY + (r * 18) - 1;
                int u = 75;
                int v = 141;

                if (layout.rows == 1) {
                    if (c == 0) u = 56;
                    else if (c == 3) u = 94;
                } else {
                    if (r == 0) { // Ligne du haut
                        if (c == 0) u = 56;
                        else if (c == 3) u = 94;
                        v = 103;
                    } else if (r == layout.rows - 1) { // Ligne du bas
                        if (c == 0) u = 56;
                        else if (c == 3) u = 94;
                        v = 122;
                    } else { // Lignes intermédiaires
                        if (c == 0) { u = 37; v = 103; } // Bord gauche
                        else if (c == 3) { u = 37; v = 122; } // Bord droit
                        else { u = 74; v = 19; }
                    }
                }
                guiGraphics.blit(WIDGETS, px, py, u, v, 18, 18);
            }
        }

        // --- RÉSERVOIRS ---
        for (int i = 0; i < layout.totalFluids; i++) {
            int px = layout.tankX + (i * 22);
            // Fond noir du réservoir ajouté avant le contour
            guiGraphics.blit(WIDGETS, px + 2, layout.tankY + 2, 79, 39, 14, 58);
            guiGraphics.blit(WIDGETS, px, layout.tankY, 94, 39, 18, 62);
        }
    }

    private Item getWoolItemFromColor(DyeColor color) {
        return switch (color) {
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
            case WHITE -> Items.WHITE_WOOL;
        };
    }
}