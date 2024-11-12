package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1906 extends NamespacedSchema {
   public V1906(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      registerInventory(pSchema, map, "minecraft:barrel");
      registerInventory(pSchema, map, "minecraft:smoker");
      registerInventory(pSchema, map, "minecraft:blast_furnace");
      pSchema.register(map, "minecraft:lectern", (p_17774_) -> {
         return DSL.optionalFields("Book", References.ITEM_STACK.in(pSchema));
      });
      pSchema.registerSimple(map, "minecraft:bell");
      return map;
   }

   protected static void registerInventory(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)));
      });
   }
}