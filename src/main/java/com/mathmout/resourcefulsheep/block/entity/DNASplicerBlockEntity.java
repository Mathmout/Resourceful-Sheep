package com.mathmout.resourcefulsheep.block.entity;

import com.mathmout.resourcefulsheep.Config;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.ModItems;
import com.mathmout.resourcefulsheep.screen.splicer.DNASplicerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DNASplicerBlockEntity extends BlockEntity implements MenuProvider {

    // On stocke les IDs des parents sélectionnés (Strings, pas items)
    private String mom_id = "";
    private String dad_id = "";

    private int progress = 0;
    private final int maxProgress = Config.DNA_SPLICER_ANALYZE_TIME.get(); // Temps un peu plus long que l'analyse

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };

    // Energy Storage
    public final ModEnergyStorage energyStorage = new ModEnergyStorage(Config.DNA_SPLICER_CAPACITY.get(), Config.DNA_SPLICER_CONSUMPTION.get()){
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> DNASplicerBlockEntity.this.progress;
                case 1 -> DNASplicerBlockEntity.this.maxProgress;
                case 2 -> DNASplicerBlockEntity.this.energyStorage.getEnergyStored();
                case 3 -> DNASplicerBlockEntity.this.energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int i1) {
            if (i == 0) DNASplicerBlockEntity.this.progress = i1;
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public DNASplicerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DNA_SPLICER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("DNA Splicer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new DNASplicerMenu(i, inventory, this, this.data);
    }

    public List<String> getNeighborDnaList() {
        if (level == null) return new ArrayList<>();

        int range = Config.DNA_SPLICER_RANGE.get();

        // On cherche dans un cube autour
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockEntity blockEntity = level.getBlockEntity(worldPosition.offset(x, y, z));
                    if (blockEntity instanceof DNASequencerBlockEntity Sequencer) {
                        return Sequencer.getStoredDna();
                    }
                }
            }
        }
        return null;
    }

    public void tick(Level level) {
        if (level.isClientSide) return;

        if (canCraft()) {
            this.energyStorage.consumeEnergy(Config.DNA_SEQUENCER_CONSUMPTION.get());
            this.progress++;
            setChanged();

            if (this.progress >= this.maxProgress) {
                craftResult();
                this.progress = 0;
                mom_id = "";
                dad_id = "";
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        } else {
            if (this.progress > 0) {
                this.progress = 0;
                setChanged();
            }
        }
    }

    private boolean canCraft() {
        if (mom_id.isEmpty() || dad_id.isEmpty()) return false;
        if (energyStorage.getEnergyStored() < Config.DNA_SEQUENCER_CONSUMPTION.get()) return false;
        return itemHandler.getStackInSlot(0).isEmpty();
    }

    private void craftResult() {
        if (level == null) return;

        ItemStack suspiciousEgg = new ItemStack(ModItems.SUSPICIOUS_SPAWN_EGG.get());

        CompoundTag data = new CompoundTag();
        data.putString("mom_id", mom_id);
        data.putString("dad_id", dad_id);

        suspiciousEgg.set(ModDataComponents.SUSPICIOUS_EGG_DATA.get(), data);
        itemHandler.setStackInSlot(0, suspiciousEgg);
    }

    public void setMom_id(String id) {
        this.mom_id = id; setChanged();
        assert level != null;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void setDad_id(String id) {
        this.dad_id = id; setChanged();
        assert level != null;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public String getMom_id() {
        return mom_id;
    }

    public String getDad_id() {
        return dad_id;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("dna_splicer.progress", progress);
        tag.putString("parent1", mom_id);
        tag.putString("parent2", dad_id);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory"))
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));

        if (tag.contains("energy"))
            energyStorage.setEnergy(tag.getInt("energy")); // Chargement propre

        if (tag.contains("dna_splicer.progress"))
            progress = tag.getInt("dna_splicer.progress");

        if (tag.contains("parent1"))
            mom_id = tag.getString("parent1");

        if (tag.contains("parent2"))
            dad_id = tag.getString("parent2");
    }

    // Sauvegarde Item (quand on casse le bloc)
    public void saveToItem(@NotNull ItemStack stack, HolderLookup.@NotNull Provider registries) {
        CompoundTag data = new CompoundTag();
        data.putInt("energy", energyStorage.getEnergyStored());
        data.put("inventory", itemHandler.serializeNBT(registries));
        data.putInt("dna_splicer.progress", progress);
        data.putString("parent1", mom_id);
        data.putString("parent2", dad_id);

        stack.set(ModDataComponents.SPLICER_DATA.get(), data);
    }

    public void loadFromItem(ItemStack stack, HolderLookup.@NotNull Provider registries) {
        if (stack.has(ModDataComponents.SPLICER_DATA.get())) {
            CompoundTag data = stack.get(ModDataComponents.SPLICER_DATA.get());
            if (data != null) {

                if (data.contains("energy"))
                    energyStorage.setEnergy(data.getInt("energy"));

                if (data.contains("inventory"))
                    itemHandler.deserializeNBT(registries, data.getCompound("inventory"));

                if (data.contains("dna_splicer.progress"))
                    progress = data.getInt("dna_splicer.progress");

                if (data.contains("parent1"))
                    mom_id = data.getString("parent1");

                if (data.contains("parent2"))
                    dad_id = data.getString("parent2");
            }
        }
    }

    // Synchro Client
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