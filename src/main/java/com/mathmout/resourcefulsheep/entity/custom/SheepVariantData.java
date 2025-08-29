package com.mathmout.resourcefulsheep.entity.custom;

public class SheepVariantData {
    public String Id;
    public String Resource;
    public int Tier;
    public String DroppedItem;
    public int MinDrops;
    public int MaxDrops;
    public String EggColorBackground;
    public String EggColorSpots;

    public SheepVariantData(String id, String resource, int tier, String droppedItem, int minDrops, int maxDrops,
                            String eggColorBackground, String eggColorSpots) {
        this.Id = id;
        this.Resource = resource;
        this.Tier = tier;
        this.DroppedItem = droppedItem;
        this.MinDrops = minDrops;
        this.MaxDrops = maxDrops;
        this.EggColorBackground = eggColorBackground;
        this.EggColorSpots = eggColorSpots;
    }

}