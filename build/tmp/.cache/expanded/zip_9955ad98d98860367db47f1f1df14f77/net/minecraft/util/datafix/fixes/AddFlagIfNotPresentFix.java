package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class AddFlagIfNotPresentFix extends DataFix {
   private final String name;
   private final boolean flagValue;
   private final String flagKey;
   private final DSL.TypeReference typeReference;

   public AddFlagIfNotPresentFix(Schema pOutputSchema, DSL.TypeReference pTypeReference, String pFlagKey, boolean pFlagValue) {
      super(pOutputSchema, true);
      this.flagValue = pFlagValue;
      this.flagKey = pFlagKey;
      this.name = "AddFlagIfNotPresentFix_" + this.flagKey + "=" + this.flagValue + " for " + pOutputSchema.getVersionKey();
      this.typeReference = pTypeReference;
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(this.typeReference);
      return this.fixTypeEverywhereTyped(this.name, type, (p_184815_) -> {
         return p_184815_.update(DSL.remainderFinder(), (p_184817_) -> {
            return p_184817_.set(this.flagKey, DataFixUtils.orElseGet(p_184817_.get(this.flagKey).result(), () -> {
               return p_184817_.createBoolean(this.flagValue);
            }));
         });
      });
   }
}