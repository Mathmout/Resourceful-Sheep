package com.mathmout.resourcefulsheep.block.custom.centrifuge.port;

import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.entity.port.CentrifugeEnergyPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeEnergyPortBlock extends AbstractCentrifugePortBlock{

    public CentrifugeEnergyPortBlock(Properties properties, CentrifugeTier centrifugeTier) {
        super(properties, centrifugeTier);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CentrifugeEnergyPortBlockEntity(blockPos, blockState);
    }
}
