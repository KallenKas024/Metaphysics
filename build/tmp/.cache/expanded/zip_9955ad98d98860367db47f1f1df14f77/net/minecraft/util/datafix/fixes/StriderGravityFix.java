package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class StriderGravityFix extends NamedEntityFix {
   public StriderGravityFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "StriderGravityFix", References.ENTITY, "minecraft:strider");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_16959_) {
      return p_16959_.get("NoGravity").asBoolean(false) ? p_16959_.set("NoGravity", p_16959_.createBoolean(false)) : p_16959_;
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}