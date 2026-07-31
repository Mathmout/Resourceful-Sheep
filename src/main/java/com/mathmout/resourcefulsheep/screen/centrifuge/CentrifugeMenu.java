package com.mathmout.resourcefulsheep.screen.centrifuge;

import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.entity.CentrifugeControllerBlockEntity;
import com.mathmout.resourcefulsheep.item.custom.ResourcefulWoolItem;
import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CentrifugeMenu extends AbstractContainerMenu {

    public final CentrifugeControllerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private final CentrifugeTier tier;

    public CentrifugeMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(11));
    }

    public CentrifugeMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.CENTRIFUGE_MENU.get(), pContainerId);
        checkContainerSize(inv, 3);
        this.blockEntity = ((CentrifugeControllerBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;
        this.tier = this.blockEntity.getTier();

        int p = tier.getParallelProcesses();
        int outputSize = this.blockEntity.getOutputInventorySize();
        int numTanks = tier.getNbFluidTank();

        int outCols = Math.max(3, p);
        int outRows = outputSize / outCols;

        // --- GÉOMÉTRIE EXACTE ---
        int imageWidth = 240;
        int availableWidth = imageWidth - 3 - (numTanks * 22) - 22;

        // Entrées (Espacement UNIQUEMENT entre les slots)
        int maxGap = 13;
        int maxPossibleGap = (p > 1) ? (availableWidth - (p * 18)) / (p - 1) : 0;
        int actualGap = (p == 1) ? 0 : Math.min(maxGap, maxPossibleGap);

        int totalInputWidth = (p * 18) + (Math.max(0, p - 1) * actualGap);
        int inputStartX = 22 + (availableWidth - totalInputWidth) / 2;

        // Sorties
        int totalOutputWidth = outCols * 18;
        int outputStartX = 22 + (availableWidth - totalOutputWidth) / 2;

        // Positions Y statiques pour centrer visuellement
        int inputY = 25;
        int outputY = 68;

        ItemStackHandler handler = this.blockEntity.itemHandler;

        // Slots d'entrée
        for (int i = 0; i < p; i++) {
            this.addSlot(new SlotItemHandler(handler, i, inputStartX + (i * (18 + actualGap)) + 1, inputY + 1));
        }

        // Slots de sortie
        for (int row = 0; row < outRows; row++) {
            for (int col = 0; col < outCols; col++) {
                int slotIndex = p + (row * outCols) + col;
                this.addSlot(new SlotItemHandler(handler, slotIndex, outputStartX + (col * 18) + 1, outputY + (row * 18) + 1){
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        addDataSlots(data);
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public int getProcessTimer(int processIndex) {
        return this.data.get(processIndex);
    }

    public int getMaxProgress() {
        return this.data.get(tier.getParallelProcesses());
    }

    public int getEnergy() {
        return this.data.get(tier.getParallelProcesses() + 1);
    }

    public int getMaxEnergy() {
        return this.data.get(tier.getParallelProcesses() + 2);
    }

    public boolean isAssembled() {
        return this.data.get(tier.getParallelProcesses() + 3) == 1;
    }

    public CentrifugeTier getTier() {
        return this.tier;
    }

    public FluidTank[] getFluidTanks() {
        return this.blockEntity.fluidTanks;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int p = tier.getParallelProcesses();
        int MACHINE_SLOTS = p + blockEntity.getOutputInventorySize();
        int INV_START = MACHINE_SLOTS;
        int HOTBAR_START = INV_START + 27;
        int TOTAL_SLOTS = HOTBAR_START + 9;

        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(sourceStack, INV_START, TOTAL_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (sourceStack.getItem() instanceof ResourcefulWoolItem) {
                if (!moveItemStackTo(sourceStack, 0, p, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < HOTBAR_START) {
                if (!moveItemStackTo(sourceStack, HOTBAR_START, TOTAL_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(sourceStack, INV_START, HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (sourceStack.getCount() == 0) sourceSlot.set(ItemStack.EMPTY);
        else sourceSlot.setChanged();

        if (sourceStack.getCount() == copyOfSourceStack.getCount()) return ItemStack.EMPTY;
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        Block block = ModBlocks.CENTRIFUGE_CONTROLLERS.get(this.tier).get();
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, block);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int invX = 40;
        int invY = 132;
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, invX + l * 18, invY + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        int invX = 40;
        int hotbarY = 190;
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, invX + i * 18, hotbarY));
        }
    }
}