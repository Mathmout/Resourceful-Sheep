package com.mathmout.resourcefulsheep.block;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.custom.DNASequencerBlock;
import com.mathmout.resourcefulsheep.block.custom.DNASplicerBlock;
import com.mathmout.resourcefulsheep.block.custom.ResourcefulWoolBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeCasingBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeControllerBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.port.CentrifugeEnergyPortBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.port.CentrifugeFluidPortBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.port.item.CentrifugeItemInPortBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.port.item.CentrifugeItemOutPortBlock;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ResourcefulSheepMod.MOD_ID);
    public static final Map<String, DeferredBlock<Block>> RESOURCEFUL_WOOL_BLOCKS = new HashMap<>();

    // Nouvelles maps pour la centrifugeuse, triées par Tier
    public static final Map<CentrifugeTier, DeferredBlock<Block>> CENTRIFUGE_CASINGS = new EnumMap<>(CentrifugeTier.class);
    public static final Map<CentrifugeTier, DeferredBlock<Block>> CENTRIFUGE_CONTROLLERS = new EnumMap<>(CentrifugeTier.class);
    public static final Map<CentrifugeTier, DeferredBlock<Block>> CENTRIFUGE_ITEM_IN_PORTS = new EnumMap<>(CentrifugeTier.class);
    public static final Map<CentrifugeTier, DeferredBlock<Block>> CENTRIFUGE_ITEM_OUT_PORTS = new EnumMap<>(CentrifugeTier.class);
    public static final Map<CentrifugeTier, DeferredBlock<Block>> CENTRIFUGE_ENERGY_PORTS = new EnumMap<>(CentrifugeTier.class);
    public static final Map<CentrifugeTier, DeferredBlock<Block>> CENTRIFUGE_FLUID_PORTS = new EnumMap<>(CentrifugeTier.class);

    public static final DeferredBlock<Block> DNA_SEQUENCER = registerBlock("dna_sequencer",
            () -> new DNASequencerBlock(
                    BlockBehaviour.Properties.of() // propriété vierge
                            .strength(4f) // Solidité
                            .explosionResistance(1200f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion() // Empêche le X-Ray
            )
    );

    public static final DeferredBlock<Block> DNA_SPLICER = registerBlock("dna_splicer",
            () -> new DNASplicerBlock(
                    BlockBehaviour.Properties.of() // propriété vierge
                            .strength(4f) // Solidité
                            .explosionResistance(1200f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion() // Empêche le X-Ray
            )
    );

    public static void registerVariantWools() {
        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {

            DeferredBlock<Block> woolBlock = BLOCKS.register(variant.Id() + "_wool",
                    () -> new ResourcefulWoolBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(0.8f) // Comme la laine vanilla
                                    .sound(SoundType.WOOL)
                                    .ignitedByLava() // Ça brûle !
                    )
            );
            RESOURCEFUL_WOOL_BLOCKS.put(variant.Id(), woolBlock);
        }
    }

    public static void registerCentrifugeBlocks() {
        for (CentrifugeTier tier : CentrifugeTier.values()) {
            // Création d'un préfixe dynamique, ex: "basic_centrifuge_casing"
            String prefix = tier.name().toLowerCase() + "_centrifuge_";

            // Casings
            CENTRIFUGE_CASINGS.put(tier, registerBlock(prefix + "casing",
                    () -> new CentrifugeCasingBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops(), tier)
            ));

            // Controllers
            CENTRIFUGE_CONTROLLERS.put(tier, registerBlock(prefix + "controller",
                    () -> new CentrifugeControllerBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops(), tier)
            ));

            // Ports Item In
            CENTRIFUGE_ITEM_IN_PORTS.put(tier, registerBlock(prefix + "item_in_port",
                    () -> new CentrifugeItemInPortBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops(), tier)
            ));

            // Ports Item Out
            CENTRIFUGE_ITEM_OUT_PORTS.put(tier, registerBlock(prefix + "item_out_port",
                    () -> new CentrifugeItemOutPortBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops(), tier)
            ));

            // Ports Energy
            CENTRIFUGE_ENERGY_PORTS.put(tier, registerBlock(prefix + "energy_port",
                    () -> new CentrifugeEnergyPortBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops(), tier)
            ));

            // Ports Fluid
            if (tier != CentrifugeTier.BASIC) {
                CENTRIFUGE_FLUID_PORTS.put(tier, registerBlock(prefix + "fluid_port",
                        () -> new CentrifugeFluidPortBlock(BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops(), tier)
                ));
            }
        }
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, deferredBlock);
        return deferredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}