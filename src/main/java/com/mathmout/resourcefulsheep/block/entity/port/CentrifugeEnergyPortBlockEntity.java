package com.mathmout.resourcefulsheep.block.entity.port;

import com.mathmout.resourcefulsheep.block.entity.CentrifugeControllerBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Optional;

public class CentrifugeEnergyPortBlockEntity extends AbstractCentrifugePortBlockEntity{

    public CentrifugeEnergyPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CENTRIFUGE_ENERGY_PORT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public Optional<IEnergyStorage> getControllerEnergy() {
        if (getControllerPos() != null && this.level != null) {
            BlockEntity be = this.level.getBlockEntity(getControllerPos());
            if (be instanceof CentrifugeControllerBlockEntity controller) {
                // On accepte l'énergie UNIQUEMENT si la machine est montée !
                if (controller.isAssembled()) {
                    return Optional.of(controller.energyStorage);
                }
            }
        }
        return Optional.empty();
    }
}
