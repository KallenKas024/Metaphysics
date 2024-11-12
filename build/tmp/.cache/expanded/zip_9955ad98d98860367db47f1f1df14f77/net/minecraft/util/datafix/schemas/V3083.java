package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3083 extends NamespacedSchema {
   public V3083(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return DSL.optionalFields("ArmorItems", DSL.list(References.ITEM_STACK.in(pSchema)), "HandItems", DSL.list(References.ITEM_STACK.in(pSchema)), "listener", DSL.optionalFields("event", DSL.optionalFields("game_event", References.GAME_EVENT_NAME.in(pSchema))));
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      registerMob(pSchema, map, "minecraft:allay");
      return map;
   }
}