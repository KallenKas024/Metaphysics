package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;

public class EntityRedundantChanceTagsFix extends DataFix {
   private static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();

   public EntityRedundantChanceTagsFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("EntityRedundantChanceTagsFix", this.getInputSchema().getType(References.ENTITY), (p_15607_) -> {
         return p_15607_.update(DSL.remainderFinder(), (p_145304_) -> {
            if (isZeroList(p_145304_.get("HandDropChances"), 2)) {
               p_145304_ = p_145304_.remove("HandDropChances");
            }

            if (isZeroList(p_145304_.get("ArmorDropChances"), 4)) {
               p_145304_ = p_145304_.remove("ArmorDropChances");
            }

            return p_145304_;
         });
      });
   }

   private static boolean isZeroList(OptionalDynamic<?> pList, int pSize) {
      return pList.flatMap(FLOAT_LIST_CODEC::parse).map((p_15605_) -> {
         return p_15605_.size() == pSize && p_15605_.stream().allMatch((p_145306_) -> {
            return p_145306_ == 0.0F;
         });
      }).result().orElse(false);
   }
}