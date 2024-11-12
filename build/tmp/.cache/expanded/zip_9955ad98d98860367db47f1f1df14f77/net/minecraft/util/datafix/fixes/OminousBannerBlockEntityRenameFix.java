package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class OminousBannerBlockEntityRenameFix extends NamedEntityFix {
   public OminousBannerBlockEntityRenameFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "OminousBannerBlockEntityRenameFix", References.BLOCK_ENTITY, "minecraft:banner");
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }

   private Dynamic<?> fixTag(Dynamic<?> p_16553_) {
      Optional<String> optional = p_16553_.get("CustomName").asString().result();
      if (optional.isPresent()) {
         String s = optional.get();
         s = s.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
         return p_16553_.set("CustomName", p_16553_.createString(s));
      } else {
         return p_16553_;
      }
   }
}