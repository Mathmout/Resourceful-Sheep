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

    static {
        // DNA Sequencer
        BUILDER.push("dna_sequencer");

        DNA_SEQUENCER_CAPACITY = BUILDER
                .comment("Max energy capacity of the DNA Sequencer (FE)")
                .defineInRange("capacity", 500_000, 1, Integer.MAX_VALUE);

        DNA_SEQUENCER_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("consumption", 1000, 1, Integer.MAX_VALUE);

        DNA_SEQUENCER_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("transfer_rate", 2000, 1, Integer.MAX_VALUE);

        DNA_SEQUENCER_ANALYZE_TIME = BUILDER
                .comment("Time required to sequence DNA (in ticks)")
                .defineInRange("analyze_time", 100, 1, Integer.MAX_VALUE);

        BUILDER.pop(); // Fin de section

        // DNA Splicer
        BUILDER.push("dna_splicer");

        DNA_SPLICER_CAPACITY = BUILDER
                .comment("Max energy capacity of the DNA Splicer (FE)")
                .defineInRange("capacity", 10_000_000, 1, Integer.MAX_VALUE);

        DNA_SPLICER_CONSUMPTION = BUILDER
                .comment("Energy consumed per operation (FE/t)")
                .defineInRange("consumption", 10000, 1, Integer.MAX_VALUE);

        DNA_SPLICER_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("transfer_rate", 20000, 1, Integer.MAX_VALUE);

        DNA_SPLICER_ANALYZE_TIME = BUILDER
                .comment("Time required to splice DNA (in ticks, 1s = 20 ticks)")
                .defineInRange("splicer_time", 6000, 1, Integer.MAX_VALUE);

        DNA_SPLICER_RANGE = BUILDER
                .comment("The radius (in blocks) to search for a DNA Sequencer. 1 means adjacent, diagonals included.")
                .defineInRange("splicer_range", 1, 1, Integer.MAX_VALUE);

        BUILDER.pop(); // Fin de section

        // Sheep Scanner
        BUILDER.push("sheep_scanner");

        SHEEP_SCANNER_CAPACITY = BUILDER
                .comment("Max energy capacity of the Sheep Scanner Item (FE)")
                .defineInRange("capacity", 20_000, 0, Integer.MAX_VALUE);

        SHEEP_SCANNER_CONSUMPTION = BUILDER
                .comment("Energy consumed per scan (FE)")
                .defineInRange("consumption_per_scan", 2000, 0, Integer.MAX_VALUE);

        SHEEP_SCANNER_MAX_TRANSFER = BUILDER
                .comment("Max energy transfer rate for recharging the item (FE/t)")
                .defineInRange("recharge_rate", 100, 0, Integer.MAX_VALUE);

        BUILDER.pop(); // Fin de section
    }
    static final ModConfigSpec SPEC = BUILDER.build();
}