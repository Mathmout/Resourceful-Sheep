package com.mathmout.resourcefulsheep.config.sheeptypes;

import java.util.List;

public class SheepTypeData {
    public String Resource;
    public String EggColorBackground;
    public String EggColorSpotsNTitle;
    public List<TierData> SheepTier;

    public SheepTypeData(String Resource, String EggColorBackground, String EggColorSpotsNTitle, List<TierData> SheepTier) {
        this.Resource = Resource;
        this.EggColorBackground = EggColorBackground;
        this.EggColorSpotsNTitle = EggColorSpotsNTitle;
        this.SheepTier = SheepTier;
    }
    public static class TierData {
        public int Tier;
        public String DroppedItem;
        public int MinDrops;
        public int MaxDrops;

        public TierData(int tier, String droppedItem, int minDrops, int maxDrops) {
            this.Tier = tier;
            this.DroppedItem = droppedItem;
            this.MinDrops = minDrops;
            this.MaxDrops = maxDrops;
        }
    }
}