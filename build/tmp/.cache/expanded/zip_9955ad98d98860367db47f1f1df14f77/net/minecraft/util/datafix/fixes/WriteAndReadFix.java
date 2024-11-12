package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class WriteAndReadFix extends DataFix {
   private final String name;
   private final DSL.TypeReference type;

   public WriteAndReadFix(Schema pOutputSchema, String pName, DSL.TypeReference pType) {
      super(pOutputSchema, true);
      this.name = pName;
      this.type = pType;
   }

   protected TypeRewriteRule makeRule() {
      return this.writeAndRead(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type));
   }
}