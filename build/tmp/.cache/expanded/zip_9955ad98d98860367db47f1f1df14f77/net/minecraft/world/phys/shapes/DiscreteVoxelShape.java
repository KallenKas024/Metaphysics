package net.minecraft.world.phys.shapes;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;

public abstract class DiscreteVoxelShape {
   private static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
   protected final int xSize;
   protected final int ySize;
   protected final int zSize;

   protected DiscreteVoxelShape(int pXSize, int pYSize, int pZSize) {
      if (pXSize >= 0 && pYSize >= 0 && pZSize >= 0) {
         this.xSize = pXSize;
         this.ySize = pYSize;
         this.zSize = pZSize;
      } else {
         throw new IllegalArgumentException("Need all positive sizes: x: " + pXSize + ", y: " + pYSize + ", z: " + pZSize);
      }
   }

   public boolean isFullWide(AxisCycle pAxis, int pX, int pY, int pZ) {
      return this.isFullWide(pAxis.cycle(pX, pY, pZ, Direction.Axis.X), pAxis.cycle(pX, pY, pZ, Direction.Axis.Y), pAxis.cycle(pX, pY, pZ, Direction.Axis.Z));
   }

   public boolean isFullWide(int pX, int pY, int pZ) {
      if (pX >= 0 && pY >= 0 && pZ >= 0) {
         return pX < this.xSize && pY < this.ySize && pZ < this.zSize ? this.isFull(pX, pY, pZ) : false;
      } else {
         return false;
      }
   }

   public boolean isFull(AxisCycle pRotation, int pX, int pY, int pZ) {
      return this.isFull(pRotation.cycle(pX, pY, pZ, Direction.Axis.X), pRotation.cycle(pX, pY, pZ, Direction.Axis.Y), pRotation.cycle(pX, pY, pZ, Direction.Axis.Z));
   }

   public abstract boolean isFull(int pX, int pY, int pZ);

   public abstract void fill(int pX, int pY, int pZ);

   public boolean isEmpty() {
      for(Direction.Axis direction$axis : AXIS_VALUES) {
         if (this.firstFull(direction$axis) >= this.lastFull(direction$axis)) {
            return true;
         }
      }

      return false;
   }

   public abstract int firstFull(Direction.Axis pAxis);

   public abstract int lastFull(Direction.Axis pAxis);

   public int firstFull(Direction.Axis pAxis, int pY, int pZ) {
      int i = this.getSize(pAxis);
      if (pY >= 0 && pZ >= 0) {
         Direction.Axis direction$axis = AxisCycle.FORWARD.cycle(pAxis);
         Direction.Axis direction$axis1 = AxisCycle.BACKWARD.cycle(pAxis);
         if (pY < this.getSize(direction$axis) && pZ < this.getSize(direction$axis1)) {
            AxisCycle axiscycle = AxisCycle.between(Direction.Axis.X, pAxis);

            for(int j = 0; j < i; ++j) {
               if (this.isFull(axiscycle, j, pY, pZ)) {
                  return j;
               }
            }

            return i;
         } else {
            return i;
         }
      } else {
         return i;
      }
   }

   /**
    * Gives the index of the last filled part in the column.
    */
   public int lastFull(Direction.Axis pAxis, int pY, int pZ) {
      if (pY >= 0 && pZ >= 0) {
         Direction.Axis direction$axis = AxisCycle.FORWARD.cycle(pAxis);
         Direction.Axis direction$axis1 = AxisCycle.BACKWARD.cycle(pAxis);
         if (pY < this.getSize(direction$axis) && pZ < this.getSize(direction$axis1)) {
            int i = this.getSize(pAxis);
            AxisCycle axiscycle = AxisCycle.between(Direction.Axis.X, pAxis);

            for(int j = i - 1; j >= 0; --j) {
               if (this.isFull(axiscycle, j, pY, pZ)) {
                  return j + 1;
               }
            }

            return 0;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public int getSize(Direction.Axis pAxis) {
      return pAxis.choose(this.xSize, this.ySize, this.zSize);
   }

   public int getXSize() {
      return this.getSize(Direction.Axis.X);
   }

   public int getYSize() {
      return this.getSize(Direction.Axis.Y);
   }

   public int getZSize() {
      return this.getSize(Direction.Axis.Z);
   }

   public void forAllEdges(DiscreteVoxelShape.IntLineConsumer pConsumer, boolean pCombine) {
      this.forAllAxisEdges(pConsumer, AxisCycle.NONE, pCombine);
      this.forAllAxisEdges(pConsumer, AxisCycle.FORWARD, pCombine);
      this.forAllAxisEdges(pConsumer, AxisCycle.BACKWARD, pCombine);
   }

   private void forAllAxisEdges(DiscreteVoxelShape.IntLineConsumer pLineConsumer, AxisCycle pAxis, boolean pCombine) {
      AxisCycle axiscycle = pAxis.inverse();
      int j = this.getSize(axiscycle.cycle(Direction.Axis.X));
      int k = this.getSize(axiscycle.cycle(Direction.Axis.Y));
      int l = this.getSize(axiscycle.cycle(Direction.Axis.Z));

      for(int i1 = 0; i1 <= j; ++i1) {
         for(int j1 = 0; j1 <= k; ++j1) {
            int i = -1;

            for(int k1 = 0; k1 <= l; ++k1) {
               int l1 = 0;
               int i2 = 0;

               for(int j2 = 0; j2 <= 1; ++j2) {
                  for(int k2 = 0; k2 <= 1; ++k2) {
                     if (this.isFullWide(axiscycle, i1 + j2 - 1, j1 + k2 - 1, k1)) {
                        ++l1;
                        i2 ^= j2 ^ k2;
                     }
                  }
               }

               if (l1 == 1 || l1 == 3 || l1 == 2 && (i2 & 1) == 0) {
                  if (pCombine) {
                     if (i == -1) {
                        i = k1;
                     }
                  } else {
                     pLineConsumer.consume(axiscycle.cycle(i1, j1, k1, Direction.Axis.X), axiscycle.cycle(i1, j1, k1, Direction.Axis.Y), axiscycle.cycle(i1, j1, k1, Direction.Axis.Z), axiscycle.cycle(i1, j1, k1 + 1, Direction.Axis.X), axiscycle.cycle(i1, j1, k1 + 1, Direction.Axis.Y), axiscycle.cycle(i1, j1, k1 + 1, Direction.Axis.Z));
                  }
               } else if (i != -1) {
                  pLineConsumer.consume(axiscycle.cycle(i1, j1, i, Direction.Axis.X), axiscycle.cycle(i1, j1, i, Direction.Axis.Y), axiscycle.cycle(i1, j1, i, Direction.Axis.Z), axiscycle.cycle(i1, j1, k1, Direction.Axis.X), axiscycle.cycle(i1, j1, k1, Direction.Axis.Y), axiscycle.cycle(i1, j1, k1, Direction.Axis.Z));
                  i = -1;
               }
            }
         }
      }

   }

   public void forAllBoxes(DiscreteVoxelShape.IntLineConsumer pConsumer, boolean pCombine) {
      BitSetDiscreteVoxelShape.forAllBoxes(this, pConsumer, pCombine);
   }

   public void forAllFaces(DiscreteVoxelShape.IntFaceConsumer pFaceConsumer) {
      this.forAllAxisFaces(pFaceConsumer, AxisCycle.NONE);
      this.forAllAxisFaces(pFaceConsumer, AxisCycle.FORWARD);
      this.forAllAxisFaces(pFaceConsumer, AxisCycle.BACKWARD);
   }

   private void forAllAxisFaces(DiscreteVoxelShape.IntFaceConsumer pFaceConsumer, AxisCycle pAxisRotation) {
      AxisCycle axiscycle = pAxisRotation.inverse();
      Direction.Axis direction$axis = axiscycle.cycle(Direction.Axis.Z);
      int i = this.getSize(axiscycle.cycle(Direction.Axis.X));
      int j = this.getSize(axiscycle.cycle(Direction.Axis.Y));
      int k = this.getSize(direction$axis);
      Direction direction = Direction.fromAxisAndDirection(direction$axis, Direction.AxisDirection.NEGATIVE);
      Direction direction1 = Direction.fromAxisAndDirection(direction$axis, Direction.AxisDirection.POSITIVE);

      for(int l = 0; l < i; ++l) {
         for(int i1 = 0; i1 < j; ++i1) {
            boolean flag = false;

            for(int j1 = 0; j1 <= k; ++j1) {
               boolean flag1 = j1 != k && this.isFull(axiscycle, l, i1, j1);
               if (!flag && flag1) {
                  pFaceConsumer.consume(direction, axiscycle.cycle(l, i1, j1, Direction.Axis.X), axiscycle.cycle(l, i1, j1, Direction.Axis.Y), axiscycle.cycle(l, i1, j1, Direction.Axis.Z));
               }

               if (flag && !flag1) {
                  pFaceConsumer.consume(direction1, axiscycle.cycle(l, i1, j1 - 1, Direction.Axis.X), axiscycle.cycle(l, i1, j1 - 1, Direction.Axis.Y), axiscycle.cycle(l, i1, j1 - 1, Direction.Axis.Z));
               }

               flag = flag1;
            }
         }
      }

   }

   public interface IntFaceConsumer {
      void consume(Direction pDirection, int pX, int pY, int pZ);
   }

   public interface IntLineConsumer {
      void consume(int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2);
   }
}