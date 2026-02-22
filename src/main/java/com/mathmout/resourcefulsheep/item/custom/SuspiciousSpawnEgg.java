package com.mathmout.resourcefulsheep.item.custom;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.config.dnacrossbreeding.ConfigDNACrossbreedingManager;
import com.mathmout.resourcefulsheep.config.dnacrossbreeding.SheepCrossbreeding;
import com.mathmout.resourcefulsheep.config.mutations.ConfigSheepMutationManager;
import com.mathmout.resourcefulsheep.config.mutations.SheepMutation;
import com.mathmout.resourcefulsheep.item.ModDataComponents;
import com.mathmout.resourcefulsheep.utils.TexteUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SuspiciousSpawnEgg extends Item {

    private static List<String> ALL_EGGS_CACHE = null;

    public SuspiciousSpawnEgg(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = pos.relative(direction);

        String entityIdToSpawn;

        if (stack.has(ModDataComponents.SUSPICIOUS_EGG_DATA.get())) {
            CompoundTag data = stack.get(ModDataComponents.SUSPICIOUS_EGG_DATA.get());
            if (data != null && data.contains("mom_id") && data.contains("dad_id")) {
                String mom = data.getString("mom_id");
                String dad = data.getString("dad_id");
                // On calcule le hasard MAINTENANT
                entityIdToSpawn = determineResultId(mom, dad, serverLevel.random);
            } else {
                entityIdToSpawn = getRandomEntity(serverLevel.random);
            }
        } else {
            // Pas de NBT (Oeuf du menu Créatif)
            entityIdToSpawn = getRandomEntity(serverLevel.random);
        }

        EntityType<?> type = EntityType.byString(entityIdToSpawn).orElse(null);
        if (type != null) {
            Entity entity = type.create(serverLevel);
            if (entity != null) {
                entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);

                if (entity instanceof AgeableMob ageable) {
                    ageable.setBaby(true);
                }

                serverLevel.addFreshEntity(entity);
                entity.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());

                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.literal("Possible results :").withStyle(ChatFormatting.GREEN));

            List<String> list;
            if (stack.has(ModDataComponents.SUSPICIOUS_EGG_DATA.get())) {
                CompoundTag data = stack.get(ModDataComponents.SUSPICIOUS_EGG_DATA.get());
                if (data != null && data.contains("mom_id") && data.contains("dad_id")) {
                    String mom = data.getString("mom_id");
                    String dad = data.getString("dad_id");
                    list = getPossibleResults(mom, dad);
                } else {
                    list = getAllSpawnEggIds();
                }
            } else {
                list = getAllSpawnEggIds();
            }

            int maxDisplay = 15;
            int count = 0;
            for (String id : list) {
                if (count >= maxDisplay) {
                    tooltipComponents.add(Component.literal("... and " + (list.size() - maxDisplay) + " more.").withStyle(ChatFormatting.GRAY));
                    break;
                }
                String name = TexteUtils.getPrettyName(id);
                tooltipComponents.add(Component.literal("- " + name).withStyle(ChatFormatting.GRAY));
                count++;
            }
        } else {
            tooltipComponents.add(Component.literal("Hold SHIFT for details.")
                    .withStyle(ChatFormatting.GRAY)
                    .withStyle(ChatFormatting.ITALIC));
        }
    }

    public static String determineResultId(String p1, String p2, RandomSource random) {
        for (SheepCrossbreeding crossbreeding : ConfigDNACrossbreedingManager.getSheepCrossbreeding()) {
            if ((crossbreeding.MomId().equals(p1) && crossbreeding.DadId().equals(p2)) ||
                    (crossbreeding.MomId().equals(p2) && crossbreeding.DadId().equals(p1))) {
                if (random.nextInt(100) < crossbreeding.Chance()) {
                    return crossbreeding.ChildId();
                } else {
                    List<String> fails = crossbreeding.ResultsIfFail();
                    if (!fails.isEmpty()) return fails.get(random.nextInt(fails.size()));
                    return p1;
                }
            }
        }

        String p1Clean = p1.replace(ResourcefulSheepMod.MOD_ID + ":", "");
        String p2Clean = p2.replace(ResourcefulSheepMod.MOD_ID + ":", "");

        for (SheepMutation mutation : ConfigSheepMutationManager.getSheepMutations()) {
            if ((mutation.MomId().equals(p1Clean) && mutation.DadId().equals(p2Clean)) ||
                    (mutation.MomId().equals(p2Clean) && mutation.DadId().equals(p1Clean))) {
                if (random.nextInt(100) < mutation.Chance()) {
                    return ResourcefulSheepMod.MOD_ID + ":" + mutation.ChildId();
                }
                return random.nextBoolean() ? p1 : p2;
            }
        }
        return random.nextBoolean() ? p1 : p2;
    }

    public static List<String> getPossibleResults(String p1, String p2) {
        List<String> list = new ArrayList<>();

        for (SheepCrossbreeding recipe : ConfigDNACrossbreedingManager.getSheepCrossbreeding()) {
            if ((recipe.MomId().equals(p1) && recipe.DadId().equals(p2)) ||
                    (recipe.MomId().equals(p2) && recipe.DadId().equals(p1))) {
                addIdSafe(list, recipe.ChildId());
                for(String s : recipe.ResultsIfFail()) addIdSafe(list, s);
            }
        }

        String p1Clean = p1.replace(ResourcefulSheepMod.MOD_ID + ":", "");
        String p2Clean = p2.replace(ResourcefulSheepMod.MOD_ID + ":", "");
        for (SheepMutation mutation : ConfigSheepMutationManager.getSheepMutations()) {
            if ((mutation.MomId().equals(p1Clean) && mutation.DadId().equals(p2Clean)) ||
                    (mutation.MomId().equals(p2Clean) && mutation.DadId().equals(p1Clean))) {
                addIdSafe(list, ResourcefulSheepMod.MOD_ID + ":" + mutation.ChildId());
            }
        }

        addIdSafe(list, p1);
        addIdSafe(list, p2);
        return list;
    }

    private static void addIdSafe(List<String> list, String id) {
        String fullId = id.contains(":") ? id : ResourcefulSheepMod.MOD_ID + ":" + id;
        if (!list.contains(fullId)) list.add(fullId);
    }

    public static List<String> getAllSpawnEggIds() {
        if (ALL_EGGS_CACHE == null) {
            ALL_EGGS_CACHE = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof SpawnEggItem egg) {
                    EntityType<?> type = egg.getType(new ItemStack(egg));
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    ALL_EGGS_CACHE.add(id.toString());
                }
            }
        }
        return ALL_EGGS_CACHE;
    }

    private String getRandomEntity(RandomSource random) {
        List<String> allEntities = getAllSpawnEggIds();
        if (allEntities.isEmpty()) return "minecraft:pig";
        return allEntities.get(random.nextInt(allEntities.size()));
    }
}