package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityItemFrameDirectionFix extends NamedEntityFix {
   public EntityItemFrameDirectionFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "EntityItemFrameDirectionFix", References.ENTITY, "minecraft:item_frame");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15475_) {
      return p_15475_.set("Facing", p_15475_.createByte(direction2dTo3d(p_15475_.get("Facing").asByte((byte)0))));
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }

   private static byte direction2dTo3d(byte pDirection2d) {
      switch (pDirection2d) {
         case 0:
            return 3;
         case 1:
            return 4;
         case 2:
         default:
            return 2;
         case 3:
            return 5;
      }
   }
}