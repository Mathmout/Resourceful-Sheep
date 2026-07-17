package com.mathmout.resourcefulsheep.block.custom.centrifuge;

import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractCentrifugePartBlock extends Block {

    private final CentrifugeTier centrifugeTier;

    public AbstractCentrifugePartBlock(Properties properties, CentrifugeTier centrifugeTier) {
        super(properties);
        this.centrifugeTier = centrifugeTier;
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