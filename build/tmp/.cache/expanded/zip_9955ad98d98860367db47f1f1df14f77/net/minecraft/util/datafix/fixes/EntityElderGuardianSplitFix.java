package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityElderGuardianSplitFix extends SimpleEntityRenameFix {
   public EntityElderGuardianSplitFix(Schema pOutputSchema, boolean pChangesType) {
      super("EntityElderGuardianSplitFix", pOutputSchema, pChangesType);
   }

   protected Pair<String, Dynamic<?>> getNewNameAndTag(String pName, Dynamic<?> pTag) {
      return Pair.of(Objects.equals(pName, "Guardian") && pTag.get("Elder").asBoolean(false) ? "ElderGuardian" : pName, pTag);
   }
}