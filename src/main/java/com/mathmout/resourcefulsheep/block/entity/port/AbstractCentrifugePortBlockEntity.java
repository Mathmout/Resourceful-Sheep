package com.mathmout.resourcefulsheep.block.entity.port;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractCentrifugePortBlockEntity extends BlockEntity {

     private BlockPos controllerPos;

    public AbstractCentrifugePortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void setControllerPos(BlockPos controllerPos) {
        if (Objects.equals(this.controllerPos, controllerPos)) return;
        this.controllerPos = controllerPos;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.level.invalidateCapabilities(this.worldPosition);
        }
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.controllerPos != null) {
            tag.putInt("ctrl_x", this.controllerPos.getX());
            tag.putInt("ctrl_y", this.controllerPos.getY());
            tag.putInt("ctrl_z", this.controllerPos.getZ());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ctrl_x") && tag.contains("ctrl_y") && tag.contains("ctrl_z")) {
            this.controllerPos = new BlockPos(tag.getInt("ctrl_x"), tag.getInt("ctrl_y"), tag.getInt("ctrl_z"));
        } else {
            this.controllerPos = null;
        }
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