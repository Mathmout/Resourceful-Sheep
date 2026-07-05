package com.mathmout.resourcefulsheep.screen.scanner;

import com.mathmout.resourcefulsheep.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SheepScannerMenu extends AbstractContainerMenu {

    private final String scannedSheepId;
    private final ContainerData data;

    // Constructeur Client
    public SheepScannerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, extraData.readUtf(), new SimpleContainerData(2));
    }

    // Constructeur Serveur
    public SheepScannerMenu(int containerId, Inventory inv, String scannedSheepId, ContainerData data) {
        super(ModMenuTypes.SHEEP_SCANNER_MENU.get(), containerId);
        this.scannedSheepId = scannedSheepId;
        this.data = data;
        this.addDataSlots(data); // Synchronise les données avec le client
    }
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        return ItemStack.EMPTY; // Pas d'inventaire à déplacer
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public String getScannedSheepId() {
        return scannedSheepId;
    }

    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }}
