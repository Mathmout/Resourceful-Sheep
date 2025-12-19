package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mojang.blaze3d.platform.Lighting;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class SheepFeedingCategory implements IRecipeCategory<SheepVariantData> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_feeding");
    private final IDrawable icon;

    public SheepFeedingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.WHEAT));
    }

    @Override
    public @NotNull RecipeType<SheepVariantData> getRecipeType() {
        return JEIResourcefulSheepModPlugin.FEEDING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_feeding");
    }

    @Override
    public @Nullable IDrawable getIcon() {
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

        Item Egg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.Id() + "_spawn_egg"));

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(Egg));
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(Egg));

        int startX = 60;
        int startY = 10;
        int slotSize = 18;
        int columns = 7;
        List<String> foodItems = recipe.FoodItems();

        // 1. Aucune nourriture définie dans le JSON donc Comportement par défaut.
        if (foodItems == null || foodItems.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, startX, startY)
                    .addItemStack(new ItemStack(Items.WHEAT));
            return;
        }

        // 2.
        int i = 0;
        for (String foodId : foodItems) {
            List<ItemStack> stacksToAdd = new ArrayList<>();

            if (foodId.startsWith("#")) {
                try {
                    ResourceLocation tagLoc = ResourceLocation.parse(foodId.substring(1));
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);
                    var tagWrapper = BuiltInRegistries.ITEM.getTag(tagKey);
                    tagWrapper.ifPresent(holders -> StreamSupport.stream(holders.spliterator(), false)
                            .map(Holder::value)
                            .forEach(item -> stacksToAdd.add(new ItemStack(item))));
                } catch (Exception ignored) {

                }
            } else {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(foodId));
                if (item != Items.AIR) {
                    stacksToAdd.add(new ItemStack(item));
                }
            }

            // Création des slots.
            if (!stacksToAdd.isEmpty()) {
                int x = startX + (i % columns) * slotSize;
                int y = startY + (i / columns) * slotSize;

                // Si on dépasse la hauteur, on arrête d'afficher.
                if (y + slotSize <= getHeight()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                            .addItemStacks(stacksToAdd);
                    i++;
                }
            }
        }
    }

    @Override
    public void draw(@NotNull SheepVariantData recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;
        int sheepX = 30;

        JEIUtilitiesMethodes.drawSheep(guiGraphics, recipe.Id(), sheepX, baseY + 3 * scale / 4, scale);

        List<Component> tips = new ArrayList<>();
        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, sheepX - scale, baseY - scale, sheepX + scale, baseY + scale)) {
            JEIUtilitiesMethodes.addTooltip(tips, recipe.Id());
        }

        if (!tips.isEmpty()) {
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
        }
        guiGraphics.drawString(Minecraft.getInstance().font, "Eats:", 60, 2, 0xFF404040, false);
        Lighting.setupForFlatItems();
    }
}
