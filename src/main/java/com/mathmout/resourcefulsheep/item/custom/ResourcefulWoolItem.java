package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResourcefulWoolItem extends BlockItem {

    private final SheepVariantData variantData;

    public ResourcefulWoolItem(Block block, Properties properties, SheepVariantData variantData) {
        super(block, properties);
        this.variantData = variantData;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable("item." + ResourcefulSheepMod.MOD_ID + ".resourceful_wool");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        SheepVariantData variantData = this.variantData;
        tooltipComponents.add(Component.literal(TexteUtils.stringToText(variantData.Name() + " wool")).withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal("Tier : ").withStyle(ChatFormatting.RED)
                         .append(Component.literal(String.valueOf(variantData.Tier())).withStyle(ChatFormatting.LIGHT_PURPLE)));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public SheepVariantData getVariantData() {
        return variantData;
    }
}
