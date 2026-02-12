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
                20));

        return DefaultCrossbreeding;
    }
}
