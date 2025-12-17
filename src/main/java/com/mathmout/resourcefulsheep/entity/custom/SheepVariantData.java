package com.mathmout.resourcefulsheep.entity.custom;

import java.util.List;

public record SheepVariantData(
        String Id,
        String Resource,
        int Tier,
        String DroppedItem,
        int MinDrops,
        int MaxDrops,
        String EggColorBackground,
        String EggColorSpotsNTitle,
        List<String> FoodItems
)
{
}