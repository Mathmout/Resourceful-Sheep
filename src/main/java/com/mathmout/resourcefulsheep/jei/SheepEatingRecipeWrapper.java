package com.mathmout.resourcefulsheep.jei;

import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;

import java.util.List;
import java.util.Map;

public record SheepEatingRecipeWrapper(SheepVariantData variant, List<Map.Entry<String, String>> pageEntries) {
}
