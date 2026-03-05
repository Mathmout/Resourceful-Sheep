package com.mathmout.resourcefulsheep.item;

import com.mathmout.resourcefulsheep.Config;
import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.mathmout.resourcefulsheep.item.ModItems.SHEEP_SPAWN_EGGS;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ResourcefulSheepMod.MOD_ID);

    public  static final Supplier<CreativeModeTab> RESOURCEFUL_SHEEP_TAB = CREATIVE_MODE_TAB.register("resourceful_sheep_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LASSO.get()))
                    .title(Component.translatable("creativetab." + ResourcefulSheepMod.MOD_ID))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.LASSO.get());
                        output.accept(ModItems.SHEEP_SCANNER.get());
                        output.accept(getChargedSheepScanner());
                        output.accept(ModItems.IRON_SYRINGE.get());
                        output.accept(ModItems.DIAMOND_SYRINGE.get());
                        output.accept(ModItems.NETHERITE_SYRINGE.get());
                        output.accept(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get());
                        output.accept(ModBlocks.DNA_SEQUENCER.get());
                        output.accept(ModBlocks.DNA_SPLICER.get());
                        output.accept(ModItems.SUSPICIOUS_SPAWN_EGG.get());

                        for (DeferredItem<? extends SpawnEggItem> egg : SHEEP_SPAWN_EGGS) {
                            output.accept(egg.get());
                        }
                    })
                        .build());

    private static ItemStack getChargedSheepScanner(){
        ItemStack sheepScanner = new ItemStack(ModItems.SHEEP_SCANNER.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", Config.SHEEP_SCANNER_CAPACITY.get());
        sheepScanner.set(ModDataComponents.SHEEP_SCANNER_DATA.get(), tag);
        return sheepScanner;
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
