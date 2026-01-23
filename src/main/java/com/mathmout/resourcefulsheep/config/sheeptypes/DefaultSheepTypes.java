package com.mathmout.resourcefulsheep.config.sheeptypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSheepTypes {
    public static List<SheepTypeData> getDefaults() {
        List<SheepTypeData> DefaultSheep = new ArrayList<>();

        // Cobblestone
        DefaultSheep.add(new SheepTypeData("cobblestone", "#808080", "#555555",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:cobblestone", 12, 20)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:cobblestone", 20, 32)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:cobblestone", 32, 48)
                        ))
                )));

        // Stone
        DefaultSheep.add(new SheepTypeData("stone", "#888888", "#555555",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:stone", 10, 18)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:stone", 18, 28)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:stone", 28, 32)
                        ))
                )));

        // Sand
        DefaultSheep.add(new SheepTypeData("sand", "#f0d890", "#d2b56b",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:sand", 16, 24)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:sand", 24, 36)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:sand", 36, 50)
                        ))
                )));

        // Dripstone
        DefaultSheep.add(new SheepTypeData("dripstone", "#c2b8a3", "#9e8f74",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:dripstone_block", 8, 14)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:dripstone_block", 14, 22)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:dripstone_block", 22, 34)
                        ))
                )));

        // Granite
        DefaultSheep.add(new SheepTypeData("granite", "#d16b6b", "#9c4747",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:granite", 10, 16)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:granite", 16, 24)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:granite", 24, 35)
                        ))
                )));

        // Diorite
        DefaultSheep.add(new SheepTypeData("diorite", "#c5c5c5", "#8a8a8a",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:diorite", 10, 16)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:diorite", 16, 24)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:diorite", 24, 33)
                        ))
                )));

        // Andesite
        DefaultSheep.add(new SheepTypeData("andesite", "#888888", "#666666",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:andesite", 10, 16)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:andesite", 16, 24)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:andesite", 24, 32)
                        ))
                )));

        // Netherrack
        DefaultSheep.add(new SheepTypeData("netherrack", "#5a2b2b", "#3d1d1d",
                List.of("minecraft:nether_wart"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherrack", 12, 20)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherrack", 20, 30)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherrack", 30, 42)
                        ))
                )));

        // Soul Sand
        DefaultSheep.add(new SheepTypeData("soul_sand", "#4a3e32", "#30261c",
                List.of("minecraft:nether_wart"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:soul_sand", 10, 18)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:soul_sand", 18, 28)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:soul_sand", 28, 40)
                        ))
                )));

        // Coal
        DefaultSheep.add(new SheepTypeData("coal", "#252525", "#101015",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        // Tier 1 : Beaucoup de charbon
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:coal", 8, 16) // 12
                        )),
                        // Tier 2 : Charbon + Petite chance de Bloc
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:coal", 10, 20),   // 15 + 9 = 24
                                new SheepTypeData.TierData.DroppedItems("minecraft:coal_block", 0, 2)
                        )),
                        // Tier 3 : Surtout des blocs
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:coal", 15, 22),  //71
                                new SheepTypeData.TierData.DroppedItems("minecraft:coal_block", 3, 7)
                        ))
                )));

        // Iron
        DefaultSheep.add(new SheepTypeData("iron", "#D8D8D8", "#A0A0A0",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_nugget", 19, 28),
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_ingot", 1, 3)   //5.7
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_nugget", 16, 24), // 10.2
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_ingot", 2, 5),
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_block", 0, 1)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_ingot", 5, 15), // 41.5
                                new SheepTypeData.TierData.DroppedItems("minecraft:iron_block", 1, 3)
                        ))
                )));

        // Copper
        DefaultSheep.add(new SheepTypeData("copper", "#e77c56", "#8a4129",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:copper_ingot", 3, 8)   //6
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:copper_ingot", 7, 12),   // 32
                                new SheepTypeData.TierData.DroppedItems("minecraft:copper_block", 1, 4)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:copper_ingot", 5, 10), // 57
                                new SheepTypeData.TierData.DroppedItems("minecraft:copper_block", 3, 8)
                        ))
                )));

        // Redstone
        DefaultSheep.add(new SheepTypeData("redstone", "#FF0000", "#8B0000",
                List.of("minecraft:nether_wart", "minecraft:wheat"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:grass_block", "minecraft:dirt",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:redstone", 10, 20) //15
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:redstone", 14, 23),  // 32
                                new SheepTypeData.TierData.DroppedItems("minecraft:redstone_block", 1, 2)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new  SheepTypeData.TierData.DroppedItems("minecraft:redstone", 11, 15),
                                new SheepTypeData.TierData.DroppedItems("minecraft:redstone_block", 4, 8)   //67
                        ))
                )));

        // Lapis Lazuli
        DefaultSheep.add(new SheepTypeData("lapis_lazuli", "#0042A3", "#002E8C",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:lapis_lazuli", 8, 16)    //12
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:lapis_lazuli", 16, 24),  //38
                                new SheepTypeData.TierData.DroppedItems("minecraft:lapis_block", 1, 3)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:lapis_lazuli", 12, 17),  //64
                                new SheepTypeData.TierData.DroppedItems("minecraft:lapis_block", 4, 7)
                        ))
                )));

        // Quartz
        DefaultSheep.add(new SheepTypeData("quartz", "#EAE5DE", "#D4CCC5",
                List.of("minecraft:nether_wart", "minecraft:wheat"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:grass_block", "minecraft:dirt",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:quartz", 10, 18)     //14
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:quartz", 7, 15),    //27
                                new SheepTypeData.TierData.DroppedItems("minecraft:quartz_block", 3, 5)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:quartz", 4, 11),    //51.5
                                new SheepTypeData.TierData.DroppedItems("minecraft:quartz_block", 8, 14)
                        ))
                )));

        // Gold
        DefaultSheep.add(new SheepTypeData("gold", "#fdf55f", "#dc9613",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:gold_nugget", 12, 24)
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:gold_ingot", 4, 10),
                                new SheepTypeData.TierData.DroppedItems("minecraft:gold_nugget", 5, 15)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:gold_block", 2, 5),
                                new SheepTypeData.TierData.DroppedItems("minecraft:gold_ingot", 2, 6)
                        ))
                )));

        // Diamond
        DefaultSheep.add(new SheepTypeData("diamond", "#63F7F2", "#00E0D8",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:diamond", 1, 4)         //2.5
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:diamond", 4, 8)         //6
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:diamond", 1, 3),        //15.5
                                new SheepTypeData.TierData.DroppedItems("minecraft:diamond_block", 1, 2)
                        ))
                )));

        // Emerald
        DefaultSheep.add(new SheepTypeData("emerald", "#00A82B", "#008C23",
                List.of(),
                false,
                List.of(),
                new HashMap<>(Map.of()),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:emerald", 3, 6)          //4.5
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:emerald", 2, 5),         //8
                                new SheepTypeData.TierData.DroppedItems("minecraft:emerald_block", 0, 1)
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:emerald", 1, 3),         //20
                                new SheepTypeData.TierData.DroppedItems("minecraft:emerald_block", 1, 3)
                        ))
                )));

        // Netherite Scrap
        DefaultSheep.add(new SheepTypeData("netherite_scrap", "#654740", "#4a281d",
                List.of("minecraft:nether_wart"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_scrap", 0, 2)  //1
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_scrap", 3, 7)  //5
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_scrap", 8, 11) //9.5
                        ))
                )));

        // Netherite
        DefaultSheep.add(new SheepTypeData("netherite", "#4d494d", "#31292a",
                List.of("minecraft:nether_wart", "minecraft:wheat"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:grass_block", "minecraft:dirt",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_ingot", 1, 2)  //1.5
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_ingot", 2, 4)  //3
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_block", 1, 2), //6
                                new SheepTypeData.TierData.DroppedItems("minecraft:netherite_ingot", 0, 1)
                        ))
                )));

        // Nether Star
        DefaultSheep.add(new SheepTypeData("nether_star", "#e7e5ff", "#777bc7",
                List.of("minecraft:nether_wart"),
                true,
                List.of(),
                new HashMap<>(Map.of(
                        "minecraft:crimson_nylium", "minecraft:netherrack",
                        "minecraft:warped_nylium", "minecraft:netherrack",
                        "minecraft:crimson_fungus", "minecraft:air",
                        "minecraft:crimson_roots", "minecraft:air",
                        "minecraft:warped_fungus","minecraft:air",
                        "minecraft:warped_roots", "minecraft:air"
                )),
                List.of(
                        new SheepTypeData.TierData(1, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:nether_star", 0, 0)  //0
                        )),
                        new SheepTypeData.TierData(2, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:nether_star", 0, 1)  //0.5
                        )),
                        new SheepTypeData.TierData(3, List.of(
                                new SheepTypeData.TierData.DroppedItems("minecraft:nether_star", 1, 2)  //1.5
                        ))
                )));

        return DefaultSheep;
    }
}