package com.mathmout.resourcefulsheep.config.spawning;

import java.util.List;

public record SheepSpawningData(String sheepId,
                                int minCount,
                                int maxCount,
                                int densityRadius,
                                int maxNearby,
                                List<String> Biomes,
                                boolean RequireSeeSky)
{

}