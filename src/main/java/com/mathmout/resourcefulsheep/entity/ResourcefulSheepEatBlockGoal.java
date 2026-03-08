package com.mathmout.resourcefulsheep.entity;

import com.mathmout.resourcefulsheep.entity.custom.ResourcefulSheepEntity;
import com.mathmout.resourcefulsheep.entity.custom.SheepVariantData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Map;

public class ResourcefulSheepEatBlockGoal extends EatBlockGoal {
    private final ResourcefulSheepEntity sheep;
    private final Level level;
    private int myEatAnimationTick;

    public ResourcefulSheepEatBlockGoal(ResourcefulSheepEntity sheep) {
        super(sheep);
        this.sheep = sheep;
        this.level = sheep.level();
    }

    @Override
    public void start() {
        this.myEatAnimationTick = this.adjustedTickDelay(40);
        this.level.broadcastEntityEvent(this.sheep, (byte)10);
        this.sheep.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.myEatAnimationTick = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.myEatAnimationTick > 0;
    }

    @Override
    public int getEatAnimationTick() {
        return this.myEatAnimationTick;
    }

    @Override
    public boolean canUse() {
        if (this.sheep.getRandom().nextInt(this.sheep.isBaby() ? 50 : 1000) != 0) {
            return false;
        }
        BlockPos pos = this.sheep.blockPosition();
        return isEdible(pos) || isEdible(pos.below());
    }

    @Override
    public void tick() {
        this.myEatAnimationTick = Math.max(0, this.myEatAnimationTick - 1);
        if (this.myEatAnimationTick == 4) {
            BlockPos pos = this.sheep.blockPosition();
            if (!tryEatBlock(pos)) {
                tryEatBlock(pos.below());
            }
        }
    }

    private String getMatchingReplacement(BlockState blockState, SheepVariantData variant) {
        if (variant == null || variant.EtableBocksMap() == null) return null;

        String blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();

        for (Map.Entry<String, String> entry : variant.EtableBocksMap().entrySet()) {
            String key = entry.getKey();

            // Tag
            if (key.startsWith("#")) {
                ResourceLocation tagLoc = ResourceLocation.tryParse(key.substring(1));
                if (tagLoc != null) {
                    TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagLoc);
                    if (blockState.is(tagKey)) {
                        return entry.getValue();
                    }
                }
            }
            // Item ID
            else {
                if (key.equals(blockId)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    // Vérifie si un bloc est comestible
    private boolean isEdible(BlockPos blockPos) {
        SheepVariantData variant = this.sheep.getSheepVariantData();
        BlockState blockState = this.level.getBlockState(blockPos);
        if (variant != null && variant.EtableBocksMap() != null && !variant.EtableBocksMap().isEmpty()) {
            return getMatchingReplacement(blockState, variant) != null;
        }
        return blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.SHORT_GRASS) || blockState.is(Blocks.FERN);
    }

    // Exécute l'action de manger
    private boolean tryEatBlock(BlockPos pos) {
        if (!isEdible(pos)) return false;

        SheepVariantData variant = this.sheep.getSheepVariantData();
        BlockState currentBlockState = this.level.getBlockState(pos);

        // MobGriefing
        boolean canGrief = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);

        if (!canGrief) {
            onSuccess(pos, currentBlockState);
            return true;
        }

        // Custom
        String newId = getMatchingReplacement(currentBlockState, variant);
        if (newId != null) {
            // Logique de remplacement
            if (newId.equals("minecraft:air") || newId.isEmpty()) {
                this.level.destroyBlock(pos, false);
            } else {
                Block newBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(newId));
                this.level.setBlock(pos, newBlock.defaultBlockState(), 3);
            }
            onSuccess(pos, currentBlockState);
            return true;
        }

        // Vanilla
        else {
            if (currentBlockState.is(Blocks.GRASS_BLOCK)) {
                this.level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                onSuccess(pos, currentBlockState);
                return true;
            } else if (currentBlockState.is(Blocks.SHORT_GRASS) || currentBlockState.is(Blocks.FERN)) {
                this.level.destroyBlock(pos, false);
                onSuccess(pos, currentBlockState);
                return true;
            }
        }
        return false;
    }

    private void onSuccess(BlockPos pos, BlockState eatenBlockState) {
        this.sheep.gameEvent(GameEvent.EAT);
        var soundType = eatenBlockState.getSoundType(this.level, pos, this.sheep);
        this.level.playSound(
                null,
                pos,
                soundType.getBreakSound(),
                SoundSource.BLOCKS,
                soundType.getVolume(),
                soundType.getPitch() * 0.8F
        );
        this.sheep.ate();
    }


}
