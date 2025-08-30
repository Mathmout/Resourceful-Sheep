package com.mathmout.resourcefulsheep.datagen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.ConfigManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModTranslationProvider  extends LanguageProvider {
    public ModTranslationProvider(PackOutput output, String locale) {
        super(output, ResourcefulSheepMod.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {

        // Traductions static des items de base

        add("item.resourceful_sheep.lasso", "Lasso");
        add("item.resourceful_sheep.sheep_scanner", "Sheep Scanner");
        add("creativetab.resourceful_sheep", "Resourceful Sheep");

        // Traductions dynamiques des spawn eggs.

        for (SheepVariantData variant : ConfigManager.getSheepVariant().values()) {
            //add("item." + ResourcefulSheepMod.MOD_ID + "." + variant.Id + "_spawn_egg", "§lResourceful Sheep Spawn Egg");
            add("entity." + ResourcefulSheepMod.MOD_ID + "." + variant.Id, "§lResourceful Sheep");

        }
    }
}