package com.mathmout.resourcefulsheep.item;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ResourcefulSheepMod.MOD_ID);

    // Lasso
    public static final Supplier<DataComponentType<CompoundTag>> CAPTURED_ENTITY = DATA_COMPONENTS.register("captured_entity", () ->
            DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                    .build());

    // Syringe
    public static final Supplier<DataComponentType<CompoundTag>> SYRINGE_CONTENT = DATA_COMPONENTS.register("syringe_content", () ->
            DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                    .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}