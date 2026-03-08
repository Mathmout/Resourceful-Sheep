package com.mathmout.resourcefulsheep.config.dnacrossbreeding;

import java.util.ArrayList;
import java.util.List;

public class DefaultDNACrossbreeding {
    public static List<SheepCrossbreeding> getDefaults() {
        List<SheepCrossbreeding> DefaultCrossbreeding = new ArrayList<>();

        // Creeper
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:creeper",
                "minecraft:sheep",
                "resourceful_sheep:creeper_tier_1",
                List.of("minecraft:sheep", "minecraft:creeper"),
                20)
        );

        // Wither
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:wither",
                "minecraft:sheep",
                "resourceful_sheep:wither_tier_1",
                List.of("minecraft:sheep", "minecraft:wither_skeleton"),
                15)
        );

        // Blaze
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:blaze",
                "minecraft:sheep",
                "resourceful_sheep:blaze_tier_1",
                List.of("minecraft:sheep", "minecraft:blaze"),
                20)
        );

        // Ghast
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:ghast",
                "minecraft:sheep",
                "resourceful_sheep:ghast_tier_1",
                List.of("minecraft:sheep", "minecraft:ghast"),
                20)
        );

        // Enderman
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:enderman",
                "minecraft:sheep",
                "resourceful_sheep:enderman_tier_1",
                List.of("minecraft:sheep", "minecraft:enderman"),
                20)
        );

        // Shulker
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:shulker",
                "minecraft:sheep",
                "resourceful_sheep:shulker_tier_1",
                List.of("minecraft:sheep", "minecraft:shulker"),
                20)
        );

        // Slime
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:slime",
                "minecraft:sheep",
                "resourceful_sheep:slime_tier_1",
                List.of("minecraft:sheep", "minecraft:slime"),
                20)
        );

        // Squid
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:squid",
                "minecraft:sheep",
                "resourceful_sheep:squid_tier_1",
                List.of("minecraft:sheep", "minecraft:squid"),
                25)
        );

        // Guardian
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:guardian",
                "minecraft:sheep",
                "resourceful_sheep:guardian_tier_1",
                List.of("minecraft:sheep", "minecraft:guardian"),
                20)
        );

        // Bee
        DefaultCrossbreeding.add(new SheepCrossbreeding(
                "minecraft:bee",
                "minecraft:sheep",
                "resourceful_sheep:bee_tier_1",
                List.of("minecraft:sheep", "minecraft:bee"),
                25)
        );

        return DefaultCrossbreeding;
    }
}
