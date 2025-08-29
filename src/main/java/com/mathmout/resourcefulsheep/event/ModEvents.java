package com.mathmout.resourcefulsheep.event;

import com.mathmout.resourcefulsheep.config.ConfigManager;
import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Random;

public class ModEvents {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof ResourcefulSheepEntity sheep) {
            if (event.getItemStack().getItem() == Items.SHEARS) {
                if (!event.getLevel().isClientSide && !sheep.isSheared()) {
                    sheep.shear(SoundSource.PLAYERS);
                    SheepVariantData variantData = ConfigManager.getSheepVariant()
                            .get(BuiltInRegistries.ENTITY_TYPE.getKey(sheep.getType()).getPath());
                    if (variantData != null) {
                        Item droppedItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(variantData.DroppedItem));
                        if (droppedItem != Items.AIR) {
                            Random random = new Random();
                            int count = variantData.MinDrops + random.nextInt(variantData.MaxDrops - variantData.MinDrops + 1);
                            sheep.spawnAtLocation(new ItemStack(droppedItem, count));
                        }
                    }
                    event.getItemStack().hurtAndBreak(1, event.getEntity(), LivingEntity.getSlotForHand(event.getHand()));
                    event.setCanceled(true);
                }
            }
        }
    }
}