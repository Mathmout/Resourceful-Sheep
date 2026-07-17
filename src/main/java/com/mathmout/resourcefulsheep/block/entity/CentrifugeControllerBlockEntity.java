package com.mathmout.resourcefulsheep.block.entity;

import com.mathmout.resourcefulsheep.block.custom.centrifuge.AbstractCentrifugePartBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeCasingBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeControllerBlock;
import com.mathmout.resourcefulsheep.block.custom.centrifuge.CentrifugeTier;
import com.mathmout.resourcefulsheep.block.entity.port.AbstractCentrifugePortBlockEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.item.custom.ResourcefulWoolItem;
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

        this.itemHandler = new ItemStackHandler(this.tier.getParallelProcesses() * 3) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot < tier.getParallelProcesses()) {
                    return stack.getItem() instanceof ResourcefulWoolItem;
                }
                return false;
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
                }
            };
        }
    }

    // --- LOGIQUE MULTIBLOCK ---

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
                        this.isAssembled = false;
                        return false;
                    }

                    if (h > 0 && !(block instanceof CentrifugeCasingBlock)) {
                        this.isAssembled = false;
                        return false;
                    }

                    tmpParts.add(checkPos);
                }
            }
        }

        this.multiblockParts.clear();
        this.multiblockParts.addAll(tmpParts);
        this.isAssembled = true;

        for (BlockPos partPos : this.multiblockParts) {
            BlockEntity blockEntity = level.getBlockEntity(partPos);
            if (blockEntity instanceof AbstractCentrifugePortBlockEntity portEntity) {
                portEntity.setControllerPos(this.getBlockPos());
            }
        }
        return true;
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
            int inputSize = this.tier.getParallelProcesses();
            int activeProcesses = 0;
            boolean[] canProcessSlot = new boolean[inputSize];

            // Déterminer les slots valides (assez de place en sortie)
            for (int i = 0; i < inputSize; i++) {
                if (canProcess(i)) {
                    canProcessSlot[i] = true;
                    activeProcesses++;
                } else if (this.processTimers[i] > 0) {
                    this.processTimers[i] = 0;
                    setChanged();
                }
            }

            // Si on a des processus actifs, on calcule l'énergie requise par processus
            if (activeProcesses > 0) {
                int energyPerProcess = this.tier.getEnergyConsumption() / activeProcesses;

                // Est-ce qu'on a assez d'énergie pour tous les processus actifs ?
                if (this.energyStorage.getEnergyStored() >= (energyPerProcess * activeProcesses)) {
                    boolean isWorking = false;
                    for (int i = 0; i < inputSize; i++) {
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
    }

    // --- LOGIQUE DE CRAFT ET DE VERIFICATION ---

    private boolean canProcess(int slot) {
        ItemStack inputStack = this.itemHandler.getStackInSlot(slot);
        if (!(inputStack.getItem() instanceof ResourcefulWoolItem woolItem)) return false;

        SheepVariantData variantData = woolItem.getVariantData();
        if (variantData == null) return false;

        List<ItemStack> maxItemDrops = new ArrayList<>();
        maxItemDrops.add(getVanillaWool(inputStack));

        List<FluidStack> maxFluidDrops = new ArrayList<>();

        if (variantData.DroppedItems() != null) {
            for (SheepVariantData.DroppedItems drop : variantData.DroppedItems()) {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(drop.ItemId());
                if (resourceLocation == null) continue;

                if (BuiltInRegistries.FLUID.containsKey(resourceLocation)) {
                    maxFluidDrops.add(new FluidStack(BuiltInRegistries.FLUID.get(resourceLocation), drop.MaxDrops()));
                } else if (BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
                    maxItemDrops.add(new ItemStack(BuiltInRegistries.ITEM.get(resourceLocation), drop.MaxDrops()));
                }
            }
        }

        return canInsertItems(maxItemDrops) && canInsertFluids(maxFluidDrops);
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

        // Consommer la ressource d'entrée
        inputStack.shrink(1);
    }

    private boolean canInsertItems(List<ItemStack> itemsToInsert) {
        int inputSize = this.tier.getParallelProcesses();
        int outputSize = inputSize * 2;

        // On copie virtuellement l'inventaire de sortie pour faire une simulation
        ItemStackHandler simulationHandler = new ItemStackHandler(outputSize);
        for (int i = 0; i < outputSize; i++) {
            simulationHandler.setStackInSlot(i, this.itemHandler.getStackInSlot(inputSize + i).copy());
        }

        for (ItemStack stack : itemsToInsert) {
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(simulationHandler, stack, false);
            // Si la simulation ne peut pas tout insérer, on bloque le craft
            if (!remainder.isEmpty()) return false;
        }
        return true;
    }

    private boolean canInsertFluids(List<FluidStack> fluidsToInsert) {
        int[] simulatedAmounts = new int[this.fluidTanks.length];
        for (int i = 0; i < fluidTanks.length; i++) simulatedAmounts[i] = fluidTanks[i].getFluidAmount();

        for (FluidStack stack : fluidsToInsert) {
            int amountLeft = stack.getAmount();
            for (int i = 0; i < fluidTanks.length; i++) {
                FluidStack tankFluid = fluidTanks[i].getFluid();
                if (tankFluid.isEmpty() || tankFluid.is(stack.getFluid())) {
                    int space = fluidTanks[i].getCapacity() - simulatedAmounts[i];
                    int filled = Math.min(space, amountLeft);
                    amountLeft -= filled;
                    simulatedAmounts[i] += filled;
                    if (amountLeft <= 0) break;
                }
            }
            if (amountLeft > 0) return false;
        }
        return true;
    }

    private void insertIntoOutput(ItemStack stack) {
        int inputSize = this.tier.getParallelProcesses();
        int outputSize = inputSize * 2;
        for (int i = inputSize; i < inputSize + outputSize; i++) {
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
        // Optionnel : adapter le nom selon le tier
        return Component.literal(this.tier.name().substring(0, 1).toUpperCase() + this.tier.name().substring(1).toLowerCase() + " Centrifuge");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        // TODO: return new CentrifugeMenu(id, inventory, this, this.data);
        return null;
    }

    public CentrifugeTier getTier() {
        return this.tier;
    }

    public boolean isAssembled() {
        return this.isAssembled;
    }
}