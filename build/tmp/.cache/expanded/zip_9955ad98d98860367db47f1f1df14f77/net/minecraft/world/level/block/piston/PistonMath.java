package net.minecraft.world.level.block.piston;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class PistonMath {
   public static AABB getMovementArea(AABB pBounds, Direction pDir, double pDelta) {
      double d0 = pDelta * (double)pDir.getAxisDirection().getStep();
      double d1 = Math.min(d0, 0.0D);
      double d2 = Math.max(d0, 0.0D);
      switch (pDir) {
         case WEST:
            return new AABB(pBounds.minX + d1, pBounds.minY, pBounds.minZ, pBounds.minX + d2, pBounds.maxY, pBounds.maxZ);
         case EAST:
            return new AABB(pBounds.maxX + d1, pBounds.minY, pBounds.minZ, pBounds.maxX + d2, pBounds.maxY, pBounds.maxZ);
         case DOWN:
            return new AABB(pBounds.minX, pBounds.minY + d1, pBounds.minZ, pBounds.maxX, pBounds.minY + d2, pBounds.maxZ);
         case UP:
         default:
            return new AABB(pBounds.minX, pBounds.maxY + d1, pBounds.minZ, pBounds.maxX, pBounds.maxY + d2, pBounds.maxZ);
         case NORTH:
            return new AABB(pBounds.minX, pBounds.minY, pBounds.minZ + d1, pBounds.maxX, pBounds.maxY, pBounds.minZ + d2);
         case SOUTH:
            return new AABB(pBounds.minX, pBounds.minY, pBounds.maxZ + d1, pBounds.maxX, pBounds.maxY, pBounds.maxZ + d2);
      }
   }
}