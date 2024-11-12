package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityCatSplitFix extends SimpleEntityRenameFix {
   public EntityCatSplitFix(Schema pOutputSchema, boolean pChangesType) {
      super("EntityCatSplitFix", pOutputSchema, pChangesType);
   }

   protected Pair<String, Dynamic<?>> getNewNameAndTag(String pName, Dynamic<?> pTag) {
      if (Objects.equals("minecraft:ocelot", pName)) {
         int i = pTag.get("CatType").asInt(0);
         if (i == 0) {
            String s = pTag.get("Owner").asString("");
            String s1 = pTag.get("OwnerUUID").asString("");
            if (s.length() > 0 || s1.length() > 0) {
               pTag.set("Trusting", pTag.createBoolean(true));
            }
         } else if (i > 0 && i < 4) {
            pTag = pTag.set("CatType", pTag.createInt(i));
            pTag = pTag.set("OwnerUUID", pTag.createString(pTag.get("OwnerUUID").asString("")));
            return Pair.of("minecraft:cat", pTag);
         }
      }

      return Pair.of(pName, pTag);
   }
}