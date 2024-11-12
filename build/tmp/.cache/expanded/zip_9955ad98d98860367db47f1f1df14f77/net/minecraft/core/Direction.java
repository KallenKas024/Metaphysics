package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public enum Direction implements StringRepresentable {
   DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
   UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
   NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
   SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
   WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
   EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

   public static final StringRepresentable.EnumCodec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);
   public static final Codec<Direction> VERTICAL_CODEC = ExtraCodecs.validate(CODEC, Direction::verifyVertical);
   /** Ordering index for D-U-N-S-W-E */
   private final int data3d;
   /** Index of the opposite Direction in the VALUES array */
   private final int oppositeIndex;
   /** Ordering index for the HORIZONTALS field (S-W-N-E) */
   private final int data2d;
   private final String name;
   private final Direction.Axis axis;
   private final Direction.AxisDirection axisDirection;
   /** Normalized vector that points in the direction of this Direction */
   private final Vec3i normal;
   private static final Direction[] VALUES = values();
   private static final Direction[] BY_3D_DATA = Arrays.stream(VALUES).sorted(Comparator.comparingInt((p_235687_) -> {
      return p_235687_.data3d;
   })).toArray((p_235681_) -> {
      return new Direction[p_235681_];
   });
   /** All Facings with horizontal axis in order S-W-N-E */
   private static final Direction[] BY_2D_DATA = Arrays.stream(VALUES).filter((p_235685_) -> {
      return p_235685_.getAxis().isHorizontal();
   }).sorted(Comparator.comparingInt((p_235683_) -> {
      return p_235683_.data2d;
   })).toArray((p_235677_) -> {
      return new Direction[p_235677_];
   });

   private Direction(int pData3d, int pOppositeIndex, int pData2d, String pName, Direction.AxisDirection pAxisDirection, Direction.Axis pAxis, Vec3i pNormal) {
      this.data3d = pData3d;
      this.data2d = pData2d;
      this.oppositeIndex = pOppositeIndex;
      this.name = pName;
      this.axis = pAxis;
      this.axisDirection = pAxisDirection;
      this.normal = pNormal;
   }

   /**
    * Gets the {@code Direction} values for the provided entity's
    * looking direction. Dependent on yaw and pitch of entity looking.
    */
   public static Direction[] orderedByNearest(Entity pEntity) {
      float f = pEntity.getViewXRot(1.0F) * ((float)Math.PI / 180F);
      float f1 = -pEntity.getViewYRot(1.0F) * ((float)Math.PI / 180F);
      float f2 = Mth.sin(f);
      float f3 = Mth.cos(f);
      float f4 = Mth.sin(f1);
      float f5 = Mth.cos(f1);
      boolean flag = f4 > 0.0F;
      boolean flag1 = f2 < 0.0F;
      boolean flag2 = f5 > 0.0F;
      float f6 = flag ? f4 : -f4;
      float f7 = flag1 ? -f2 : f2;
      float f8 = flag2 ? f5 : -f5;
      float f9 = f6 * f3;
      float f10 = f8 * f3;
      Direction direction = flag ? EAST : WEST;
      Direction direction1 = flag1 ? UP : DOWN;
      Direction direction2 = flag2 ? SOUTH : NORTH;
      if (f6 > f8) {
         if (f7 > f9) {
            return makeDirectionArray(direction1, direction, direction2);
         } else {
            return f10 > f7 ? makeDirectionArray(direction, direction2, direction1) : makeDirectionArray(direction, direction1, direction2);
         }
      } else if (f7 > f10) {
         return makeDirectionArray(direction1, direction2, direction);
      } else {
         return f9 > f7 ? makeDirectionArray(direction2, direction, direction1) : makeDirectionArray(direction2, direction1, direction);
      }
   }

   private static Direction[] makeDirectionArray(Direction pFirst, Direction pSecond, Direction pThird) {
      return new Direction[]{pFirst, pSecond, pThird, pThird.getOpposite(), pSecond.getOpposite(), pFirst.getOpposite()};
   }

   public static Direction rotate(Matrix4f pMatrix, Direction pDirection) {
      Vec3i vec3i = pDirection.getNormal();
      Vector4f vector4f = pMatrix.transform(new Vector4f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), 0.0F));
      return getNearest(vector4f.x(), vector4f.y(), vector4f.z());
   }

   public static Collection<Direction> allShuffled(RandomSource pRandom) {
      return Util.shuffledCopy(values(), pRandom);
   }

   public static Stream<Direction> stream() {
      return Stream.of(VALUES);
   }

   public Quaternionf getRotation() {
      Quaternionf quaternionf;
      switch (this) {
         case DOWN:
            quaternionf = (new Quaternionf()).rotationX((float)Math.PI);
            break;
         case UP:
            quaternionf = new Quaternionf();
            break;
         case NORTH:
            quaternionf = (new Quaternionf()).rotationXYZ(((float)Math.PI / 2F), 0.0F, (float)Math.PI);
            break;
         case SOUTH:
            quaternionf = (new Quaternionf()).rotationX(((float)Math.PI / 2F));
            break;
         case WEST:
            quaternionf = (new Quaternionf()).rotationXYZ(((float)Math.PI / 2F), 0.0F, ((float)Math.PI / 2F));
            break;
         case EAST:
            quaternionf = (new Quaternionf()).rotationXYZ(((float)Math.PI / 2F), 0.0F, (-(float)Math.PI / 2F));
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return quaternionf;
   }

   /**
    * @return the index of this Direction (0-5). The order is D-U-N-S-W-E
    */
   public int get3DDataValue() {
      return this.data3d;
   }

   /**
    * @return the index of this horizontal facing (0-3). The order is S-W-N-E
    */
   public int get2DDataValue() {
      return this.data2d;
   }

   public Direction.AxisDirection getAxisDirection() {
      return this.axisDirection;
   }

   public static Direction getFacingAxis(Entity pEntity, Direction.Axis pAxis) {
      Direction direction;
      switch (pAxis) {
         case X:
            direction = EAST.isFacingAngle(pEntity.getViewYRot(1.0F)) ? EAST : WEST;
            break;
         case Z:
            direction = SOUTH.isFacingAngle(pEntity.getViewYRot(1.0F)) ? SOUTH : NORTH;
            break;
         case Y:
            direction = pEntity.getViewXRot(1.0F) < 0.0F ? UP : DOWN;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return direction;
   }

   /**
    * @return the opposite Direction (e.g. DOWN => UP)
    */
   public Direction getOpposite() {
      return from3DDataValue(this.oppositeIndex);
   }

   public Direction getClockWise(Direction.Axis pAxis) {
      Direction direction;
      switch (pAxis) {
         case X:
            direction = this != WEST && this != EAST ? this.getClockWiseX() : this;
            break;
         case Z:
            direction = this != NORTH && this != SOUTH ? this.getClockWiseZ() : this;
            break;
         case Y:
            direction = this != UP && this != DOWN ? this.getClockWise() : this;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return direction;
   }

   public Direction getCounterClockWise(Direction.Axis pAxis) {
      Direction direction;
      switch (pAxis) {
         case X:
            direction = this != WEST && this != EAST ? this.getCounterClockWiseX() : this;
            break;
         case Z:
            direction = this != NORTH && this != SOUTH ? this.getCounterClockWiseZ() : this;
            break;
         case Y:
            direction = this != UP && this != DOWN ? this.getCounterClockWise() : this;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return direction;
   }

   /**
    * Rotate this Direction around the Y axis clockwise (NORTH => EAST => SOUTH => WEST => NORTH)
    */
   public Direction getClockWise() {
      Direction direction;
      switch (this) {
         case NORTH:
            direction = EAST;
            break;
         case SOUTH:
            direction = WEST;
            break;
         case WEST:
            direction = NORTH;
            break;
         case EAST:
            direction = SOUTH;
            break;
         default:
            throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      }

      return direction;
   }

   private Direction getClockWiseX() {
      Direction direction;
      switch (this) {
         case DOWN:
            direction = SOUTH;
            break;
         case UP:
            direction = NORTH;
            break;
         case NORTH:
            direction = DOWN;
            break;
         case SOUTH:
            direction = UP;
            break;
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      }

      return direction;
   }

   private Direction getCounterClockWiseX() {
      Direction direction;
      switch (this) {
         case DOWN:
            direction = NORTH;
            break;
         case UP:
            direction = SOUTH;
            break;
         case NORTH:
            direction = UP;
            break;
         case SOUTH:
            direction = DOWN;
            break;
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      }

      return direction;
   }

   private Direction getClockWiseZ() {
      Direction direction;
      switch (this) {
         case DOWN:
            direction = WEST;
            break;
         case UP:
            direction = EAST;
            break;
         case NORTH:
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST:
            direction = UP;
            break;
         case EAST:
            direction = DOWN;
      }

      return direction;
   }

   private Direction getCounterClockWiseZ() {
      Direction direction;
      switch (this) {
         case DOWN:
            direction = EAST;
            break;
         case UP:
            direction = WEST;
            break;
         case NORTH:
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST:
            direction = DOWN;
            break;
         case EAST:
            direction = UP;
      }

      return direction;
   }

   /**
    * Rotate this Direction around the Y axis counter-clockwise (NORTH => WEST => SOUTH => EAST => NORTH)
    */
   public Direction getCounterClockWise() {
      Direction direction;
      switch (this) {
         case NORTH:
            direction = WEST;
            break;
         case SOUTH:
            direction = EAST;
            break;
         case WEST:
            direction = SOUTH;
            break;
         case EAST:
            direction = NORTH;
            break;
         default:
            throw new IllegalStateException("Unable to get CCW facing of " + this);
      }

      return direction;
   }

   /**
    * @return the offset in the x direction
    */
   public int getStepX() {
      return this.normal.getX();
   }

   /**
    * @return the offset in the y direction
    */
   public int getStepY() {
      return this.normal.getY();
   }

   /**
    * @return the offset in the z direction
    */
   public int getStepZ() {
      return this.normal.getZ();
   }

   public Vector3f step() {
      return new Vector3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
   }

   public String getName() {
      return this.name;
   }

   public Direction.Axis getAxis() {
      return this.axis;
   }

   /**
    * @return the Direction specified by the given name or null if no such Direction exists
    */
   @Nullable
   public static Direction byName(@Nullable String pName) {
      return CODEC.byName(pName);
   }

   /**
    * @return the {@code Direction} corresponding to the given index (0-5). Out of bounds values are wrapped around. The
    * order is D-U-N-S-W-E.
    * @see #get3DDataValue
    */
   public static Direction from3DDataValue(int pIndex) {
      return BY_3D_DATA[Mth.abs(pIndex % BY_3D_DATA.length)];
   }

   /**
    * @return the Direction corresponding to the given horizontal index (0-3). Out of bounds values are wrapped around.
    * The order is S-W-N-E.
    * @see #get2DDataValue
    */
   public static Direction from2DDataValue(int pHorizontalIndex) {
      return BY_2D_DATA[Mth.abs(pHorizontalIndex % BY_2D_DATA.length)];
   }

   @Nullable
   public static Direction fromDelta(int pX, int pY, int pZ) {
      if (pX == 0) {
         if (pY == 0) {
            if (pZ > 0) {
               return SOUTH;
            }

            if (pZ < 0) {
               return NORTH;
            }
         } else if (pZ == 0) {
            if (pY > 0) {
               return UP;
            }

            return DOWN;
         }
      } else if (pY == 0 && pZ == 0) {
         if (pX > 0) {
            return EAST;
         }

         return WEST;
      }

      return null;
   }

   /**
    * @return the Direction corresponding to the given angle in degrees (0-360). Out of bounds values are wrapped
    * around. An angle of 0 is SOUTH, an angle of 90 would be WEST.
    */
   public static Direction fromYRot(double pAngle) {
      return from2DDataValue(Mth.floor(pAngle / 90.0D + 0.5D) & 3);
   }

   public static Direction fromAxisAndDirection(Direction.Axis pAxis, Direction.AxisDirection pAxisDirection) {
      Direction direction;
      switch (pAxis) {
         case X:
            direction = pAxisDirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;
            break;
         case Z:
            direction = pAxisDirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
            break;
         case Y:
            direction = pAxisDirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return direction;
   }

   /**
    * @return the angle in degrees corresponding to this Direction.
    * @see #fromYRot
    */
   public float toYRot() {
      return (float)((this.data2d & 3) * 90);
   }

   public static Direction getRandom(RandomSource pRandom) {
      return Util.getRandom(VALUES, pRandom);
   }

   public static Direction getNearest(double pX, double pY, double pZ) {
      return getNearest((float)pX, (float)pY, (float)pZ);
   }

   public static Direction getNearest(float pX, float pY, float pZ) {
      Direction direction = NORTH;
      float f = Float.MIN_VALUE;

      for(Direction direction1 : VALUES) {
         float f1 = pX * (float)direction1.normal.getX() + pY * (float)direction1.normal.getY() + pZ * (float)direction1.normal.getZ();
         if (f1 > f) {
            f = f1;
            direction = direction1;
         }
      }

      return direction;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   private static DataResult<Direction> verifyVertical(Direction p_194529_) {
      return p_194529_.getAxis().isVertical() ? DataResult.success(p_194529_) : DataResult.error(() -> {
         return "Expected a vertical direction";
      });
   }

   public static Direction get(Direction.AxisDirection pAxisDirection, Direction.Axis pAxis) {
      for(Direction direction : VALUES) {
         if (direction.getAxisDirection() == pAxisDirection && direction.getAxis() == pAxis) {
            return direction;
         }
      }

      throw new IllegalArgumentException("No such direction: " + pAxisDirection + " " + pAxis);
   }

   /**
    * @return the normalized Vector that points in the direction of this Direction.
    */
   public Vec3i getNormal() {
      return this.normal;
   }

   public boolean isFacingAngle(float pDegrees) {
      float f = pDegrees * ((float)Math.PI / 180F);
      float f1 = -Mth.sin(f);
      float f2 = Mth.cos(f);
      return (float)this.normal.getX() * f1 + (float)this.normal.getZ() * f2 > 0.0F;
   }

   public static enum Axis implements StringRepresentable, Predicate<Direction> {
      X("x") {
         public int choose(int p_122496_, int p_122497_, int p_122498_) {
            return p_122496_;
         }

         public double choose(double p_122492_, double p_122493_, double p_122494_) {
            return p_122492_;
         }
      },
      Y("y") {
         public int choose(int p_122510_, int p_122511_, int p_122512_) {
            return p_122511_;
         }

         public double choose(double p_122506_, double p_122507_, double p_122508_) {
            return p_122507_;
         }
      },
      Z("z") {
         public int choose(int p_122524_, int p_122525_, int p_122526_) {
            return p_122526_;
         }

         public double choose(double p_122520_, double p_122521_, double p_122522_) {
            return p_122522_;
         }
      };

      public static final Direction.Axis[] VALUES = values();
      public static final StringRepresentable.EnumCodec<Direction.Axis> CODEC = StringRepresentable.fromEnum(Direction.Axis::values);
      private final String name;

      Axis(String pName) {
         this.name = pName;
      }

      /**
       * @return the Axis specified by the given name or {@code null} if no such Axis exists
       */
      @Nullable
      public static Direction.Axis byName(String pName) {
         return CODEC.byName(pName);
      }

      public String getName() {
         return this.name;
      }

      public boolean isVertical() {
         return this == Y;
      }

      /**
       * @return whether this Axis is on the horizontal plane (true for X and Z)
       */
      public boolean isHorizontal() {
         return this == X || this == Z;
      }

      public String toString() {
         return this.name;
      }

      public static Direction.Axis getRandom(RandomSource pRandom) {
         return Util.getRandom(VALUES, pRandom);
      }

      public boolean test(@Nullable Direction pDirection) {
         return pDirection != null && pDirection.getAxis() == this;
      }

      /**
       * @return this Axis' Plane (VERTICAL for Y, HORIZONTAL for X and Z)
       */
      public Direction.Plane getPlane() {
         Direction.Plane direction$plane;
         switch (this) {
            case X:
            case Z:
               direction$plane = Direction.Plane.HORIZONTAL;
               break;
            case Y:
               direction$plane = Direction.Plane.VERTICAL;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return direction$plane;
      }

      public String getSerializedName() {
         return this.name;
      }

      public abstract int choose(int pX, int pY, int pZ);

      public abstract double choose(double pX, double pY, double pZ);
   }

   public static enum AxisDirection {
      POSITIVE(1, "Towards positive"),
      NEGATIVE(-1, "Towards negative");

      private final int step;
      private final String name;

      private AxisDirection(int pStep, String pName) {
         this.step = pStep;
         this.name = pName;
      }

      /**
       * @return the offset for this AxisDirection. 1 for POSITIVE, -1 for NEGATIVE
       */
      public int getStep() {
         return this.step;
      }

      public String getName() {
         return this.name;
      }

      public String toString() {
         return this.name;
      }

      public Direction.AxisDirection opposite() {
         return this == POSITIVE ? NEGATIVE : POSITIVE;
      }
   }

   public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
      HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
      VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

      private final Direction[] faces;
      private final Direction.Axis[] axis;

      private Plane(Direction[] pFaces, Direction.Axis[] pAxis) {
         this.faces = pFaces;
         this.axis = pAxis;
      }

      public Direction getRandomDirection(RandomSource pRandom) {
         return Util.getRandom(this.faces, pRandom);
      }

      public Direction.Axis getRandomAxis(RandomSource pRandom) {
         return Util.getRandom(this.axis, pRandom);
      }

      public boolean test(@Nullable Direction pDirection) {
         return pDirection != null && pDirection.getAxis().getPlane() == this;
      }

      public Iterator<Direction> iterator() {
         return Iterators.forArray(this.faces);
      }

      public Stream<Direction> stream() {
         return Arrays.stream(this.faces);
      }

      public List<Direction> shuffledCopy(RandomSource pRandom) {
         return Util.shuffledCopy(this.faces, pRandom);
      }
   }
}