package net.minecraft.client.renderer.chunk;

import java.util.BitSet;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisibilitySet {
   private static final int FACINGS = Direction.values().length;
   private final BitSet data = new BitSet(FACINGS * FACINGS);

   public void add(Set<Direction> pFaces) {
      for(Direction direction : pFaces) {
         for(Direction direction1 : pFaces) {
            this.set(direction, direction1, true);
         }
      }

   }

   public void set(Direction pFace, Direction pOtherFace, boolean pVisible) {
      this.data.set(pFace.ordinal() + pOtherFace.ordinal() * FACINGS, pVisible);
      this.data.set(pOtherFace.ordinal() + pFace.ordinal() * FACINGS, pVisible);
   }

   public void setAll(boolean pVisible) {
      this.data.set(0, this.data.size(), pVisible);
   }

   public boolean visibilityBetween(Direction pFace, Direction pOtherFace) {
      return this.data.get(pFace.ordinal() + pOtherFace.ordinal() * FACINGS);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(' ');

      for(Direction direction : Direction.values()) {
         stringbuilder.append(' ').append(direction.toString().toUpperCase().charAt(0));
      }

      stringbuilder.append('\n');

      for(Direction direction2 : Direction.values()) {
         stringbuilder.append(direction2.toString().toUpperCase().charAt(0));

         for(Direction direction1 : Direction.values()) {
            if (direction2 == direction1) {
               stringbuilder.append("  ");
            } else {
               boolean flag = this.visibilityBetween(direction2, direction1);
               stringbuilder.append(' ').append((char)(flag ? 'Y' : 'n'));
            }
         }

         stringbuilder.append('\n');
      }

      return stringbuilder.toString();
   }
}