package com.mathmout.resourcefulsheep.compat;

import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;

import java.util.List;

public record SheepSpawningRecipeWrapper(SheepSpawningData originalData, List<String> biomesForPage) {
}
