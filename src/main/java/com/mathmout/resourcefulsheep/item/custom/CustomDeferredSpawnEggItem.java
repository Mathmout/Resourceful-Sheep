package com.mathmout.resourcefulsheep.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CustomDeferredSpawnEggItem extends DeferredSpawnEggItem {

    private final String displayName;
    private final int nameColor;

    public CustomDeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type,
                                      int backgroundColor,
                                      int highlightColor,
                                      Properties props,
                                      String displayName,
                                      int nameColor) {

        super(type, backgroundColor, highlightColor, props);
        this.displayName = displayName;
        this.nameColor = nameColor;
    }
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.literal(displayName).withStyle(Style.EMPTY.withColor(nameColor));
    }

}
