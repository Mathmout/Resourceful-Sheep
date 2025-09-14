package com.mathmout.resourcefulsheep.config.spawning;

import java.util.List;

public record SheepSpawningData(String sheepId, int weight, int minCount, int maxCount, List<String> biomes, List<String> structures) {

}