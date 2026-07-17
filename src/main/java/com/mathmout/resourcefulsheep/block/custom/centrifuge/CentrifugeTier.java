package com.mathmout.resourcefulsheep.block.custom.centrifuge;

import com.mathmout.resourcefulsheep.Config;

import java.util.function.Supplier;

public enum CentrifugeTier {

    BASIC(1,
            Config.CENTRIFUGE_BASIC_SPEED,
            Config.CENTRIFUGE_BASIC_ENERGY_CAPACITY,
            Config.CENTRIFUGE_BASIC_ENERGY_CONSUMPTION,
            Config.CENTRIFUGE_BASIC_FLUID_CAPACITY,
            Config.CENTRIFUGE_BASIC_ENERGY_TRANSFER,
            0),

    ADVANCED(3,
            Config.CENTRIFUGE_ADVANCED_SPEED,
            Config.CENTRIFUGE_ADVANCED_ENERGY_CAPACITY,
            Config.CENTRIFUGE_ADVANCED_ENERGY_CONSUMPTION,
            Config.CENTRIFUGE_ADVANCED_FLUID_CAPACITY,
            Config.CENTRIFUGE_ADVANCED_ENERGY_TRANSFER,
            1),

    ELITE(5,
            Config.CENTRIFUGE_ELITE_SPEED,
            Config.CENTRIFUGE_ELITE_ENERGY_CAPACITY,
            Config.CENTRIFUGE_ELITE_ENERGY_CONSUMPTION,
            Config.CENTRIFUGE_ELITE_FLUID_CAPACITY,
            Config.CENTRIFUGE_ELITE_ENERGY_TRANSFER,
            2),

    ULTIMATE(7,
            Config.CENTRIFUGE_ULTIMATE_SPEED,
            Config.CENTRIFUGE_ULTIMATE_ENERGY_CAPACITY,
            Config.CENTRIFUGE_ULTIMATE_ENERGY_CONSUMPTION,
            Config.CENTRIFUGE_ULTIMATE_FLUID_CAPACITY,
            Config.CENTRIFUGE_ULTIMATE_ENERGY_TRANSFER,
            3);

    private final int parallelProcesses;
    private final Supplier<Integer> speedProcesses;
    private final Supplier<Integer> energyCapacity;
    private final Supplier<Integer> energyConsumption;
    private final Supplier<Integer> fluidCapacity;
    private final Supplier<Integer> energyTransfer;
    private final int nbFluidTank;

    CentrifugeTier(int parallelProcesses, Supplier<Integer> speedProcesses, Supplier<Integer> energyCapacity, Supplier<Integer> energyConsumption, Supplier<Integer> fluidCapacity, Supplier<Integer> energyTransfer, int nbFluidTank) {
        this.parallelProcesses = parallelProcesses;
        this.speedProcesses = speedProcesses;
        this.energyCapacity = energyCapacity;
        this.fluidCapacity = fluidCapacity;
        this.energyConsumption = energyConsumption;
        this.energyTransfer = energyTransfer;
        this.nbFluidTank = nbFluidTank;
    }

    public int getEnergyConsumption() {
        return energyConsumption.get();
    }

    public int getParallelProcesses() {
        return parallelProcesses;
    }

    public int getSpeedProcesses() {
        return speedProcesses.get();
    }

    public int getEnergyCapacity() {
        return energyCapacity.get();
    }

    public int getFluidCapacity() {
        return fluidCapacity.get();
    }

    public int getNbFluidTank() {
        return nbFluidTank;
    }

    public int getEnergyTransfer() {
        return energyTransfer.get();
    }
}
