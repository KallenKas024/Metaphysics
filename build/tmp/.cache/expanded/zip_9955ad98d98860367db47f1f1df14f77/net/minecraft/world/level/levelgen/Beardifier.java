package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * Modifies terrain noise to be flatter near structures such as villages.
 */
public class Beardifier implements DensityFunctions.BeardifierOrMarker {
   public static final int BEARD_KERNEL_RADIUS = 12;
   private static final int BEARD_KERNEL_SIZE = 24;
   private static final float[] BEARD_KERNEL = Util.make(new float[13824], (p_158082_) -> {
      for(int i = 0; i < 24; ++i) {
         for(int j = 0; j < 24; ++j) {
            for(int k = 0; k < 24; ++k) {
               p_158082_[i * 24 * 24 + j * 24 + k] = (float)computeBeardContribution(j - 12, k - 12, i - 12);
            }
         }
      }

   });
   protected final ObjectListIterator<Beardifier.Rigid> pieceIterator;
   protected final ObjectListIterator<JigsawJunction> junctionIterator;

   public static Beardifier forStructuresInChunk(StructureManager pStructureManager, ChunkPos pChunkPos) {
      int i = pChunkPos.getMinBlockX();
      int j = pChunkPos.getMinBlockZ();
      ObjectList<Beardifier.Rigid> objectlist = new ObjectArrayList<>(10);
      ObjectList<JigsawJunction> objectlist1 = new ObjectArrayList<>(32);
      pStructureManager.startsForStructure(pChunkPos, (p_223941_) -> {
         return p_223941_.terrainAdaptation() != TerrainAdjustment.NONE;
      }).forEach((p_223936_) -> {
         TerrainAdjustment terrainadjustment = p_223936_.getStructure().terrainAdaptation();

         for(StructurePiece structurepiece : p_223936_.getPieces()) {
            if (structurepiece.isCloseToChunk(pChunkPos, 12)) {
               if (structurepiece instanceof net.minecraftforge.common.world.PieceBeardifierModifier pieceBeardifierModifier) {
                  if (pieceBeardifierModifier.getTerrainAdjustment() != TerrainAdjustment.NONE) {
                     objectlist.add(new Beardifier.Rigid(pieceBeardifierModifier.getBeardifierBox(), pieceBeardifierModifier.getTerrainAdjustment(), pieceBeardifierModifier.getGroundLevelDelta()));
                  }
               } else
               if (structurepiece instanceof PoolElementStructurePiece) {
                  PoolElementStructurePiece poolelementstructurepiece = (PoolElementStructurePiece)structurepiece;
                  StructureTemplatePool.Projection structuretemplatepool$projection = poolelementstructurepiece.getElement().getProjection();
                  if (structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID) {
                     objectlist.add(new Beardifier.Rigid(poolelementstructurepiece.getBoundingBox(), terrainadjustment, poolelementstructurepiece.getGroundLevelDelta()));
                  }

                  for(JigsawJunction jigsawjunction : poolelementstructurepiece.getJunctions()) {
                     int k = jigsawjunction.getSourceX();
                     int l = jigsawjunction.getSourceZ();
                     if (k > i - 12 && l > j - 12 && k < i + 15 + 12 && l < j + 15 + 12) {
                        objectlist1.add(jigsawjunction);
                     }
                  }
               } else {
                  objectlist.add(new Beardifier.Rigid(structurepiece.getBoundingBox(), terrainadjustment, 0));
               }
            }
         }

      });
      return new Beardifier(objectlist.iterator(), objectlist1.iterator());
   }

   @VisibleForTesting
   public Beardifier(ObjectListIterator<Beardifier.Rigid> pPieceIterator, ObjectListIterator<JigsawJunction> pJunctionIterator) {
      this.pieceIterator = pPieceIterator;
      this.junctionIterator = pJunctionIterator;
   }

   public double compute(DensityFunction.FunctionContext pContext) {
      int i = pContext.blockX();
      int j = pContext.blockY();
      int k = pContext.blockZ();

      double d0;
      double d1;
      for(d0 = 0.0D; this.pieceIterator.hasNext(); d0 += d1) {
         Beardifier.Rigid beardifier$rigid = this.pieceIterator.next();
         BoundingBox boundingbox = beardifier$rigid.box();
         int l = beardifier$rigid.groundLevelDelta();
         int i1 = Math.max(0, Math.max(boundingbox.minX() - i, i - boundingbox.maxX()));
         int j1 = Math.max(0, Math.max(boundingbox.minZ() - k, k - boundingbox.maxZ()));
         int k1 = boundingbox.minY() + l;
         int l1 = j - k1;
         int i3;
         switch (beardifier$rigid.terrainAdjustment()) {
            case NONE:
               i3 = 0;
               break;
            case BURY:
            case BEARD_THIN:
               i3 = l1;
               break;
            case BEARD_BOX:
               i3 = Math.max(0, Math.max(k1 - j, j - boundingbox.maxY()));
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         int i2 = i3;
         switch (beardifier$rigid.terrainAdjustment()) {
            case NONE:
               d1 = 0.0D;
               break;
            case BURY:
               d1 = getBuryContribution(i1, i2, j1);
               break;
            case BEARD_THIN:
            case BEARD_BOX:
               d1 = getBeardContribution(i1, i2, j1, l1) * 0.8D;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }
      }

      this.pieceIterator.back(Integer.MAX_VALUE);

      while(this.junctionIterator.hasNext()) {
         JigsawJunction jigsawjunction = this.junctionIterator.next();
         int j2 = i - jigsawjunction.getSourceX();
         int k2 = j - jigsawjunction.getSourceGroundY();
         int l2 = k - jigsawjunction.getSourceZ();
         d0 += getBeardContribution(j2, k2, l2, k2) * 0.4D;
      }

      this.junctionIterator.back(Integer.MAX_VALUE);
      return d0;
   }

   public double minValue() {
      return Double.NEGATIVE_INFINITY;
   }

   public double maxValue() {
      return Double.POSITIVE_INFINITY;
   }

   protected static double getBuryContribution(int pX, int pY, int pZ) {
      double d0 = Mth.length((double)pX, (double)pY / 2.0D, (double)pZ);
      return Mth.clampedMap(d0, 0.0D, 6.0D, 1.0D, 0.0D);
   }

   protected static double getBeardContribution(int pX, int pY, int pZ, int pHeight) {
      int i = pX + 12;
      int j = pY + 12;
      int k = pZ + 12;
      if (isInKernelRange(i) && isInKernelRange(j) && isInKernelRange(k)) {
         double d0 = (double)pHeight + 0.5D;
         double d1 = Mth.lengthSquared((double)pX, d0, (double)pZ);
         double d2 = -d0 * Mth.fastInvSqrt(d1 / 2.0D) / 2.0D;
         return d2 * (double)BEARD_KERNEL[k * 24 * 24 + i * 24 + j];
      } else {
         return 0.0D;
      }
   }

   private static boolean isInKernelRange(int pValue) {
      return pValue >= 0 && pValue < 24;
   }

   private static double computeBeardContribution(int pX, int pY, int pZ) {
      return computeBeardContribution(pX, (double)pY + 0.5D, pZ);
   }

   private static double computeBeardContribution(int pX, double pY, int pZ) {
      double d0 = Mth.lengthSquared((double)pX, pY, (double)pZ);
      return Math.pow(Math.E, -d0 / 16.0D);
   }

   @VisibleForTesting
   public static record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
   }
}
