package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.concurrent.ThreadLocalRandom;


public class ModEvents {

    // Handle shearing of Resourceful Sheep to drop custom items.
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof ResourcefulSheepEntity sheep) {
            if (event.getItemStack().getItem() == Items.SHEARS) {
                if (!event.getLevel().isClientSide && !sheep.isSheared()) {
                    sheep.shear(SoundSource.PLAYERS);
                    SheepVariantData variantData = ConfigSheepTypeManager.getSheepVariant()
                            .get(BuiltInRegistries.ENTITY_TYPE.getKey(sheep.getType()).getPath());
                    if (variantData != null) {
                        Item droppedItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(variantData.DroppedItem));
                        if (droppedItem != Items.AIR) {
                            int count = ThreadLocalRandom.current().nextInt(variantData.MinDrops, variantData.MaxDrops) + 1;
                            sheep.spawnAtLocation(new ItemStack(droppedItem, count));
                        }
                    }
                    event.getItemStack().hurtAndBreak(1, event.getEntity(), LivingEntity.getSlotForHand(event.getHand()));
                    event.setCanceled(true);
                }
            }
        }
    }

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
                if (variant != null && variant.EggColorSpotsNTitle != null) {
                    int nameColor = Integer.parseInt(variant.EggColorSpotsNTitle.substring(1), 16);
                    String displayName = "§l" + StringToText(variant.Resource) + " Resourceful Sheep Egg";
                    if (!event.getToolTip().isEmpty()) {
                        event.getToolTip().set(0, Component.literal(displayName).withStyle(Style.EMPTY.withColor(nameColor)));
                        if (!isShiftKeyDown) {
                            event.getToolTip().add(Component.literal("Hold SHIFT for more info.")
                                    .withStyle(ChatFormatting.ITALIC)
                                    .withStyle(ChatFormatting.GRAY));
                        } else {
                            // Dropped Item.
                            MutableComponent line = Component.literal("Dropped Item : ").withStyle(ChatFormatting.BLUE)
                                    .append(Component.literal(ItemIdToName(variant.DroppedItem)).withStyle(ChatFormatting.YELLOW));
                            event.getToolTip().add(line);

                            // Tier.
                            line = Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                                    .append(Component.literal(String.valueOf(variant.Tier)).withStyle(ChatFormatting.LIGHT_PURPLE));
                            event.getToolTip().add(line);

                            // Quantité.
                            if (variant.MinDrops != variant.MaxDrops) {
                                line = Component.literal("Amount : ").withStyle(ChatFormatting.DARK_GREEN)
                                        .append(Component.literal("From " + variant.MinDrops + " to " + variant.MaxDrops).withStyle(ChatFormatting.DARK_AQUA));
                                event.getToolTip().add(line);
                            } else if (variant.MinDrops == 0) {
                                line = Component.literal("Amount : ").withStyle(ChatFormatting.DARK_GREEN)
                                        .append(Component.literal("Nothing").withStyle(ChatFormatting.DARK_AQUA));
                                event.getToolTip().add(line);
                            } else {
                                line = Component.literal("Amount : ").withStyle(ChatFormatting.DARK_GREEN)
                                        .append(Component.literal(String.valueOf(variant.MinDrops)).withStyle(ChatFormatting.DARK_AQUA));
                                event.getToolTip().add(line);
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
