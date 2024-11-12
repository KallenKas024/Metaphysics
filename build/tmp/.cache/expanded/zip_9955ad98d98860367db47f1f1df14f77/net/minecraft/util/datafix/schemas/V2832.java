package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2832 extends NamespacedSchema {
   public V2832(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> pEntityTypes, Map<String, Supplier<TypeTemplate>> pBlockEntityTypes) {
      super.registerTypes(pSchema, pEntityTypes, pBlockEntityTypes);
      pSchema.registerType(false, References.CHUNK, () -> {
         return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(pSchema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(pSchema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(pSchema))), "Sections", DSL.list(DSL.optionalFields("biomes", DSL.optionalFields("palette", DSL.list(References.BIOME.in(pSchema))), "block_states", DSL.optionalFields("palette", DSL.list(References.BLOCK_STATE.in(pSchema))))), "Structures", DSL.optionalFields("Starts", DSL.compoundList(References.STRUCTURE_FEATURE.in(pSchema)))));
      });
      pSchema.registerType(false, References.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, () -> {
         return DSL.constType(namespacedString());
      });
      pSchema.registerType(false, References.WORLD_GEN_SETTINGS, () -> {
         return DSL.fields("dimensions", DSL.compoundList(DSL.constType(namespacedString()), DSL.fields("generator", DSL.or(DSL.taggedChoiceLazy("type", DSL.string(), ImmutableMap.of("minecraft:debug", DSL::remainder, "minecraft:flat", () -> {
            return DSL.optionalFields("settings", DSL.optionalFields("biome", References.BIOME.in(pSchema), "layers", DSL.list(DSL.optionalFields("block", References.BLOCK_NAME.in(pSchema)))));
         }, "minecraft:noise", () -> {
            return DSL.optionalFields("biome_source", DSL.taggedChoiceLazy("type", DSL.string(), ImmutableMap.of("minecraft:fixed", () -> {
               return DSL.fields("biome", References.BIOME.in(pSchema));
            }, "minecraft:multi_noise", () -> {
               return DSL.or(DSL.fields("preset", References.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST.in(pSchema)), DSL.list(DSL.fields("biome", References.BIOME.in(pSchema))));
            }, "minecraft:checkerboard", () -> {
               return DSL.fields("biomes", DSL.list(References.BIOME.in(pSchema)));
            }, "minecraft:the_end", DSL::remainder)), "settings", DSL.or(DSL.constType(DSL.string()), DSL.optionalFields("default_block", References.BLOCK_NAME.in(pSchema), "default_fluid", References.BLOCK_NAME.in(pSchema))));
         })), DSL.remainder()))));
      });
   }
}
