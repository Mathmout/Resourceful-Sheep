package com.mathmout.resourcefulsheep.client.renderer;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class ResourcefulSheepRenderer extends MobRenderer<ResourcefulSheepEntity, SheepModel<ResourcefulSheepEntity>> {

    public ResourcefulSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
        this.addLayer(new ResourcefulSheepFurLayer(this, context.getModelSet()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ResourcefulSheepEntity entity) {
        String variantId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(variantId);

        if (variant != null && variant.Id != null) {
            ResourceLocation customBodyTexture = ResourceLocation.fromNamespaceAndPath(
                    ResourcefulSheepMod.MOD_ID,
                    "textures/entity/sheep/" + variant.Id + ".png"
            );

            if (Minecraft.getInstance().getResourceManager().getResource(customBodyTexture).isPresent()) {
                return customBodyTexture;
            }
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/sheep/sheep.png");
    }

    private ResourceLocation getFurTextureLocation(ResourcefulSheepEntity entity) {
        String variantId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(variantId);

        if (variant != null && variant.Id != null) {
            ResourceLocation customFurTexture = ResourceLocation.fromNamespaceAndPath(
                    ResourcefulSheepMod.MOD_ID,
                    "textures/entity/sheep/" + variant.Id + "_fur.png"
            );

            if (Minecraft.getInstance().getResourceManager().getResource(customFurTexture).isPresent()) {
                return customFurTexture;
            }
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/sheep/sheep_fur.png");
    }

    public class ResourcefulSheepFurLayer extends RenderLayer<ResourcefulSheepEntity, SheepModel<ResourcefulSheepEntity>> {
        private final SheepFurModel<ResourcefulSheepEntity> model;

        public ResourcefulSheepFurLayer(MobRenderer<ResourcefulSheepEntity, SheepModel<ResourcefulSheepEntity>> renderer, EntityModelSet modelSet) {
            super(renderer);
            this.model = new SheepFurModel<>(modelSet.bakeLayer(ModelLayers.SHEEP_FUR));
        }

        private float[] getColorComponents(DyeColor color) {
            int colorValue = color.getTextureDiffuseColor();
            float red = ((colorValue >> 16) & 0xFF) / 255.0F;
            float green = ((colorValue >> 8) & 0xFF) / 255.0F;
            float blue = (colorValue & 0xFF) / 255.0F;
            return new float[]{red, green, blue};
        }

        @Override
        public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull ResourcefulSheepEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isSheared()) {
                return;
            }

            ResourceLocation furTexture = getFurTextureLocation(entity);
            float[] color;
            if (entity.hasCustomName() && "jeb_".equals(entity.getName().getString())) {
                int i = entity.tickCount / 25 + entity.getId();
                int j = DyeColor.values().length;
                int k = i % j;
                int l = (i + 1) % j;
                float f = ((float)(entity.tickCount % 25) + partialTicks) / 25.0F;
                float[] afloat1 = getColorComponents(DyeColor.byId(k));
                float[] afloat2 = getColorComponents(DyeColor.byId(l));
                color = new float[]{afloat1[0] * (1.0F - f) + afloat2[0] * f, afloat1[1] * (1.0F - f) + afloat2[1] * f, afloat1[2] * (1.0F - f) + afloat2[2] * f};
            } else {
                color = getColorComponents(entity.getColor());
            }

            int r = (int)(color[0] * 255.0F);
            int g = (int)(color[1] * 255.0F);
            int b = (int)(color[2] * 255.0F);
            int packedColor = (255 << 24) | (r << 16) | (g << 8) | b;

            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(furTexture));
            this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, getOverlayCoords(entity, 0.0F), packedColor);
        }
    }
}