package com.mathmout.resourcefulsheep.block.entity.port;

import com.mathmout.resourcefulsheep.block.entity.CentrifugeControllerBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CentrifugeFluidPortBlockEntity extends AbstractCentrifugePortBlockEntity{

    public CentrifugeFluidPortBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CENTRIFUGE_FLUID_PORT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public Optional<IFluidHandler> getFluidHandler() {
        if (getControllerPos() != null && this.level != null) {
            BlockEntity be = this.level.getBlockEntity(getControllerPos());
            if (be instanceof CentrifugeControllerBlockEntity controller && controller.isAssembled()) {
                // On passe le tableau complet des réservoirs au wrapper
                return Optional.of(new MultiFluidTankWrapper(controller.fluidTanks));
            }
        }
        return Optional.empty();
    }

    // Le Wrapper qui fusionne l'accès aux multiples réservoirs pour les tuyaux
    private static class MultiFluidTankWrapper implements IFluidHandler {
        private final FluidTank[] tanks;

        public MultiFluidTankWrapper(FluidTank[] tanks) {
            this.tanks = tanks;
        }

        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tanks[tank].getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tanks[tank].getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tanks[tank].isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, @NotNull FluidAction action) {
            FluidStack copy = resource.copy();
            int totalFilled = 0;
            for (FluidTank tank : tanks) {
                int filled = tank.fill(copy, action);
                totalFilled += filled;
                copy.shrink(filled);
                if (copy.isEmpty()) break;
            }
            return totalFilled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, @NotNull FluidAction action) {
            FluidStack copy = resource.copy();
            int totalDrained = 0;
            for (FluidTank tank : tanks) {
                FluidStack drained = tank.drain(copy, action);
                totalDrained += drained.getAmount();
                copy.shrink(drained.getAmount());
                if (copy.isEmpty()) break;
            }
            return new FluidStack(resource.getFluid(), totalDrained);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            for (FluidTank tank : tanks) {
                FluidStack drained = tank.drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    return drained; // On draine le premier réservoir contenant du liquide
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
