package com.mathmout.resourcefulsheep.block.custom;

import com.mathmout.resourcefulsheep.block.entity.DNASplicerBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DNASplicerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<DNASplicerBlock> CODEC = simpleCodec(DNASplicerBlock::new);

    public DNASplicerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new DNASplicerBlockEntity(blockPos, blockState);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.DNA_SPLICER_BLOCK_ENTITY.get(),
                (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick(pLevel));
    }

    // Partie Interaction
    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DNASplicerBlockEntity splicer) {
                ItemStack itemStack = new ItemStack(this);
                splicer.saveToItem(itemStack, level.registryAccess());
                popResource(level, pos, itemStack);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DNASplicerBlockEntity splicer) {
            splicer.loadFromItem(stack, level.registryAccess());
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DNASplicerBlockEntity) {
                player.openMenu((DNASplicerBlockEntity) blockEntity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        if (stack.has(ModDataComponents.SPLICER_DATA.get())){
            CompoundTag data = stack.get(ModDataComponents.SPLICER_DATA.get());
                if (data != null) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.literal("Data :").withStyle(ChatFormatting.GREEN));
                    } else {
                        tooltipComponents.add(Component.literal("Hold SHIFT for details.")
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(ChatFormatting.ITALIC));
                    }
                }
        } else {
            tooltipComponents.add(Component.literal("Allows you to do something cool with DNA...")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}