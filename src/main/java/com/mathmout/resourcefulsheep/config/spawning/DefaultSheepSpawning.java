package com.mathmout.resourcefulsheep.config.spawning;

import java.util.ArrayList;
import java.util.List;

public class DefaultSheepSpawning {

    public static List<SheepSpawningData> getDefaults() {
        List<SheepSpawningData> defaultSpawning = new ArrayList<>();

        //Stone
        defaultSpawning.add(new SheepSpawningData("stone_tier_1", 8, 1, 1,
                List.of(), List.of()));

        //Cobblestone
        defaultSpawning.add(new SheepSpawningData("cobblestone_tier_1", 8, 1, 1,
                List.of(), List.of()));

        //Netherack
        defaultSpawning.add(new SheepSpawningData("netherrack_tier_1", 8, 1, 1,
                List.of("minecraft:nether_wastes"), List.of()));

        //Soul Sand
        defaultSpawning.add(new SheepSpawningData("soul_sand_tier_1", 8, 1, 1,
                List.of("minecraft:soul_sand_valley","minecraft:crimson_forest"), List.of()));

        //Sand
        defaultSpawning.add(new SheepSpawningData("sand_tier_1", 8, 1, 1,
                List.of("minecraft:savanna"), List.of()));

        //Dripstone
        defaultSpawning.add(new SheepSpawningData("dripstone_tier_1", 8, 1, 1,
                List.of(), List.of()));

        //Nether Star
        defaultSpawning.add(new SheepSpawningData("nether_star_tier_1", 4, 1, 1,
                List.of("minecraft:soul_sand_valley"), List.of()));

        return defaultSpawning;
    }
}