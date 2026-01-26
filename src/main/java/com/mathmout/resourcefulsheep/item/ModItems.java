package com.mathmout.resourcefulsheep.item;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.custom.CustomDeferredSpawnEggItem;
import com.mathmout.resourcefulsheep.item.custom.Lasso;
import com.mathmout.resourcefulsheep.item.custom.SheepScanner;
import com.mathmout.resourcefulsheep.item.custom.Syringe;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Function;


public class ModItems {

    public static final List<DeferredItem<? extends SpawnEggItem>> SHEEP_SPAWN_EGGS = new ArrayList<>();

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

    public static final DeferredItem<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("diamond_upgrade_smithing_template",
            () -> new SmithingTemplateItem(
                    // 1. "Applies to:" (Ce sur quoi on l'utilise)
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(
                                    ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.applies_to")))
                            .withStyle(ChatFormatting.BLUE),

                    // 2. "Ingredients:" (Ce qu'on ajoute)
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.ingredients")))
                            .withStyle(ChatFormatting.BLUE),

                    // 3. Titre de l'upgrade (ex: "Diamond Upgrade")
                    Component.translatable(Util.makeDescriptionId("upgrade",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "diamond_upgrade")))
                            .withStyle(ChatFormatting.GRAY),

                    // 4. Description slot gauche (Base)
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.base_slot_description"))),

                    // 5. Description slot droite (Addition)
                    Component.translatable(Util.makeDescriptionId("item",
                            ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,
                                    "smithing_template.diamond_upgrade.additions_slot_description"))),

                    // 6. Icônes fantômes slot gauche (On réutilise l'icône d'épée vide vanilla par exemple, ou ta seringue)
                    List.of(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,"item/empty_slot_syringe")),

                    // 7. Icônes fantômes slot droite (Diamant)
                    List.of(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID,"item/empty_slot_block"))
            ));

    public static void registerVariantSpawnEggs() {
        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
            DeferredItem<SpawnEggItem> spawnEgg = ITEMS.registerItem(
            variant.Id() + "_spawn_egg", (Function<Item.Properties, ? extends SpawnEggItem>)
            properties -> new CustomDeferredSpawnEggItem( ModEntities.SHEEP_ENTITIES.get(variant.Id()),
                    Integer.parseInt(variant.EggColorBackground().substring(1), 16),
                    Integer.parseInt(variant.EggColorSpotsNTitle().substring(1), 16),
                    properties,
                    "§l" + TexteUtils.StringToText(variant.Resource()) + " Resourceful Sheep Egg",
                    Integer.parseInt(variant.EggColorSpotsNTitle().substring(1), 16)
                    ));
            SHEEP_SPAWN_EGGS.add(spawnEgg);
        }
            SHEEP_SPAWN_EGGS.sort((egg1, egg2) -> {
            String id1 = egg1.getId().getPath();
            String id2 = egg2.getId().getPath();

            try {
                String resource1 = id1.substring(0, id1.indexOf("_tier_"));
                String resource2 = id2.substring(0, id2.indexOf("_tier_"));

                int resourceCompare = resource1.compareTo(resource2);
                if (resourceCompare != 0) {
                    return resourceCompare;
                }

                String tierString1 = id1.substring(id1.indexOf("_tier_") + 6, id1.lastIndexOf("_spawn_egg"));
                String tierString2 = id2.substring(id2.indexOf("_tier_") + 6, id2.lastIndexOf("_spawn_egg"));

                int tier1 = Integer.parseInt(tierString1);
                int tier2 = Integer.parseInt(tierString2);

                return Integer.compare(tier1, tier2);
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                return id1.compareTo(id2);
            }
        });
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

