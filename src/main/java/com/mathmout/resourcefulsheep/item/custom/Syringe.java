package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Syringe extends Item {

    public enum SyringeTiers {
        IRON,
        DIAMOND,
        NETHERITE
    }

    private final SyringeTiers tier;

    public Syringe(Properties properties, SyringeTiers tier) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity interactionTarget, @NotNull InteractionHand usedHand) {

        if (stack.has(ModDataComponents.SYRINGE_CONTENT.get()) || !isValidTarget(interactionTarget))
            return InteractionResult.FAIL;

        // Prélèvement de l'ADN
        if (!player.level().isClientSide) {

            String entityResourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(interactionTarget.getType()).toString();

            // On crée le tag
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entityResourceLocation);

            // On applique la modification sur l'ItemStack
            ItemStack filledStack = stack.copy();
            filledStack.set(ModDataComponents.SYRINGE_CONTENT.get(), tag);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.setItemInHand(usedHand, filledStack);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean isValidTarget(LivingEntity target) {
        return switch (this.tier) {
            case IRON -> (target instanceof AgeableMob ||
                         target instanceof Allay ||
                         target instanceof AmbientCreature ||
                         target instanceof WaterAnimal ||
                         target instanceof AbstractGolem) &&
                         !(target instanceof Shulker);

            case DIAMOND -> (target instanceof AgeableMob ||
                            target instanceof Allay ||
                            target instanceof AmbientCreature ||
                            target instanceof WaterAnimal ||
                            target instanceof FlyingMob ||
                            target instanceof Monster ||
                            target instanceof Slime ||
                            target instanceof AbstractGolem) &&
                            !(target instanceof WitherBoss) &&
                            !(target instanceof Warden);

            case NETHERITE -> target instanceof Mob;
        };
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        // Full
        if (stack.has(ModDataComponents.SYRINGE_CONTENT.get())) {
            CompoundTag tag = stack.get(ModDataComponents.SYRINGE_CONTENT.get());
            if (tag != null && tag.contains("id")){
                String entityId = tag.getString("id");
                tooltipComponents.add(Component.literal("Contains DNA : ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(TexteUtils.getPrettyName(entityId)).withStyle(ChatFormatting.GRAY)));
            }
        }
        // Empty
        else {
            if (this.tier == SyringeTiers.IRON) {
                tooltipComponents.add(Component.literal("Allows you to extract blood from").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" Animals").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(" only.")));
            } else if (this.tier == SyringeTiers.DIAMOND) {
                tooltipComponents.add(Component.literal("Allows you to extract blood from").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" Animals").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(" and"))
                        .append(Component.literal(" Monsters.").withStyle(ChatFormatting.BOLD)));
            } else if (this.tier == SyringeTiers.NETHERITE) {
                tooltipComponents.add(Component.literal("Allows you to extract blood from").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" Animals").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(","))
                        .append(Component.literal(" Monsters").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(" and"))
                        .append(Component.literal(" Bosses").withStyle(ChatFormatting.BOLD)));
            }
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return stack.has(ModDataComponents.SYRINGE_CONTENT.get());
    }
}