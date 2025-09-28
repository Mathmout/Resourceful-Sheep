package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.config.spawning.SheepSpawningData;

import java.util.List;

public record SheepSpawningRecipeWrapper(SheepSpawningData originalData, List<String> biomesForPage) {
}
