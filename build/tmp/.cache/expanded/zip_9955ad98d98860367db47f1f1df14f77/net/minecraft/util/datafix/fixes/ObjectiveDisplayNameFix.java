package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.network.chat.Component;

public class ObjectiveDisplayNameFix extends DataFix {
   public ObjectiveDisplayNameFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.OBJECTIVE);
      return this.fixTypeEverywhereTyped("ObjectiveDisplayNameFix", type, (p_181039_) -> {
         return p_181039_.update(DSL.remainderFinder(), (p_145556_) -> {
            return p_145556_.update("DisplayName", (p_145559_) -> {
               return DataFixUtils.orElse(p_145559_.asString().map((p_145561_) -> {
                  return Component.Serializer.toJson(Component.literal(p_145561_));
               }).map(p_145556_::createString).result(), p_145559_);
            });
         });
      });
   }
}