package com.mathmout.resourcefulsheep.screen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.awt.*;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, ResourcefulSheepMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DNASequencerMenu>> DNA_SEQUENCER_MENU =
            MENUS.register("dna_sequencer_menu", () -> IMenuTypeExtension.create(DNASequencerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
