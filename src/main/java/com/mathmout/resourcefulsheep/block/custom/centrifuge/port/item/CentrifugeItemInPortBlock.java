package com.mathmout.resourcefulsheep.block.custom.centrifuge.port.item;

import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.port.AbstractCentrifugePortBlock;
import com.mathmout.resourcefulsheep.block.entity.port.item.CentrifugeItemInPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeItemInPortBlock extends AbstractCentrifugePortBlock {

    public CentrifugeItemInPortBlock(Properties properties, CentrifugeTier centrifugeTier) {
        super(properties, centrifugeTier);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CentrifugeItemInPortBlockEntity(blockPos, blockState);
    }
}
