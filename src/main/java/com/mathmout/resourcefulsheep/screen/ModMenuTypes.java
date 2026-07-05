package com.mathmout.resourcefulsheep.screen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.screen.scanner.SheepScannerMenu;
import com.mathmout.resourcefulsheep.screen.sequencer.DNASequencerMenu;
import com.mathmout.resourcefulsheep.screen.splicer.DNASplicerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, ResourcefulSheepMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DNASequencerMenu>> DNA_SEQUENCER_MENU =
            MENUS.register("dna_sequencer_menu", () -> IMenuTypeExtension.create(DNASequencerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DNASplicerMenu>> DNA_SPLICER_MENU =
            MENUS.register("dna_splicer_menu", () -> IMenuTypeExtension.create(DNASplicerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SheepScannerMenu>> SHEEP_SCANNER_MENU =
            MENUS.register("sheep_scanner_menu", () -> IMenuTypeExtension.create(SheepScannerMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
