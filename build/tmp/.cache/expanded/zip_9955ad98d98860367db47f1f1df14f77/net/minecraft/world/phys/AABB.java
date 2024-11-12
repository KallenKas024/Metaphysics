package net.minecraft.world.phys;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AABB {
   private static final double EPSILON = 1.0E-7D;
   public final double minX;
   public final double minY;
   public final double minZ;
   public final double maxX;
   public final double maxY;
   public final double maxZ;

   public AABB(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      this.minX = Math.min(pX1, pX2);
      this.minY = Math.min(pY1, pY2);
      this.minZ = Math.min(pZ1, pZ2);
      this.maxX = Math.max(pX1, pX2);
      this.maxY = Math.max(pY1, pY2);
      this.maxZ = Math.max(pZ1, pZ2);
   }

   public AABB(BlockPos pPos) {
      this((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), (double)(pPos.getX() + 1), (double)(pPos.getY() + 1), (double)(pPos.getZ() + 1));
   }

   public AABB(BlockPos pStart, BlockPos pEnd) {
      this((double)pStart.getX(), (double)pStart.getY(), (double)pStart.getZ(), (double)pEnd.getX(), (double)pEnd.getY(), (double)pEnd.getZ());
   }

   public AABB(Vec3 pStart, Vec3 pEnd) {
      this(pStart.x, pStart.y, pStart.z, pEnd.x, pEnd.y, pEnd.z);
   }

   public static AABB of(BoundingBox pMutableBox) {
      return new AABB((double)pMutableBox.minX(), (double)pMutableBox.minY(), (double)pMutableBox.minZ(), (double)(pMutableBox.maxX() + 1), (double)(pMutableBox.maxY() + 1), (double)(pMutableBox.maxZ() + 1));
   }

   public static AABB unitCubeFromLowerCorner(Vec3 pVector) {
      return new AABB(pVector.x, pVector.y, pVector.z, pVector.x + 1.0D, pVector.y + 1.0D, pVector.z + 1.0D);
   }

   public AABB setMinX(double pMinX) {
      return new AABB(pMinX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public AABB setMinY(double pMinY) {
      return new AABB(this.minX, pMinY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public AABB setMinZ(double pMinZ) {
      return new AABB(this.minX, this.minY, pMinZ, this.maxX, this.maxY, this.maxZ);
   }

   public AABB setMaxX(double pMaxX) {
      return new AABB(this.minX, this.minY, this.minZ, pMaxX, this.maxY, this.maxZ);
   }

   public AABB setMaxY(double pMaxY) {
      return new AABB(this.minX, this.minY, this.minZ, this.maxX, pMaxY, this.maxZ);
   }

   public AABB setMaxZ(double pMaxZ) {
      return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, pMaxZ);
   }

   public double min(Direction.Axis pAxis) {
      return pAxis.choose(this.minX, this.minY, this.minZ);
   }

   public double max(Direction.Axis pAxis) {
      return pAxis.choose(this.maxX, this.maxY, this.maxZ);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof AABB)) {
         return false;
      } else {
         AABB aabb = (AABB)pOther;
         if (Double.compare(aabb.minX, this.minX) != 0) {
            return false;
         } else if (Double.compare(aabb.minY, this.minY) != 0) {
            return false;
         } else if (Double.compare(aabb.minZ, this.minZ) != 0) {
            return false;
         } else if (Double.compare(aabb.maxX, this.maxX) != 0) {
            return false;
         } else if (Double.compare(aabb.maxY, this.maxY) != 0) {
            return false;
         } else {
            return Double.compare(aabb.maxZ, this.maxZ) == 0;
         }
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.minX);
      int j = (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.minY);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.minZ);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxX);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxY);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxZ);
      return 31 * j + (int)(i ^ i >>> 32);
   }

   /**
    * Creates a new {@link AxisAlignedBB} that has been contracted by the given amount, with positive changes decreasing
    * max values and negative changes increasing min values.
    * <br/>
    * If the amount to contract by is larger than the length of a side, then the side will wrap (still creating a valid
    * AABB - see last sample).
    * 
    * <h3>Samples:</h3>
    * <table>
    * <tr><th>Input</th><th>Result</th></tr>
    * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 4, 4, 4).contract(2, 2, 2)</code></pre></td><td><pre><samp>box[0.0,
    * 0.0, 0.0 -> 2.0, 2.0, 2.0]</samp></pre></td></tr>
    * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 4, 4, 4).contract(-2, -2, -
    * 2)</code></pre></td><td><pre><samp>box[2.0, 2.0, 2.0 -> 4.0, 4.0, 4.0]</samp></pre></td></tr>
    * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).contract(0, 1, -1)</code></pre></td><td><pre><samp>box[5.0,
    * 5.0, 6.0 -> 7.0, 6.0, 7.0]</samp></pre></td></tr>
    * <tr><td><pre><code>new AxisAlignedBB(-2, -2, -2, 2, 2, 2).contract(4, -4, 0)</code></pre></td><td><pre><samp>box[-
    * 8.0, 2.0, -2.0 -> -2.0, 8.0, 2.0]</samp></pre></td></tr>
    * </table>
    * 
    * <h3>See Also:</h3>
    * <ul>
    * <li>{@link #expand(double, double, double)} - like this, except for expanding.</li>
    * <li>{@link #grow(double, double, double)} and {@link #grow(double)} - expands in all directions.</li>
    * <li>{@link #shrink(double)} - contracts in all directions (like {@link #grow(double)})</li>
    * </ul>
    * 
    * @return A new modified bounding box.
    */
   public AABB contract(double pX, double pY, double pZ) {
      double d0 = this.minX;
      double d1 = this.minY;
      double d2 = this.minZ;
      double d3 = this.maxX;
      double d4 = this.maxY;
      double d5 = this.maxZ;
      if (pX < 0.0D) {
         d0 -= pX;
      } else if (pX > 0.0D) {
         d3 -= pX;
      }

      if (pY < 0.0D) {
         d1 -= pY;
      } else if (pY > 0.0D) {
         d4 -= pY;
      }

      if (pZ < 0.0D) {
         d2 -= pZ;
      } else if (pZ > 0.0D) {
         d5 -= pZ;
      }

      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   public AABB expandTowards(Vec3 pVector) {
      return this.expandTowards(pVector.x, pVector.y, pVector.z);
   }

   /**
    * Creates a new {@link AxisAlignedBB} that has been expanded by the given amount, with positive changes increasing
    * max values and negative changes decreasing min values.
    * 
    * <h3>Samples:</h3>
    * <table>
    * <tr><th>Input</th><th>Result</th></tr>
    * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).expand(2, 2, 2)</code></pre></td><td><pre><samp>box[0, 0, 0
    * -> 3, 3, 3]</samp></pre></td><td>
    * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).expand(-2, -2, -2)</code></pre></td><td><pre><samp>box[-2,
    * -2, -2 -> 1, 1, 1]</samp></pre></td><td>
    * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).expand(0, 1, -1)</code></pre></td><td><pre><samp>box[5, 5,
    * 4, 7, 8, 7]</samp></pre></td><td>
    * </table>
    * 
    * <h3>See Also:</h3>
    * <ul>
    * <li>{@link #contract(double, double, double)} - like this, except for shrinking.</li>
    * <li>{@link #grow(double, double, double)} and {@link #grow(double)} - expands in all directions.</li>
    * <li>{@link #shrink(double)} - contracts in all directions (like {@link #grow(double)})</li>
    * </ul>
    * 
    * @return A modified bounding box that will always be equal or greater in volume to this bounding box.
    */
   public AABB expandTowards(double pX, double pY, double pZ) {
      double d0 = this.minX;
      double d1 = this.minY;
      double d2 = this.minZ;
      double d3 = this.maxX;
      double d4 = this.maxY;
      double d5 = this.maxZ;
      if (pX < 0.0D) {
         d0 += pX;
      } else if (pX > 0.0D) {
         d3 += pX;
      }

      if (pY < 0.0D) {
         d1 += pY;
      } else if (pY > 0.0D) {
         d4 += pY;
      }

      if (pZ < 0.0D) {
         d2 += pZ;
      } else if (pZ > 0.0D) {
         d5 += pZ;
      }

      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   /**
    * Creates a new {@link AxisAlignedBB} that has been contracted by the given amount in both directions. Negative
    * values will shrink the AABB instead of expanding it.
    * <br/>
    * Side lengths will be increased by 2 times the value of the parameters, since both min and max are changed.
    * <br/>
    * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
    * creating a valid AABB - see last ample).
    * 
    * <h3>Samples:</h3>
    * <table>
    * <tr><th>Input</th><th>Result</th></tr>
    * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(2, 2, 2)</code></pre></td><td><pre><samp>box[-2.0, -
    * 2.0, -2.0 -> 3.0, 3.0, 3.0]</samp></pre></td></tr>
    * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 6, 6, 6).grow(-2, -2, -2)</code></pre></td><td><pre><samp>box[2.0,
    * 2.0, 2.0 -> 4.0, 4.0, 4.0]</samp></pre></td></tr>
    * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).grow(0, 1, -1)</code></pre></td><td><pre><samp>box[5.0,
    * 4.0, 6.0 -> 7.0, 8.0, 6.0]</samp></pre></td></tr>
    * <tr><td><pre><code>new AxisAlignedBB(1, 1, 1, 3, 3, 3).grow(-4, -2, -3)</code></pre></td><td><pre><samp>box[-1.0,
    * 1.0, 0.0 -> 5.0, 3.0, 4.0]</samp></pre></td></tr>
    * </table>
    * 
    * <h3>See Also:</h3>
    * <ul>
    * <li>{@link #expand(double, double, double)} - expands in only one direction.</li>
    * <li>{@link #contract(double, double, double)} - contracts in only one direction.</li>
    * <lu>{@link #grow(double)} - version of this that expands in all directions from one parameter.</li>
    * <li>{@link #shrink(double)} - contracts in all directions</li>
    * </ul>
    * 
    * @return A modified bounding box.
    */
   public AABB inflate(double pX, double pY, double pZ) {
      double d0 = this.minX - pX;
      double d1 = this.minY - pY;
      double d2 = this.minZ - pZ;
      double d3 = this.maxX + pX;
      double d4 = this.maxY + pY;
      double d5 = this.maxZ + pZ;
      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   /**
    * Creates a new {@link AxisAlignedBB} that is expanded by the given value in all directions. Equivalent to {@link
    * #grow(double, double, double)} with the given value for all 3 params. Negative values will shrink the AABB.
    * <br/>
    * Side lengths will be increased by 2 times the value of the parameter, since both min and max are changed.
    * <br/>
    * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
    * creating a valid AABB - see samples on {@link #grow(double, double, double)}).
    * 
    * @return A modified AABB.
    */
   public AABB inflate(double pValue) {
      return this.inflate(pValue, pValue, pValue);
   }

   public AABB intersect(AABB pOther) {
      double d0 = Math.max(this.minX, pOther.minX);
      double d1 = Math.max(this.minY, pOther.minY);
      double d2 = Math.max(this.minZ, pOther.minZ);
      double d3 = Math.min(this.maxX, pOther.maxX);
      double d4 = Math.min(this.maxY, pOther.maxY);
      double d5 = Math.min(this.maxZ, pOther.maxZ);
      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   public AABB minmax(AABB pOther) {
      double d0 = Math.min(this.minX, pOther.minX);
      double d1 = Math.min(this.minY, pOther.minY);
      double d2 = Math.min(this.minZ, pOther.minZ);
      double d3 = Math.max(this.maxX, pOther.maxX);
      double d4 = Math.max(this.maxY, pOther.maxY);
      double d5 = Math.max(this.maxZ, pOther.maxZ);
      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   /**
    * Offsets the current bounding box by the specified amount.
    */
   public AABB move(double pX, double pY, double pZ) {
      return new AABB(this.minX + pX, this.minY + pY, this.minZ + pZ, this.maxX + pX, this.maxY + pY, this.maxZ + pZ);
   }

   public AABB move(BlockPos pPos) {
      return new AABB(this.minX + (double)pPos.getX(), this.minY + (double)pPos.getY(), this.minZ + (double)pPos.getZ(), this.maxX + (double)pPos.getX(), this.maxY + (double)pPos.getY(), this.maxZ + (double)pPos.getZ());
   }

   public AABB move(Vec3 pVec) {
      return this.move(pVec.x, pVec.y, pVec.z);
   }

   /**
    * Checks if the bounding box intersects with another.
    */
   public boolean intersects(AABB pOther) {
      return this.intersects(pOther.minX, pOther.minY, pOther.minZ, pOther.maxX, pOther.maxY, pOther.maxZ);
   }

   public boolean intersects(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      return this.minX < pX2 && this.maxX > pX1 && this.minY < pY2 && this.maxY > pY1 && this.minZ < pZ2 && this.maxZ > pZ1;
   }

   public boolean intersects(Vec3 pMin, Vec3 pMax) {
      return this.intersects(Math.min(pMin.x, pMax.x), Math.min(pMin.y, pMax.y), Math.min(pMin.z, pMax.z), Math.max(pMin.x, pMax.x), Math.max(pMin.y, pMax.y), Math.max(pMin.z, pMax.z));
   }

   /**
    * Returns if the supplied Vec3D is completely inside the bounding box
    */
   public boolean contains(Vec3 pVec) {
      return this.contains(pVec.x, pVec.y, pVec.z);
   }

   public boolean contains(double pX, double pY, double pZ) {
      return pX >= this.minX && pX < this.maxX && pY >= this.minY && pY < this.maxY && pZ >= this.minZ && pZ < this.maxZ;
   }

   /**
    * Returns the average length of the edges of the bounding box.
    */
   public double getSize() {
      double d0 = this.getXsize();
      double d1 = this.getYsize();
      double d2 = this.getZsize();
      return (d0 + d1 + d2) / 3.0D;
   }

   public double getXsize() {
      return this.maxX - this.minX;
   }

   public double getYsize() {
      return this.maxY - this.minY;
   }

   public double getZsize() {
      return this.maxZ - this.minZ;
   }

   public AABB deflate(double pX, double pY, double pZ) {
      return this.inflate(-pX, -pY, -pZ);
   }

   /**
    * Creates a new {@link AxisAlignedBB} that is expanded by the given value in all directions. Equivalent to {@link
    * #grow(double)} with value set to the negative of the value provided here. Passing a negative value to this method
    * values will grow the AABB.
    * <br/>
    * Side lengths will be decreased by 2 times the value of the parameter, since both min and max are changed.
    * <br/>
    * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
    * creating a valid AABB - see samples on {@link #grow(double, double, double)}).
    * 
    * @return A modified AABB.
    */
   public AABB deflate(double pValue) {
      return this.inflate(-pValue);
   }

   public Optional<Vec3> clip(Vec3 pFrom, Vec3 pTo) {
      double[] adouble = new double[]{1.0D};
      double d0 = pTo.x - pFrom.x;
      double d1 = pTo.y - pFrom.y;
      double d2 = pTo.z - pFrom.z;
      Direction direction = getDirection(this, pFrom, adouble, (Direction)null, d0, d1, d2);
      if (direction == null) {
         return Optional.empty();
      } else {
         double d3 = adouble[0];
         return Optional.of(pFrom.add(d3 * d0, d3 * d1, d3 * d2));
      }
   }

   @Nullable
   public static BlockHitResult clip(Iterable<AABB> pBoxes, Vec3 pStart, Vec3 pEnd, BlockPos pPos) {
      double[] adouble = new double[]{1.0D};
      Direction direction = null;
      double d0 = pEnd.x - pStart.x;
      double d1 = pEnd.y - pStart.y;
      double d2 = pEnd.z - pStart.z;

      for(AABB aabb : pBoxes) {
         direction = getDirection(aabb.move(pPos), pStart, adouble, direction, d0, d1, d2);
      }

      if (direction == null) {
         return null;
      } else {
         double d3 = adouble[0];
         return new BlockHitResult(pStart.add(d3 * d0, d3 * d1, d3 * d2), direction, pPos, false);
      }
   }

   @Nullable
   private static Direction getDirection(AABB pAabb, Vec3 pStart, double[] pMinDistance, @Nullable Direction pFacing, double pDeltaX, double pDeltaY, double pDeltaZ) {
      if (pDeltaX > 1.0E-7D) {
         pFacing = clipPoint(pMinDistance, pFacing, pDeltaX, pDeltaY, pDeltaZ, pAabb.minX, pAabb.minY, pAabb.maxY, pAabb.minZ, pAabb.maxZ, Direction.WEST, pStart.x, pStart.y, pStart.z);
      } else if (pDeltaX < -1.0E-7D) {
         pFacing = clipPoint(pMinDistance, pFacing, pDeltaX, pDeltaY, pDeltaZ, pAabb.maxX, pAabb.minY, pAabb.maxY, pAabb.minZ, pAabb.maxZ, Direction.EAST, pStart.x, pStart.y, pStart.z);
      }

      if (pDeltaY > 1.0E-7D) {
         pFacing = clipPoint(pMinDistance, pFacing, pDeltaY, pDeltaZ, pDeltaX, pAabb.minY, pAabb.minZ, pAabb.maxZ, pAabb.minX, pAabb.maxX, Direction.DOWN, pStart.y, pStart.z, pStart.x);
      } else if (pDeltaY < -1.0E-7D) {
         pFacing = clipPoint(pMinDistance, pFacing, pDeltaY, pDeltaZ, pDeltaX, pAabb.maxY, pAabb.minZ, pAabb.maxZ, pAabb.minX, pAabb.maxX, Direction.UP, pStart.y, pStart.z, pStart.x);
      }

      if (pDeltaZ > 1.0E-7D) {
         pFacing = clipPoint(pMinDistance, pFacing, pDeltaZ, pDeltaX, pDeltaY, pAabb.minZ, pAabb.minX, pAabb.maxX, pAabb.minY, pAabb.maxY, Direction.NORTH, pStart.z, pStart.x, pStart.y);
      } else if (pDeltaZ < -1.0E-7D) {
         pFacing = clipPoint(pMinDistance, pFacing, pDeltaZ, pDeltaX, pDeltaY, pAabb.maxZ, pAabb.minX, pAabb.maxX, pAabb.minY, pAabb.maxY, Direction.SOUTH, pStart.z, pStart.x, pStart.y);
      }

      return pFacing;
   }

   @Nullable
   private static Direction clipPoint(double[] pMinDistance, @Nullable Direction pPrevDirection, double pDistanceSide, double pDistanceOtherA, double pDistanceOtherB, double pMinSide, double pMinOtherA, double pMaxOtherA, double pMinOtherB, double pMaxOtherB, Direction pHitSide, double pStartSide, double pStartOtherA, double pStartOtherB) {
      double d0 = (pMinSide - pStartSide) / pDistanceSide;
      double d1 = pStartOtherA + d0 * pDistanceOtherA;
      double d2 = pStartOtherB + d0 * pDistanceOtherB;
      if (0.0D < d0 && d0 < pMinDistance[0] && pMinOtherA - 1.0E-7D < d1 && d1 < pMaxOtherA + 1.0E-7D && pMinOtherB - 1.0E-7D < d2 && d2 < pMaxOtherB + 1.0E-7D) {
         pMinDistance[0] = d0;
         return pHitSide;
      } else {
         return pPrevDirection;
      }
   }

   public double distanceToSqr(Vec3 pVec) {
      double d0 = Math.max(Math.max(this.minX - pVec.x, pVec.x - this.maxX), 0.0D);
      double d1 = Math.max(Math.max(this.minY - pVec.y, pVec.y - this.maxY), 0.0D);
      double d2 = Math.max(Math.max(this.minZ - pVec.z, pVec.z - this.maxZ), 0.0D);
      return Mth.lengthSquared(d0, d1, d2);
   }

   public String toString() {
      return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
   }

   public boolean hasNaN() {
      return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
   }

   public Vec3 getCenter() {
      return new Vec3(Mth.lerp(0.5D, this.minX, this.maxX), Mth.lerp(0.5D, this.minY, this.maxY), Mth.lerp(0.5D, this.minZ, this.maxZ));
   }

   public static AABB ofSize(Vec3 pCenter, double pXSize, double pYSize, double pZSize) {
      return new AABB(pCenter.x - pXSize / 2.0D, pCenter.y - pYSize / 2.0D, pCenter.z - pZSize / 2.0D, pCenter.x + pXSize / 2.0D, pCenter.y + pYSize / 2.0D, pCenter.z + pZSize / 2.0D);
   }
}