package com.mathmout.resourcefulsheep.block.entity;

import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.custom.Syringe;
import com.mathmout.resourcefulsheep.screen.DNASequencerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DNASequencerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int ENERGY_CAPACITY = 500_000;
    public static final int ENERGY_CONSUMPTION = 1000;
    public static final int ENERGY_TRANSFER = 2000;
    public static final int ANALYZE_TIME = 100;

    private final List<String> storedDna = new ArrayList<>();
    private int progress = 0;
    private int maxProgress = ANALYZE_TIME;

    // Inventory
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                if (stack.getItem() instanceof Syringe) {
                    return stack.has(ModDataComponents.SYRINGE_CONTENT.get());
                }
            }
            return false;
        }
    };

    // Energy
    public final ModEnergyStorage energyStorage = new ModEnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER){
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    protected final ContainerData data = new ContainerData() {

        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> DNASequencerBlockEntity.this.progress;
                case 1 -> DNASequencerBlockEntity.this.maxProgress;
                case 2 -> DNASequencerBlockEntity.this.energyStorage.getEnergyStored();
                case 3 -> DNASequencerBlockEntity.this.energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int i1) {
            switch (i) {
                case 0 -> DNASequencerBlockEntity.this.progress = i1;
                case 1 -> DNASequencerBlockEntity.this.maxProgress = i1;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public DNASequencerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DNA_SEQUENCER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("DNA Sequencer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new DNASequencerMenu(i, inventory, this, this.data);
    }

    public void tick(Level level) {
        if (level.isClientSide) return;

        if (hasEnergy() && hasRecipe()) {
            this.energyStorage.consumeEnergy(ENERGY_CONSUMPTION);
            this.progress++;
            setChanged();

            if (this.progress >= this.maxProgress) {
                craftItem();
                this.progress = 0;
            }
        } else {
            if (this.progress > 0) {
                this.progress = 0;
                setChanged();
            }
        }
    }

    private boolean hasEnergy() {
        return this.energyStorage.getEnergyStored() >= ENERGY_CONSUMPTION;
    }

    private boolean hasRecipe() {
        ItemStack inputStack = itemHandler.getStackInSlot(0);
        if (inputStack.isEmpty() || !inputStack.has(ModDataComponents.SYRINGE_CONTENT.get())) {
            return false;
        }
        ItemStack resultStack = new ItemStack(inputStack.getItem());
        ItemStack outputSlot = itemHandler.getStackInSlot(1);

        return outputSlot.isEmpty() || (outputSlot.getItem() == resultStack.getItem() && outputSlot.getCount() < outputSlot.getMaxStackSize());
    }

    private void craftItem() {
        ItemStack inputStack = itemHandler.getStackInSlot(0);

        Item syringeItem = inputStack.getItem();

        if (inputStack.has(ModDataComponents.SYRINGE_CONTENT.get())) {
            CompoundTag tag = inputStack.get(ModDataComponents.SYRINGE_CONTENT.get());
            if (tag != null && tag.contains("id")) {
                String entityId = tag.getString("id");
                if (!storedDna.contains(entityId)) {
                    storedDna.add(entityId);
                    setChanged();
                    if (level != null && !level.isClientSide) {
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                }
            }
        }

        inputStack.shrink(1);
        ItemStack outputItem = new ItemStack(syringeItem);
        ItemStack currentOutput = itemHandler.getStackInSlot(1);

        if (currentOutput.isEmpty()) {
            itemHandler.setStackInSlot(1, outputItem);
        } else {
            currentOutput.grow(1);
            itemHandler.setStackInSlot(1, currentOutput);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("dna_sequencer.progress", progress);

        // Sauvegarde de la liste d'ADN
        ListTag DNAList = new ListTag();
        for (String dna : storedDna) {
            DNAList.add(StringTag.valueOf(dna));
        }
        tag.put("dna_list", DNAList);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if(tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        progress = tag.getInt("dna_sequencer.progress");

        storedDna.clear();
        if (tag.contains("dna_list", 9)) {
            ListTag dnaList = tag.getList("dna_list", 8);
            for (Tag t : dnaList) {
                storedDna.add(t.getAsString());
            }
        }
    }

    // Pour charger les données depuis l'Item quand on pose le bloc
    public void loadFromItem(ItemStack stack, HolderLookup.@NotNull Provider registries) {
        if (stack.has(ModDataComponents.SEQUENCER_DATA.get())) {
            CompoundTag data = stack.get(ModDataComponents.SEQUENCER_DATA.get());
            if (data != null) {
                // ADN
                if (data.contains("dna_list", 9)) {
                    ListTag list = data.getList("dna_list", 8);
                    for (Tag t : list) {
                        if (!storedDna.contains(t.getAsString())) {
                            storedDna.add(t.getAsString());
                        }
                    }
                }
                // Charger Inventaire
                if (data.contains("inventory")) {
                    itemHandler.deserializeNBT(registries, data.getCompound("inventory"));
                }
                // Charger Énergie
                if (data.contains("energy")) {
                    energyStorage.setEnergy(data.getInt("energy"));
                }
                // Charger Progress
                if (data.contains("dna_sequencer.progress")) {
                    this.progress = data.getInt("dna_sequencer.progress");
                }
            }
        }
    }

    public void saveToItem(@NotNull ItemStack stack, HolderLookup.@NotNull Provider registries) {
        CompoundTag data = new CompoundTag();

        // Sauvegarde des ADN
        ListTag dnaList = new ListTag();
        for (String dna : storedDna) {
            dnaList.add(StringTag.valueOf(dna));
        }
        data.put("dna_list", dnaList);

        // Inventaire
        data.put("inventory", itemHandler.serializeNBT(registries));

        // Energy
        data.putInt("energy", energyStorage.getEnergyStored());

        // Progression
        data.putInt("dna_sequencer.progress", progress);

        stack.set(ModDataComponents.SEQUENCER_DATA.get(), data);
    }

    // Pour le GUI.
    public List<String> getStoredDna() {
        return storedDna;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}