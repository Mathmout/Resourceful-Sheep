package com.mathmout.resourcefulsheep.item;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.custom.*;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;

public class ModItems {

    public static final List<DeferredItem<CustomDeferredSpawnEggItem>> SHEEP_SPAWN_EGGS = new ArrayList<>();
    public static final List<DeferredItem<ResourcefulWoolItem>> RESOURCEFUL_WOOLS = new ArrayList<>();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ResourcefulSheepMod.MOD_ID);

    public static final DeferredItem<Item> LASSO = ITEMS.register("lasso",
            () -> new Lasso(new Item.Properties()));

    public static final DeferredItem<Item> SHEEP_SCANNER = ITEMS.register("sheep_scanner",
            () -> new SheepScanner(new Item.Properties()));

    public static final DeferredItem<Item> IRON_SYRINGE = ITEMS.register("iron_syringe",
            () -> new Syringe(new Item.Properties(), Syringe.SyringeTiers.IRON));

    public static final DeferredItem<Item> DIAMOND_SYRINGE = ITEMS.register("diamond_syringe",
            () -> new Syringe(new Item.Properties(), Syringe.SyringeTiers.DIAMOND));

    public static final DeferredItem<Item> NETHERITE_SYRINGE = ITEMS.register("netherite_syringe",
            () -> new Syringe(new Item.Properties(), Syringe.SyringeTiers.NETHERITE));

    public static final DeferredItem<Item> SUSPICIOUS_SPAWN_EGG = ITEMS.register("suspicious_spawn_egg",
            () -> new SuspiciousSpawnEgg(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("diamond_upgrade_smithing_template",
            () -> new SmithingTemplateItem(
                    // Applies to
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(
                                    ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.applies_to")))
                            .withStyle(ChatFormatting.BLUE),

                    // Ingredients
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.ingredients")))
                            .withStyle(ChatFormatting.BLUE),

                    // Nom de l'upgrade
                    Component.translatable(Util.makeDescriptionId("upgrade",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "diamond_upgrade")))
                            .withStyle(ChatFormatting.GRAY),

                    // Description slot gauche
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.base_slot_description"))),

                    // Description slot droite
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.additions_slot_description"))),

                    // Icônes fantômes slot gauche
                    List.of(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,"item/empty_slot_syringe")),

                    // Icônes fantômes slot droite
                    List.of(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,"item/empty_slot_block"))
            ));


    // La méthode pour les œufs
    public static void registerVariantSpawnEggs() {
        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
            DeferredItem<CustomDeferredSpawnEggItem> spawnEgg = ITEMS.registerItem(
                    variant.Id() + "_spawn_egg",properties -> new CustomDeferredSpawnEggItem(
                                    ModEntities.SHEEP_ENTITIES.get(variant.Id()),
                                    Integer.parseInt(variant.EggColorBackground().substring(1), 16),
                                    Integer.parseInt(variant.EggColorSpotsNTitle().substring(1), 16),
                                    properties,
                                    "§l" + TexteUtils.stringToText(variant.Name()) + " Resourceful Sheep Egg",
                                    Integer.parseInt(variant.EggColorSpotsNTitle().substring(1), 16)
                            ));
            SHEEP_SPAWN_EGGS.add(spawnEgg);
        }
        sortVariantItems(SHEEP_SPAWN_EGGS);
    }

    public static void registerVariantWools() {
        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
            DeferredBlock<Block> correspondingBlock = ModBlocks.RESOURCEFUL_WOOL_BLOCKS.get(variant.Id());
            DeferredItem<ResourcefulWoolItem> woolItem = ITEMS.registerItem(
                    variant.Id() + "_wool",
                    properties -> new ResourcefulWoolItem(correspondingBlock.get(), new Item.Properties(), variant)
            );
            RESOURCEFUL_WOOLS.add(woolItem);
        }
        sortVariantItems(RESOURCEFUL_WOOLS);
    }

    public static <T extends Item> void sortVariantItems(List<DeferredItem<T>> list) {
        list.sort((item1, item2) -> {
            String id1 = item1.getId().getPath();
            String id2 = item2.getId().getPath();

            try {
                String resource1 = id1.substring(0, id1.indexOf("_tier_"));
                String resource2 = id2.substring(0, id2.indexOf("_tier_"));

                int resourceCompare = resource1.compareTo(resource2);
                if (resourceCompare != 0) {
                    return resourceCompare;
                }

                int tierStartIndex1 = id1.indexOf("_tier_") + 6;
                int tierStartIndex2 = id2.indexOf("_tier_") + 6;

                // On cherche la position du prochain "_" après le numéro (ex: avant "_wool" ou "_spawn_egg")
                int tierEndIndex1 = id1.indexOf('_', tierStartIndex1);
                int tierEndIndex2 = id2.indexOf('_', tierStartIndex2);

                // Si la chaîne s'arrête au numéro
                if (tierEndIndex1 == -1) tierEndIndex1 = id1.length();
                if (tierEndIndex2 == -1) tierEndIndex2 = id2.length();

                int tier1 = Integer.parseInt(id1.substring(tierStartIndex1, tierEndIndex1));
                int tier2 = Integer.parseInt(id2.substring(tierStartIndex2, tierEndIndex2));

                return Integer.compare(tier1, tier2);
            } catch (Exception e) {
                return id1.compareTo(id2);
            }
        });
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}