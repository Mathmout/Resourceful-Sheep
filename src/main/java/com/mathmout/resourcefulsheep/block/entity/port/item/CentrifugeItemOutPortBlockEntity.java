package com.mathmout.resourcefulsheep.block.entity.port.item;

import com.mathmout.resourcefulsheep.block.entity.CentrifugeControllerBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import com.mathmout.resourcefulsheep.block.entity.port.AbstractCentrifugePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CentrifugeItemOutPortBlockEntity extends AbstractCentrifugePortBlockEntity {

    public CentrifugeItemOutPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CENTRIFUGE_ITEM_OUT_PORT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public Optional<IItemHandler> getExtractInventory() {
        if (getControllerPos() != null && this.level != null) {
            BlockEntity blockEntity = this.level.getBlockEntity(getControllerPos());
            if (blockEntity instanceof CentrifugeControllerBlockEntity controller && controller.isAssembled()) {
                int inputSize = controller.getTier().getParallelProcesses();
                int totalSize = inputSize * 3;
                // RangedWrapper expose uniquement la zone de sortie
                return Optional.of(new RangedWrapper(controller.itemHandler, inputSize, totalSize){
                    @Override
                    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                        return stack;
                    }
                });
            }
        }
        return Optional.empty();
    }
}
