package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.slf4j.Logger;

/**
 * A simple three-dimensional mutable integer bounding box.
 * Note that this box is both mutable, and has an implementation of {@code hashCode()} and {@code equals()}.
 * This can be used as {@code HashMap} keys for example, if the user can ensure the instances themselves are not
 * modified.
 */
public class BoundingBox {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM.comapFlatMap((p_162383_) -> {
      return Util.fixedSize(p_162383_, 6).map((p_162385_) -> {
         return new BoundingBox(p_162385_[0], p_162385_[1], p_162385_[2], p_162385_[3], p_162385_[4], p_162385_[5]);
      });
   }, (p_162391_) -> {
      return IntStream.of(p_162391_.minX, p_162391_.minY, p_162391_.minZ, p_162391_.maxX, p_162391_.maxY, p_162391_.maxZ);
   }).stable();
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;

   public BoundingBox(BlockPos pPos) {
      this(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX(), pPos.getY(), pPos.getZ());
   }

   public BoundingBox(int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      this.minX = pMinX;
      this.minY = pMinY;
      this.minZ = pMinZ;
      this.maxX = pMaxX;
      this.maxY = pMaxY;
      this.maxZ = pMaxZ;
      if (pMaxX < pMinX || pMaxY < pMinY || pMaxZ < pMinZ) {
         String s = "Invalid bounding box data, inverted bounds for: " + this;
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw new IllegalStateException(s);
         }

         LOGGER.error(s);
         this.minX = Math.min(pMinX, pMaxX);
         this.minY = Math.min(pMinY, pMaxY);
         this.minZ = Math.min(pMinZ, pMaxZ);
         this.maxX = Math.max(pMinX, pMaxX);
         this.maxY = Math.max(pMinY, pMaxY);
         this.maxZ = Math.max(pMinZ, pMaxZ);
      }

   }

   public static BoundingBox fromCorners(Vec3i pFirst, Vec3i pSecond) {
      return new BoundingBox(Math.min(pFirst.getX(), pSecond.getX()), Math.min(pFirst.getY(), pSecond.getY()), Math.min(pFirst.getZ(), pSecond.getZ()), Math.max(pFirst.getX(), pSecond.getX()), Math.max(pFirst.getY(), pSecond.getY()), Math.max(pFirst.getZ(), pSecond.getZ()));
   }

   public static BoundingBox infinite() {
      return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   /**
    * Create a bounding box with the specified dimensions and rotate it. Used to project a possible new component
    * Bounding Box - to check if it would cut anything already spawned.
    */
   public static BoundingBox orientBox(int pStructureMinX, int pStructureMinY, int pStructureMinZ, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax, Direction pFacing) {
      switch (pFacing) {
         case SOUTH:
         default:
            return new BoundingBox(pStructureMinX + pXMin, pStructureMinY + pYMin, pStructureMinZ + pZMin, pStructureMinX + pXMax - 1 + pXMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pZMax - 1 + pZMin);
         case NORTH:
            return new BoundingBox(pStructureMinX + pXMin, pStructureMinY + pYMin, pStructureMinZ - pZMax + 1 + pZMin, pStructureMinX + pXMax - 1 + pXMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pZMin);
         case WEST:
            return new BoundingBox(pStructureMinX - pZMax + 1 + pZMin, pStructureMinY + pYMin, pStructureMinZ + pXMin, pStructureMinX + pZMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pXMax - 1 + pXMin);
         case EAST:
            return new BoundingBox(pStructureMinX + pZMin, pStructureMinY + pYMin, pStructureMinZ + pXMin, pStructureMinX + pZMax - 1 + pZMin, pStructureMinY + pYMax - 1 + pYMin, pStructureMinZ + pXMax - 1 + pXMin);
      }
   }

   /**
    * @return {@code true} if {@code box} intersects this box.
    */
   public boolean intersects(BoundingBox pBox) {
      return this.maxX >= pBox.minX && this.minX <= pBox.maxX && this.maxZ >= pBox.minZ && this.minZ <= pBox.maxZ && this.maxY >= pBox.minY && this.minY <= pBox.maxY;
   }

   /**
    * @return {@code true} if this bounding box intersects the horizontal x/z region described by the min and max
    * parameters.
    */
   public boolean intersects(int pMinX, int pMinZ, int pMaxX, int pMaxZ) {
      return this.maxX >= pMinX && this.minX <= pMaxX && this.maxZ >= pMinZ && this.minZ <= pMaxZ;
   }

   public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> pPositions) {
      Iterator<BlockPos> iterator = pPositions.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BoundingBox boundingbox = new BoundingBox(iterator.next());
         iterator.forEachRemaining(boundingbox::encapsulate);
         return Optional.of(boundingbox);
      }
   }

   public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> pBoxes) {
      Iterator<BoundingBox> iterator = pBoxes.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BoundingBox boundingbox = iterator.next();
         BoundingBox boundingbox1 = new BoundingBox(boundingbox.minX, boundingbox.minY, boundingbox.minZ, boundingbox.maxX, boundingbox.maxY, boundingbox.maxZ);
         iterator.forEachRemaining(boundingbox1::encapsulate);
         return Optional.of(boundingbox1);
      }
   }

   /** @deprecated */
   /**
    * Expands this box to be at least large enough to contain {@code box}.
    */
   @Deprecated
   public BoundingBox encapsulate(BoundingBox p_162387_) {
      this.minX = Math.min(this.minX, p_162387_.minX);
      this.minY = Math.min(this.minY, p_162387_.minY);
      this.minZ = Math.min(this.minZ, p_162387_.minZ);
      this.maxX = Math.max(this.maxX, p_162387_.maxX);
      this.maxY = Math.max(this.maxY, p_162387_.maxY);
      this.maxZ = Math.max(this.maxZ, p_162387_.maxZ);
      return this;
   }

   /** @deprecated */
   /**
    * Expands this box to be at least large enough to contain {@code pos}.
    */
   @Deprecated
   public BoundingBox encapsulate(BlockPos p_162372_) {
      this.minX = Math.min(this.minX, p_162372_.getX());
      this.minY = Math.min(this.minY, p_162372_.getY());
      this.minZ = Math.min(this.minZ, p_162372_.getZ());
      this.maxX = Math.max(this.maxX, p_162372_.getX());
      this.maxY = Math.max(this.maxY, p_162372_.getY());
      this.maxZ = Math.max(this.maxZ, p_162372_.getZ());
      return this;
   }

   /** @deprecated */
   /**
    * Translates this box by the given coordinates, modifying the current box.
    */
   @Deprecated
   public BoundingBox move(int pX, int pY, int pZ) {
      this.minX += pX;
      this.minY += pY;
      this.minZ += pZ;
      this.maxX += pX;
      this.maxY += pY;
      this.maxZ += pZ;
      return this;
   }

   /** @deprecated */
   /**
    * Translates this box by the given vector, modifying the current box.
    */
   @Deprecated
   public BoundingBox move(Vec3i pVector) {
      return this.move(pVector.getX(), pVector.getY(), pVector.getZ());
   }

   /**
    * @return A new bounding box equal to this box, translated by the given coordinates.
    */
   public BoundingBox moved(int pX, int pY, int pZ) {
      return new BoundingBox(this.minX + pX, this.minY + pY, this.minZ + pZ, this.maxX + pX, this.maxY + pY, this.maxZ + pZ);
   }

   /**
    * Expands this box by a fixed {@code value} in all directions.
    */
   public BoundingBox inflatedBy(int pValue) {
      return new BoundingBox(this.minX() - pValue, this.minY() - pValue, this.minZ() - pValue, this.maxX() + pValue, this.maxY() + pValue, this.maxZ() + pValue);
   }

   /**
    * @return {@code true} if the bounding box contains the {@code vector}.
    */
   public boolean isInside(Vec3i pVector) {
      return this.isInside(pVector.getX(), pVector.getY(), pVector.getZ());
   }

   public boolean isInside(int pX, int pY, int pZ) {
      return pX >= this.minX && pX <= this.maxX && pZ >= this.minZ && pZ <= this.maxZ && pY >= this.minY && pY <= this.maxY;
   }

   /**
    * Returns a vector describing the dimensions of this bounding box.
    * Note that unlike {@code getXSpan()}, {@code getYSpan()}, and {@code getZSpan()}, the length is interpreted here as
    * the difference in coordinates. So a box over a 1x1x1 area, which still contains a single point, will report length
    * zero.
    */
   public Vec3i getLength() {
      return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
   }

   /**
    * @return The length of this bounding box along the x-axis.
    */
   public int getXSpan() {
      return this.maxX - this.minX + 1;
   }

   /**
    * @return The length of this bounding box along the y-axis.
    */
   public int getYSpan() {
      return this.maxY - this.minY + 1;
   }

   /**
    * @return The length of this bounding box along the z-axis.
    */
   public int getZSpan() {
      return this.maxZ - this.minZ + 1;
   }

   /**
    * @return The center of this bounding box. Note in even-sized dimensions the center position will be offset in the
    * positive direction.
    */
   public BlockPos getCenter() {
      return new BlockPos(this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2);
   }

   public void forAllCorners(Consumer<BlockPos> pPos) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      pPos.accept(blockpos$mutableblockpos.set(this.maxX, this.maxY, this.maxZ));
      pPos.accept(blockpos$mutableblockpos.set(this.minX, this.maxY, this.maxZ));
      pPos.accept(blockpos$mutableblockpos.set(this.maxX, this.minY, this.maxZ));
      pPos.accept(blockpos$mutableblockpos.set(this.minX, this.minY, this.maxZ));
      pPos.accept(blockpos$mutableblockpos.set(this.maxX, this.maxY, this.minZ));
      pPos.accept(blockpos$mutableblockpos.set(this.minX, this.maxY, this.minZ));
      pPos.accept(blockpos$mutableblockpos.set(this.maxX, this.minY, this.minZ));
      pPos.accept(blockpos$mutableblockpos.set(this.minX, this.minY, this.minZ));
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("minX", this.minX).add("minY", this.minY).add("minZ", this.minZ).add("maxX", this.maxX).add("maxY", this.maxY).add("maxZ", this.maxZ).toString();
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof BoundingBox)) {
         return false;
      } else {
         BoundingBox boundingbox = (BoundingBox)pOther;
         return this.minX == boundingbox.minX && this.minY == boundingbox.minY && this.minZ == boundingbox.minZ && this.maxX == boundingbox.maxX && this.maxY == boundingbox.maxY && this.maxZ == boundingbox.maxZ;
      }
   }

   public int hashCode() {
      return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public int minX() {
      return this.minX;
   }

   public int minY() {
      return this.minY;
   }

   public int minZ() {
      return this.minZ;
   }

   public int maxX() {
      return this.maxX;
   }

   public int maxY() {
      return this.maxY;
   }

   public int maxZ() {
      return this.maxZ;
   }
}