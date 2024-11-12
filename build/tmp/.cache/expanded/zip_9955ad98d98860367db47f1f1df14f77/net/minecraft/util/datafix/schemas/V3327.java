package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3327 extends NamespacedSchema {
   public V3327(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      pSchema.register(map, "minecraft:decorated_pot", () -> {
         return DSL.optionalFields("shards", DSL.list(References.ITEM_NAME.in(pSchema)));
      });
      pSchema.register(map, "minecraft:suspicious_sand", () -> {
         return DSL.optionalFields("item", References.ITEM_STACK.in(pSchema));
      });
      return map;
   }
}