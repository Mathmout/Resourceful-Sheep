package com.mathmout.resourcefulsheep.config.spawning;

import java.util.ArrayList;
import java.util.List;

public class DefaultSheepSpawning {

    public static List<SheepSpawningData> getDefaults() {
        List<SheepSpawningData> defaultSpawning = new ArrayList<>();

        //Stone
        defaultSpawning.add(new SheepSpawningData("stone_tier_1", 1, 1, 512, 1,
                List.of(), true));

        //Cobblestone
        defaultSpawning.add(new SheepSpawningData("cobblestone_tier_1", 1, 1, 512, 1,
                List.of(), true));

        //Netherack
        defaultSpawning.add(new SheepSpawningData("netherrack_tier_1", 1, 1, 256, 1,
                List.of("minecraft:crimson_forest", "minecraft:warped_forest"), false));

        //Dripstone
        defaultSpawning.add(new SheepSpawningData("dripstone_tier_1", 1, 1, 512, 1,
                List.of(), true));

        //Soul Sand
        defaultSpawning.add(new SheepSpawningData("soul_sand_tier_1", 1, 1, 256, 1,
                List.of("minecraft:soul_sand_valley"), false));

        //Sand
        defaultSpawning.add(new SheepSpawningData("sand_tier_1", 1, 1, 512, 1,
                List.of(), true));

        //Nether Star
        defaultSpawning.add(new SheepSpawningData("nether_star_tier_1", 1, 1, 512, 1,
                List.of("minecraft:soul_sand_valley"), false));

        //Gold
        defaultSpawning.add(new SheepSpawningData("gold_tier_1", 1, 1, 1024, 1,
                List.of("minecraft:wooded_badlands"), true));

        return defaultSpawning;
    }
}