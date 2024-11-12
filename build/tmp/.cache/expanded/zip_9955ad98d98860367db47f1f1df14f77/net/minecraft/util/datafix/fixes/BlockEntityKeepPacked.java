package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityKeepPacked extends NamedEntityFix {
   public BlockEntityKeepPacked(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "BlockEntityKeepPacked", References.BLOCK_ENTITY, "DUMMY");
   }

   private static Dynamic<?> fixTag(Dynamic<?> p_14853_) {
      return p_14853_.set("keepPacked", p_14853_.createBoolean(true));
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), BlockEntityKeepPacked::fixTag);
   }
}