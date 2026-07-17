package com.mathmout.resourcefulsheep.block.entity;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.entity.port.CentrifugeEnergyPortBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.port.CentrifugeFluidPortBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.port.item.CentrifugeItemInPortBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.port.item.CentrifugeItemOutPortBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourcefulSheepMod.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<DNASequencerBlockEntity>> DNA_SEQUENCER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dna_sequencer_be", () ->
                    BlockEntityType.Builder.of(DNASequencerBlockEntity::new,
                            ModBlocks.DNA_SEQUENCER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<DNASplicerBlockEntity>> DNA_SPLICER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dna_splicer_be", () ->
                    BlockEntityType.Builder.of(DNASplicerBlockEntity::new,
                            ModBlocks.DNA_SPLICER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CentrifugeControllerBlockEntity>> CENTRIFUGE_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("centrifuge_controller_be", () ->
                    BlockEntityType.Builder.of(CentrifugeControllerBlockEntity::new,
                            ModBlocks.CENTRIFUGE_CONTROLLERS.values().stream().map(Supplier::get).toArray(Block[]::new)
                    ).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CentrifugeItemOutPortBlockEntity>> CENTRIFUGE_ITEM_OUT_PORT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("centrifuge_item_out_port_be", () ->
                    BlockEntityType.Builder.of(CentrifugeItemOutPortBlockEntity::new,
                            ModBlocks.CENTRIFUGE_ITEM_OUT_PORTS.values().stream().map(Supplier::get).toArray(Block[]::new)
                    ).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CentrifugeItemInPortBlockEntity>> CENTRIFUGE_ITEM_IN_PORT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("centrifuge_item_in_port_be", () ->
                    BlockEntityType.Builder.of(CentrifugeItemInPortBlockEntity::new,
                            ModBlocks.CENTRIFUGE_ITEM_IN_PORTS.values().stream().map(Supplier::get).toArray(Block[]::new)
                    ).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CentrifugeEnergyPortBlockEntity>> CENTRIFUGE_ENERGY_PORT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("centrifuge_energy_port_be", () ->
                    BlockEntityType.Builder.of(CentrifugeEnergyPortBlockEntity::new,
                            ModBlocks.CENTRIFUGE_ENERGY_PORTS.values().stream().map(Supplier::get).toArray(Block[]::new)
                    ).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<CentrifugeFluidPortBlockEntity>> CENTRIFUGE_FLUID_PORT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("centrifuge_fluid_port_be", () ->
                    BlockEntityType.Builder.of(CentrifugeFluidPortBlockEntity::new,
                            ModBlocks.CENTRIFUGE_FLUID_PORTS.values().stream().map(Supplier::get).toArray(Block[]::new)
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}