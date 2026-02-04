package com.mathmout.resourcefulsheep;

import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.datagen.DataGenerators;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.item.ModCreativeTabs;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.ModItems;

import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ResourcefulSheepMod.MOD_ID)
public class ResourcefulSheepMod {
    public static final String MOD_ID = "resourceful_sheep";

    public ResourcefulSheepMod(IEventBus modEventBus, ModContainer modContainer) {

        // Config
        ConfigSheepTypeManager.init();
        ConfigSheepMutationManager.init();
        ConfigSheepSpawningManager.init();

        // Registries
        ModEntities.registerVariantEntity();
        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModCreativeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModItems.registerVariantSpawnEggs();

        // Components
        ModDataComponents.register(modEventBus);

        // Data Gen
        modEventBus.addListener(DataGenerators::gatherData);

        // Block Entities
        ModBlockEntities.register(modEventBus);

        // Blocks
        ModBlocks.register(modEventBus);

        // Menu
        ModMenuTypes.register(modEventBus);

        // Config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}