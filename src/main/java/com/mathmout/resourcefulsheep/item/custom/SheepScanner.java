package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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

import java.util.List;

public class SheepScanner extends Item {

    public static final int ENERGY_CAPACITY = 20_000;
    public static final int ENERGY_CONSUMPTION = 2000;
    public static final int MAX_ENERGY_TRANSFER = 100;

    public SheepScanner(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, @NotNull Player pPlayer,
                                                           @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pHand) {
        if (pInteractionTarget instanceof Sheep sheep) {

            int currentEnergy = getStoredEnergy(pStack);
            if (currentEnergy < ENERGY_CONSUMPTION) {
                return InteractionResult.FAIL;
            }

            if (pPlayer.level().isClientSide) {
                Component message;
                if (sheep instanceof ResourcefulSheepEntity resourcefulSheep) {
                    message = buildSheepInfoComponent(resourcefulSheep);
                } else {
                    message = buildVanillaSheepInfoComponent(sheep);
                }
                pPlayer.sendSystemMessage(message);
            }

            if (!pPlayer.level().isClientSide) {
                setStoredEnergy(pStack, currentEnergy - ENERGY_CONSUMPTION);
            }
            pPlayer.getCooldowns().addCooldown(this, 20);

            pPlayer.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;    }

    private Component buildSheepInfoComponent(ResourcefulSheepEntity sheep) {
        String variantId = BuiltInRegistries.ENTITY_TYPE.getKey(sheep.getType()).getPath();
        SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(variantId);

        if (variant == null) {
            return Component.literal("Unknown Sheep Variant").withStyle(ChatFormatting.RED);
        }

        MutableComponent mainComponent = Component.literal("");

        // 1. Header
        MutableComponent header = Component.literal("=== Sheep Scanner Result ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        mainComponent.append(header).append("\n");

        // 2. Tier.
        MutableComponent lineTier = Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(variant.Tier())).withStyle(ChatFormatting.LIGHT_PURPLE));
        mainComponent.append(lineTier).append("\n");

        // 3. Drops.
        mainComponent.append(Component.literal("Drops :").withStyle(ChatFormatting.BLUE)).append("\n");

        List<SheepVariantData.DroppedItems> drops = variant.DroppedItems();

        if (drops != null && !drops.isEmpty()) {
            for (SheepVariantData.DroppedItems dropData : drops) {
                if( dropData.ItemId().equals("minecraft:air")) {
                    mainComponent.append(Component.literal(" - None").withStyle(ChatFormatting.GRAY)).append("\n");
                }else {
                    String itemName = TexteUtils.getPrettyName(dropData.ItemId());
                    String amountString;
                    if (dropData.MinDrops() == dropData.MaxDrops()) {
                        amountString = String.valueOf(dropData.MinDrops());
                    } else {
                        amountString = dropData.MinDrops() + " to " + dropData.MaxDrops();
                    }

                    // Construction de la ligne : " - NomItem : Quantité"
                    MutableComponent dropLine = Component.literal("- ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(itemName).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" : ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(amountString).withStyle(ChatFormatting.DARK_AQUA));

                    mainComponent.append(dropLine).append("\n");
                }
            }
        } else {
            mainComponent.append(Component.literal(" - None").withStyle(ChatFormatting.GRAY)).append("\n");
        }

        // 4. Couleur
        DyeColor dyeColor = sheep.getColor();
        String colorName = dyeColor.getName().substring(0, 1).toUpperCase() + dyeColor.getName().substring(1);
        MutableComponent lineColor = Component.literal("Color : ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(TexteUtils.StringToText(colorName)).withStyle(Style.EMPTY.withColor(dyeColor.getTextColor())));
        mainComponent.append(lineColor);

        return mainComponent;
    }

    private Component buildVanillaSheepInfoComponent(Sheep sheep) {
        MutableComponent mainComponent = Component.literal("");

        // Header.
        MutableComponent header = Component.literal("=== Sheep Scanner Result ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        mainComponent.append(header).append("\n");

        // Description
        DyeColor dyeColor = sheep.getColor();
        String colorName = TexteUtils.StringToText(dyeColor.getName());
        MutableComponent line2 = Component.literal("It's just a ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(colorName).withStyle(Style.EMPTY.withColor(dyeColor.getTextColor())))
                .append(Component.literal(" sheep.").withStyle(ChatFormatting.GRAY));
        mainComponent.append(line2);
        return mainComponent;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {

        tooltipComponents.add(Component.literal("Energy : ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(getStoredEnergy(stack) + "/" + ENERGY_CAPACITY + " FE").withStyle(ChatFormatting.GRAY)));

        tooltipComponents.add(Component.literal("Right click on a sheep to scan it.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // Energy

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.round(13.0F * (float) getStoredEnergy(stack) / ENERGY_CAPACITY);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        float energyPourcentage = (float) getStoredEnergy(stack) / ENERGY_CAPACITY;
        return Mth.hsvToRgb(energyPourcentage / 3, 1, 1);    }

    public int getStoredEnergy(ItemStack stack) {
        if (stack.has(ModDataComponents.SHEEP_SCANNER_DATA.get())) {
            CompoundTag tag = stack.get(ModDataComponents.SHEEP_SCANNER_DATA.get());
                if (tag != null && tag.contains("Energy")) {
                return tag.getInt("Energy");
            }
        }
        return 0;
    }

    public void setStoredEnergy(ItemStack stack, int energy) {
        int clampedEnergy = Mth.clamp(energy, 0, ENERGY_CAPACITY);

        // Récupère le tag existant ou on en crée un vide
        CompoundTag tag = stack.getOrDefault(ModDataComponents.SHEEP_SCANNER_DATA.get(), new CompoundTag()).copy();

        // On modifie la valeur
        tag.putInt("Energy", clampedEnergy);

        // On sauvegarde le tag dans le composant
        stack.set(ModDataComponents.SHEEP_SCANNER_DATA.get(), tag);    }
}