package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2501 extends NamespacedSchema {
   public V2501(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   private static void registerFurnace(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "RecipesUsed", DSL.compoundList(References.RECIPE.in(pSchema), DSL.constType(DSL.intType())));
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      registerFurnace(pSchema, map, "minecraft:furnace");
      registerFurnace(pSchema, map, "minecraft:smoker");
      registerFurnace(pSchema, map, "minecraft:blast_furnace");
      return map;
   }
}