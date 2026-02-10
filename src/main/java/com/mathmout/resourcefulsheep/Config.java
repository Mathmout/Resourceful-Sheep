package com.mathmout.resourcefulsheep;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // DNA Sequencer
    public static ModConfigSpec.IntValue DNA_SEQUENCER_CAPACITY;
    public static ModConfigSpec.IntValue DNA_SEQUENCER_CONSUMPTION;
    public static ModConfigSpec.IntValue DNA_SEQUENCER_TRANSFER;
    public static ModConfigSpec.IntValue DNA_SEQUENCER_ANALYZE_TIME;

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
                .comment("Energy consumed per operation (FE)")
                .defineInRange("consumption", 1000, 1, Integer.MAX_VALUE);

        DNA_SEQUENCER_TRANSFER = BUILDER
                .comment("Max energy transfer rate per tick (FE/t)")
                .defineInRange("transfer_rate", 2000, 1, Integer.MAX_VALUE);

        DNA_SEQUENCER_ANALYZE_TIME = BUILDER
                .comment("Time required to sequence DNA (in ticks)")
                .defineInRange("analyze_time", 100, 1, Integer.MAX_VALUE);

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