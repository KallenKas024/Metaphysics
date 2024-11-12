package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum Rotation implements StringRepresentable {
   NONE("none", OctahedralGroup.IDENTITY),
   CLOCKWISE_90("clockwise_90", OctahedralGroup.ROT_90_Y_NEG),
   CLOCKWISE_180("180", OctahedralGroup.ROT_180_FACE_XZ),
   COUNTERCLOCKWISE_90("counterclockwise_90", OctahedralGroup.ROT_90_Y_POS);

   public static final Codec<Rotation> CODEC = StringRepresentable.fromEnum(Rotation::values);
   private final String id;
   private final OctahedralGroup rotation;

   private Rotation(String pId, OctahedralGroup pRotation) {
      this.id = pId;
      this.rotation = pRotation;
   }

   public Rotation getRotated(Rotation pRotation) {
      switch (pRotation) {
         case CLOCKWISE_180:
            switch (this) {
               case NONE:
                  return CLOCKWISE_180;
               case CLOCKWISE_90:
                  return COUNTERCLOCKWISE_90;
               case CLOCKWISE_180:
                  return NONE;
               case COUNTERCLOCKWISE_90:
                  return CLOCKWISE_90;
            }
         case COUNTERCLOCKWISE_90:
            switch (this) {
               case NONE:
                  return COUNTERCLOCKWISE_90;
               case CLOCKWISE_90:
                  return NONE;
               case CLOCKWISE_180:
                  return CLOCKWISE_90;
               case COUNTERCLOCKWISE_90:
                  return CLOCKWISE_180;
            }
         case CLOCKWISE_90:
            switch (this) {
               case NONE:
                  return CLOCKWISE_90;
               case CLOCKWISE_90:
                  return CLOCKWISE_180;
               case CLOCKWISE_180:
                  return COUNTERCLOCKWISE_90;
               case COUNTERCLOCKWISE_90:
                  return NONE;
            }
         default:
            return this;
      }
   }

   public OctahedralGroup rotation() {
      return this.rotation;
   }

   public Direction rotate(Direction pFacing) {
      if (pFacing.getAxis() == Direction.Axis.Y) {
         return pFacing;
      } else {
         switch (this) {
            case CLOCKWISE_90:
               return pFacing.getClockWise();
            case CLOCKWISE_180:
               return pFacing.getOpposite();
            case COUNTERCLOCKWISE_90:
               return pFacing.getCounterClockWise();
            default:
               return pFacing;
         }
      }
   }

   public int rotate(int pRotation, int pPositionCount) {
      switch (this) {
         case CLOCKWISE_90:
            return (pRotation + pPositionCount / 4) % pPositionCount;
         case CLOCKWISE_180:
            return (pRotation + pPositionCount / 2) % pPositionCount;
         case COUNTERCLOCKWISE_90:
            return (pRotation + pPositionCount * 3 / 4) % pPositionCount;
         default:
            return pRotation;
      }
   }

   /**
    * Chooses a random rotation.
    */
   public static Rotation getRandom(RandomSource pRandom) {
      return Util.getRandom(values(), pRandom);
   }

   /**
    * Get a list of all rotations in random order.
    */
   public static List<Rotation> getShuffled(RandomSource pRandom) {
      return Util.shuffledCopy(values(), pRandom);
   }

   public String getSerializedName() {
      return this.id;
   }
}