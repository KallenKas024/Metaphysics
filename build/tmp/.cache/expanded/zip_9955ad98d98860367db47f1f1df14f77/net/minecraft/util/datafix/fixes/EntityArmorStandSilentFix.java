package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityArmorStandSilentFix extends NamedEntityFix {
   public EntityArmorStandSilentFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "EntityArmorStandSilentFix", References.ENTITY, "ArmorStand");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15329_) {
      return p_15329_.get("Silent").asBoolean(false) && !p_15329_.get("Marker").asBoolean(false) ? p_15329_.remove("Silent") : p_15329_;
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}