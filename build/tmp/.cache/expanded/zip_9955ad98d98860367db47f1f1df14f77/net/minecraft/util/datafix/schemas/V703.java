package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V703 extends Schema {
   public V703(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      map.remove("EntityHorse");
      pSchema.register(map, "Horse", () -> {
         return DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(pSchema), "SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      pSchema.register(map, "Donkey", () -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      pSchema.register(map, "Mule", () -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      pSchema.register(map, "ZombieHorse", () -> {
         return DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      pSchema.register(map, "SkeletonHorse", () -> {
         return DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      return map;
   }
}