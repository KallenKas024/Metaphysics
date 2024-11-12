package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2842 extends NamespacedSchema {
   public V2842(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      super.registerTypes(pSchema, pEntityTypes, pBlockEntityTypes);
      pSchema.registerType(false, References.CHUNK, () -> {
         return DSL.optionalFields("entities", DSL.list(References.ENTITY_TREE.in(pSchema)), "block_entities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(pSchema), DSL.remainder())), "block_ticks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(pSchema))), "sections", DSL.list(DSL.optionalFields("biomes", DSL.optionalFields("palette", DSL.list(References.BIOME.in(pSchema))), "block_states", DSL.optionalFields("palette", DSL.list(References.BLOCK_STATE.in(pSchema))))), "structures", DSL.optionalFields("starts", DSL.compoundList(References.STRUCTURE_FEATURE.in(pSchema))));
      });
   }
}