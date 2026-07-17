package com.mathmout.resourcefulsheep.block.entity.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractCentrifugePortBlockEntity extends BlockEntity {

     private BlockPos controllerPos;

    public AbstractCentrifugePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        this.setChanged();
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }
}