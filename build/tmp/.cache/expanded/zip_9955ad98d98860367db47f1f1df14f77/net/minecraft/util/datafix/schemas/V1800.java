package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1800 extends NamespacedSchema {
   public V1800(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      pSchema.register(map, "minecraft:panda", () -> {
         return V100.equipment(pSchema);
      });
      pSchema.register(map, "minecraft:pillager", (p_17738_) -> {
         return DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), V100.equipment(pSchema));
      });
      return map;
   }
}