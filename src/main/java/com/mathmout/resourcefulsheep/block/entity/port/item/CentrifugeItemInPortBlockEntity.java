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

public class CentrifugeItemInPortBlockEntity extends AbstractCentrifugePortBlockEntity {

    public CentrifugeItemInPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CENTRIFUGE_ITEM_IN_PORT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public Optional<IItemHandler> getInsertInventory() {
        if (getControllerPos() != null && this.level != null) {
            BlockEntity blockEntity = this.level.getBlockEntity(getControllerPos());
            if (blockEntity instanceof CentrifugeControllerBlockEntity controller && controller.isAssembled()) {
                int inputSize = controller.getTier().getParallelProcesses();
                // RangedWrapper expose uniquement les slots de 0 à inputSize (exclusif)
                return Optional.of(new RangedWrapper(controller.itemHandler, 0, inputSize){
                    @Override
                    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                        return ItemStack.EMPTY;
                    }
                });
            }
        }
        return Optional.empty();
    }
}
