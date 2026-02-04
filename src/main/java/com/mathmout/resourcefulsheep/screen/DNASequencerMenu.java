package com.mathmout.resourcefulsheep.screen;

import com.mathmout.resourcefulsheep.block.ModBlocks;
import com.mathmout.resourcefulsheep.block.entity.DNASequencerBlockEntity;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.custom.Syringe;
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

public class DNASequencerMenu extends AbstractContainerMenu {

    public final DNASequencerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    // Constructeur Client (appelé quand on reçoit le paquet réseau)
    public DNASequencerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    // Constructeur Serveur (appelé par le BlockEntity)
    public DNASequencerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DNA_SEQUENCER_MENU.get(), pContainerId);
        checkContainerSize(inv, 2);
        this.blockEntity = ((DNASequencerBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        // On ajoute les slots de la machine
        var handler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, this.blockEntity.getBlockPos(), null);
        if (handler != null) {
            this.addSlot(new SlotItemHandler(handler, 0, 51, 35));
            this.addSlot(new SlotItemHandler(handler, 1, 109, 35));
        }

        addDataSlots(data);
        // Ajout de l'inventaire du joueur
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);  // Max Progress
        int progressArrowSize = 26; // Taille de la flèche en pixels sur ta texture

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getEnergy() {
        return this.data.get(2);
    }

    public int getMaxEnergy() {
        return this.data.get(3);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Définition des bornes (C'est plus propre que des chiffres magiques partout)
        int MACHINE_SLOTS = 2; // Slot 0 et 1
        int INV_START = 2;     // Premier slot de l'inventaire joueur
        int HOTBAR_START = 29; // Premier slot de la hotbar (2 + 27)
        int TOTAL_SLOTS = 38;  // Tout (2 + 27 + 9)

        // 1. Si on clique dans la MACHINE (Slot 0 ou 1) -> On renvoie vers le JOUEUR
        if (index < MACHINE_SLOTS) {
            // Le 'true' à la fin veut dire "commence par la fin" (remplit la hotbar en premier)
            if (!moveItemStackTo(sourceStack, INV_START, TOTAL_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
        }
        // 2. Si on clique dans l'inventaire du JOUEUR
        else {
            // A. C'est une seringue ? → On tente de la mettre dans l'INPUT de la machine (Slot 0)
            if (sourceStack.getItem() instanceof Syringe) {
                if (sourceStack.has(ModDataComponents.SYRINGE_CONTENT.get())) {
                    // Si ça réussit, on s'arrête là. Sinon, on continue vers le rangement classique.
                    if (!moveItemStackTo(sourceStack, 0, 1, false)) {
                        // Si le slot machine est plein, ne retourne pas tout de suite !
                        // Laisse le code continuer pour qu'il range l'item dans la hotbar/inventaire
                    } else {
                        // Si ça a marché (transféré dans la machine), on a fini
                        return ItemStack.EMPTY; // Petite astuce : retourner empty ici évite les bugs de duplication visuelle
                    }
                }
            }

            // B. Rangement Classique (Inventaire <-> Hotbar)
            // Si on est dans l'inventaire principal -> Vers la Hotbar
            if (index < HOTBAR_START) {
                if (!moveItemStackTo(sourceStack, HOTBAR_START, TOTAL_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Si on est dans la Hotbar -> Vers l'inventaire principal
            else {
                if (!moveItemStackTo(sourceStack, INV_START, HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        // --- Gestion de la fin du transfert (Standard) ---
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == copyOfSourceStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.DNA_SEQUENCER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public DNASequencerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}