package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EntityPaintingFieldsRenameFix extends NamedEntityFix {
   public EntityPaintingFieldsRenameFix(Schema pOutputSchema) {
      super(pOutputSchema, false, "EntityPaintingFieldsRenameFix", References.ENTITY, "minecraft:painting");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_216610_) {
      return this.renameField(this.renameField(p_216610_, "Motive", "variant"), "Facing", "facing");
   }

   private Dynamic<?> renameField(Dynamic<?> pDynamic, String pOldName, String pNewName) {
      Optional<? extends Dynamic<?>> optional = pDynamic.get(pOldName).result();
      Optional<? extends Dynamic<?>> optional1 = optional.map((p_216619_) -> {
         return pDynamic.remove(pOldName).set(pNewName, p_216619_);
      });
      return DataFixUtils.orElse(optional1, pDynamic);
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}