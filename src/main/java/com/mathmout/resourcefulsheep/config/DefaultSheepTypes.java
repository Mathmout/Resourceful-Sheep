package com.mathmout.resourcefulsheep.config;

import java.util.ArrayList;
import java.util.List;

public class DefaultSheepTypes {
    public static List<SheepTypeData> getDefaults() {
        List<SheepTypeData> DefaultSheep = new ArrayList<>();

        // Vérifier que cela n'ecrase pas la config existante !

        // Coal
        DefaultSheep.add(new SheepTypeData("coal","#252525", "#101015", List.of(
                new SheepTypeData.TierData(1,"minecraft:coal", 8, 13),
                new SheepTypeData.TierData(2,"minecraft:coal_block", 4, 8)
        )));

        // Iron
        DefaultSheep.add(new SheepTypeData("iron", "#D8D8D8", "#A0A0A0", List.of(
                new SheepTypeData.TierData(1,"minecraft:iron_nugget", 7, 11),
                new SheepTypeData.TierData(2,"minecraft:iron_ingot", 5, 11),
                new SheepTypeData.TierData(3,"minecraft:iron_block", 2, 7)
        )));

        // Copper
        DefaultSheep.add(new SheepTypeData("copper", "#e77c56", "#8a4129", List.of(
                new SheepTypeData.TierData(1,"minecraft:copper_ingot", 8, 14),
                new SheepTypeData.TierData(2,"minecraft:copper_block", 5, 9)
        )));

        // Redstone
        DefaultSheep.add(new SheepTypeData("redstone", "#FF0000", "#8B0000", List.of(
                new SheepTypeData.TierData(1,"minecraft:redstone", 8, 13),
                new SheepTypeData.TierData(2,"minecraft:redstone_block", 4, 7)
        )));

        // Lapis Lazuli
        DefaultSheep.add(new SheepTypeData("lapis_lazuli", "#0042A3", "#002E8C", List.of(
                new SheepTypeData.TierData(1,"minecraft:lapis_lazuli", 5, 17),
                new SheepTypeData.TierData(2,"minecraft:lapis_block", 3, 7)
        )));

        // Quartz
        DefaultSheep.add(new SheepTypeData("quartz", "#EAE5DE", "#D4CCC5", List.of(
                new SheepTypeData.TierData(1,"minecraft:quartz", 11, 17),
                new SheepTypeData.TierData(2,"minecraft:quartz_block", 8, 11)
        )));

        // Gold
        DefaultSheep.add(new SheepTypeData("gold", "#fdf55f", "#dc9613", List.of(
                new SheepTypeData.TierData(1,"minecraft:gold_nugget", 7, 11),
                new SheepTypeData.TierData(2,"minecraft:gold_ingot", 3, 8),
                new SheepTypeData.TierData(3,"minecraft:gold_block", 2, 5)
        )));

        // Diamond
        DefaultSheep.add(new SheepTypeData("diamond", "#63F7F2", "#00E0D8", List.of(
                new SheepTypeData.TierData(1,"minecraft:diamond", 1, 5),
                new SheepTypeData.TierData(2,"minecraft:diamond_block", 1, 3)
        )));

        // Emerald
        DefaultSheep.add(new SheepTypeData("emerald", "#00A82B", "#008C23", List.of(
                new SheepTypeData.TierData(1,"minecraft:emerald", 5, 8),
                new SheepTypeData.TierData(2,"minecraft:emerald_block", 2, 5)
        )));

        // Netherite Scrap
        DefaultSheep.add(new SheepTypeData("netherite_scrap", "#654740", "#4a281d", List.of(
                new SheepTypeData.TierData(1,"minecraft:netherite_scrap", 0, 1),
                new SheepTypeData.TierData(2,"minecraft:netherite_scrap", 3, 7),
                new SheepTypeData.TierData(3,"minecraft:netherite_scrap", 8, 11)
        )));

        // Netherite
        DefaultSheep.add(new SheepTypeData("netherite", "#4d494d", "#31292a", List.of(
                new SheepTypeData.TierData(1,"minecraft:netherite_ingot", 1, 3),
                new SheepTypeData.TierData(2,"minecraft:netherite_block", 0, 1)
        )));

        // Nether Star
        DefaultSheep.add(new SheepTypeData("nether_star", "#e7e5ff", "#777bc7", List.of(
                new SheepTypeData.TierData(1,"minecraft:nether_star", 0, 0),
                new SheepTypeData.TierData(2,"minecraft:nether_star", 0, 2)
        )));
        return DefaultSheep;
    }
}