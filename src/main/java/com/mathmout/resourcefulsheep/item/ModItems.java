package com.mathmout.resourcefulsheep.item;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.ConfigManager;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.custom.CustomDeferredSpawnEggItem;
import com.mathmout.resourcefulsheep.item.custom.Lasso;
import com.mathmout.resourcefulsheep.item.custom.SheepScanner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;


public class ModItems {

    public static final List<DeferredItem<? extends SpawnEggItem>> SHEEP_SPAWN_EGGS = new ArrayList<>();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ResourcefulSheepMod.MOD_ID);

    public static final DeferredItem<Item> LASSO = ITEMS.register("lasso", () -> new Lasso(new Item.Properties()));
    public static final DeferredItem<Item> SHEEP_SCANNER = ITEMS.register("sheep_scanner", () -> new SheepScanner(new Item.Properties()));

    public static void registerVariantSpawnEggs() {
        for (SheepVariantData variant : ConfigManager.getSheepVariant().values()) {
            DeferredItem<SpawnEggItem> spawnEgg = ITEMS.registerItem(
            variant.Id + "_spawn_egg", (Function<Item.Properties, ? extends SpawnEggItem>)
            properties -> new CustomDeferredSpawnEggItem( ModEntities.SHEEP_ENTITIES.get(variant.Id),
                    Integer.parseInt(variant.EggColorBackground.substring(1), 16),
                    Integer.parseInt(variant.EggColorSpots.substring(1), 16),
                    properties,
                    "§lResourceful Sheep Spawn Egg",
                    Integer.parseInt(variant.EggColorSpots.substring(1), 16)
                    ));
            SHEEP_SPAWN_EGGS.add(spawnEgg);
        }
        SHEEP_SPAWN_EGGS.sort(Comparator.comparing(DeferredHolder::getId));
    }


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

