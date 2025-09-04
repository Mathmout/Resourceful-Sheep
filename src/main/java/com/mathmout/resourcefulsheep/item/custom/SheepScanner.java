package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.event.ModEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SheepScanner extends Item {
    private static final Map<UUID, Long> lastScanTime = new HashMap<>();
    private static final long SCAN_COOLDOWN_MS = 1000; // 1 second

    public SheepScanner(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, @NotNull Player pPlayer,
                                                           @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pHand) {
        if (pInteractionTarget instanceof Sheep sheep) { // Broad check for any sheep
            if (pPlayer.level().isClientSide) {
                long currentTime = System.currentTimeMillis();
                long lastTime = lastScanTime.getOrDefault(pPlayer.getUUID(), 0L);

                if (currentTime - lastTime < SCAN_COOLDOWN_MS) {
                    return InteractionResult.SUCCESS; // Cooldown active, do nothing.
                }
                lastScanTime.put(pPlayer.getUUID(), currentTime);

                Component message;
                if (sheep instanceof ResourcefulSheepEntity resourcefulSheep) {
                    message = buildSheepInfoComponent(resourcefulSheep);
                } else {
                    message = buildVanillaSheepInfoComponent(sheep);
                }
                pPlayer.sendSystemMessage(message);
            }
            pPlayer.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private Component buildSheepInfoComponent(ResourcefulSheepEntity sheep) {
        String variantId = BuiltInRegistries.ENTITY_TYPE.getKey(sheep.getType()).getPath();
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(variantId);

        if (variant == null) {
            return Component.literal("Unknown Sheep Variant").withStyle(ChatFormatting.RED);
        }

        MutableComponent mainComponent = Component.literal("");

        // Header.
        MutableComponent header = Component.literal("=== Sheep Scanner Result ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        mainComponent.append(header).append("\n");

        // Dropped Item.
        MutableComponent line1 = Component.literal("Dropped Item : ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(ModEvents.itemIdToText(variant.DroppedItem)).withStyle(ChatFormatting.YELLOW));
        mainComponent.append(line1).append("\n");

        // Tier.
        MutableComponent line2 = Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(variant.Tier)).withStyle(ChatFormatting.LIGHT_PURPLE));
        mainComponent.append(line2).append("\n");

        // Nombre.
        MutableComponent line3;
        if (variant.MinDrops != variant.MaxDrops) {
            line3 = Component.literal("Amount : ").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal("From " + variant.MinDrops + " to " + variant.MaxDrops).withStyle(ChatFormatting.DARK_AQUA));
        } else if (variant.MinDrops == 0) {
            line3 = Component.literal("Amount : ").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal("Nothing").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            line3 = Component.literal("Amount : ").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal(String.valueOf(variant.MinDrops)).withStyle(ChatFormatting.DARK_AQUA));
        }
        // Couleur.
        mainComponent.append(line3).append("\n");
        DyeColor dyeColor = sheep.getColor();
        String colorName = dyeColor.getName().substring(0, 1).toUpperCase() + dyeColor.getName().substring(1);
        MutableComponent line4 = Component.literal("Color : ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(colorName).withStyle(Style.EMPTY.withColor(dyeColor.getTextColor())));
        mainComponent.append(line4);
        return mainComponent;
    }

    private Component buildVanillaSheepInfoComponent(Sheep sheep) {
        MutableComponent mainComponent = Component.literal("");

        // Header.
        MutableComponent header = Component.literal("=== Sheep Scanner Result ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        mainComponent.append(header).append("\n");

        // Description
        DyeColor dyeColor = sheep.getColor();
        String colorName = dyeColor.getName();
        MutableComponent line2 = Component.literal("It's just a ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(colorName).withStyle(Style.EMPTY.withColor(dyeColor.getTextColor())))
                .append(Component.literal(" sheep.").withStyle(ChatFormatting.GRAY));
        mainComponent.append(line2);
        return mainComponent;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip." + ResourcefulSheepMod.MOD_ID + ".sheep_scanner").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}