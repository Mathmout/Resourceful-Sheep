package com.mathmout.resourcefulsheep.config;

import java.util.List;

public class SheepTypeData {
    public String Resource;
    public String EggColorBackground;
    public String EggColorSpots;
    public List<TierData> SheepTier;

    public SheepTypeData(String Resource, String EggColorBackground, String EggColorSpots, List<TierData> SheepTier) {
        this.Resource = Resource;
        this.EggColorBackground = EggColorBackground;
        this.EggColorSpots = EggColorSpots;
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