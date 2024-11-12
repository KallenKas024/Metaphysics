package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlendingDataFix extends DataFix {
   private final String name;
   private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of("minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes");

   public BlendingDataFix(Schema pOutputSchema) {
      super(pOutputSchema, false);
      this.name = "Blending Data Fix v" + pOutputSchema.getVersionKey();
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(References.CHUNK);
      return this.fixTypeEverywhereTyped(this.name, type, (p_216563_) -> {
         return p_216563_.update(DSL.remainderFinder(), (p_240248_) -> {
            return updateChunkTag(p_240248_, p_240248_.get("__context"));
         });
      });
   }

   private static Dynamic<?> updateChunkTag(Dynamic<?> pChunkTag, OptionalDynamic<?> pContext) {
      pChunkTag = pChunkTag.remove("blending_data");
      boolean flag = "minecraft:overworld".equals(pContext.get("dimension").asString().result().orElse(""));
      Optional<? extends Dynamic<?>> optional = pChunkTag.get("Status").result();
      if (flag && optional.isPresent()) {
         String s = NamespacedSchema.ensureNamespaced(optional.get().asString("empty"));
         Optional<? extends Dynamic<?>> optional1 = pChunkTag.get("below_zero_retrogen").result();
         if (!STATUSES_TO_SKIP_BLENDING.contains(s)) {
            pChunkTag = updateBlendingData(pChunkTag, 384, -64);
         } else if (optional1.isPresent()) {
            Dynamic<?> dynamic = optional1.get();
            String s1 = NamespacedSchema.ensureNamespaced(dynamic.get("target_status").asString("empty"));
            if (!STATUSES_TO_SKIP_BLENDING.contains(s1)) {
               pChunkTag = updateBlendingData(pChunkTag, 256, 0);
            }
         }
      }

      return pChunkTag;
   }

   private static Dynamic<?> updateBlendingData(Dynamic<?> pChunkTag, int pMaxY, int pMinY) {
      return pChunkTag.set("blending_data", pChunkTag.createMap(Map.of(pChunkTag.createString("min_section"), pChunkTag.createInt(SectionPos.blockToSectionCoord(pMinY)), pChunkTag.createString("max_section"), pChunkTag.createInt(SectionPos.blockToSectionCoord(pMinY + pMaxY)))));
   }
}