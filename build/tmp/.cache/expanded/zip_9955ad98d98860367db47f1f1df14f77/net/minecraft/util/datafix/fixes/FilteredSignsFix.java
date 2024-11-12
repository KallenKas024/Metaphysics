package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class FilteredSignsFix extends NamedEntityFix {
   public FilteredSignsFix(Schema pOutputSchema) {
      super(pOutputSchema, false, "Remove filtered text from signs", References.BLOCK_ENTITY, "minecraft:sign");
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), (p_216670_) -> {
         return p_216670_.remove("FilteredText1").remove("FilteredText2").remove("FilteredText3").remove("FilteredText4");
      });
   }
}