package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SheepDroppingCategory implements IRecipeCategory<SheepVariantData> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_dropping");
    private final IDrawable icon;

    public SheepDroppingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SHEARS));
    }

    @Override
    public @NotNull RecipeType<SheepVariantData> getRecipeType() {
        return JEIResourcefulSheepModPlugin.DROPPING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_dropping");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepVariantData recipe, @NotNull IFocusGroup focuses) {

        Item sheepEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.Id() + "_spawn_egg"));
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(sheepEgg));

        int startX = 60;
        int startY = 18;
        int slotSize = 18;
        int columns = 7;
        List<SheepVariantData.DroppedItems> DroppedItemsData = recipe.DroppedItems();

        if (DroppedItemsData == null || DroppedItemsData.isEmpty()) {
            return;
        }

        int i = 0;
        for (SheepVariantData.DroppedItems droppedItems : DroppedItemsData) {
            List<ItemStack> stacksToAdd = new ArrayList<>();

            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(droppedItems.ItemId()));
            if (item != Items.AIR) {
                ItemStack stack = new ItemStack(item);
                int average = (droppedItems.MinDrops() + droppedItems.MaxDrops()) / 2;
                if (average == 0) average = 1;
                stack.setCount(average);
                stacksToAdd.add(stack);
            }

            // Slots.
            if (!stacksToAdd.isEmpty()) {
                int x = startX + (i % columns) * slotSize;
                int y = startY + (i / columns) * slotSize;

                if (y + slotSize <= getHeight()) {
                    if (droppedItems.MinDrops() == droppedItems.MaxDrops() && droppedItems.MinDrops() > 0) {
                        builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                                .addItemStacks(stacksToAdd).addRichTooltipCallback((recipeSlotView, tooltip)
                                        -> tooltip.add(Component.literal("Amount : " + droppedItems.MinDrops())
                                        .withStyle(ChatFormatting.DARK_AQUA)));
                        i++;
                    } else {
                        builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                                .addItemStacks(stacksToAdd).addRichTooltipCallback((recipeSlotView, tooltip)
                                        -> tooltip.add(Component.literal("Amount : " + droppedItems.MinDrops() + " to " + droppedItems.MaxDrops())
                                        .withStyle(ChatFormatting.DARK_AQUA)));
                    }
                    i++;
                }
            }
        }
    }

    @Override
    public void draw(SheepVariantData recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;
        int sheepX = getWidth() / 6;

        // Dessiner le mouton
        JEIUtilitiesMethodes.drawSheep(guiGraphics, recipe.Id(), sheepX, baseY + 3 * scale / 4, scale);

        // Tooltip mouton
        List<Component> tips = new ArrayList<>();
        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, sheepX - scale, baseY - scale, sheepX + scale, baseY + scale)) {
            JEIUtilitiesMethodes.addTooltip(tips, recipe.Id());
        }
        if (!tips.isEmpty()) {
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
        }

        String text = "Average Drops :";

        if (recipe.DroppedItems() != null) {
            for (SheepVariantData.DroppedItems drop : recipe.DroppedItems()) {
                if ("minecraft:air".equals(drop.ItemId())) {
                    text = "Drops nothing.";
                    break;
                }
            }
        }

        guiGraphics.drawString(Minecraft.getInstance().font, text,
                (getWidth() - Minecraft.getInstance().font.width(text)) / 2, 4, 0xFF404040, false);
        Lighting.setupForFlatItems();
    }
}
