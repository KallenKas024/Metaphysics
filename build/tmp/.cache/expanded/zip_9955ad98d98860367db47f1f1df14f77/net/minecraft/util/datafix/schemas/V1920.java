package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1920 extends NamespacedSchema {
   public V1920(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   protected static void registerInventory(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)));
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      registerInventory(pSchema, map, "minecraft:campfire");
      return map;
   }
}