package com.mathmout.resourcefulsheep;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // DNA Sequencer
    public static ModConfigSpec.IntValue DNA_SEQUENCER_CAPACITY;
    public static ModConfigSpec.IntValue DNA_SEQUENCER_CONSUMPTION;
    public static ModConfigSpec.IntValue DNA_SEQUENCER_TRANSFER;
    public static ModConfigSpec.IntValue DNA_SEQUENCER_ANALYZE_TIME;

    // DNA Splicer
    public static ModConfigSpec.IntValue DNA_SPLICER_CAPACITY;
    public static ModConfigSpec.IntValue DNA_SPLICER_CONSUMPTION;
    public static ModConfigSpec.IntValue DNA_SPLICER_TRANSFER;
    public static ModConfigSpec.IntValue DNA_SPLICER_ANALYZE_TIME;
    public static ModConfigSpec.IntValue DNA_SPLICER_RANGE;

    // Sheep Scanner
    public static ModConfigSpec.IntValue SHEEP_SCANNER_CAPACITY;
    public static ModConfigSpec.IntValue SHEEP_SCANNER_CONSUMPTION;
    public static ModConfigSpec.IntValue SHEEP_SCANNER_MAX_TRANSFER;

    // Wools
    public static ModConfigSpec.BooleanValue DISPLAY_WOOLS;

    // Centrifuge
    public static ModConfigSpec.IntValue CENTRIFUGE_BASIC_SPEED;
    public static ModConfigSpec.IntValue CENTRIFUGE_BASIC_ENERGY_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_BASIC_ENERGY_CONSUMPTION;
    public static ModConfigSpec.IntValue CENTRIFUGE_BASIC_FLUID_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_BASIC_ENERGY_TRANSFER;

    public static ModConfigSpec.IntValue CENTRIFUGE_ADVANCED_SPEED;
    public static ModConfigSpec.IntValue CENTRIFUGE_ADVANCED_ENERGY_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_ADVANCED_ENERGY_CONSUMPTION;
    public static ModConfigSpec.IntValue CENTRIFUGE_ADVANCED_FLUID_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_ADVANCED_ENERGY_TRANSFER;

    public static ModConfigSpec.IntValue CENTRIFUGE_ELITE_SPEED;
    public static ModConfigSpec.IntValue CENTRIFUGE_ELITE_ENERGY_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_ELITE_ENERGY_CONSUMPTION;
    public static ModConfigSpec.IntValue CENTRIFUGE_ELITE_FLUID_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_ELITE_ENERGY_TRANSFER;

    public static ModConfigSpec.IntValue CENTRIFUGE_ULTIMATE_SPEED;
    public static ModConfigSpec.IntValue CENTRIFUGE_ULTIMATE_ENERGY_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_ULTIMATE_ENERGY_CONSUMPTION;
    public static ModConfigSpec.IntValue CENTRIFUGE_ULTIMATE_FLUID_CAPACITY;
    public static ModConfigSpec.IntValue CENTRIFUGE_ULTIMATE_ENERGY_TRANSFER;

    static {
        // DNA Sequencer
        BUILDER.push("dna_sequencer");

        DNA_SEQUENCER_CAPACITY = BUILDER
                .comment("Max energy capacity of the DNA Sequencer (FE)")
                .defineInRange("capacity", 500_000, 0, Integer.MAX_VALUE);

        DNA_SEQUENCER_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("consumption", 1000, 0, Integer.MAX_VALUE);

        DNA_SEQUENCER_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("transfer_rate", 2000, 0, Integer.MAX_VALUE);

        DNA_SEQUENCER_ANALYZE_TIME = BUILDER
                .comment("Time required to sequence DNA (in ticks)")
                .defineInRange("analyze_time", 100, 1, Integer.MAX_VALUE);

        BUILDER.pop(); // Fin de section

        // DNA Splicer
        BUILDER.push("dna_splicer");

        DNA_SPLICER_CAPACITY = BUILDER
                .comment("Max energy capacity of the DNA Splicer (FE)")
                .defineInRange("capacity", 10_000_000, 0, Integer.MAX_VALUE);

        DNA_SPLICER_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("consumption", 10000, 0, Integer.MAX_VALUE);

        DNA_SPLICER_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("transfer_rate", 20000, 0, Integer.MAX_VALUE);

        DNA_SPLICER_ANALYZE_TIME = BUILDER
                .comment("Time required to splice DNA (in ticks, 1s = 20 ticks)")
                .defineInRange("splicer_time", 6000, 0, Integer.MAX_VALUE);

        DNA_SPLICER_RANGE = BUILDER
                .comment("The radius (in blocks) to search for a DNA Sequencer. 1 means adjacent, diagonals included.")
                .defineInRange("splicer_range", 1, 1, Integer.MAX_VALUE);

        BUILDER.pop(); // Fin de section

        // Sheep Scanner
        BUILDER.push("sheep_scanner");

        SHEEP_SCANNER_CAPACITY = BUILDER
                .comment("Max energy capacity of the Sheep Scanner Item (FE)")
                .defineInRange("capacity", 50_000, 0, Integer.MAX_VALUE);

        SHEEP_SCANNER_CONSUMPTION = BUILDER
                .comment("Energy consumed per scan (FE)")
                .defineInRange("consumption_per_scan", 2000, 0, Integer.MAX_VALUE);

        SHEEP_SCANNER_MAX_TRANSFER = BUILDER
                .comment("Max energy transfer rate for recharging the item (FE/t)")
                .defineInRange("recharge_rate", 100, 0, Integer.MAX_VALUE);

        BUILDER.pop(); // Fin de section

        BUILDER.push("display_wools");

            DISPLAY_WOOLS = BUILDER
                    .comment("Displays wools in creative inventory and in JEI")
                    .define("display_wools", false);

        BUILDER.pop();

        // --- CENTRIFUGE ---
        BUILDER.push("centrifuge");

        // BASIC TIER
        BUILDER.push("basic_tier");

        CENTRIFUGE_BASIC_SPEED = BUILDER
                .comment("Time required to process items (in ticks)")
                .defineInRange("speed", 100, 1, Integer.MAX_VALUE);

        CENTRIFUGE_BASIC_ENERGY_CAPACITY = BUILDER
                .comment("Max energy capacity (FE)")
                .defineInRange("energy_capacity", 10_000_000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_BASIC_ENERGY_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("energy_consumption", 10000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_BASIC_FLUID_CAPACITY = BUILDER
                .comment("Max fluid capacity (mB)")
                .defineInRange("fluid_capacity", 10000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_BASIC_ENERGY_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("energy_transfer", 20_000, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        // ADVANCED TIER
        BUILDER.push("advanced_tier");

        CENTRIFUGE_ADVANCED_SPEED = BUILDER
                .comment("Time required to process items (in ticks)")
                .defineInRange("speed", 80, 1, Integer.MAX_VALUE);

        CENTRIFUGE_ADVANCED_ENERGY_CAPACITY = BUILDER
                .comment("Max energy capacity (FE)")
                .defineInRange("energy_capacity", 20_000_000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ADVANCED_ENERGY_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("energy_consumption", 30000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ADVANCED_FLUID_CAPACITY = BUILDER
                .comment("Max fluid capacity (mB)")
                .defineInRange("fluid_capacity", 20000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ADVANCED_ENERGY_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("energy_transfer", 40_000, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        // ELITE TIER
        BUILDER.push("elite_tier");

        CENTRIFUGE_ELITE_SPEED = BUILDER
                .comment("Time required to process items (in ticks)")
                .defineInRange("speed", 50, 1, Integer.MAX_VALUE);

        CENTRIFUGE_ELITE_ENERGY_CAPACITY = BUILDER
                .comment("Max energy capacity (FE)")
                .defineInRange("energy_capacity", 50_000_000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ELITE_ENERGY_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("energy_consumption", 50000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ELITE_FLUID_CAPACITY = BUILDER
                .comment("Max fluid capacity (mB)")
                .defineInRange("fluid_capacity", 50000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ELITE_ENERGY_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("energy_transfer", 100_000, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        // ULTIMATE TIER
        BUILDER.push("ultimate_tier");

        CENTRIFUGE_ULTIMATE_SPEED = BUILDER
                .comment("Time required to process items (in ticks)")
                .defineInRange("speed", 20, 1, Integer.MAX_VALUE);

        CENTRIFUGE_ULTIMATE_ENERGY_CAPACITY = BUILDER
                .comment("Max energy capacity (FE)")
                .defineInRange("energy_capacity", 100_000_000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ULTIMATE_ENERGY_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("energy_consumption", 90000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ULTIMATE_FLUID_CAPACITY = BUILDER
                .comment("Max fluid capacity (mB)")
                .defineInRange("fluid_capacity", 100000, 0, Integer.MAX_VALUE);

        CENTRIFUGE_ULTIMATE_ENERGY_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("energy_transfer", 200_000, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.pop(); // Fin de la section centrifuge
    }
    static final ModConfigSpec SPEC = BUILDER.build();
}