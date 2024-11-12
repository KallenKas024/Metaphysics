package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.function.Function;

public class AdvancementsRenameFix extends DataFix {
   private final String name;
   private final Function<String, String> renamer;

   public AdvancementsRenameFix(Schema pOutputSchema, boolean pChangesType, String pName, Function<String, String> pRenamer) {
      super(pOutputSchema, pChangesType);
      this.name = pName;
      this.renamer = pRenamer;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.ADVANCEMENTS), (p_14657_) -> {
         return p_14657_.update(DSL.remainderFinder(), (p_145063_) -> {
            return p_145063_.updateMapValues((p_145066_) -> {
               String s = p_145066_.getFirst().asString("");
               return p_145066_.mapFirst((p_145070_) -> {
                  return p_145063_.createString(this.renamer.apply(s));
               });
            });
         });
      });
   }
}