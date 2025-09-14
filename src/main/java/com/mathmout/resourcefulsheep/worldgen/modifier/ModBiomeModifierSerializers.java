package com.mathmout.resourcefulsheep.worldgen.modifier;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifierSerializers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ResourcefulSheepMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddSpawnIfSheepPresentModifier>> ADD_SPAWN_IF_SHEEP_PRESENT =
            BIOME_MODIFIER_SERIALIZERS.register("add_spawn_if_sheep_present", () -> AddSpawnIfSheepPresentModifier.CODEC);

    public static void register(IEventBus eventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(eventBus);
    }
}