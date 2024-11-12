package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAmbientOcclusionFix extends DataFix {
   public OptionsAmbientOcclusionFix(Schema pOutputSchema) {
      super(pOutputSchema, false);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("OptionsAmbientOcclusionFix", this.getInputSchema().getType(References.OPTIONS), (p_263493_) -> {
         return p_263493_.update(DSL.remainderFinder(), (p_263531_) -> {
            return DataFixUtils.orElse(p_263531_.get("ao").asString().map((p_263546_) -> {
               return p_263531_.set("ao", p_263531_.createString(updateValue(p_263546_)));
            }).result(), p_263531_);
         });
      });
   }

   private static String updateValue(String pOldValue) {
      String s;
      switch (pOldValue) {
         case "0":
            s = "false";
            break;
         case "1":
         case "2":
            s = "true";
            break;
         default:
            s = pOldValue;
      }

      return s;
   }
}