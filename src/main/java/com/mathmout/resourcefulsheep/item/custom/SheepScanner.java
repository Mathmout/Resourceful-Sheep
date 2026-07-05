package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.Config;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.screen.scanner.SheepScannerMenu;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SheepScanner extends Item {

    public SheepScanner(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            CompoundTag tag = stack.get(ModDataComponents.SHEEP_SCANNER_DATA.get());
            if (tag != null && tag.contains("scanned_sheep")) {
                String sheepId = tag.getString("scanned_sheep");

                SimpleContainerData data = new SimpleContainerData(2);
                data.set(0, getStoredEnergy(stack));
                data.set(1, Config.SHEEP_SCANNER_CAPACITY.get());

                // On ouvre le Menu et on envoie l'ID au client via le buffer
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new SheepScannerMenu(containerId, playerInventory, sheepId, data),
                        Component.literal("Sheep Scanner")
                ), buf -> buf.writeUtf(sheepId));

            } else {
                player.displayClientMessage(
                        Component.literal("No sheep scanned yet!").withStyle(ChatFormatting.RED),
                        true
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, @NotNull Player pPlayer,
                                                           @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pHand) {

        if (pPlayer.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.PASS;
        }

        if (pInteractionTarget instanceof Sheep sheep) {
            int currentEnergy = getStoredEnergy(pStack);
            int sheepScannerConsumption = Config.SHEEP_SCANNER_CONSUMPTION.get();
            if (currentEnergy >= sheepScannerConsumption || sheepScannerConsumption == 0) {

                if (!pPlayer.level().isClientSide()) {
                    setStoredEnergy(pStack, currentEnergy - Config.SHEEP_SCANNER_CONSUMPTION.get());
                    CompoundTag tag = pStack.getOrDefault(ModDataComponents.SHEEP_SCANNER_DATA.get(), new CompoundTag()).copy();
                    if (sheep instanceof ResourcefulSheepEntity resourcefulSheep) {
                        tag.putString("scanned_sheep", resourcefulSheep.getSheepVariantData().Id());
                        pStack.set(ModDataComponents.SHEEP_SCANNER_DATA.get(), tag);

                        pPlayer.displayClientMessage(
                                Component.literal("Sheep scanned successfully !").withStyle(ChatFormatting.GREEN),
                                true
                        );
                    } else {
                        MutableComponent mutableComponent = Component.literal("");

                        DyeColor dyeColor = sheep.getColor();
                        String colorName = TexteUtils.stringToText(dyeColor.getName());
                        MutableComponent line2 = Component.literal("It's just a ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(colorName).withStyle(Style.EMPTY.withColor(dyeColor.getTextColor())))
                                .append(Component.literal(" sheep.").withStyle(ChatFormatting.GRAY));
                        mutableComponent.append(line2);

                        pPlayer.displayClientMessage(mutableComponent, true);
                    }
                }

                pPlayer.getCooldowns().addCooldown(this, 20);
                pPlayer.level().playSound(pPlayer, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

            } else {
                if (!pPlayer.level().isClientSide()) {
                    pPlayer.displayClientMessage(
                            Component.literal("Not enough energy !").withStyle(ChatFormatting.RED),
                            true
                    );
                }
            }
            return InteractionResult.sidedSuccess(pPlayer.level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        String[] energyStored = TexteUtils.formatEnergy(getStoredEnergy(stack));
        String[] energyMax = TexteUtils.formatEnergy(Config.SHEEP_SCANNER_CAPACITY.get());

        if (Config.SHEEP_SCANNER_CONSUMPTION.get() > 0) {
            tooltipComponents.add(Component.literal("Energy : ").withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal(energyStored[0] + energyStored[1] + "/" + energyMax[0] + energyMax[1]).withStyle(ChatFormatting.GRAY)));
        }

        tooltipComponents.add(Component.literal("Right click on a sheep to scan it.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // Energy
    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return Config.SHEEP_SCANNER_CONSUMPTION.get() > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.round(13.0F * (float) getStoredEnergy(stack) / Config.SHEEP_SCANNER_CAPACITY.get());
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        float energyPourcentage = (float) getStoredEnergy(stack) / Config.SHEEP_SCANNER_CAPACITY.get();
        return Mth.hsvToRgb(energyPourcentage / 3, 1, 1);
    }

    public int getStoredEnergy(ItemStack stack) {
        if (stack.has(ModDataComponents.SHEEP_SCANNER_DATA.get())) {
            CompoundTag tag = stack.get(ModDataComponents.SHEEP_SCANNER_DATA.get());
                if (tag != null && tag.contains("energy")) {
                return tag.getInt("energy");
            }
        }
        return 0;
    }

    public void setStoredEnergy(ItemStack stack, int energy) {
        int clampedEnergy = Mth.clamp(energy, 0, Config.SHEEP_SCANNER_CAPACITY.get());

        // Récupère le tag existant ou on en crée un vide
        CompoundTag tag = stack.getOrDefault(ModDataComponents.SHEEP_SCANNER_DATA.get(), new CompoundTag()).copy();

        // On modifie la valeur
        tag.putInt("energy", clampedEnergy);

        // On sauvegarde le tag dans le composant
        stack.set(ModDataComponents.SHEEP_SCANNER_DATA.get(), tag);
    }
}