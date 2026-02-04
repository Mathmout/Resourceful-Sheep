package com.mathmout.resourcefulsheep.block.entity;

import net.neoforged.neoforge.energy.EnergyStorage;

public class ModEnergyStorage extends EnergyStorage {

    public ModEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer, 0);
    }

    public void consumeEnergy(int amount) {
        this.energy = Math.max(0, this.energy - amount);
        onEnergyChanged();
    }

    public void setEnergy(int energy) {
        this.energy = energy;
        onEnergyChanged();
    }

    public void onEnergyChanged() {
    }
}
