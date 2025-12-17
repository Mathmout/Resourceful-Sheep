package com.mathmout.resourcefulsheep.config.sheeptypes;

import java.util.List;

public record SheepTypeData(String Resource, String EggColorBackground, String EggColorSpotsNTitle, List<String> FoodItems, List<TierData> SheepTier) {

    public record TierData(int Tier, String DroppedItem, int MinDrops, int MaxDrops) {
    }
}
