package net.minecraft.commands.arguments.coordinates;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
   Vec3 getPosition(CommandSourceStack pSource);

   Vec2 getRotation(CommandSourceStack pSource);

   default BlockPos getBlockPos(CommandSourceStack pSource) {
      return BlockPos.containing(this.getPosition(pSource));
   }

   boolean isXRelative();

   boolean isYRelative();

   boolean isZRelative();
}