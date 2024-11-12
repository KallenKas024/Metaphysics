package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1125 extends NamespacedSchema {
   public V1125(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      pSchema.registerSimple(map, "minecraft:bed");
      return map;
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      super.registerTypes(pSchema, pEntityTypes, pBlockEntityTypes);
      pSchema.registerType(false, References.ADVANCEMENTS, () -> {
         return DSL.optionalFields("minecraft:adventure/adventuring_time", DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in(pSchema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_a_mob", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_all_mobs", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.string()))), "minecraft:husbandry/bred_all_animals", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(pSchema), DSL.constType(DSL.string()))));
      });
      pSchema.registerType(false, References.BIOME, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.ENTITY_NAME, () -> {
         return DSL.constType(namespacedString());
      });
   }
}