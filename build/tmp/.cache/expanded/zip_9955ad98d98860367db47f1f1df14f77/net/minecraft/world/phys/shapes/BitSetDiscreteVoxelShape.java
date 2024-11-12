package net.minecraft.world.phys.shapes;

import java.util.BitSet;
import net.minecraft.core.Direction;

public final class BitSetDiscreteVoxelShape extends DiscreteVoxelShape {
   private final BitSet storage;
   private int xMin;
   private int yMin;
   private int zMin;
   private int xMax;
   private int yMax;
   private int zMax;

   public BitSetDiscreteVoxelShape(int pXSize, int pYSize, int pZSize) {
      super(pXSize, pYSize, pZSize);
      this.storage = new BitSet(pXSize * pYSize * pZSize);
      this.xMin = pXSize;
      this.yMin = pYSize;
      this.zMin = pZSize;
   }

   public static BitSetDiscreteVoxelShape withFilledBounds(int pX, int pY, int pZ, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax) {
      BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = new BitSetDiscreteVoxelShape(pX, pY, pZ);
      bitsetdiscretevoxelshape.xMin = pXMin;
      bitsetdiscretevoxelshape.yMin = pYMin;
      bitsetdiscretevoxelshape.zMin = pZMin;
      bitsetdiscretevoxelshape.xMax = pXMax;
      bitsetdiscretevoxelshape.yMax = pYMax;
      bitsetdiscretevoxelshape.zMax = pZMax;

      for(int i = pXMin; i < pXMax; ++i) {
         for(int j = pYMin; j < pYMax; ++j) {
            for(int k = pZMin; k < pZMax; ++k) {
               bitsetdiscretevoxelshape.fillUpdateBounds(i, j, k, false);
            }
         }
      }

      return bitsetdiscretevoxelshape;
   }

   public BitSetDiscreteVoxelShape(DiscreteVoxelShape pShape) {
      super(pShape.xSize, pShape.ySize, pShape.zSize);
      if (pShape instanceof BitSetDiscreteVoxelShape) {
         this.storage = (BitSet)((BitSetDiscreteVoxelShape)pShape).storage.clone();
      } else {
         this.storage = new BitSet(this.xSize * this.ySize * this.zSize);

         for(int i = 0; i < this.xSize; ++i) {
            for(int j = 0; j < this.ySize; ++j) {
               for(int k = 0; k < this.zSize; ++k) {
                  if (pShape.isFull(i, j, k)) {
                     this.storage.set(this.getIndex(i, j, k));
                  }
               }
            }
         }
      }

      this.xMin = pShape.firstFull(Direction.Axis.X);
      this.yMin = pShape.firstFull(Direction.Axis.Y);
      this.zMin = pShape.firstFull(Direction.Axis.Z);
      this.xMax = pShape.lastFull(Direction.Axis.X);
      this.yMax = pShape.lastFull(Direction.Axis.Y);
      this.zMax = pShape.lastFull(Direction.Axis.Z);
   }

   protected int getIndex(int pX, int pY, int pZ) {
      return (pX * this.ySize + pY) * this.zSize + pZ;
   }

   public boolean isFull(int pX, int pY, int pZ) {
      return this.storage.get(this.getIndex(pX, pY, pZ));
   }

   private void fillUpdateBounds(int pX, int pY, int pZ, boolean pUpdate) {
      this.storage.set(this.getIndex(pX, pY, pZ));
      if (pUpdate) {
         this.xMin = Math.min(this.xMin, pX);
         this.yMin = Math.min(this.yMin, pY);
         this.zMin = Math.min(this.zMin, pZ);
         this.xMax = Math.max(this.xMax, pX + 1);
         this.yMax = Math.max(this.yMax, pY + 1);
         this.zMax = Math.max(this.zMax, pZ + 1);
      }

   }

   public void fill(int pX, int pY, int pZ) {
      this.fillUpdateBounds(pX, pY, pZ, true);
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public int firstFull(Direction.Axis pAxis) {
      return pAxis.choose(this.xMin, this.yMin, this.zMin);
   }

   public int lastFull(Direction.Axis pAxis) {
      return pAxis.choose(this.xMax, this.yMax, this.zMax);
   }

   static BitSetDiscreteVoxelShape join(DiscreteVoxelShape pMainShape, DiscreteVoxelShape pSecondaryShape, IndexMerger pMergerX, IndexMerger pMergerY, IndexMerger pMergerZ, BooleanOp pOperator) {
      BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = new BitSetDiscreteVoxelShape(pMergerX.size() - 1, pMergerY.size() - 1, pMergerZ.size() - 1);
      int[] aint = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
      pMergerX.forMergedIndexes((p_82670_, p_82671_, p_82672_) -> {
         boolean[] aboolean = new boolean[]{false};
         pMergerY.forMergedIndexes((p_165978_, p_165979_, p_165980_) -> {
            boolean[] aboolean1 = new boolean[]{false};
            pMergerZ.forMergedIndexes((p_165960_, p_165961_, p_165962_) -> {
               if (pOperator.apply(pMainShape.isFullWide(p_82670_, p_165978_, p_165960_), pSecondaryShape.isFullWide(p_82671_, p_165979_, p_165961_))) {
                  bitsetdiscretevoxelshape.storage.set(bitsetdiscretevoxelshape.getIndex(p_82672_, p_165980_, p_165962_));
                  aint[2] = Math.min(aint[2], p_165962_);
                  aint[5] = Math.max(aint[5], p_165962_);
                  aboolean1[0] = true;
               }

               return true;
            });
            if (aboolean1[0]) {
               aint[1] = Math.min(aint[1], p_165980_);
               aint[4] = Math.max(aint[4], p_165980_);
               aboolean[0] = true;
            }

            return true;
         });
         if (aboolean[0]) {
            aint[0] = Math.min(aint[0], p_82672_);
            aint[3] = Math.max(aint[3], p_82672_);
         }

         return true;
      });
      bitsetdiscretevoxelshape.xMin = aint[0];
      bitsetdiscretevoxelshape.yMin = aint[1];
      bitsetdiscretevoxelshape.zMin = aint[2];
      bitsetdiscretevoxelshape.xMax = aint[3] + 1;
      bitsetdiscretevoxelshape.yMax = aint[4] + 1;
      bitsetdiscretevoxelshape.zMax = aint[5] + 1;
      return bitsetdiscretevoxelshape;
   }

   protected static void forAllBoxes(DiscreteVoxelShape pShape, DiscreteVoxelShape.IntLineConsumer pConsumer, boolean pCombine) {
      BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = new BitSetDiscreteVoxelShape(pShape);

      for(int i = 0; i < bitsetdiscretevoxelshape.ySize; ++i) {
         for(int j = 0; j < bitsetdiscretevoxelshape.xSize; ++j) {
            int k = -1;

            for(int l = 0; l <= bitsetdiscretevoxelshape.zSize; ++l) {
               if (bitsetdiscretevoxelshape.isFullWide(j, i, l)) {
                  if (pCombine) {
                     if (k == -1) {
                        k = l;
                     }
                  } else {
                     pConsumer.consume(j, i, l, j + 1, i + 1, l + 1);
                  }
               } else if (k != -1) {
                  int i1 = j;
                  int j1 = i;
                  bitsetdiscretevoxelshape.clearZStrip(k, l, j, i);

                  while(bitsetdiscretevoxelshape.isZStripFull(k, l, i1 + 1, i)) {
                     bitsetdiscretevoxelshape.clearZStrip(k, l, i1 + 1, i);
                     ++i1;
                  }

                  while(bitsetdiscretevoxelshape.isXZRectangleFull(j, i1 + 1, k, l, j1 + 1)) {
                     for(int k1 = j; k1 <= i1; ++k1) {
                        bitsetdiscretevoxelshape.clearZStrip(k, l, k1, j1 + 1);
                     }

                     ++j1;
                  }

                  pConsumer.consume(j, i, k, i1 + 1, j1 + 1, l);
                  k = -1;
               }
            }
         }
      }

   }

   private boolean isZStripFull(int pZMin, int pZMax, int pX, int pY) {
      if (pX < this.xSize && pY < this.ySize) {
         return this.storage.nextClearBit(this.getIndex(pX, pY, pZMin)) >= this.getIndex(pX, pY, pZMax);
      } else {
         return false;
      }
   }

   private boolean isXZRectangleFull(int pXMin, int pXMax, int pZMin, int pZMax, int pY) {
      for(int i = pXMin; i < pXMax; ++i) {
         if (!this.isZStripFull(pZMin, pZMax, i, pY)) {
            return false;
         }
      }

      return true;
   }

   private void clearZStrip(int pZMin, int pZMax, int pX, int pY) {
      this.storage.clear(this.getIndex(pX, pY, pZMin), this.getIndex(pX, pY, pZMax));
   }
}