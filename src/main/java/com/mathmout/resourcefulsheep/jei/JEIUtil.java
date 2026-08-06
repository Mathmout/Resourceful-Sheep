package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.custom.ResourcefulWoolBlock;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.screen.ScreenRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class JEIUtil {

    private static String getNamespacedId(String id) {
        return id.contains(":") ? id : ResourcefulSheepMod.MOD_ID + ":" + id;
    }

    public static void drawEntity(GuiGraphics g, String rawEntityId, int x, int y, float baseScale) {
        String entityId = getNamespacedId(rawEntityId);
        LivingEntity entity = ScreenRenderer.getEntity(entityId);
        if (entity == null) return;

        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, y, 50);

        float scale = getEntityScale(entity, baseScale);
        ps.scale(scale, scale, scale);

        Quaternionf rot = new Quaternionf()
                .rotateZ((float) Math.PI)
                .rotateY((float) Math.toRadians(-40.0f))
                .rotateX((float) Math.toRadians(5));
        ps.mulPose(rot);

        EntityRenderDispatcher ed = Minecraft.getInstance().getEntityRenderDispatcher();
        MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();

        RenderSystem.setShaderLights(new Vector3f(0f, 1f, 1f), new Vector3f(0f, 0f, 0f));
        ed.setRenderShadow(false);
        ed.render(entity, 0, 0, 0, 0, 0f, ps, buf, 15728880);
        ed.setRenderShadow(true);
        buf.endBatch();
        ps.popPose();
    }

    private static float getEntityScale(LivingEntity entity, float baseScale) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        float ratio = height / width;
        float targetPixelSize;

        if (ratio <= 0.6f) targetPixelSize = 14;
        else if (ratio >= 1.8f) targetPixelSize = 20;
        else targetPixelSize = 15;

        if (entity.getType() == EntityType.ENDER_DRAGON) targetPixelSize = 45;
        else if (entity.getType() == EntityType.GHAST) targetPixelSize = 12;
        else if (entity instanceof Squid) targetPixelSize = 10;

        float maxDim = Math.max(height, width);

        return (targetPixelSize / 15.0f) * (baseScale / maxDim) * 1.2f;
    }

    public static boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
    }

    public static void addTooltip(List<Component> tips, String rawEntityId) {
        String entityId = getNamespacedId(rawEntityId);
        LivingEntity entity = ScreenRenderer.getEntity(entityId);
        if (entity == null) return;

        tips.add(Component.literal(ScreenRenderer.getDisplayName(entityId, entity).replaceAll("(?i)\\s*Tier\\s*\\d+", "")).withStyle(ChatFormatting.BLUE));

        if (entity instanceof ResourcefulSheepEntity) {
            String path = entityId.split(":")[1];
            SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(path);

            if (variant != null) {
                tips.add(Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(String.valueOf(variant.Tier())).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        }
    }

    public static List<ItemStack> getColoredWools(Item wool) {
        if (wool != Items.AIR) {
            List<ItemStack> woolVariants = new ArrayList<>();
            for (DyeColor color : DyeColor.values()) {
                ItemStack coloredWool = new ItemStack(wool);
                coloredWool.set(
                        DataComponents.BLOCK_STATE,
                        BlockItemStateProperties.EMPTY.with(ResourcefulWoolBlock.COLOR, color)
                );
                woolVariants.add(coloredWool);
            }
            return woolVariants;
        }
        return List.of(new ItemStack(wool));
    }

    public static void addPhantomEntitySlot(IRecipeLayoutBuilder builder, String rawEntityId, int centerX, int centerY, int slotWidth, int slotHeight) {
        // Déduction de l'ID correct de l'œuf (comme dans SheepCrossBreedingCategory)
        String eggId = rawEntityId.contains(":") ? rawEntityId + "_spawn_egg" : ResourcefulSheepMod.MOD_ID + ":" + rawEntityId + "_spawn_egg";
        Item eggItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(eggId));

        if (eggItem != Items.AIR) {
            // Calcul du coin supérieur gauche du slot
            int slotX = centerX - (slotWidth / 2);
            int slotY = centerY - (slotHeight / 2);

            // Création du slot fantôme
            builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                    .addItemStack(new ItemStack(eggItem))
                    .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                        @Override
                        public void render(@NotNull GuiGraphics guiGraphics, @NotNull ItemStack ingredient) {
                            // Rendu invisible
                        }

                        @Override
                        public @NotNull List<Component> getTooltip(@NotNull ItemStack ingredient, @NotNull TooltipFlag tooltipFlag) {
                            return List.of();
                        }

                        @Override
                        public int getWidth() {
                            return slotWidth;
                        }

                        @Override
                        public int getHeight() {
                            return slotHeight;
                        }
                    }).addRichTooltipCallback((recipeSlotView, tooltip) -> tooltip.clear());
        }
    }
}