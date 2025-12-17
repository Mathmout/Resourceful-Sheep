package com.mathmout.resourcefulsheep.entity;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.sheeptypes.ConfigSheepTypeManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModEntities {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModEntities.class);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ResourcefulSheepMod.MOD_ID);

    public static final Map<String, Supplier<EntityType<ResourcefulSheepEntity>>> SHEEP_ENTITIES = new HashMap<>();

    public static void registerVariantEntity() {
        for(SheepVariantData variant : ConfigSheepTypeManager.getSheepVariant().values()){
            String id = variant.Id();
            LOGGER.info("Registering entity with ID: {}", id);
            Supplier<EntityType<ResourcefulSheepEntity>> resourcefulSheep =
            ENTITY_TYPES.register(id,() -> EntityType.Builder.of(ResourcefulSheepEntity::new, MobCategory.AMBIENT)
                            .sized(0.9F, 1.3F)
                            .build(id));
            SHEEP_ENTITIES.put(id, resourcefulSheep);
        }
    }
}