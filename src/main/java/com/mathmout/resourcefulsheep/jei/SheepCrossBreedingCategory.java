package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.config.dnacrossbreeding.SheepCrossbreeding;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.mathmout.resourcefulsheep.jei.JEIResourcefulSheepModPlugin.CROSS_BREADING_TYPE;

public class SheepCrossBreedingCategory implements IRecipeCategory<SheepCrossbreeding> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_cross_breeding");
    private final IDrawable icon;

    public SheepCrossBreedingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.DNA_SPLICER.get()));
    }

    @Override
    public @NotNull RecipeType<SheepCrossbreeding> getRecipeType() {
        return CROSS_BREADING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_cross_breeding");
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
        return  180;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepCrossbreeding recipe, @NotNull IFocusGroup focuses) {
        Item momEgg = getEggItem(recipe.MomId());
        Item dadEgg = getEggItem(recipe.DadId());
        Item childEgg = getEggItem(recipe.ChildId());

        if (momEgg != Items.AIR) builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(momEgg));
        if (dadEgg != Items.AIR) builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(dadEgg));
        if (childEgg != Items.AIR) builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(childEgg));
    }

    private Item getEggItem(String entityId) {
        String eggId = entityId.contains(":") ? entityId + "_spawn_egg" : ResourcefulSheepMod.MOD_ID + ":" + entityId + "_spawn_egg";
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(eggId));
    }

    @Override
    public void draw(@NotNull SheepCrossbreeding recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;
        int momX = getWidth() / 6;
        int dadX = getWidth() / 2;
        int childX = 5 * getWidth() / 6;

        int plusX = (momX + dadX) / 2;
        int eqX = (dadX + childX) / 2;
        guiGraphics.drawString(Minecraft.getInstance().font, "+", plusX, baseY, 0xFF404040, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "=", eqX, baseY, 0xFF404040, false);

        // Utilisation de la NOUVELLE méthode drawEntity !
        JEIUtilitiesMethodes.drawEntity(guiGraphics, recipe.MomId(), momX, baseY + 3 * scale / 4, scale);
        JEIUtilitiesMethodes.drawEntity(guiGraphics, recipe.DadId(), dadX, baseY + 3 * scale / 4, scale);
        JEIUtilitiesMethodes.drawEntity(guiGraphics, recipe.ChildId(), childX, baseY + 3 * scale / 4, scale);

        guiGraphics.drawString(Minecraft.getInstance().font, "Chance of success : " + recipe.Chance() + " %",
                ((childX + momX) - Minecraft.getInstance().font.width("Chance of success : " + recipe.Chance() + " %")) / 2,
                4, 0xFF404040, false);

        List<Component> tips = new ArrayList<>();
        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, momX - scale, baseY - scale, momX + scale, baseY + scale))
            JEIUtilitiesMethodes.addTooltip(tips, recipe.MomId());

        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, dadX - scale, baseY - scale, dadX + scale, baseY + scale))
            JEIUtilitiesMethodes.addTooltip(tips, recipe.DadId());

        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, childX - scale, baseY - scale, childX + scale, baseY + scale))
            JEIUtilitiesMethodes.addTooltip(tips, recipe.ChildId());

        if (!tips.isEmpty()) guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
    }
}
