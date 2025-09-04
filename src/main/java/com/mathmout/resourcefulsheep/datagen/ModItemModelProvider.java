package com.mathmout.resourcefulsheep.datagen;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ResourcefulSheepMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()) {
            String id = variant.Id + "_spawn_egg";
            withExistingParent(id, mcLoc("item/template_spawn_egg"));
        }
    }
}