package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class CatTypeFix extends NamedEntityFix {
   public CatTypeFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "CatTypeFix", References.ENTITY, "minecraft:cat");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15012_) {
      return p_15012_.get("CatType").asInt(0) == 9 ? p_15012_.set("CatType", p_15012_.createInt(10)) : p_15012_;
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}