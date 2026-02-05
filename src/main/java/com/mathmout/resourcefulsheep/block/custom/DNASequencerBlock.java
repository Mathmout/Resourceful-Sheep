package com.mathmout.resourcefulsheep.block.custom;

import com.mathmout.resourcefulsheep.block.entity.DNASequencerBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.ModBlockEntities;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DNASequencerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<DNASequencerBlock> CODEC = simpleCodec(DNASequencerBlock::new);

    public DNASequencerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DNASequencerBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;

        return createTickerHelper(type, ModBlockEntities.DNA_SEQUENCER_BLOCK_ENTITY.get(),
                (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick(pLevel));
    }

    // Partie Interaction

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DNASequencerBlockEntity sequencer) {
                ItemStack itemStack = new ItemStack(this);
                sequencer.saveToItem(itemStack, level.registryAccess());
                popResource(level, pos, itemStack);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DNASequencerBlockEntity sequencer) {
            sequencer.loadFromItem(stack, level.registryAccess());
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DNASequencerBlockEntity) {
                player.openMenu((DNASequencerBlockEntity) blockEntity, pos);
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
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        if (stack.has(ModDataComponents.SEQUENCER_DATA.get())) {
            CompoundTag data = stack.get(ModDataComponents.SEQUENCER_DATA.get());

            if (data != null && data.contains("dna_list", 9)) {
                ListTag nbtList = data.getList("dna_list", 8); // 8 = Type String

                if (!nbtList.isEmpty()) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.literal("Stored DNA Sequences :").withStyle(ChatFormatting.GREEN));

                        List<String> sortedIds = new ArrayList<>();
                        for (Tag tag : nbtList) {
                            sortedIds.add(tag.getAsString());
                        }

                        int maxDisplayed = 22;
                        Collections.sort(sortedIds);
                        for (int i = 0; i < maxDisplayed; i++) {
                            tooltipComponents.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(TexteUtils.getPrettyName(sortedIds.get(i))).withStyle(ChatFormatting.GRAY)));
                        }
                        if (sortedIds.size() > maxDisplayed) {
                            tooltipComponents.add(Component.literal("And " + (sortedIds.size() - maxDisplayed) + " more...").withStyle(ChatFormatting.GRAY));
                        }
                    }
                    else {
                        tooltipComponents.add(Component.literal("Hold SHIFT for details.")
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(ChatFormatting.ITALIC));
                    }
                } else {
                    tooltipComponents.add(Component.literal("Allows you to store and analyze DNA sequences.")
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        } else {
            tooltipComponents.add(Component.literal("Allows you to store and analyze DNA sequences.")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}