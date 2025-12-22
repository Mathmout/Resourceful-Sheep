package com.mathmout.resourcefulsheep.entity.custom;

import java.util.List;
import java.util.Map;

public record SheepVariantData(
        String Id,
        String Resource,
        int Tier,
        List<DroppedItems> DroppedItems,
        String EggColorBackground,
        String EggColorSpotsNTitle,
        List<String> FoodItems,
        boolean FireImmune,
        List<String> ImmuneEffects,
        Map<String, String> EtableBocksMap)
{
    public record DroppedItems(String ItemId,
                               int MinDrops,
                               int MaxDrops) {
    }

}