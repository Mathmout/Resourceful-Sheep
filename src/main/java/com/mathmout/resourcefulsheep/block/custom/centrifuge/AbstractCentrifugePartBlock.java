package com.mathmout.resourcefulsheep.block.custom.centrifuge;

import com.mathmout.resourcefulsheep.block.entity.CentrifugeControllerBlockEntity;
import com.mathmout.resourcefulsheep.block.entity.port.AbstractCentrifugePortBlockEntity;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractCentrifugePartBlock extends Block {

    private final CentrifugeTier centrifugeTier;

    public AbstractCentrifugePartBlock(Properties properties, CentrifugeTier centrifugeTier) {
        super(properties);
        this.centrifugeTier = centrifugeTier;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            switch (blockEntity) {
                // Le joueur a cliqué directement sur le contrôleur
                case CentrifugeControllerBlockEntity controller -> {
                    if (controller.isAssembled()) {
                        player.openMenu(controller, pos);
                        return InteractionResult.CONSUME;
                    }
                }
                // Le joueur a cliqué sur un Port (qui possède les coordonnées du maître)
                case AbstractCentrifugePortBlockEntity port when port.getControllerPos() != null -> {
                    BlockEntity masterEntity = level.getBlockEntity(port.getControllerPos());
                    if (masterEntity instanceof CentrifugeControllerBlockEntity controller && controller.isAssembled()) {
                        player.openMenu(controller, port.getControllerPos());
                        return InteractionResult.CONSUME;
                    }
                }
                // Le joueur a cliqué sur un Casing
                case null -> {
                    for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-3, -3, -3), pos.offset(3, 3, 3))) {
                        if (level.getBlockEntity(checkPos) instanceof CentrifugeControllerBlockEntity controller) {

                            if (controller.isAssembled() && controller.hasPart(pos)) {
                                player.openMenu(controller, checkPos);
                                return InteractionResult.CONSUME;
                            }

                        }
                    }
                }
                default -> {}
            }
        }
        return InteractionResult.PASS;
    }

    public CentrifugeTier getCentrifugeTier() {
        return centrifugeTier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ChatFormatting tierColor = switch (getCentrifugeTier()) {
            case BASIC -> ChatFormatting.GREEN;
            case ADVANCED -> ChatFormatting.RED;
            case ELITE -> ChatFormatting.BLUE;
            case ULTIMATE -> ChatFormatting.DARK_PURPLE;
        };

        tooltipComponents.add(Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                         .append(Component.literal(TexteUtils.stringToText(this.centrifugeTier.name())).withStyle(tierColor)));
    }
}