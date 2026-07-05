package com.mathmout.resourcefulsheep;

import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import com.mathmout.resourcefulsheep.config.dnacrossbreeding.ConfigDNACrossbreedingManager;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.config.spawning.ConfigSheepSpawningManager;
import com.mathmout.resourcefulsheep.datagen.DataGenerators;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.item.ModCreativeTabs;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.ModItems;

import com.mathmout.resourcefulsheep.loot.ModLootModifiers;
import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(ResourcefulSheepMod.MOD_ID)
public class ResourcefulSheepMod {
    public static final String MOD_ID = "resourceful_sheep";

    public ResourcefulSheepMod(IEventBus modEventBus, ModContainer modContainer) {

        // Config
        ConfigSheepTypeManager.init();
        ConfigSheepMutationManager.init();
        ConfigSheepSpawningManager.init();
        ConfigDNACrossbreedingManager.init();

        // Registries
        ModEntities.registerVariantEntity();
        ModEntities.ENTITY_TYPES.register(modEventBus);

        // Creative Tabs
        ModCreativeTabs.register(modEventBus);

        // Blocks
        ModBlocks.register(modEventBus);
        ModBlocks.registerVariantWools();

        // Items
        ModItems.registerVariantSpawnEggs();
        ModItems.registerVariantWools();
        ModItems.register(modEventBus);

        // Components
        ModDataComponents.register(modEventBus);

        // Data Gen
        modEventBus.addListener(DataGenerators::gatherData);

        // Block Entities
        ModBlockEntities.register(modEventBus);

        // Menu
        ModMenuTypes.register(modEventBus);

        // Loot Table
        ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);

        // Config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}

// TODO
//
// TAGS
// Ajouter la possibilité d'utiliser les tags de mouton dans les json de sheep_spawning.❌
//
// MACHINES
// Faire en sorte que les machine puisse fonctionner sans énergie si la config est faite ainsi
// et adapter l'interface en conséquence et les tooltips. ✅
// Ajouter un bouton supprimer pour les cards dans le Splicer. ✅
// Possible problème en cas d'explosion d'une machine elle ne risque de ne pas droper prbl avec playerWillDestroy. ✅
// Enhanced Splicer ❌
// Si on fait Shif on peut voir l'energie au FE pres.❌
//
// Probleme avec la texture du bee sheep. ✅
// Le lasso montre toute les info d'un mouton comme le Scanner... ❌
//
// ETA
// Ajouter un indicateur sur le temps restant dans les machines.✅
//
// Suspicious egg
// Petit bug sur la couleur de l'egg quand on fusion 2 ADN qui sont sencés rien donner.✅
//
// SHEEP SCANNER
// Ajouter une vraie interface pour le SheepScanner. ✅
// Ajouter un mécanisme afin de lier un SheepScanner a un DNA Sequencer on peut utiliser les UUID.❌
//
// CENTRIFUGE
// Ajouter des laines pour tous les moutons. ✅
// Ajouter la centrifugeuse.❌
//
// JEI
// Changer l'organisation des catégories JEI par rapport a la centrifuge.❌
// Faire en sorte qu'on puisse clicker sur un mouton dans JEI.❌
//
// Lasso
// Changer le nom et la texture mais garder le focntionnement global.❌
//
// TEXTURES
// Amériorer les algo pour les texture des moutons et des laines.❌
//
// CAGES
// Ajouter des cages pour enfermer des moutons pour qu'ils produisent de la laine dans une machine.❌
// Répresenter un environment naturel dans la cage vitrée customisable.❌