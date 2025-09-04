package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Lasso extends Item {
    public Lasso(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip." + ResourcefulSheepMod.MOD_ID + ".lasso").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
