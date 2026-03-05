package com.mathmout.resourcefulsheep.screen.splicer;

import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.entity.DNASplicerBlockEntity;
import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DNASplicerMenu extends AbstractContainerMenu {

    public final DNASplicerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public DNASplicerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public DNASplicerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DNA_SPLICER_MENU.get(), pContainerId);
        checkContainerSize(inv, 1);
        this.blockEntity = ((DNASplicerBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        var handler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, this.blockEntity.getBlockPos(), null);
        if (handler != null) {
            this.addSlot(new SlotItemHandler(handler, 0, 275, 42 - 18 / 2));
        }

        addDataSlots(data);
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        Slot sourceSlot = slots.get(i);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int MACHINE_SLOTS = 1;
        int INV_START = 1;     // L'inventaire joueur commence juste après le slot 0
        int HOTBAR_START = 28; // 1 + 27
        int TOTAL_SLOTS = 37;  // 1 + 27 + 9

        // Si on clique dans la MACHINE (Slot 0 - Sortie) -> Vers le JOUEUR
        if (i < MACHINE_SLOTS) {
            // Le 'true' remplit en partant de la fin (Hotbar)
            if (!moveItemStackTo(sourceStack, INV_START, TOTAL_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
            sourceSlot.onQuickCraft(sourceStack, copyOfSourceStack); // Notifie que l'item a été crafté/sorti
        }
        // Si on clique dans l'inventaire du JOUEUR
        else {
            // Si Inventaire principal -> Vers Hotbar
            if (i < HOTBAR_START) {
                if (!moveItemStackTo(sourceStack, HOTBAR_START, TOTAL_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Si Hotbar -> Vers Inventaire principal
            else {
                if (!moveItemStackTo(sourceStack, INV_START, HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == copyOfSourceStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.DNA_SPLICER.get());
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    public int getEnergy() {
        return data.get(2);
    }

    public int getMaxEnergy() {
        return data.get(3);
    }

    public String getParent1() {
        return blockEntity.getMom_id();
    }

    public String getParent2() {
        return blockEntity.getDad_id();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int Xstart = (304 - 18 * 9) / 2 + 1;
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, Xstart + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        int Xstart = (304 - 18 * 9) / 2 + 1;
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, Xstart + i * 18, 142));
        }
    }

    public List<String> getNeighborDnaList() {
        return this.blockEntity.getNeighborDnaList();
    }

    public int getScaledProgress() {
        int progress = getProgress();
        int maxProgress = getMaxProgress();
        int ArrowNPlusSize = 21;
        return maxProgress != 0 && progress != 0 ? progress * ArrowNPlusSize / maxProgress : 0;
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }
}