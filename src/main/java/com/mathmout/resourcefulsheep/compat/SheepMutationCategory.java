package com.mathmout.resourcefulsheep.compat;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.event.ModEvents;
import com.mathmout.resourcefulsheep.item.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheepMutationCategory implements IRecipeCategory<SheepMutation> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "sheep_mutation");
    private final IDrawable background;
    private final IDrawable icon;

    private final Map<String, ResourcefulSheepEntity> sheepCache = new HashMap<>();

    private @Nullable ResourcefulSheepEntity getSheep(String sheepId) {
        if (Minecraft.getInstance().level == null) return null;
        var holder = ModEntities.SHEEP_ENTITIES.get(sheepId);
        if (holder == null) return null;
        return sheepCache.computeIfAbsent(sheepId, id -> {
            EntityType<?> t = holder.get();
            return (ResourcefulSheepEntity) t.create(Minecraft.getInstance().level);
        });
    }

    public SheepMutationCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(getWidth(), getHeight());
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.LASSO.get()));
    }

    @Override
    public @NotNull RecipeType<SheepMutation> getRecipeType() {
        return JEIResourcefulSheepModPlugin.MUTATION_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("recipe.resourceful_sheep.mutation");
    }

    public @NotNull IDrawable getBackground() {
        return background;
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
        // eggs pour l’index JEI, mais non dessinés
        Item momEgg   = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.MomId   + "_spawn_egg"));
        Item dadEgg   = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.DadId   + "_spawn_egg"));
        Item childEgg = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, recipe.Child   + "_spawn_egg"));

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(momEgg));
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(dadEgg));
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(childEgg));
    }


    @Override
    public void draw(SheepMutation recipe, @NotNull IRecipeSlotsView slots, @NotNull GuiGraphics g, double mouseX, double mouseY) {
        // Placement de moutons et leur taille.
        int scale = 22;
        int baseY = getHeight()/2;
        int momX = getWidth()/6;
        int dadX = getWidth()/2;
        int childX = 5*getWidth()/6;

        // Symboles + et =.
        int plusX = (momX + dadX) / 2;
        int eqX   = (dadX + childX) / 2;
        g.drawString(Minecraft.getInstance().font, "+", plusX, baseY, 0xFF404040, false);
        g.drawString(Minecraft.getInstance().font, "=",   eqX, baseY, 0xFF404040, false);

        // Dessine les moutons.
        drawSheep(g, recipe.MomId, momX,   baseY + scale, scale);
        drawSheep(g, recipe.DadId, dadX,   baseY + scale, scale);
        drawSheep(g, recipe.Child, childX, baseY + scale, scale);

        // Affiche le % de chance.
        g.drawString(Minecraft.getInstance().font, "Chance of succes : " + recipe.Chance + " %",
                ((childX + momX) - Minecraft.getInstance().font.width("Chance of succes : " + recipe.Chance + " %"))/2,
                baseY - scale, 0xFF404040, false);

        // tooltips : zones compactes autour des modèles (calées sur l’alignement)
        List<Component> tips = new ArrayList<>();
        if (isMouseOver(mouseX, mouseY, momX   - 18, baseY - 10, momX   + 18, baseY + 28))
            addTooltip(tips, recipe.MomId);

        if (isMouseOver(mouseX, mouseY, dadX   - 18, baseY - 10, dadX   + 18, baseY + 28))
            addTooltip(tips, recipe.DadId);

        if (isMouseOver(mouseX, mouseY, childX - 18, baseY - 10, childX + 18, baseY + 28))
            addTooltip(tips, recipe.Child);

        if (!tips.isEmpty()) g.renderComponentTooltip(Minecraft.getInstance().font, tips, (int) mouseX, (int) mouseY);
    }

    private void drawSheep(GuiGraphics g, String sheepId, int x, int y, float scale) {
        var sheep = getSheep(sheepId);
        if (sheep == null) return;

        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, y, 50);
        ps.scale(scale, scale, scale);

        // correction "tête en bas" → rotation Z 180°
        Quaternionf rot = new Quaternionf()
                .rotateZ((float) Math.PI)                              // 180°
                .rotateY((float) Math.toRadians((float) -40.0))              // vue 3/4
                .rotateX((float) Math.toRadians((float) 5));             // légère plongée
        ps.mulPose(rot);

        EntityRenderDispatcher ed = Minecraft.getInstance().getEntityRenderDispatcher();
        var buf = Minecraft.getInstance().renderBuffers().bufferSource();

        RenderSystem.setShaderLights(new Vector3f(0f, 1f, 1f), new Vector3f(0f, 0f, 0f));
        ed.setRenderShadow(false);
        ed.render(sheep, 0, 0, 0, 0, 0f, ps, buf, 15728880); // ← partialTicks = 0f : plus de tremblement
        ed.setRenderShadow(true);
        buf.endBatch();
        ps.popPose();
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
    }

    private void addTooltip(List<Component> tips, String sheepId) {
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(sheepId);
        if (variant == null) return;

        String resourceText;

        resourceText = ModEvents.itemIdToText(variant.Resource);

        MutableComponent lineRes = Component.literal("Resource : ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(resourceText).withStyle(ChatFormatting.YELLOW));
        MutableComponent lineTier = Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(variant.Tier)).withStyle(ChatFormatting.LIGHT_PURPLE));

        tips.add(lineRes);
        tips.add(lineTier);
    }

}