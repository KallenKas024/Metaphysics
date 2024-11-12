package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1466 extends NamespacedSchema {
   public V1466(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      super.registerTypes(pSchema, pEntityTypes, pBlockEntityTypes);
      pSchema.registerType(false, References.CHUNK, () -> {
         return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(pSchema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(pSchema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(pSchema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in(pSchema)))), "Structures", DSL.optionalFields("Starts", DSL.compoundList(References.STRUCTURE_FEATURE.in(pSchema)))));
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(pSchema);
      map.put("DUMMY", DSL::remainder);
      return map;
   }
}