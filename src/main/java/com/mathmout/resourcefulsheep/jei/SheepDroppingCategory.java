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
        Item droppedItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipe.DroppedItem()));
        ItemStack droppedItemStack = new ItemStack(droppedItem);

        var sheepEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.Id() + "_spawn_egg"));
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(sheepEgg));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 3 * getWidth() / 4, getHeight()/2 - 9/2).addItemStack(droppedItemStack);

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(droppedItemStack);
    }

    @Override
    public void draw(SheepVariantData recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics g, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;
        int sheepX = getWidth() / 4;

        JEIUtilitiesMethodes.drawSheep(g, recipe.Id(), sheepX, baseY + 3 * scale/4, scale);

        g.drawString(Minecraft.getInstance().font, "→", sheepX * 2, baseY, 0xFF404040, false);

        List<Component> tips = new ArrayList<>();
        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, sheepX - scale, baseY - 3 * scale/4,sheepX + scale, baseY + 3 * scale/4)) {
            JEIUtilitiesMethodes.addTooltip(tips, recipe.Id());
        }

        if (!tips.isEmpty()) {
            g.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
        }
        Lighting.setupForFlatItems();
    }
}
