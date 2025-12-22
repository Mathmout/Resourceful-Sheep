package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class ModEvents {

    // Add tooltip information to spawn eggs.
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        boolean isShiftKeyDown = Screen.hasShiftDown();

        if (item instanceof SpawnEggItem) {
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

            if (itemId.startsWith(ResourcefulSheepMod.MOD_ID + ":")) {
                String variantId = itemId
                        .replace(ResourcefulSheepMod.MOD_ID + ":", "")
                        .replace("_spawn_egg", "");

                SheepVariantData variant = ConfigSheepTypeManager.getSheepVariant().get(variantId);

                if (variant != null && variant.EggColorSpotsNTitle() != null) {
                    // 1. Titre Coloré
                    int nameColor = Integer.parseInt(variant.EggColorSpotsNTitle().substring(1), 16);
                    String displayName = "§l" + ModEvents.StringToText(variant.Resource()) + " Resourceful Sheep Egg";

                    if (!event.getToolTip().isEmpty()) {
                        event.getToolTip().set(0, Component.literal(displayName).withStyle(Style.EMPTY.withColor(nameColor)));

                        if (!isShiftKeyDown) {
                            event.getToolTip().add(Component.literal("Hold SHIFT for details.")
                                    .withStyle(ChatFormatting.ITALIC)
                                    .withStyle(ChatFormatting.GRAY));
                        } else {
                            // 2. Tier
                            MutableComponent tierLine = Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                                    .append(Component.literal(String.valueOf(variant.Tier())).withStyle(ChatFormatting.LIGHT_PURPLE));
                            event.getToolTip().add(tierLine);

                            // 3. Liste des Drops
                            event.getToolTip().add(Component.literal("Drops :").withStyle(ChatFormatting.BLUE));

                            List<SheepVariantData.DroppedItems> drops = variant.DroppedItems();

                            if (drops != null && !drops.isEmpty() && !drops.getFirst().ItemId().equals("minecraft:air")) {
                                for (SheepVariantData.DroppedItems dropData : drops) {
                                    // Nom de l'item
                                    String itemName = ModEvents.ItemIdToName(dropData.ItemId());

                                    // Quantité (ex: "5" ou "2-5")
                                    String amount;
                                    if (dropData.MinDrops() == dropData.MaxDrops()) {
                                        amount = String.valueOf(dropData.MinDrops());
                                    } else {
                                        amount = dropData.MinDrops() + " to " + dropData.MaxDrops();
                                    }

                                    // Ligne formatée : " - Cobblestone : 12-20"
                                    MutableComponent line = Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(itemName).withStyle(ChatFormatting.YELLOW))
                                            .append(Component.literal(" : ").withStyle(ChatFormatting.GRAY))
                                            .append(Component.literal(amount).withStyle(ChatFormatting.DARK_AQUA));

                                    event.getToolTip().add(line);
                                }
                            } else {
                                event.getToolTip().add(Component.literal(" - None").withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                }
            }
        }
    }

    // Utility to convert an item ID to its display name.
    public static String ItemIdToName(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return itemId;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        Component name = item.getDescription();
        return name.getString();
    }

    // Utility to convert an item ID to readable text.
    public static String StringToText(String itemId) {
        String IdWithoutUnderscore = itemId.replace('_', ' ');
        String[] words = IdWithoutUnderscore.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

}