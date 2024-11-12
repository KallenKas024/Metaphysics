package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class GossipUUIDFix extends NamedEntityFix {
   public GossipUUIDFix(Schema pOutputSchema, String pEntityName) {
      super(pOutputSchema, false, "Gossip for for " + pEntityName, References.ENTITY, pEntityName);
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), (p_15883_) -> {
         return p_15883_.update("Gossips", (p_145376_) -> {
            return DataFixUtils.orElse(p_145376_.asStreamOpt().result().map((p_145374_) -> {
               return p_145374_.map((p_145378_) -> {
                  return AbstractUUIDFix.replaceUUIDLeastMost(p_145378_, "Target", "Target").orElse(p_145378_);
               });
            }).map(p_145376_::createList), p_145376_);
         });
      });
   }
}