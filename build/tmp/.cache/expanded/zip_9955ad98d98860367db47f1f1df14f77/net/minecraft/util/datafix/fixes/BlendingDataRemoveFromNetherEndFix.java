package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;

public class BlendingDataRemoveFromNetherEndFix extends DataFix {
   public BlendingDataRemoveFromNetherEndFix(Schema pOutputSchema) {
      super(pOutputSchema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(References.CHUNK);
      return this.fixTypeEverywhereTyped("BlendingDataRemoveFromNetherEndFix", type, (p_240286_) -> {
         return p_240286_.update(DSL.remainderFinder(), (p_240254_) -> {
            return updateChunkTag(p_240254_, p_240254_.get("__context"));
         });
      });
   }

   private static Dynamic<?> updateChunkTag(Dynamic<?> pChunkTag, OptionalDynamic<?> pContext) {
      boolean flag = "minecraft:overworld".equals(pContext.get("dimension").asString().result().orElse(""));
      return flag ? pChunkTag : pChunkTag.remove("blending_data");
   }
}