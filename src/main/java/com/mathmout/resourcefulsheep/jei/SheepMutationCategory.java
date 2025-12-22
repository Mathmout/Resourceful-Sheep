package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.item.ModItems;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SheepMutationCategory implements IRecipeCategory<SheepMutation> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_mutations");
    private final IDrawable icon;


    public SheepMutationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.LASSO.get()));
    }

    @Override
    public @NotNull RecipeType<SheepMutation> getRecipeType() {
        return JEIResourcefulSheepModPlugin.MUTATION_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe."+ ResourcefulSheepMod.MOD_ID +".sheep_mutations");
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepMutation recipe, @NotNull IFocusGroup focuses) {
        Item momEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.MomId() + "_spawn_egg"));
        Item dadEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.DadId() + "_spawn_egg"));
        Item childEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.ChildId() + "_spawn_egg"));

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(momEgg));
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(dadEgg));
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(childEgg));
    }

    @Override
    public void draw(SheepMutation recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics g, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;
        int momX = getWidth() / 6;
        int dadX = getWidth() / 2;
        int childX = 5 * getWidth() / 6;

        int plusX = (momX + dadX) / 2;
        int eqX = (dadX + childX) / 2;
        g.drawString(Minecraft.getInstance().font, "+", plusX, baseY, 0xFF404040, false);
        g.drawString(Minecraft.getInstance().font, "=", eqX, baseY, 0xFF404040, false);

        JEIUtilitiesMethodes.drawSheep(g, recipe.MomId(), momX, baseY + 3 * scale / 4, scale);
        JEIUtilitiesMethodes.drawSheep(g, recipe.DadId(), dadX, baseY + 3 * scale / 4, scale);
        JEIUtilitiesMethodes.drawSheep(g, recipe.ChildId(), childX, baseY + 3 * scale / 4, scale);

        g.drawString(Minecraft.getInstance().font, "Chance of success : " + recipe.Chance() + " %",
                ((childX + momX) - Minecraft.getInstance().font.width("Chance of success : " + recipe.Chance() + " %")) / 2,
                4, 0xFF404040, false);

        List<Component> tips = new ArrayList<>();
        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, momX - scale, baseY - scale, momX + scale, baseY + scale))
            JEIUtilitiesMethodes.addTooltip(tips, recipe.MomId());

        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, dadX - scale, baseY - scale, dadX + scale, baseY + scale))
            JEIUtilitiesMethodes.addTooltip(tips, recipe.DadId());

        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, childX - scale, baseY - scale, childX + scale, baseY + scale))
            JEIUtilitiesMethodes.addTooltip(tips, recipe.ChildId());

        if (!tips.isEmpty()) g.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
    }
}
