package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;

public class StructurePiecesBuilder implements StructurePieceAccessor {
   private final List<StructurePiece> pieces = Lists.newArrayList();

   public void addPiece(StructurePiece pPiece) {
      this.pieces.add(pPiece);
   }

   @Nullable
   public StructurePiece findCollisionPiece(BoundingBox pBox) {
      return StructurePiece.findCollisionPiece(this.pieces, pBox);
   }

   /** @deprecated */
   @Deprecated
   public void offsetPiecesVertically(int pOffset) {
      for(StructurePiece structurepiece : this.pieces) {
         structurepiece.move(0, pOffset, 0);
      }

   }

   /** @deprecated */
   @Deprecated
   public int moveBelowSeaLevel(int pSeaLevel, int pMinY, RandomSource pRandom, int p_226969_) {
      int i = pSeaLevel - p_226969_;
      BoundingBox boundingbox = this.getBoundingBox();
      int j = boundingbox.getYSpan() + pMinY + 1;
      if (j < i) {
         j += pRandom.nextInt(i - j);
      }

      int k = j - boundingbox.maxY();
      this.offsetPiecesVertically(k);
      return k;
   }

   /** @deprecated */
   public void moveInsideHeights(RandomSource pRandom, int p_226972_, int p_226973_) {
      BoundingBox boundingbox = this.getBoundingBox();
      int i = p_226973_ - p_226972_ + 1 - boundingbox.getYSpan();
      int j;
      if (i > 1) {
         j = p_226972_ + pRandom.nextInt(i);
      } else {
         j = p_226972_;
      }

      int k = j - boundingbox.minY();
      this.offsetPiecesVertically(k);
   }

   public PiecesContainer build() {
      return new PiecesContainer(this.pieces);
   }

   public void clear() {
      this.pieces.clear();
   }

   public boolean isEmpty() {
      return this.pieces.isEmpty();
   }

   public BoundingBox getBoundingBox() {
      return StructurePiece.createBoundingBox(this.pieces.stream());
   }
}