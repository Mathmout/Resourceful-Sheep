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

        // Traductions static.

        add("item.resourceful_sheep.lasso", "Lasso");
        add("item.resourceful_sheep.sheep_scanner", "Sheep Scanner");
        add("creativetab.resourceful_sheep", "Resourceful Sheep");

        // Traductions dynamiques des moutons.

        for (SheepVariantData variant : ConfigManager.getSheepVariant().values()) {
            add("entity." + ResourcefulSheepMod.MOD_ID + "." + variant.Id, "§lResourceful Sheep");

        }
    }
}