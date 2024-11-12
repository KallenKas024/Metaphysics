package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;

public class EntityTippedArrowFix extends SimplestEntityRenameFix {
   public EntityTippedArrowFix(Schema pOutputSchema, boolean pChangesType) {
      super("EntityTippedArrowFix", pOutputSchema, pChangesType);
   }

   protected String rename(String pName) {
      return Objects.equals(pName, "TippedArrow") ? "Arrow" : pName;
   }
}