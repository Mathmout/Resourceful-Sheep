package com.mathmout.resourcefulsheep.compat;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;
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

public class SheepSpawningCategory implements IRecipeCategory<SheepSpawningRecipeWrapper> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_spawning");
    private final IDrawable background;
    private final IDrawable icon;

    public SheepSpawningCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 120);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.SHEEP_SCANNER.get()));
    }

    @Override
    public @NotNull RecipeType<SheepSpawningRecipeWrapper> getRecipeType() {
        return JEIResourcefulSheepModPlugin.SPAWNING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe."+ ResourcefulSheepMod.MOD_ID +".sheep_spawning");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepSpawningRecipeWrapper recipe, @NotNull IFocusGroup focuses) {
        Item egg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.originalData().sheepId() + "_spawn_egg"));
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(egg));
    }

    @Override
    public void draw(SheepSpawningRecipeWrapper recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics g, double mouseX, double mouseY) {
        SheepSpawningData spawnData = recipe.originalData();
        int scale = 22;
        int sheepX = getWidth() / 6;

        SheepMutationCategory.drawSheep(g, spawnData.sheepId(), sheepX, sheepX + scale/2, scale);

        List<Component> tips = new ArrayList<>();
        if (SheepMutationCategory.isMouseOver(mouseX, mouseY, sheepX - scale, sheepX - scale, sheepX + scale, sheepX + scale/2)) {
            SheepMutationCategory.addTooltip(tips, spawnData.sheepId());
        }

        if (!tips.isEmpty()) g.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);

        int textX = (getWidth() + sheepX + scale)/2;
        int textY = sheepX - 10;

        g.drawString(Minecraft.getInstance().font, String.format("About %d sheep every", spawnData.maxNearby()),
                textX - Minecraft.getInstance().font.width(String.format("About %d sheep every", spawnData.maxNearby()))/2,
                textY,
                0xFF404040, false);

        textY += 10;

        g.drawString(Minecraft.getInstance().font, String.format("%d blocks", spawnData.densityRadius()),
                textX - Minecraft.getInstance().font.width(String.format("%d blocks", spawnData.densityRadius()))/2,
                textY,
                0xFF404040, false);

        if (spawnData.biomes().isEmpty()) {
            String texte = "Spawns where the Minecraft";
            g.drawString(Minecraft.getInstance().font, texte,
                    (getWidth() - Minecraft.getInstance().font.width(texte))/2,
                    sheepX + scale/2 + 5,
                    0xFF404040, false);

            texte = "sheep spawns";
            g.drawString(Minecraft.getInstance().font, texte,
                    (getWidth() - Minecraft.getInstance().font.width(texte))/2,
                    sheepX + scale/2 + 15,
                    0xFF404040, false);
        } else {
            String texte = "Spawns in the following biomes:";
            g.drawString(Minecraft.getInstance().font, texte,
                    (getWidth() - Minecraft.getInstance().font.width(texte))/2,
                    sheepX + scale/2 + 5,
                    0xFF404040, false);

            textY = sheepX + scale/2 + 25;

            for (String biomeId : recipe.biomesForPage()) {
                g.drawString(Minecraft.getInstance().font, biomeIdToName(biomeId),
                        (getWidth() - Minecraft.getInstance().font.width("- " + biomeIdToName(biomeId)))/2,
                        textY, 0xFF404040, false);
                textY += 10;
            }
        }
    }

    private String biomeIdToName(String biomeId) {
        ResourceLocation id = ResourceLocation.tryParse(biomeId);
        if (id != null) {
            String translationKey = "biome." + id.getNamespace() + "." + id.getPath();
            return Component.translatable(translationKey).getString();
        }
        return biomeId;
    }
}
