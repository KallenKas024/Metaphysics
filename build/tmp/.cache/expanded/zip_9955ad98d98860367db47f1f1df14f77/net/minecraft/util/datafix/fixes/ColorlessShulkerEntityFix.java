package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class ColorlessShulkerEntityFix extends NamedEntityFix {
   public ColorlessShulkerEntityFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "Colorless shulker entity fix", References.ENTITY, "minecraft:shulker");
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), (p_15320_) -> {
         return p_15320_.get("Color").asInt(0) == 10 ? p_15320_.set("Color", p_15320_.createByte((byte)16)) : p_15320_;
      });
   }
}