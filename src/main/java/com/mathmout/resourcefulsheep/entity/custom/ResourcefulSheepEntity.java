package com.mathmout.resourcefulsheep.entity.custom;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;

public class ResourcefulSheepEntity extends Sheep {
    public ResourcefulSheepEntity(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }
}
