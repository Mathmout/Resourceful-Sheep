package com.mathmout.resourcefulsheep.block.entity;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourcefulSheepMod.MOD_ID);

    public static final Supplier<BlockEntityType<DNASequencerBlockEntity>> DNA_SEQUENCER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dna_sequencer_be", () ->
                    BlockEntityType.Builder.of(DNASequencerBlockEntity::new,
                            ModBlocks.DNA_SEQUENCER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}