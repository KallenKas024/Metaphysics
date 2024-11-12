package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsRenameFieldFix extends DataFix {
   private final String fixName;
   private final String fieldFrom;
   private final String fieldTo;

   public OptionsRenameFieldFix(Schema pOutputSchema, boolean pChangesType, String pFixName, String pFieldFrom, String pFieldTo) {
      super(pOutputSchema, pChangesType);
      this.fixName = pFixName;
      this.fieldFrom = pFieldFrom;
      this.fieldTo = pFieldTo;
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(this.fixName, this.getInputSchema().getType(References.OPTIONS), (p_16676_) -> {
         return p_16676_.update(DSL.remainderFinder(), (p_145592_) -> {
            return DataFixUtils.orElse(p_145592_.get(this.fieldFrom).result().map((p_145595_) -> {
               return p_145592_.set(this.fieldTo, p_145595_).remove(this.fieldFrom);
            }), p_145592_);
         });
      });
   }
}