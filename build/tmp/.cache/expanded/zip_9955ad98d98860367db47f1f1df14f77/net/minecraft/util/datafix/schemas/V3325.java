package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3325 extends NamespacedSchema {
   public V3325(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      pSchema.register(map, "minecraft:item_display", (p_270589_) -> {
         return DSL.optionalFields("item", References.ITEM_STACK.in(pSchema));
      });
      pSchema.register(map, "minecraft:block_display", (p_270174_) -> {
         return DSL.optionalFields("block_state", References.BLOCK_STATE.in(pSchema));
      });
      pSchema.registerSimple(map, "minecraft:text_display");
      return map;
   }
}