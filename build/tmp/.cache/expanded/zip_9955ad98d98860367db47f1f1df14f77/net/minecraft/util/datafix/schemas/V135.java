package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V135 extends Schema {
   public V135(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      super.registerTypes(pSchema, pEntityTypes, pBlockEntityTypes);
      pSchema.registerType(false, References.PLAYER, () -> {
         return DSL.optionalFields("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(pSchema)), "Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "EnderItems", DSL.list(References.ITEM_STACK.in(pSchema)));
      });
      pSchema.registerType(true, References.ENTITY_TREE, () -> {
         return DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(pSchema)), References.ENTITY.in(pSchema));
      });
   }
}