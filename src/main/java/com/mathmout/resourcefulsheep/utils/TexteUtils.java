package com.mathmout.resourcefulsheep.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class TexteUtils {

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

    // Utility to convert an item ID or item TAG to its display name.
    public static String getPrettyName(String Id) {
        if (Id == null || Id.isEmpty()) return "";

        // TAG
        if (Id.startsWith("#")) {
            String tagContent = Id.substring(1);
            ResourceLocation loc = ResourceLocation.tryParse(tagContent);
            if (loc != null) {
                return StringToText(loc.getPath());
            }
            return StringToText(tagContent);
        }

        // Item ID
        else {
            ResourceLocation loc = ResourceLocation.tryParse(Id);
            if (loc != null) {
                Item item = BuiltInRegistries.ITEM.get(loc);
                if (item != Items.AIR || Id.equals("minecraft:air")) {
                    return item.getDescription().getString();
                }
            }
            return StringToText(Id);
        }
    }
}
