package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisGraph {
   private static final int SIZE_IN_BITS = 4;
   private static final int LEN = 16;
   private static final int MASK = 15;
   private static final int SIZE = 4096;
   private static final int X_SHIFT = 0;
   private static final int Z_SHIFT = 4;
   private static final int Y_SHIFT = 8;
   private static final int DX = (int)Math.pow(16.0D, 0.0D);
   private static final int DZ = (int)Math.pow(16.0D, 1.0D);
   private static final int DY = (int)Math.pow(16.0D, 2.0D);
   private static final int INVALID_INDEX = -1;
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BitSet bitSet = new BitSet(4096);
   private static final int[] INDEX_OF_EDGES = Util.make(new int[1352], (p_112974_) -> {
      int i = 0;
      int j = 15;
      int k = 0;

      for(int l = 0; l < 16; ++l) {
         for(int i1 = 0; i1 < 16; ++i1) {
            for(int j1 = 0; j1 < 16; ++j1) {
               if (l == 0 || l == 15 || i1 == 0 || i1 == 15 || j1 == 0 || j1 == 15) {
                  p_112974_[k++] = getIndex(l, i1, j1);
               }
            }
         }
      }

   });
   private int empty = 4096;

   public void setOpaque(BlockPos pPos) {
      this.bitSet.set(getIndex(pPos), true);
      --this.empty;
   }

   private static int getIndex(BlockPos pPos) {
      return getIndex(pPos.getX() & 15, pPos.getY() & 15, pPos.getZ() & 15);
   }

   private static int getIndex(int pX, int pY, int pZ) {
      return pX << 0 | pY << 8 | pZ << 4;
   }

   public VisibilitySet resolve() {
      VisibilitySet visibilityset = new VisibilitySet();
      if (4096 - this.empty < 256) {
         visibilityset.setAll(true);
      } else if (this.empty == 0) {
         visibilityset.setAll(false);
      } else {
         for(int i : INDEX_OF_EDGES) {
            if (!this.bitSet.get(i)) {
               visibilityset.add(this.floodFill(i));
            }
         }
      }

      return visibilityset;
   }

   private Set<Direction> floodFill(int pIndex) {
      Set<Direction> set = EnumSet.noneOf(Direction.class);
      IntPriorityQueue intpriorityqueue = new IntArrayFIFOQueue();
      intpriorityqueue.enqueue(pIndex);
      this.bitSet.set(pIndex, true);

      while(!intpriorityqueue.isEmpty()) {
         int i = intpriorityqueue.dequeueInt();
         this.addEdges(i, set);

         for(Direction direction : DIRECTIONS) {
            int j = this.getNeighborIndexAtFace(i, direction);
            if (j >= 0 && !this.bitSet.get(j)) {
               this.bitSet.set(j, true);
               intpriorityqueue.enqueue(j);
            }
         }
      }

      return set;
   }

   private void addEdges(int pIndex, Set<Direction> pFaces) {
      int i = pIndex >> 0 & 15;
      if (i == 0) {
         pFaces.add(Direction.WEST);
      } else if (i == 15) {
         pFaces.add(Direction.EAST);
      }

      int j = pIndex >> 8 & 15;
      if (j == 0) {
         pFaces.add(Direction.DOWN);
      } else if (j == 15) {
         pFaces.add(Direction.UP);
      }

      int k = pIndex >> 4 & 15;
      if (k == 0) {
         pFaces.add(Direction.NORTH);
      } else if (k == 15) {
         pFaces.add(Direction.SOUTH);
      }

   }

   private int getNeighborIndexAtFace(int pIndex, Direction pFace) {
      switch (pFace) {
         case DOWN:
            if ((pIndex >> 8 & 15) == 0) {
               return -1;
            }

            return pIndex - DY;
         case UP:
            if ((pIndex >> 8 & 15) == 15) {
               return -1;
            }

            return pIndex + DY;
         case NORTH:
            if ((pIndex >> 4 & 15) == 0) {
               return -1;
            }

            return pIndex - DZ;
         case SOUTH:
            if ((pIndex >> 4 & 15) == 15) {
               return -1;
            }

            return pIndex + DZ;
         case WEST:
            if ((pIndex >> 0 & 15) == 0) {
               return -1;
            }

            return pIndex - DX;
         case EAST:
            if ((pIndex >> 0 & 15) == 15) {
               return -1;
            }

            return pIndex + DX;
         default:
            return -1;
      }
   }
}