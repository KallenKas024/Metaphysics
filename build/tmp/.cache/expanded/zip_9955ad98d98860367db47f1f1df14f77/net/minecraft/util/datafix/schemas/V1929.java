package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1929 extends NamespacedSchema {
   public V1929(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      pSchema.register(map, "minecraft:wandering_trader", (p_17818_) -> {
         return DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(pSchema), "buyB", References.ITEM_STACK.in(pSchema), "sell", References.ITEM_STACK.in(pSchema)))), V100.equipment(pSchema));
      });
      pSchema.register(map, "minecraft:trader_llama", (p_17815_) -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "SaddleItem", References.ITEM_STACK.in(pSchema), "DecorItem", References.ITEM_STACK.in(pSchema), V100.equipment(pSchema));
      });
      return map;
   }
}