package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.item.ModDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class SheepScannerEnergyStorage implements IEnergyStorage {

    private final ItemStack itemStack;

    public SheepScannerEnergyStorage(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int stored = getEnergyStored();
        int capacity = getMaxEnergyStored();

        int energyReceived = Math.min(capacity - stored, Math.min(maxReceive, SheepScanner.MAX_ENERGY_TRANSFER));

        if (!simulate && energyReceived > 0) {
            setEnergy(stored + energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        // On récupère le CompoundTag, s'il n'existe pas, l'énergie est 0
        CompoundTag tag = itemStack.get(ModDataComponents.SHEEP_SCANNER_DATA.get());
        return (tag != null && tag.contains("Energy")) ? tag.getInt("Energy") : 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return SheepScanner.ENERGY_CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private void setEnergy(int energy) {
        CompoundTag tag = itemStack.getOrDefault(ModDataComponents.SHEEP_SCANNER_DATA.get(), new CompoundTag()).copy();
        tag.putInt("Energy", energy);
        itemStack.set(ModDataComponents.SHEEP_SCANNER_DATA.get(), tag);
    }
}
