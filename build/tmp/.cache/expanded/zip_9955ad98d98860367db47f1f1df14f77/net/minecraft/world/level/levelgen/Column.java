package net.minecraft.world.level.levelgen;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A representation of an integer valued interval, either bounded or unbounded.
 * While the class itself does not imply any coordinate in particular, this is practically used to represent a column in
 * the Y direction.
 */
public abstract class Column {
   /**
    * @return A column of the closed interval [floor, ceiling].
    */
   public static Column.Range around(int pFloor, int pCeiling) {
      return new Column.Range(pFloor - 1, pCeiling + 1);
   }

   /**
    * @return A column of the open interval (floor, ceiling).
    */
   public static Column.Range inside(int pFloor, int pCeiling) {
      return new Column.Range(pFloor, pCeiling);
   }

   /**
    * @return A column of the unbounded interval (-infinity, ceiling).
    */
   public static Column below(int pCeiling) {
      return new Column.Ray(pCeiling, false);
   }

   /**
    * @return A column of the unbounded interval (-infinity, ceiling].
    */
   public static Column fromHighest(int pCeiling) {
      return new Column.Ray(pCeiling + 1, false);
   }

   /**
    * @return A column of the unbounded interval (floor, infinity).
    */
   public static Column above(int pFloor) {
      return new Column.Ray(pFloor, true);
   }

   /**
    * @return A column of the unbounded interval [floor, infinity).
    */
   public static Column fromLowest(int pFloor) {
      return new Column.Ray(pFloor - 1, true);
   }

   /**
    * @return A column of the unbounded interval (-infinity, infinity).
    */
   public static Column line() {
      return Column.Line.INSTANCE;
   }

   public static Column create(OptionalInt pFloor, OptionalInt pCeiling) {
      if (pFloor.isPresent() && pCeiling.isPresent()) {
         return inside(pFloor.getAsInt(), pCeiling.getAsInt());
      } else if (pFloor.isPresent()) {
         return above(pFloor.getAsInt());
      } else {
         return pCeiling.isPresent() ? below(pCeiling.getAsInt()) : line();
      }
   }

   public abstract OptionalInt getCeiling();

   public abstract OptionalInt getFloor();

   public abstract OptionalInt getHeight();

   public Column withFloor(OptionalInt pFloor) {
      return create(pFloor, this.getCeiling());
   }

   public Column withCeiling(OptionalInt pCeiling) {
      return create(this.getFloor(), pCeiling);
   }

   /**
    * Scans for a column of states satisfying {@code columnPredicate}, up to a length of {@code maxDistance} from the
    * origin, and ending with a state which satisfies {@code tipPredicate}.
    * @return A column representing the tips found. The column will be bounded if a tip was reached in the given
    * direction, unbounded otherwise.
    */
   public static Optional<Column> scan(LevelSimulatedReader pLevel, BlockPos pPos, int pMaxDistance, Predicate<BlockState> pColumnPredicate, Predicate<BlockState> pTipPredicate) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
      if (!pLevel.isStateAtPosition(pPos, pColumnPredicate)) {
         return Optional.empty();
      } else {
         int i = pPos.getY();
         OptionalInt optionalint = scanDirection(pLevel, pMaxDistance, pColumnPredicate, pTipPredicate, blockpos$mutableblockpos, i, Direction.UP);
         OptionalInt optionalint1 = scanDirection(pLevel, pMaxDistance, pColumnPredicate, pTipPredicate, blockpos$mutableblockpos, i, Direction.DOWN);
         return Optional.of(create(optionalint1, optionalint));
      }
   }

   /**
    * Scans for a sequence of states in a given {@code direction}, up to a length of {@code maxDistance} which satisfy
    * {@code columnPredicate}, and ending with a state which satisfies {@code tipPredicate}.
    * @return The y position of the tip, if found.
    */
   private static OptionalInt scanDirection(LevelSimulatedReader pLevel, int pMaxDistance, Predicate<BlockState> pColumnPredicate, Predicate<BlockState> pTipPredicate, BlockPos.MutableBlockPos pMutablePos, int pStartY, Direction pDirection) {
      pMutablePos.setY(pStartY);

      for(int i = 1; i < pMaxDistance && pLevel.isStateAtPosition(pMutablePos, pColumnPredicate); ++i) {
         pMutablePos.move(pDirection);
      }

      return pLevel.isStateAtPosition(pMutablePos, pTipPredicate) ? OptionalInt.of(pMutablePos.getY()) : OptionalInt.empty();
   }

   public static final class Line extends Column {
      static final Column.Line INSTANCE = new Column.Line();

      private Line() {
      }

      public OptionalInt getCeiling() {
         return OptionalInt.empty();
      }

      public OptionalInt getFloor() {
         return OptionalInt.empty();
      }

      public OptionalInt getHeight() {
         return OptionalInt.empty();
      }

      public String toString() {
         return "C(-)";
      }
   }

   public static final class Range extends Column {
      private final int floor;
      private final int ceiling;

      protected Range(int pFloor, int pCeiling) {
         this.floor = pFloor;
         this.ceiling = pCeiling;
         if (this.height() < 0) {
            throw new IllegalArgumentException("Column of negative height: " + this);
         }
      }

      public OptionalInt getCeiling() {
         return OptionalInt.of(this.ceiling);
      }

      public OptionalInt getFloor() {
         return OptionalInt.of(this.floor);
      }

      public OptionalInt getHeight() {
         return OptionalInt.of(this.height());
      }

      public int ceiling() {
         return this.ceiling;
      }

      public int floor() {
         return this.floor;
      }

      public int height() {
         return this.ceiling - this.floor - 1;
      }

      public String toString() {
         return "C(" + this.ceiling + "-" + this.floor + ")";
      }
   }

   public static final class Ray extends Column {
      private final int edge;
      private final boolean pointingUp;

      public Ray(int pEdge, boolean pPointingUp) {
         this.edge = pEdge;
         this.pointingUp = pPointingUp;
      }

      public OptionalInt getCeiling() {
         return this.pointingUp ? OptionalInt.empty() : OptionalInt.of(this.edge);
      }

      public OptionalInt getFloor() {
         return this.pointingUp ? OptionalInt.of(this.edge) : OptionalInt.empty();
      }

      public OptionalInt getHeight() {
         return OptionalInt.empty();
      }

      public String toString() {
         return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
      }
   }
}