package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3078 extends NamespacedSchema {
   public V3078(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return V100.equipment(pSchema);
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      registerMob(pSchema, map, "minecraft:frog");
      registerMob(pSchema, map, "minecraft:tadpole");
      return map;
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      pSchema.register(map, "minecraft:sculk_shrieker", () -> {
         return DSL.optionalFields("listener", DSL.optionalFields("event", DSL.optionalFields("game_event", References.GAME_EVENT_NAME.in(pSchema))));
      });
      return map;
   }
}