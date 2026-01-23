package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JEIUtilitiesMethodes {

    private static final Map<String, ResourcefulSheepEntity> sheepCache = new HashMap<>();

    public static void drawSheep(GuiGraphics g, String sheepId, int x, int y, float scale) {
        ResourcefulSheepEntity sheep = getSheep(sheepId);
        if (sheep == null) return;

        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x, y, 50);
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
        ed.render(sheep, 0, 0, 0, 0, 0f, ps, buf, 15728880);
        ed.setRenderShadow(true);
        buf.endBatch();
        ps.popPose();
    }

    public static boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
    }

    public static void addTooltip(List<Component> tips, String sheepId) {
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(sheepId);
        if (variant == null) return;

        String resourceText = TexteUtils.StringToText(variant.Resource());

        MutableComponent lineRes = Component.literal("Resource : ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(resourceText).withStyle(ChatFormatting.YELLOW));
        MutableComponent lineTier = Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(variant.Tier())).withStyle(ChatFormatting.LIGHT_PURPLE));

        tips.add(lineRes);
        tips.add(lineTier);
    }

    private static @Nullable ResourcefulSheepEntity getSheep(String sheepId) {
        if (Minecraft.getInstance().level == null) return null;
        var holder = ModEntities.SHEEP_ENTITIES.get(sheepId);
        if (holder == null) return null;
        return sheepCache.computeIfAbsent(sheepId, id -> {
            EntityType<?> t = holder.get();
            return (ResourcefulSheepEntity) t.create(Minecraft.getInstance().level);
        });
    }
}
