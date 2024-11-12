package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityBannerColorFix extends NamedEntityFix {
   public BlockEntityBannerColorFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "BlockEntityBannerColorFix", References.BLOCK_ENTITY, "minecraft:banner");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_14798_) {
      p_14798_ = p_14798_.update("Base", (p_14808_) -> {
         return p_14808_.createInt(15 - p_14808_.asInt(0));
      });
      return p_14798_.update("Patterns", (p_14802_) -> {
         return DataFixUtils.orElse(p_14802_.asStreamOpt().map((p_145125_) -> {
            return p_145125_.map((p_145127_) -> {
               return p_145127_.update("Color", (p_145129_) -> {
                  return p_145129_.createInt(15 - p_145129_.asInt(0));
               });
            });
         }).map(p_14802_::createList).result(), p_14802_);
      });
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}