package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityWolfColorFix extends NamedEntityFix {
   public EntityWolfColorFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "EntityWolfColorFix", References.ENTITY, "minecraft:wolf");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15794_) {
      return p_15794_.update("CollarColor", (p_15796_) -> {
         return p_15796_.createByte((byte)(15 - p_15796_.asInt(0)));
      });
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}