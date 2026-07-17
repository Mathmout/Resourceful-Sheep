package com.mathmout.resourcefulsheep.block.custom.centrifuge.port.item;

import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.port.AbstractCentrifugePortBlock;
import com.mathmout.resourcefulsheep.block.entity.port.item.CentrifugeItemOutPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeItemOutPortBlock extends AbstractCentrifugePortBlock {

    public CentrifugeItemOutPortBlock(Properties properties, CentrifugeTier centrifugeTier) {
        super(properties, centrifugeTier);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CentrifugeItemOutPortBlockEntity(blockPos, blockState);
    }
}
