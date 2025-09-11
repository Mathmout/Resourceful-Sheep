package com.mathmout.resourcefulsheep;

import com.mathmout.resourcefulsheep.client.renderer.ResourcefulSheepRenderer;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.datagen.DataGenerators;
import com.mathmout.resourcefulsheep.entity.ModEntities;
import com.mathmout.resourcefulsheep.event.ModEvents;
import com.mathmout.resourcefulsheep.item.ModCreativeTabs;
import com.mathmout.resourcefulsheep.item.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ResourcefulSheepMod.MOD_ID)
public class ResourcefulSheepMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "resourceful_sheep";


    public ResourcefulSheepMod(IEventBus modEventBus, ModContainer modContainer) {

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ModEvents.class);

        modEventBus.addListener(ClientModEvents::onClientSetup);
        modEventBus.addListener(this::addEntityAttributes);

        ConfigSheepTypeManager.init();
        ConfigSheepMutationManager.init();

        ModEntities.registerVariantEntity();
        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModCreativeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModItems.registerVariantSpawnEggs();

        modEventBus.addListener(DataGenerators::gatherData);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void addEntityAttributes(EntityAttributeCreationEvent event) {
        ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                event.put(entityType.get(), Sheep.createAttributes().build()));

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            ModEntities.SHEEP_ENTITIES.forEach((id, entityType) ->
                    EntityRenderers.register(entityType.get(), ResourcefulSheepRenderer::new));
        }
    }
}
