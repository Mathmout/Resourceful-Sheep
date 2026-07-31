package com.mathmout.resourcefulsheep.block.entity;

import com.mathmout.resourcefulsheep.block.custom.centrifuge.AbstractCentrifugePartBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeControllerBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.entity.port.AbstractCentrifugePortBlockEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.custom.ResourcefulWoolItem;
import com.mathmout.resourcefulsheep.screen.centrifuge.CentrifugeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CentrifugeControllerBlockEntity extends BlockEntity implements MenuProvider {

    private final List<BlockPos> multiblockParts = new ArrayList<>();

    private boolean isAssembled = false;
    private final int[] processTimers;
    private final int maxProgress;
    private final CentrifugeTier tier;

    public final ItemStackHandler itemHandler;
    public final ModEnergyStorage energyStorage;
    public final FluidTank[] fluidTanks;

    private boolean isDistributing = false;
    private boolean needsBalancing = false;

    protected final ContainerData data = new ContainerData() {

        @Override
        public int get(int index) {
            int p = CentrifugeControllerBlockEntity.this.tier.getParallelProcesses();

            // Les premiers index (ex: 0 à 8) correspondent au tableau des timers
            if (index < p) {
                return CentrifugeControllerBlockEntity.this.processTimers[index];
            }

            // Les index suivants correspondent aux variables globales
            if (index == p) return CentrifugeControllerBlockEntity.this.maxProgress;
            if (index == p + 1) return CentrifugeControllerBlockEntity.this.energyStorage.getEnergyStored();
            if (index == p + 2) return CentrifugeControllerBlockEntity.this.energyStorage.getMaxEnergyStored();
            if (index == p + 3) return CentrifugeControllerBlockEntity.this.isAssembled ? 1 : 0;

            return 0;
        }

        @Override
        public void set(int index, int value) {
            int p = CentrifugeControllerBlockEntity.this.tier.getParallelProcesses();

            if (index < p) {
                CentrifugeControllerBlockEntity.this.processTimers[index] = value;
            } else if (index == p + 3) {
                CentrifugeControllerBlockEntity.this.isAssembled = (value == 1);
            }
        }

        @Override
        public int getCount() {
            // Le nombre total de variables à synchroniser = taille du tableau + 4 variables statiques
            return CentrifugeControllerBlockEntity.this.tier.getParallelProcesses() + 4;
        }
    };

    public CentrifugeControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.CENTRIFUGE_CONTROLLER_BLOCK_ENTITY.get(), blockPos, blockState);

        this.tier = ((AbstractCentrifugePartBlock) blockState.getBlock()).getCentrifugeTier();
        this.maxProgress = this.tier.getSpeedProcesses();
        this.processTimers = new int[this.tier.getParallelProcesses()];

        this.itemHandler = new ItemStackHandler(this.tier.getParallelProcesses() + getOutputInventorySize()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot < tier.getParallelProcesses() && !isDistributing) {
                    needsBalancing = true;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot < tier.getParallelProcesses()) {
                    return stack.getItem() instanceof ResourcefulWoolItem;
                }
                return true;
            }
        };

        this.energyStorage = new ModEnergyStorage(this.tier.getEnergyCapacity(), this.tier.getEnergyTransfer()) {
            @Override
            public void onEnergyChanged() {
                setChanged();
            }
        };

        this.fluidTanks = new FluidTank[this.tier.getNbFluidTank()];
        for (int i = 0; i < this.fluidTanks.length; i++) {
            this.fluidTanks[i] = new FluidTank(this.tier.getFluidCapacity()) {
                @Override
                protected void onContentsChanged() {
                    setChanged();
                    if (level != null && !level.isClientSide()) {
                        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    }
                }
            };
        }
    }

    public int getOutputInventorySize() {
        return switch (this.tier) {
            case BASIC -> 3;
            case ADVANCED -> 6;
            case ELITE -> 10;
            case ULTIMATE -> 14;
        };
    }

    // --- LOGIQUE MULTIBLOCK ---

    public void disassemble() {
        if (!this.isAssembled) return;
        this.isAssembled = false;

        if (this.level != null && !this.level.isClientSide()) {
            for (BlockPos partPos : this.multiblockParts) {
                BlockEntity be = this.level.getBlockEntity(partPos);
                if (be instanceof AbstractCentrifugePortBlockEntity port) {
                    port.setControllerPos(null); // Coupe instantanément la connexion aux câbles
                }
            }
        }
        this.multiblockParts.clear();
    }

    public void assemble(List<BlockPos> tmpParts) {
        this.multiblockParts.clear();
        this.multiblockParts.addAll(tmpParts);
        this.isAssembled = true;

        if (this.level != null && !this.level.isClientSide()) {
            for (BlockPos partPos : this.multiblockParts) {
                BlockEntity be = this.level.getBlockEntity(partPos);
                if (be instanceof AbstractCentrifugePortBlockEntity port) {
                    port.setControllerPos(this.getBlockPos()); // Lance la connexion aux câbles
                }
            }
        }
    }

    public boolean checkMultiblockStructure(Level level, BlockPos controllerPos, Direction facing) {
        Direction back = facing.getOpposite();
        Direction right = facing.getCounterClockWise();
        List<BlockPos> tmpParts = new ArrayList<>();

        for (int h = 0; h < 4; h++) {
            for (int d = 0; d < 3; d++) {
                for (int w = -1; w <= 1; w++) {
                    if (h == 0 && d == 0 && w == 0) continue;

                    BlockPos checkPos = controllerPos.above(h).relative(back, d).relative(right, w);
                    BlockState state = level.getBlockState(checkPos);
                    Block block = state.getBlock();

                    if (!(block instanceof AbstractCentrifugePartBlock)) {
                        disassemble(); // La structure est brisée (ex: casing cassé)
                        return false;
                    }
                    tmpParts.add(checkPos);
                }
            }
        }

        // Si on arrive ici, la machine est physiquement valide
        if (!this.isAssembled || this.multiblockParts.isEmpty()) {
            assemble(tmpParts); // On l'assemble uniquement si elle ne l'était pas déjà !
        }
        return true;
    }

    @Override
    public void setRemoved() {
        disassemble();
        super.setRemoved();
    }

    public boolean hasPart(BlockPos pos) {
        return this.multiblockParts.contains(pos);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 20 == 0) {
            Direction facing = blockState.getValue(CentrifugeControllerBlock.FACING);
            boolean wasAssembled = this.isAssembled;
            boolean isNowAssembled = checkMultiblockStructure(level, blockPos, facing);

            if (wasAssembled != isNowAssembled) {
                setChanged();
                level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            }
        }

        if (this.isAssembled) {
            int p = this.tier.getParallelProcesses();
            int outSize = getOutputInventorySize();

            // 1. Initialiser les simulations
            ItemStackHandler simItemHandler = new ItemStackHandler(outSize);
            for (int i = 0; i < outSize; i++) {
                simItemHandler.setStackInSlot(i, this.itemHandler.getStackInSlot(p + i).copy());
            }

            int[] simFluids = new int[this.fluidTanks.length];
            for (int i = 0; i < this.fluidTanks.length; i++) {
                simFluids[i] = this.fluidTanks[i].getFluidAmount();
            }

            int activeProcesses = 0;
            boolean[] canProcessSlot = new boolean[p];

            // 2. Évaluation slot par slot
            for (int i = 0; i < p; i++) {
                ItemStack inputStack = this.itemHandler.getStackInSlot(i);

                if (inputStack.isEmpty() || !(inputStack.getItem() instanceof ResourcefulWoolItem)) {
                    if (this.processTimers[i] > 0) {
                        this.processTimers[i] = 0;
                        setChanged();
                    }
                    continue;
                }

                if (canProcess(i, simItemHandler, simFluids)) {
                    canProcessSlot[i] = true;
                    activeProcesses++;
                }
            }

            // 3. Exécution
            if (activeProcesses > 0) {
                int energyPerProcess = this.tier.getEnergyConsumption() / activeProcesses;

                if (this.energyStorage.getEnergyStored() >= (energyPerProcess * activeProcesses)) {
                    boolean isWorking = false;
                    for (int i = 0; i < p; i++) {
                        if (canProcessSlot[i]) {
                            this.energyStorage.consumeEnergy(energyPerProcess);
                            this.processTimers[i]++;
                            isWorking = true;

                            if (this.processTimers[i] >= this.maxProgress) {
                                craftItem(i);
                                this.processTimers[i] = 0;
                            }
                        }
                    }
                    if (isWorking) setChanged();
                }
            }
        } else {
            boolean changed = false;
            for (int i = 0; i < this.processTimers.length; i++) {
                if (this.processTimers[i] > 0) {
                    this.processTimers[i] = 0;
                    changed = true;
                }
            }
            if (changed) setChanged();
        }
        if (this.needsBalancing) {
            balanceInputs();
            this.needsBalancing = false;
        }
    }

    private void balanceInputs() {
        int p = this.tier.getParallelProcesses();
        if (p <= 1) return; // Inutile si on a un seul slot

        this.isDistributing = true;

        List<ItemGroup> groups = new ArrayList<>();
        List<Integer> emptySlots = new ArrayList<>();

        // On scanne l'inventaire pour rassembler les laines identiques
        for (int i = 0; i < p; i++) {
            ItemStack stack = this.itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                emptySlots.add(i);
            } else {
                boolean found = false;
                for (ItemGroup group : groups) {
                    if (ItemStack.isSameItemSameComponents(group.sample, stack)) {
                        group.slots.add(i);
                        group.totalCount += stack.getCount();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ItemGroup newGroup = new ItemGroup();
                    newGroup.sample = stack.copy();
                    newGroup.slots.add(i);
                    newGroup.totalCount = stack.getCount();
                    groups.add(newGroup);
                }
            }
        }

        // 2. On distribue les slots vides au groupe d'items le plus nombreux
        while (!emptySlots.isEmpty() && !groups.isEmpty()) {
            ItemGroup bestGroup = null;
            double bestRatio = 1.0;

            for (ItemGroup group : groups) {
                double ratio = (double) group.totalCount / group.slots.size();
                if (ratio > bestRatio) {
                    bestRatio = ratio;
                    bestGroup = group;
                }
            }

            if (bestGroup != null) {
                bestGroup.slots.add(emptySlots.removeFirst());
            } else {
                break; // Impossible de diviser plus (chaque slot est à 1)
            }
        }

        // 3. On applique la distribution parfaite
        for (ItemGroup group : groups) {
            int slotsCount = group.slots.size();
            int baseCount = group.totalCount / slotsCount;
            int remainder = group.totalCount % slotsCount;

            for (int i = 0; i < slotsCount; i++) {
                int slotIndex = group.slots.get(i);
                int countForThisSlot = baseCount + (i < remainder ? 1 : 0);

                if (countForThisSlot > 0) {
                    ItemStack newStack = group.sample.copyWithCount(countForThisSlot);
                    this.itemHandler.setStackInSlot(slotIndex, newStack);
                } else {
                    this.itemHandler.setStackInSlot(slotIndex, ItemStack.EMPTY);
                }
            }
        }
        this.isDistributing = false;
    }

    private static class ItemGroup {
        ItemStack sample;
        List<Integer> slots = new ArrayList<>();
        int totalCount = 0;
    }

    // --- LOGIQUE DE CRAFT ET DE VERIFICATION ---

    private boolean canProcess(int slot, ItemStackHandler simItemHandler, int[] simFluids) {
        ItemStack inputStack = this.itemHandler.getStackInSlot(slot);
        SheepVariantData variantData = ((ResourcefulWoolItem) inputStack.getItem()).getVariantData();
        if (variantData == null) return false;

        List<ItemStack> maxItemDrops = new ArrayList<>();
        maxItemDrops.add(getVanillaWool(inputStack));
        List<FluidStack> maxFluidDrops = new ArrayList<>();

        if (variantData.DroppedItems() != null) {
            for (SheepVariantData.DroppedItems drop : variantData.DroppedItems()) {
                ResourceLocation res = ResourceLocation.tryParse(drop.ItemId());
                if (res != null) {
                    if (BuiltInRegistries.FLUID.containsKey(res)) {
                        maxFluidDrops.add(new FluidStack(BuiltInRegistries.FLUID.get(res), drop.MaxDrops()));
                    } else if (BuiltInRegistries.ITEM.containsKey(res)) {
                        maxItemDrops.add(new ItemStack(BuiltInRegistries.ITEM.get(res), drop.MaxDrops()));
                    }
                }
            }
        }

        // On fait une copie locale de la simulation
        ItemStackHandler tempSimItems = new ItemStackHandler(simItemHandler.getSlots());
        for (int j = 0; j < simItemHandler.getSlots(); j++) {
            tempSimItems.setStackInSlot(j, simItemHandler.getStackInSlot(j).copy());
        }
        int[] tempSimFluids = Arrays.copyOf(simFluids, simFluids.length);

        // Appel des petites méthodes
        if (!canInsertItems(tempSimItems, maxItemDrops)) return false;
        if (!canInsertFluids(tempSimFluids, maxFluidDrops)) return false;

        // Si on arrive ici, tout est validé. On met à jour la vraie simulation.
        for (int j = 0; j < simItemHandler.getSlots(); j++) {
            simItemHandler.setStackInSlot(j, tempSimItems.getStackInSlot(j));
        }
        System.arraycopy(tempSimFluids, 0, simFluids, 0, simFluids.length);

        return true;
    }

    private void craftItem(int slot) {
        ItemStack inputStack = this.itemHandler.getStackInSlot(slot);
        if (!(inputStack.getItem() instanceof ResourcefulWoolItem woolItem)) return;
        SheepVariantData variantData = woolItem.getVariantData();

        // Rendre la laine de base de Minecraft
        insertIntoOutput(getVanillaWool(inputStack));

        // Générer et insérer les drops aléatoires
        if (variantData != null && variantData.DroppedItems() != null) {
            for (SheepVariantData.DroppedItems drop : variantData.DroppedItems()) {
                int count = ThreadLocalRandom.current().nextInt(drop.MinDrops(), drop.MaxDrops() + 1);
                if (count <= 0) continue;

                ResourceLocation resourceLocation = ResourceLocation.tryParse(drop.ItemId());
                if (resourceLocation == null) continue;

                if (BuiltInRegistries.FLUID.containsKey(resourceLocation)) {
                    insertFluid(new FluidStack(BuiltInRegistries.FLUID.get(resourceLocation), count));
                } else if (BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
                    insertIntoOutput(new ItemStack(BuiltInRegistries.ITEM.get(resourceLocation), count));
                }
            }
        }
        this.itemHandler.extractItem(slot, 1, false);
    }

    private boolean canInsertItems(ItemStackHandler tempSimItems, List<ItemStack> itemsToInsert) {
        for (ItemStack stack : itemsToInsert) {
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(tempSimItems, stack, false);
            if (!remainder.isEmpty()) return false;
        }
        return true;
    }

    private boolean canInsertFluids(int[] tempSimFluids, List<FluidStack> fluidsToInsert) {
        for (FluidStack stack : fluidsToInsert) {
            int amountLeft = stack.getAmount();
            for (int t = 0; t < this.fluidTanks.length; t++) {
                FluidStack tankFluid = this.fluidTanks[t].getFluid();
                if (tankFluid.isEmpty() || tankFluid.is(stack.getFluid())) {
                    int space = this.fluidTanks[t].getCapacity() - tempSimFluids[t];
                    int filled = Math.min(space, amountLeft);
                    amountLeft -= filled;
                    tempSimFluids[t] += filled;
                    if (amountLeft <= 0) break;
                }
            }
            if (amountLeft > 0) return false;
        }
        return true;
    }

    private void insertIntoOutput(ItemStack stack) {
        int inputSize = this.tier.getParallelProcesses();
        for (int i = inputSize; i < inputSize + getOutputInventorySize(); i++) {
            stack = this.itemHandler.insertItem(i, stack, false);
            if (stack.isEmpty()) break;
        }
    }

    private void insertFluid(FluidStack stack) {
        for (FluidTank tank : this.fluidTanks) {
            int filled = tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            stack.shrink(filled);
            if (stack.isEmpty()) break;
        }
    }

    private ItemStack getVanillaWool(ItemStack resourcefulWool) {
        DyeColor color = DyeColor.WHITE; // Couleur par défaut en cas de problème

        // On extrait le DataComponent qui stocke les blockstates (la couleur) de l'item
        BlockItemStateProperties stateProps = resourcefulWool.get(DataComponents.BLOCK_STATE);
        if (stateProps != null) {
            String colorName = stateProps.properties().get("color");
            if (colorName != null) {
                color = DyeColor.byName(colorName, DyeColor.WHITE);
            }
        }
        return new ItemStack(getWoolItemFromColor(color), 1);
    }

    private Item getWoolItemFromColor(DyeColor color) {
        return switch (color) {
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
            case WHITE -> Items.WHITE_WOOL;
        };
    }

    // --- SAUVEGARDE & CHARGEMENT ---

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putIntArray("centrifuge.process_timers", processTimers);
        tag.putBoolean("centrifuge.isAssembled", isAssembled);

        // Sauvegarde des fluides
        CompoundTag fluidTag = new CompoundTag();
        for (int i = 0; i < fluidTanks.length; i++) {
            fluidTag.put("tank_" + i, fluidTanks[i].writeToNBT(registries, new CompoundTag()));
        }
        tag.put("fluids", fluidTag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        loadData(registries, tag);
        if (tag.contains("centrifuge.isAssembled")) isAssembled = tag.getBoolean("centrifuge.isAssembled");
        loadFluids(registries, tag);
    }

    public void saveToItem(@NotNull ItemStack stack, HolderLookup.@NotNull Provider registries) {
        CompoundTag data = new CompoundTag();

        data.put("inventory", itemHandler.serializeNBT(registries));
        data.putInt("energy", energyStorage.getEnergyStored());
        data.putIntArray("centrifuge.process_timers", processTimers);

        CompoundTag fluidTag = new CompoundTag();
        for (int i = 0; i < fluidTanks.length; i++) {
            fluidTag.put("tank_" + i, fluidTanks[i].writeToNBT(registries, new CompoundTag()));
        }
        data.put("fluids", fluidTag);

        stack.set(ModDataComponents.CENTRIFUGE_DATA.get(), data);
    }

    public void loadFromItem(ItemStack stack, HolderLookup.@NotNull Provider registries) {
        if (stack.has(ModDataComponents.CENTRIFUGE_DATA.get())) {
            CompoundTag data = stack.get(ModDataComponents.CENTRIFUGE_DATA.get());
            if (data != null) {
                loadData(registries, data);
                loadFluids(registries, data);
            }
        }
    }

    private void loadData(HolderLookup.@NotNull Provider registries, CompoundTag data) {
        if (data.contains("inventory")) itemHandler.deserializeNBT(registries, data.getCompound("inventory"));
        if (data.contains("energy")) energyStorage.setEnergy(data.getInt("energy"));

        if (data.contains("centrifuge.process_timers")) {
            int[] loadedTimers = data.getIntArray("centrifuge.process_timers");
            System.arraycopy(loadedTimers, 0, this.processTimers, 0, Math.min(this.processTimers.length, loadedTimers.length));
        }
    }

    private void loadFluids(HolderLookup.@NotNull Provider registries, CompoundTag data) {
        if (data.contains("fluids")) {
            CompoundTag fluidTag = data.getCompound("fluids");
            for (int i = 0; i < fluidTanks.length; i++) {
                if (fluidTag.contains("tank_" + i)) {
                    fluidTanks[i].readFromNBT(registries, fluidTag.getCompound("tank_" + i));
                }
            }
        }
    }

    // --- SYNCHRONISATION CLIENT ---

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

    // --- MENU PROVIDER ---

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal(this.tier.name().substring(0, 1).toUpperCase() + this.tier.name().substring(1).toLowerCase() + " Centrifuge");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new CentrifugeMenu(id, inventory, this, this.data);
    }

    public CentrifugeTier getTier() {
        return this.tier;
    }

    public boolean isAssembled() {
        return this.isAssembled;
    }
}