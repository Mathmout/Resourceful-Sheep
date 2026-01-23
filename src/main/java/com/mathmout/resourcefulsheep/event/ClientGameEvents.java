package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.client.data.DynamicSheepTextureGenerator;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = ResourcefulSheepMod.MOD_ID, value = Dist.CLIENT)
public class ClientGameEvents {

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            new DynamicSheepTextureGenerator().generateAllTextures(resourceManager);
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

                if (variant != null && variant.EggColorSpotsNTitle() != null) {
                    // 1. Titre Coloré
                    int nameColor = Integer.parseInt(variant.EggColorSpotsNTitle().substring(1), 16);
                    String displayName = "§l" + TexteUtils.StringToText(variant.Resource()) + " Resourceful Sheep Egg";

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
                                    String itemName = TexteUtils.getPrettyName(dropData.ItemId());
                                    // Quantité
                                    String amount;
                                    if (dropData.MinDrops() == dropData.MaxDrops()) {
                                        amount = String.valueOf(dropData.MinDrops());
                                    } else {
                                        amount = dropData.MinDrops() + " to " + dropData.MaxDrops();
                                    }

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
}