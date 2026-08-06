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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SheepShearingCategory implements IRecipeCategory<SheepVariantData> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_shearing");
    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "textures/gui/widgets.png");

    private final IDrawable icon;
    private final IDrawable arrow;

    public SheepShearingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.SHEARS));
        this.arrow = guiHelper.createDrawable(WIDGETS, 36, 161, 78, 11);
    }

    @Override
    public @NotNull RecipeType<SheepVariantData> getRecipeType() {
        return JEIResourcefulSheepModPlugin.SHEARING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_shearing");
    }

    @Override
    public @NotNull IDrawable getIcon() {
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

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepVariantData recipe, @NotNull IFocusGroup focuses) {

        JEIUtil.addPhantomEntitySlot(builder, recipe.Id(), 32, getHeight() / 2, 32, 32);

        // --- SLOT DE LA LAINE ---
        ResourceLocation woolId = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.Id() + "_wool");
        Item woolItem = BuiltInRegistries.ITEM.get(woolId);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 147, (getHeight() - 18) / 2).addItemStacks(JEIUtil.getColoredWools(woolItem));

        // --- SLOT DES CISAILLES ---
        List<ItemStack> shears = new ArrayList<>();
        TagKey<Item> shearsTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "shears"));

        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(shearsTag)) {
            shears.add(new ItemStack(holder.value()));
        }

        if (shears.isEmpty()) {
            shears.add(new ItemStack(Items.SHEARS));
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 85, 45).addItemStacks(shears);
    }

    @Override
    public void draw(@NotNull SheepVariantData recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;

        int sheepX = 32;

        // Dessiner le mouton
        JEIUtil.drawEntity(guiGraphics, recipe.Id(), sheepX, baseY + 3 * scale / 4, scale);

        this.arrow.draw(guiGraphics, (getWidth() - 78) / 2, (getHeight() - 11) / 2);

        // Tooltip mouton
        List<Component> tips = new ArrayList<>();
        if (JEIUtil.isMouseOver(mouseX, mouseY, sheepX - scale, baseY - scale, sheepX + scale, baseY + scale)) {
            JEIUtil.addTooltip(tips, recipe.Id());
        }
        if (!tips.isEmpty()) {
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
        }

        Lighting.setupForFlatItems();
    }
}