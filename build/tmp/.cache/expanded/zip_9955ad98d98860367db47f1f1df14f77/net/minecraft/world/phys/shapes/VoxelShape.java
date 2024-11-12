package net.minecraft.world.phys.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class VoxelShape {
   protected final DiscreteVoxelShape shape;
   @Nullable
   private VoxelShape[] faces;

   VoxelShape(DiscreteVoxelShape pShape) {
      this.shape = pShape;
   }

   public double min(Direction.Axis pAxis) {
      int i = this.shape.firstFull(pAxis);
      return i >= this.shape.getSize(pAxis) ? Double.POSITIVE_INFINITY : this.get(pAxis, i);
   }

   public double max(Direction.Axis pAxis) {
      int i = this.shape.lastFull(pAxis);
      return i <= 0 ? Double.NEGATIVE_INFINITY : this.get(pAxis, i);
   }

   public AABB bounds() {
      if (this.isEmpty()) {
         throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
      } else {
         return new AABB(this.min(Direction.Axis.X), this.min(Direction.Axis.Y), this.min(Direction.Axis.Z), this.max(Direction.Axis.X), this.max(Direction.Axis.Y), this.max(Direction.Axis.Z));
      }
   }

   protected double get(Direction.Axis pAxis, int pIndex) {
      return this.getCoords(pAxis).getDouble(pIndex);
   }

   protected abstract DoubleList getCoords(Direction.Axis pAxis);

   public boolean isEmpty() {
      return this.shape.isEmpty();
   }

   public VoxelShape move(double pXOffset, double pYOffset, double pZOffset) {
      return (VoxelShape)(this.isEmpty() ? Shapes.empty() : new ArrayVoxelShape(this.shape, (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.X), pXOffset)), (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Y), pYOffset)), (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Z), pZOffset))));
   }

   public VoxelShape optimize() {
      VoxelShape[] avoxelshape = new VoxelShape[]{Shapes.empty()};
      this.forAllBoxes((p_83275_, p_83276_, p_83277_, p_83278_, p_83279_, p_83280_) -> {
         avoxelshape[0] = Shapes.joinUnoptimized(avoxelshape[0], Shapes.box(p_83275_, p_83276_, p_83277_, p_83278_, p_83279_, p_83280_), BooleanOp.OR);
      });
      return avoxelshape[0];
   }

   public void forAllEdges(Shapes.DoubleLineConsumer pAction) {
      this.shape.forAllEdges((p_83228_, p_83229_, p_83230_, p_83231_, p_83232_, p_83233_) -> {
         pAction.consume(this.get(Direction.Axis.X, p_83228_), this.get(Direction.Axis.Y, p_83229_), this.get(Direction.Axis.Z, p_83230_), this.get(Direction.Axis.X, p_83231_), this.get(Direction.Axis.Y, p_83232_), this.get(Direction.Axis.Z, p_83233_));
      }, true);
   }

   public void forAllBoxes(Shapes.DoubleLineConsumer pAction) {
      DoubleList doublelist = this.getCoords(Direction.Axis.X);
      DoubleList doublelist1 = this.getCoords(Direction.Axis.Y);
      DoubleList doublelist2 = this.getCoords(Direction.Axis.Z);
      this.shape.forAllBoxes((p_83239_, p_83240_, p_83241_, p_83242_, p_83243_, p_83244_) -> {
         pAction.consume(doublelist.getDouble(p_83239_), doublelist1.getDouble(p_83240_), doublelist2.getDouble(p_83241_), doublelist.getDouble(p_83242_), doublelist1.getDouble(p_83243_), doublelist2.getDouble(p_83244_));
      }, true);
   }

   public List<AABB> toAabbs() {
      List<AABB> list = Lists.newArrayList();
      this.forAllBoxes((p_83267_, p_83268_, p_83269_, p_83270_, p_83271_, p_83272_) -> {
         list.add(new AABB(p_83267_, p_83268_, p_83269_, p_83270_, p_83271_, p_83272_));
      });
      return list;
   }

   public double min(Direction.Axis pAxis, double pPrimaryPosition, double pSecondaryPosition) {
      Direction.Axis direction$axis = AxisCycle.FORWARD.cycle(pAxis);
      Direction.Axis direction$axis1 = AxisCycle.BACKWARD.cycle(pAxis);
      int i = this.findIndex(direction$axis, pPrimaryPosition);
      int j = this.findIndex(direction$axis1, pSecondaryPosition);
      int k = this.shape.firstFull(pAxis, i, j);
      return k >= this.shape.getSize(pAxis) ? Double.POSITIVE_INFINITY : this.get(pAxis, k);
   }

   public double max(Direction.Axis pAxis, double pPrimaryPosition, double pSecondaryPosition) {
      Direction.Axis direction$axis = AxisCycle.FORWARD.cycle(pAxis);
      Direction.Axis direction$axis1 = AxisCycle.BACKWARD.cycle(pAxis);
      int i = this.findIndex(direction$axis, pPrimaryPosition);
      int j = this.findIndex(direction$axis1, pSecondaryPosition);
      int k = this.shape.lastFull(pAxis, i, j);
      return k <= 0 ? Double.NEGATIVE_INFINITY : this.get(pAxis, k);
   }

   protected int findIndex(Direction.Axis pAxis, double pPosition) {
      return Mth.binarySearch(0, this.shape.getSize(pAxis) + 1, (p_166066_) -> {
         return pPosition < this.get(pAxis, p_166066_);
      }) - 1;
   }

   @Nullable
   public BlockHitResult clip(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
      if (this.isEmpty()) {
         return null;
      } else {
         Vec3 vec3 = pEndVec.subtract(pStartVec);
         if (vec3.lengthSqr() < 1.0E-7D) {
            return null;
         } else {
            Vec3 vec31 = pStartVec.add(vec3.scale(0.001D));
            return this.shape.isFullWide(this.findIndex(Direction.Axis.X, vec31.x - (double)pPos.getX()), this.findIndex(Direction.Axis.Y, vec31.y - (double)pPos.getY()), this.findIndex(Direction.Axis.Z, vec31.z - (double)pPos.getZ())) ? new BlockHitResult(vec31, Direction.getNearest(vec3.x, vec3.y, vec3.z).getOpposite(), pPos, true) : AABB.clip(this.toAabbs(), pStartVec, pEndVec, pPos);
         }
      }
   }

   public Optional<Vec3> closestPointTo(Vec3 pPoint) {
      if (this.isEmpty()) {
         return Optional.empty();
      } else {
         Vec3[] avec3 = new Vec3[1];
         this.forAllBoxes((p_166072_, p_166073_, p_166074_, p_166075_, p_166076_, p_166077_) -> {
            double d0 = Mth.clamp(pPoint.x(), p_166072_, p_166075_);
            double d1 = Mth.clamp(pPoint.y(), p_166073_, p_166076_);
            double d2 = Mth.clamp(pPoint.z(), p_166074_, p_166077_);
            if (avec3[0] == null || pPoint.distanceToSqr(d0, d1, d2) < pPoint.distanceToSqr(avec3[0])) {
               avec3[0] = new Vec3(d0, d1, d2);
            }

         });
         return Optional.of(avec3[0]);
      }
   }

   /**
    * Projects this shape onto the given side. For each box in the shape, if it does not touch the given side, it is
    * eliminated. Otherwise, the box is extended in the given axis to cover the entire range [0, 1].
    */
   public VoxelShape getFaceShape(Direction pSide) {
      if (!this.isEmpty() && this != Shapes.block()) {
         if (this.faces != null) {
            VoxelShape voxelshape = this.faces[pSide.ordinal()];
            if (voxelshape != null) {
               return voxelshape;
            }
         } else {
            this.faces = new VoxelShape[6];
         }

         VoxelShape voxelshape1 = this.calculateFace(pSide);
         this.faces[pSide.ordinal()] = voxelshape1;
         return voxelshape1;
      } else {
         return this;
      }
   }

   private VoxelShape calculateFace(Direction pSide) {
      Direction.Axis direction$axis = pSide.getAxis();
      DoubleList doublelist = this.getCoords(direction$axis);
      if (doublelist.size() == 2 && DoubleMath.fuzzyEquals(doublelist.getDouble(0), 0.0D, 1.0E-7D) && DoubleMath.fuzzyEquals(doublelist.getDouble(1), 1.0D, 1.0E-7D)) {
         return this;
      } else {
         Direction.AxisDirection direction$axisdirection = pSide.getAxisDirection();
         int i = this.findIndex(direction$axis, direction$axisdirection == Direction.AxisDirection.POSITIVE ? 0.9999999D : 1.0E-7D);
         return new SliceShape(this, direction$axis, i);
      }
   }

   public double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
      return this.collideX(AxisCycle.between(pMovementAxis, Direction.Axis.X), pCollisionBox, pDesiredOffset);
   }

   protected double collideX(AxisCycle pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
      if (this.isEmpty()) {
         return pDesiredOffset;
      } else if (Math.abs(pDesiredOffset) < 1.0E-7D) {
         return 0.0D;
      } else {
         AxisCycle axiscycle = pMovementAxis.inverse();
         Direction.Axis direction$axis = axiscycle.cycle(Direction.Axis.X);
         Direction.Axis direction$axis1 = axiscycle.cycle(Direction.Axis.Y);
         Direction.Axis direction$axis2 = axiscycle.cycle(Direction.Axis.Z);
         double d0 = pCollisionBox.max(direction$axis);
         double d1 = pCollisionBox.min(direction$axis);
         int i = this.findIndex(direction$axis, d1 + 1.0E-7D);
         int j = this.findIndex(direction$axis, d0 - 1.0E-7D);
         int k = Math.max(0, this.findIndex(direction$axis1, pCollisionBox.min(direction$axis1) + 1.0E-7D));
         int l = Math.min(this.shape.getSize(direction$axis1), this.findIndex(direction$axis1, pCollisionBox.max(direction$axis1) - 1.0E-7D) + 1);
         int i1 = Math.max(0, this.findIndex(direction$axis2, pCollisionBox.min(direction$axis2) + 1.0E-7D));
         int j1 = Math.min(this.shape.getSize(direction$axis2), this.findIndex(direction$axis2, pCollisionBox.max(direction$axis2) - 1.0E-7D) + 1);
         int k1 = this.shape.getSize(direction$axis);
         if (pDesiredOffset > 0.0D) {
            for(int l1 = j + 1; l1 < k1; ++l1) {
               for(int i2 = k; i2 < l; ++i2) {
                  for(int j2 = i1; j2 < j1; ++j2) {
                     if (this.shape.isFullWide(axiscycle, l1, i2, j2)) {
                        double d2 = this.get(direction$axis, l1) - d0;
                        if (d2 >= -1.0E-7D) {
                           pDesiredOffset = Math.min(pDesiredOffset, d2);
                        }

                        return pDesiredOffset;
                     }
                  }
               }
            }
         } else if (pDesiredOffset < 0.0D) {
            for(int k2 = i - 1; k2 >= 0; --k2) {
               for(int l2 = k; l2 < l; ++l2) {
                  for(int i3 = i1; i3 < j1; ++i3) {
                     if (this.shape.isFullWide(axiscycle, k2, l2, i3)) {
                        double d3 = this.get(direction$axis, k2 + 1) - d1;
                        if (d3 <= 1.0E-7D) {
                           pDesiredOffset = Math.max(pDesiredOffset, d3);
                        }

                        return pDesiredOffset;
                     }
                  }
               }
            }
         }

         return pDesiredOffset;
      }
   }

   public String toString() {
      return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]";
   }
}