package com.mathmout.resourcefulsheep.config.mutations;

import java.util.ArrayList;
import java.util.List;

public class DefaultSheepMutations {
    public static List<SheepMutation> getDefaults() {
        List<SheepMutation> DefaultMutation = new ArrayList<>();

        // Tier 1 -> Tier 2
        DefaultMutation.add(new SheepMutation("cobblestone_tier_1", "cobblestone_tier_1", "cobblestone_tier_2", 40));
        DefaultMutation.add(new SheepMutation("stone_tier_1", "stone_tier_1", "stone_tier_2", 40));
        DefaultMutation.add(new SheepMutation("sand_tier_1", "sand_tier_1", "sand_tier_2", 40));
        DefaultMutation.add(new SheepMutation("dripstone_tier_1", "dripstone_tier_1", "dripstone_tier_2", 40));
        DefaultMutation.add(new SheepMutation("granite_tier_1", "granite_tier_1", "granite_tier_2", 40));
        DefaultMutation.add(new SheepMutation("diorite_tier_1", "diorite_tier_1", "diorite_tier_2", 40));
        DefaultMutation.add(new SheepMutation("andesite_tier_1", "andesite_tier_1", "andesite_tier_2", 40));
        DefaultMutation.add(new SheepMutation("netherrack_tier_1", "netherrack_tier_1", "netherrack_tier_2", 40));
        DefaultMutation.add(new SheepMutation("soul_sand_tier_1", "soul_sand_tier_1", "soul_sand_tier_2", 40));
        DefaultMutation.add(new SheepMutation("coal_tier_1", "coal_tier_1", "coal_tier_2", 40));
        DefaultMutation.add(new SheepMutation("copper_tier_1", "copper_tier_1", "copper_tier_2", 40));
        DefaultMutation.add(new SheepMutation("redstone_tier_1", "redstone_tier_1", "redstone_tier_2", 40));
        DefaultMutation.add(new SheepMutation("lapis_lazuli_tier_1", "lapis_lazuli_tier_1", "lapis_lazuli_tier_2", 40));
        DefaultMutation.add(new SheepMutation("quartz_tier_1", "quartz_tier_1", "quartz_tier_2", 40));
        DefaultMutation.add(new SheepMutation("gold_tier_1", "gold_tier_1", "gold_tier_2", 40));
        DefaultMutation.add(new SheepMutation("iron_tier_1", "iron_tier_1", "iron_tier_2", 40));
        DefaultMutation.add(new SheepMutation("diamond_tier_1", "diamond_tier_1", "diamond_tier_2", 40));
        DefaultMutation.add(new SheepMutation("emerald_tier_1", "emerald_tier_1", "emerald_tier_2", 40));
        DefaultMutation.add(new SheepMutation("netherite_scrap_tier_1", "netherite_scrap_tier_1", "netherite_scrap_tier_2", 40));
        DefaultMutation.add(new SheepMutation("nether_star_tier_1", "nether_star_tier_1", "nether_star_tier_2", 20));
        DefaultMutation.add(new SheepMutation("netherite_tier_1", "netherite_tier_1", "netherite_tier_2", 20));

        // Tier 2 -> Tier 3
        DefaultMutation.add(new SheepMutation("cobblestone_tier_2", "cobblestone_tier_2", "cobblestone_tier_3", 25));
        DefaultMutation.add(new SheepMutation("stone_tier_2", "stone_tier_2", "stone_tier_3", 25));
        DefaultMutation.add(new SheepMutation("sand_tier_2", "sand_tier_2", "sand_tier_3", 25));
        DefaultMutation.add(new SheepMutation("dripstone_tier_2", "dripstone_tier_2", "dripstone_tier_3", 25));
        DefaultMutation.add(new SheepMutation("granite_tier_2", "granite_tier_2", "granite_tier_3", 25));
        DefaultMutation.add(new SheepMutation("diorite_tier_2", "diorite_tier_2", "diorite_tier_3", 25));
        DefaultMutation.add(new SheepMutation("andesite_tier_2", "andesite_tier_2", "andesite_tier_3", 25));
        DefaultMutation.add(new SheepMutation("netherrack_tier_2", "netherrack_tier_2", "netherrack_tier_3", 25));
        DefaultMutation.add(new SheepMutation("iron_tier_2", "iron_tier_2", "iron_tier_3", 25));
        DefaultMutation.add(new SheepMutation("gold_tier_2", "gold_tier_2", "gold_tier_3", 25));
        DefaultMutation.add(new SheepMutation("netherite_scrap_tier_2", "netherite_scrap_tier_2", "netherite_scrap_tier_3", 25));
        DefaultMutation.add(new SheepMutation("soul_sand_tier_2", "soul_sand_tier_2", "soul_sand_tier_3", 25));

        // Cobblestone + Stone = Diorite
        DefaultMutation.add(new SheepMutation("cobblestone_tier_3", "stone_tier_3", "diorite_tier_1", 30));

        // Cobblestone + Stone = Granite
        DefaultMutation.add(new SheepMutation("cobblestone_tier_3", "stone_tier_3", "granite_tier_1", 30));

        // Cobblestone + Stone = Andesite
        DefaultMutation.add(new SheepMutation("cobblestone_tier_3", "stone_tier_3", "andesite_tier_1", 30));

        // Granite + Dripstone = Copper
        DefaultMutation.add(new SheepMutation("granite_tier_3", "dripstone_tier_3", "copper_tier_1", 40));

        // Cobblestone + Andesite = Coal
        DefaultMutation.add(new SheepMutation("cobblestone_tier_3", "andesite_tier_3", "coal_tier_1", 40));

        // Sand + Cobblestone = Gold
        DefaultMutation.add(new SheepMutation("sand_tier_3", "cobblestone_tier_3", "gold_tier_1", 25));

        // Cobblestone + Andesite = Lapis Lazuli
        DefaultMutation.add(new SheepMutation("cobblestone_tier_3", "andesite_tier_3", "lapis_lazuli_tier_1", 30));

        // Sand + Netherrack = Redstone
        DefaultMutation.add(new SheepMutation("sand_tier_3", "netherrack_tier_3", "redstone_tier_1", 30));

        // Coal + Dripstone = Diamond
        DefaultMutation.add(new SheepMutation("coal_tier_2", "dripstone_tier_3", "diamond_tier_1", 25));

        //Diorite + Stone = Emerald
        DefaultMutation.add(new SheepMutation("diorite_tier_3", "stone_tier_3", "emerald_tier_1", 25));

        // Diorite + Soul Sand = Quartz
        DefaultMutation.add(new SheepMutation("diorite_tier_3", "soul_sand_tier_3", "quartz_tier_1", 30));

        //Cobblestone + Dripstone = Iron
        DefaultMutation.add(new SheepMutation("cobblestone_tier_3", "dripstone_tier_3", "iron_tier_1", 30));

        // Gold + Netherite Scrap = Netherite
        DefaultMutation.add(new SheepMutation("gold_tier_3", "netherite_scrap_tier_3", "netherite_tier_1", 20));

        // Netherrack + Soul Sand = Netherite Scrap
        DefaultMutation.add(new SheepMutation("netherrack_tier_3", "soul_sand_tier_3", "netherite_scrap_tier_1", 20));
        return DefaultMutation;
    }
}