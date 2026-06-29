package com.mathmout.resourcefulsheep.config.mutations;

import java.util.ArrayList;
import java.util.List;

public class DefaultSheepMutations {
    public static List<SheepMutation> getDefaults() {
        List<SheepMutation> defaultMutation = new ArrayList<>();

        // Tier 1 -> Tier 2
        // Resources
        defaultMutation.add(new SheepMutation("cobblestone_tier_1", "cobblestone_tier_1", "cobblestone_tier_2", 40));
        defaultMutation.add(new SheepMutation("stone_tier_1", "stone_tier_1", "stone_tier_2", 40));
        defaultMutation.add(new SheepMutation("sand_tier_1", "sand_tier_1", "sand_tier_2", 40));
        defaultMutation.add(new SheepMutation("dripstone_tier_1", "dripstone_tier_1", "dripstone_tier_2", 40));
        defaultMutation.add(new SheepMutation("granite_tier_1", "granite_tier_1", "granite_tier_2", 40));
        defaultMutation.add(new SheepMutation("diorite_tier_1", "diorite_tier_1", "diorite_tier_2", 40));
        defaultMutation.add(new SheepMutation("andesite_tier_1", "andesite_tier_1", "andesite_tier_2", 40));
        defaultMutation.add(new SheepMutation("netherrack_tier_1", "netherrack_tier_1", "netherrack_tier_2", 40));
        defaultMutation.add(new SheepMutation("soul_sand_tier_1", "soul_sand_tier_1", "soul_sand_tier_2", 40));
        defaultMutation.add(new SheepMutation("coal_tier_1", "coal_tier_1", "coal_tier_2", 40));
        defaultMutation.add(new SheepMutation("copper_tier_1", "copper_tier_1", "copper_tier_2", 40));
        defaultMutation.add(new SheepMutation("redstone_tier_1", "redstone_tier_1", "redstone_tier_2", 40));
        defaultMutation.add(new SheepMutation("lapis_lazuli_tier_1", "lapis_lazuli_tier_1", "lapis_lazuli_tier_2", 40));
        defaultMutation.add(new SheepMutation("quartz_tier_1", "quartz_tier_1", "quartz_tier_2", 40));
        defaultMutation.add(new SheepMutation("gold_tier_1", "gold_tier_1", "gold_tier_2", 40));
        defaultMutation.add(new SheepMutation("iron_tier_1", "iron_tier_1", "iron_tier_2", 40));
        defaultMutation.add(new SheepMutation("diamond_tier_1", "diamond_tier_1", "diamond_tier_2", 40));
        defaultMutation.add(new SheepMutation("emerald_tier_1", "emerald_tier_1", "emerald_tier_2", 40));
        defaultMutation.add(new SheepMutation("netherite_scrap_tier_1", "netherite_scrap_tier_1", "netherite_scrap_tier_2", 40));
        defaultMutation.add(new SheepMutation("wither_tier_1", "wither_tier_1", "wither_tier_2", 20));
        defaultMutation.add(new SheepMutation("netherite_tier_1", "netherite_tier_1", "netherite_tier_2", 20));

        // Mobs
        defaultMutation.add(new SheepMutation("slime_tier_1", "slime_tier_1", "slime_tier_2", 30));
        defaultMutation.add(new SheepMutation("squid_tier_1", "squid_tier_1", "squid_tier_2", 30));
        defaultMutation.add(new SheepMutation("bee_tier_1", "bee_tier_1", "bee_tier_2", 30));
        defaultMutation.add(new SheepMutation("creeper_tier_1", "creeper_tier_1", "creeper_tier_2", 25));
        defaultMutation.add(new SheepMutation("blaze_tier_1", "blaze_tier_1", "blaze_tier_2", 25));
        defaultMutation.add(new SheepMutation("enderman_tier_1", "enderman_tier_1", "enderman_tier_2", 25));
        defaultMutation.add(new SheepMutation("breeze_tier_1", "breeze_tier_1", "breeze_tier_2", 20));
        defaultMutation.add(new SheepMutation("guardian_tier_1", "guardian_tier_1", "guardian_tier_2", 20));
        defaultMutation.add(new SheepMutation("ghast_tier_1", "ghast_tier_1", "ghast_tier_2", 15));
        defaultMutation.add(new SheepMutation("shulker_tier_1", "shulker_tier_1", "shulker_tier_2", 15));

        // Tier 2 -> Tier 3
        // Resources
        defaultMutation.add(new SheepMutation("cobblestone_tier_2", "cobblestone_tier_2", "cobblestone_tier_3", 25));
        defaultMutation.add(new SheepMutation("stone_tier_2", "stone_tier_2", "stone_tier_3", 25));
        defaultMutation.add(new SheepMutation("sand_tier_2", "sand_tier_2", "sand_tier_3", 25));
        defaultMutation.add(new SheepMutation("dripstone_tier_2", "dripstone_tier_2", "dripstone_tier_3", 25));
        defaultMutation.add(new SheepMutation("granite_tier_2", "granite_tier_2", "granite_tier_3", 25));
        defaultMutation.add(new SheepMutation("diorite_tier_2", "diorite_tier_2", "diorite_tier_3", 25));
        defaultMutation.add(new SheepMutation("andesite_tier_2", "andesite_tier_2", "andesite_tier_3", 25));
        defaultMutation.add(new SheepMutation("netherrack_tier_2", "netherrack_tier_2", "netherrack_tier_3", 25));
        defaultMutation.add(new SheepMutation("iron_tier_2", "iron_tier_2", "iron_tier_3", 25));
        defaultMutation.add(new SheepMutation("gold_tier_2", "gold_tier_2", "gold_tier_3", 25));
        defaultMutation.add(new SheepMutation("netherite_scrap_tier_2", "netherite_scrap_tier_2", "netherite_scrap_tier_3", 25));
        defaultMutation.add(new SheepMutation("soul_sand_tier_2", "soul_sand_tier_2", "soul_sand_tier_3", 25));
        defaultMutation.add(new SheepMutation("coal_tier_2", "coal_tier_2", "coal_tier_3", 25));
        defaultMutation.add(new SheepMutation("copper_tier_2", "copper_tier_2", "copper_tier_3", 25));
        defaultMutation.add(new SheepMutation("redstone_tier_2", "redstone_tier_2", "redstone_tier_3", 25));
        defaultMutation.add(new SheepMutation("lapis_lazuli_tier_2", "lapis_lazuli_tier_2", "lapis_lazuli_tier_3", 25));
        defaultMutation.add(new SheepMutation("quartz_tier_2", "quartz_tier_2", "quartz_tier_3", 25));
        defaultMutation.add(new SheepMutation("diamond_tier_2", "diamond_tier_2", "diamond_tier_3", 25));
        defaultMutation.add(new SheepMutation("emerald_tier_2", "emerald_tier_2", "emerald_tier_3", 25));
        defaultMutation.add(new SheepMutation("wither_tier_2", "wither_tier_2", "wither_tier_3", 15));
        defaultMutation.add(new SheepMutation("netherite_tier_2", "netherite_tier_2", "netherite_tier_3", 15));

        // Mobs
        defaultMutation.add(new SheepMutation("slime_tier_2", "slime_tier_2", "slime_tier_3", 20));
        defaultMutation.add(new SheepMutation("squid_tier_2", "squid_tier_2", "squid_tier_3", 20));
        defaultMutation.add(new SheepMutation("bee_tier_2", "bee_tier_2", "bee_tier_3", 20));
        defaultMutation.add(new SheepMutation("creeper_tier_2", "creeper_tier_2", "creeper_tier_3", 15));
        defaultMutation.add(new SheepMutation("blaze_tier_2", "blaze_tier_2", "blaze_tier_3", 15));
        defaultMutation.add(new SheepMutation("enderman_tier_2", "enderman_tier_2", "enderman_tier_3", 15));
        defaultMutation.add(new SheepMutation("breeze_tier_2", "breeze_tier_2", "breeze_tier_3", 15));
        defaultMutation.add(new SheepMutation("guardian_tier_2", "guardian_tier_2", "guardian_tier_3", 15));
        defaultMutation.add(new SheepMutation("ghast_tier_2", "ghast_tier_2", "ghast_tier_3", 10));
        defaultMutation.add(new SheepMutation("shulker_tier_2", "shulker_tier_2", "shulker_tier_3", 10));

        // Cobblestone + Stone = Diorite
        defaultMutation.add(new SheepMutation("cobblestone_tier_3", "stone_tier_3", "diorite_tier_1", 30));

        // Cobblestone + Stone = Granite
        defaultMutation.add(new SheepMutation("cobblestone_tier_3", "stone_tier_3", "granite_tier_1", 30));

        // Cobblestone + Stone = Andesite
        defaultMutation.add(new SheepMutation("cobblestone_tier_3", "stone_tier_3", "andesite_tier_1", 30));

        // Granite + Dripstone = Copper
        defaultMutation.add(new SheepMutation("granite_tier_3", "dripstone_tier_3", "copper_tier_1", 40));

        // Cobblestone + Andesite = Coal
        defaultMutation.add(new SheepMutation("cobblestone_tier_3", "andesite_tier_3", "coal_tier_1", 40));

        // Sand + Cobblestone = Gold
        defaultMutation.add(new SheepMutation("sand_tier_3", "cobblestone_tier_3", "gold_tier_1", 25));

        // Cobblestone + Andesite = Lapis Lazuli
        defaultMutation.add(new SheepMutation("cobblestone_tier_3", "andesite_tier_3", "lapis_lazuli_tier_1", 30));

        // Sand + Netherrack = Redstone
        defaultMutation.add(new SheepMutation("sand_tier_3", "netherrack_tier_3", "redstone_tier_1", 30));

        // Coal + Dripstone = Diamond
        defaultMutation.add(new SheepMutation("coal_tier_3", "dripstone_tier_3", "diamond_tier_1", 25));

        //Diorite + Stone = Emerald
        defaultMutation.add(new SheepMutation("diorite_tier_3", "stone_tier_3", "emerald_tier_1", 25));

        // Diorite + Soul Sand = Quartz
        defaultMutation.add(new SheepMutation("diorite_tier_3", "soul_sand_tier_3", "quartz_tier_1", 30));

        //Cobblestone + Dripstone = Iron
        defaultMutation.add(new SheepMutation("cobblestone_tier_3", "dripstone_tier_3", "iron_tier_1", 30));

        // Gold + Netherite Scrap = Netherite
        defaultMutation.add(new SheepMutation("gold_tier_3", "netherite_scrap_tier_3", "netherite_tier_1", 20));

        // Netherrack + Soul Sand = Netherite Scrap
        defaultMutation.add(new SheepMutation("netherrack_tier_3", "soul_sand_tier_3", "netherite_scrap_tier_1", 20));
        return defaultMutation;
    }
}