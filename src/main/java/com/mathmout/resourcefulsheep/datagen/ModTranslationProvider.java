package com.mathmout.resourcefulsheep.datagen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
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

        add("item." + ResourcefulSheepMod.MOD_ID + ".lasso", "Lasso");
        add("item." + ResourcefulSheepMod.MOD_ID + ".sheep_scanner", "Sheep Scanner");
        add("creativetab." + ResourcefulSheepMod.MOD_ID, "Resourceful Sheep");
        add("tooltip." + ResourcefulSheepMod.MOD_ID + ".sheep_scanner", "Right click on a sheep to scan it.");
        add("tooltip." + ResourcefulSheepMod.MOD_ID + ".lasso", "Coming soon...");
        add("recipe." + ResourcefulSheepMod.MOD_ID + ".mutation", "Sheep Mutation");


        // Traductions dynamiques des moutons.

        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
            add("entity." + ResourcefulSheepMod.MOD_ID + "." + variant.Id, "§lResourceful Sheep");

        }
    }
}