package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityShulkerColorFix extends NamedEntityFix {
   public EntityShulkerColorFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "EntityShulkerColorFix", References.ENTITY, "minecraft:shulker");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15678_) {
      return !p_15678_.get("Color").map(Dynamic::asNumber).result().isPresent() ? p_15678_.set("Color", p_15678_.createByte((byte)10)) : p_15678_;
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}