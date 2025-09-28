package com.mathmout.resourcefulsheep.config.spawning;

import java.util.ArrayList;
import java.util.List;

public class DefaultSheepSpawning {

    public static List<SheepSpawningData> getDefaults() {
        List<SheepSpawningData> defaultSpawning = new ArrayList<>();

        //Stone
        defaultSpawning.add(new SheepSpawningData("stone_tier_1", 1, 1, 256, 1,
                List.of()));

        //Cobblestone
        defaultSpawning.add(new SheepSpawningData("cobblestone_tier_1", 1, 1, 256, 1,
                List.of()));

        //Netherack
        defaultSpawning.add(new SheepSpawningData("netherrack_tier_1", 1, 1, 256, 1,
                List.of("minecraft:nether_wastes")));

        //Dripstone
        defaultSpawning.add(new SheepSpawningData("dripstone_tier_1", 1, 1, 256, 1,
                List.of()));


        //Soul Sand
        defaultSpawning.add(new SheepSpawningData("soul_sand_tier_1", 1, 1, 256, 1,
                List.of("minecraft:soul_sand_valley")));

        //Sand
        defaultSpawning.add(new SheepSpawningData("sand_tier_1", 1, 1, 256, 1,
                List.of()));

        //Nether Star
        defaultSpawning.add(new SheepSpawningData("nether_star_tier_1", 1, 1, 512, 1,
                List.of("minecraft:soul_sand_valley")));

        //Gold
        defaultSpawning.add(new SheepSpawningData("gold_tier_1", 1, 1, 512, 1,
                List.of("minecraft:wooded_badlands")));

        return defaultSpawning;
    }
}