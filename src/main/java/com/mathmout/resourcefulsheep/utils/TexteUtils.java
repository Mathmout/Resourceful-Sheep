package com.mathmout.resourcefulsheep.utils;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.screen.DNAScreenRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                // ITEM
                if (BuiltInRegistries.ITEM.containsKey(loc)) {
                    Item item = BuiltInRegistries.ITEM.get(loc);
                    return item.getDescription().getString();
                }
                // ENTITY
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(loc)) {
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(loc);
                    return entityType.getDescription().getString();
                }
            }
        }
            return StringToText(Id);
    }

    public static String[] formatEnergy(int energy) {
        String[] result = new String[2];
        if (energy < 1000) {
            result[0] = String.valueOf(energy);
            result[1] = " FE";
            return result;
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        DecimalFormat decimalFormat = new DecimalFormat("#.###", symbols);

        if (energy < 1_000_000) {
            result[0] = decimalFormat.format(energy / 1000.0);
            result[1] = " kFE";
        } else if (energy < 1_000_000_000) {
            result[0] = decimalFormat.format(energy / 1_000_000.0);
            result[1] = " MFE";
        } else {
            result[0] = decimalFormat.format(energy / 1_000_000_000.0);
            result[1] = " GFE";
        }
        return result;
    }

    public static List<String> sortEntityIdsByName(List<String> entityIds) {
        List<String> sortedList = new ArrayList<>(entityIds);

        sortedList.sort((id1, id2) -> {
            String fullId1 = id1.contains(":") ? id1 : "resourceful_sheep:" + id1;
            String fullId2 = id2.contains(":") ? id2 : "resourceful_sheep:" + id2;

            LivingEntity e1 = DNAScreenRenderer.getEntity(fullId1);
            LivingEntity e2 = DNAScreenRenderer.getEntity(fullId2);

            String name1 = DNAScreenRenderer.getDisplayName(fullId1, e1);
            String name2 = DNAScreenRenderer.getDisplayName(fullId2, e2);

            return name1.compareToIgnoreCase(name2);
        });

        return sortedList;
    }

    public static void DisplayEntityList(List<Component> tooltipComponents, List<String> ids){
        int count = 0;
        int maxDisplayed = 22;
        for (String id : ids) {

            if (count >= maxDisplayed) {
                tooltipComponents.add(Component.literal("... and " + (ids.size() - maxDisplayed) + " more.").withStyle(ChatFormatting.GRAY));
                break;
            }

            String fullId = id.contains(":") ? id : ResourcefulSheepMod.MOD_ID + ":" + id;
            LivingEntity entity = DNAScreenRenderer.getEntity(fullId);
            String name = DNAScreenRenderer.getDisplayName(fullId, entity);

            tooltipComponents.add(Component.literal(" - " + name).withStyle(ChatFormatting.GRAY));
            count++;
        }
    }
}

