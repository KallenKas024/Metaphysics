package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class SubShape extends DiscreteVoxelShape {
   private final DiscreteVoxelShape parent;
   private final int startX;
   private final int startY;
   private final int startZ;
   private final int endX;
   private final int endY;
   private final int endZ;

   protected SubShape(DiscreteVoxelShape pParent, int pStartX, int pStartY, int pStartZ, int pEndX, int pEndY, int pEndZ) {
      super(pEndX - pStartX, pEndY - pStartY, pEndZ - pStartZ);
      this.parent = pParent;
      this.startX = pStartX;
      this.startY = pStartY;
      this.startZ = pStartZ;
      this.endX = pEndX;
      this.endY = pEndY;
      this.endZ = pEndZ;
   }

   public boolean isFull(int pX, int pY, int pZ) {
      return this.parent.isFull(this.startX + pX, this.startY + pY, this.startZ + pZ);
   }

   public void fill(int pX, int pY, int pZ) {
      this.parent.fill(this.startX + pX, this.startY + pY, this.startZ + pZ);
   }

   public int firstFull(Direction.Axis pAxis) {
      return this.clampToShape(pAxis, this.parent.firstFull(pAxis));
   }

   public int lastFull(Direction.Axis pAxis) {
      return this.clampToShape(pAxis, this.parent.lastFull(pAxis));
   }

   private int clampToShape(Direction.Axis pAxis, int pValue) {
      int i = pAxis.choose(this.startX, this.startY, this.startZ);
      int j = pAxis.choose(this.endX, this.endY, this.endZ);
      return Mth.clamp(pValue, i, j) - i;
   }
}