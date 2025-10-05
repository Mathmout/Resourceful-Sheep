package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.event.ModEvents;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Lasso extends Item {

    public static final String TAG_ENTITY_ID = "id";

    public Lasso(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity interactionTarget, @NotNull InteractionHand usedHand) {
        if (!player.level().isClientSide && interactionTarget instanceof Sheep sheep) {
            if (!stack.has(ModDataComponents.CAPTURED_ENTITY.get())) {
                ItemStack fullLasso = new ItemStack(this);
                CompoundTag entityTag = new CompoundTag();
                sheep.saveWithoutId(entityTag);
                entityTag.putString(TAG_ENTITY_ID, BuiltInRegistries.ENTITY_TYPE.getKey(sheep.getType()).toString());
                fullLasso.set(ModDataComponents.CAPTURED_ENTITY.get(), entityTag);

                sheep.discard();
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.5F);

                // Handle inventory change correctly for both modes
                if (player.getAbilities().instabuild) { // Creative Mode
                    player.setItemInHand(usedHand, fullLasso);
                } else { // Survival Mode
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        player.setItemInHand(usedHand, fullLasso);
                    } else if (!player.getInventory().add(fullLasso)) {
                        player.drop(fullLasso, false);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag entityTag = stack.get(ModDataComponents.CAPTURED_ENTITY.get());

        if (entityTag != null) {
            Optional<EntityType<?>> entityType = EntityType.by(entityTag);
            if (entityType.isPresent()) {
                EntityType.loadEntityRecursive(entityTag, context.getLevel(), (entity) -> {
                    var pos = context.getClickedPos().relative(context.getClickedFace());
                    entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    context.getLevel().addFreshEntity(entity);
                    return entity;
                });

                ItemStack emptyLasso = new ItemStack(this);

                // Handle inventory change correctly for both modes
                if (player.getAbilities().instabuild) { // Creative Mode
                    player.setItemInHand(context.getHand(), emptyLasso);
                } else { // Survival Mode
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        player.setItemInHand(context.getHand(), emptyLasso);
                    } else if (!player.getInventory().add(emptyLasso)) {
                        player.drop(emptyLasso, false);
                    }
                }

                context.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.5F, 1.5F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        CompoundTag entityTag = stack.get(ModDataComponents.CAPTURED_ENTITY.get());

        if (entityTag != null) {
            ResourceLocation entityId = ResourceLocation.tryParse(entityTag.getString(TAG_ENTITY_ID));

            if (entityId != null) {
                tooltipComponents.add(Component.literal("Contains a sheep.").withStyle(ChatFormatting.GREEN));

                if (Screen.hasShiftDown()) {
                    if (entityId.getNamespace().equals(ResourcefulSheepMod.MOD_ID)) {
                        appendResourcefulSheepInfo(tooltipComponents, entityTag, entityId, context);
                    } else {
                        appendVanillaSheepInfo(tooltipComponents, entityTag, context);
                    }
                } else {
                    tooltipComponents.add(Component.literal("Hold SHIFT for details.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            }
        } else {
            tooltipComponents.add(Component.literal("Allows you to carry a sheep in your pocket.").withStyle(ChatFormatting.GRAY));
        }
    }

    private void appendResourcefulSheepInfo(List<Component> tooltips, CompoundTag nbt, ResourceLocation entityId, @NotNull TooltipContext context) {
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(entityId.getPath());
        if (variant == null) return;

        tooltips.add(Component.literal("Dropped Item: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(ModEvents.ItemIdToName(variant.DroppedItem)).withStyle(ChatFormatting.YELLOW)));

        tooltips.add(Component.literal("Tier: ").withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(variant.Tier)).withStyle(ChatFormatting.LIGHT_PURPLE)));

        appendCommonSheepInfo(tooltips, nbt, context);
    }

    private void appendVanillaSheepInfo(List<Component> tooltips, CompoundTag nbt, @NotNull TooltipContext context) {
        tooltips.add(Component.literal("Standard Minecraft Sheep.").withStyle(ChatFormatting.WHITE));
        appendCommonSheepInfo(tooltips, nbt, context);
    }

    private void appendCommonSheepInfo(List<Component> tooltips, CompoundTag nbt, @NotNull TooltipContext context) {
        // Health
        if (nbt.contains("Health", 99)) { // 99 is the ID for any numeric type
            float health = nbt.getFloat("Health");
            tooltips.add(Component.literal("Health: ").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal(String.format("%.1f", health)).withStyle(ChatFormatting.DARK_AQUA)));
        }

        // Color
        if (nbt.contains("Color", 1)) { // 1 is the ID for a Byte
            DyeColor dyeColor = DyeColor.byId(nbt.getByte("Color"));
            tooltips.add(Component.literal("Color: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(ModEvents.StringToText(dyeColor.getName())).withStyle(Style.EMPTY.withColor(dyeColor.getTextColor()))));
        }

        // Custom Name
        if (nbt.contains("CustomName", 8)) { // 8 is the ID for a String
            try {
                Component customName = Component.Serializer.fromJsonLenient(nbt.getString("CustomName"), Objects.requireNonNull(context.registries()));
                if (customName != null) {
                    tooltips.add(Component.literal("Name: ").withStyle(ChatFormatting.GOLD)
                            .append(customName).withStyle(ChatFormatting.GREEN));
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return stack.has(ModDataComponents.CAPTURED_ENTITY.get());
    }
}
