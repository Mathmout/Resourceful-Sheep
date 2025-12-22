package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SheepEatingCategory implements IRecipeCategory<SheepEatingRecipeWrapper> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_eating");
    private final IDrawable icon;

    public SheepEatingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.GRASS_BLOCK));
    }

    @Override
    public @NotNull RecipeType<SheepEatingRecipeWrapper> getRecipeType() {
        return JEIResourcefulSheepModPlugin.EATING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe." + ResourcefulSheepMod.MOD_ID + ".sheep_eating");
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull SheepEatingRecipeWrapper recipe, @NotNull IFocusGroup focuses) {

        Item egg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.variant().Id() + "_spawn_egg"));
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(egg));

        int startX = 60;
        int startY = 18;
        int colWidth = 64;
        int rowHeight = 24;

        List<Map.Entry<String, String>> entries = recipe.pageEntries();

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, String> entry = entries.get(i);
            String inputKey = entry.getKey();

            int col = i % 2;
            int row = i / 2;
            int x = startX + (col * colWidth);
            int y = startY + (row * rowHeight);

            // Block mangé
            List<ItemStack> inputStacks = new ArrayList<>();

            if (inputKey.startsWith("#")) {
                // Tag
                ResourceLocation tagLoc = ResourceLocation.tryParse(inputKey.substring(1));
                if (tagLoc != null) {
                    // On récupère le Tag de block
                    TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, tagLoc);

                    // On cherche tous les blocs du jeu qui ont ce tag
                    Iterable<Holder<Block>> blocks = BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag);

                    // On les convertit en ItemStack pour JEI
                    for (Holder<Block> blockHolder : blocks) {
                        inputStacks.add(new ItemStack(blockHolder.value()));
                    }
                }
            } else {
                // Item
                Item inputItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(inputKey));
                inputStacks.add(new ItemStack(inputItem));
            }
            // On ajoute la liste
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStacks(inputStacks);


            // Block placé.
            if (!entry.getValue().equals("minecraft:air") && !entry.getValue().isEmpty()) {
                Item outputItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.getValue()));
                builder.addSlot(RecipeIngredientRole.OUTPUT, x + 32, y)
                        .addItemStack(new ItemStack(outputItem));
            } else {
                ItemStack barrier = new ItemStack(Items.BARRIER);
                barrier.set(DataComponents.CUSTOM_NAME,
                        Component.literal("Destroys block")
                                .withStyle(ChatFormatting.WHITE)
                                .withStyle(style -> style.withItalic(false)));
                builder.addSlot(RecipeIngredientRole.OUTPUT, x + 32, y)
                        .addItemStack(barrier);
            }
        }
    }

    @Override
    public void draw(@NotNull SheepEatingRecipeWrapper recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int scale = 22;
        int baseY = getHeight() / 2;
        int sheepX = getWidth() / 6;;

        // Dessiner le mouton
        JEIUtilitiesMethodes.drawSheep(guiGraphics, recipe.variant().Id(), sheepX, baseY + 3 * scale / 4, scale);

        // Tooltip mouton
        List<Component> tips = new ArrayList<>();
        if (JEIUtilitiesMethodes.isMouseOver(mouseX, mouseY, sheepX - scale, baseY - scale, sheepX + scale, baseY + scale)) {
            JEIUtilitiesMethodes.addTooltip(tips, recipe.variant().Id());
        }
        if (!tips.isEmpty()) {
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
        }

        // Titre
        guiGraphics.drawString(Minecraft.getInstance().font, "Eats :",
                (getWidth() - Minecraft.getInstance().font.width("Eats :")) / 2, 4, 0xFF404040, false);

        // Dessiner les flèches "→"
        int startX = 60;
        int startY = 18;
        int colWidth = 64;
        int rowHeight = 24;

        for (int i = 0; i < recipe.pageEntries().size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int x = startX + (col * colWidth);
            int y = startY + (row * rowHeight);
            guiGraphics.drawString(Minecraft.getInstance().font, "→", x + 20, y + 4, 0xFF404040, false);
        }
        Lighting.setupForFlatItems();
    }
}
