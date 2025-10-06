package com.mathmout.resourcefulsheep.item;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, ResourcefulSheepMod.MOD_ID);

    public static final Supplier<DataComponentType<CompoundTag>> CAPTURED_ENTITY = DATA_COMPONENTS.register("captured_entity", () ->
            DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG).build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}