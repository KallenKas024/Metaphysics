package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;

public class MobSpawnerEntityIdentifiersFix extends DataFix {
   public MobSpawnerEntityIdentifiersFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
   }

   private Dynamic<?> fix(Dynamic<?> pDynamic) {
      if (!"MobSpawner".equals(pDynamic.get("id").asString(""))) {
         return pDynamic;
      } else {
         Optional<String> optional = pDynamic.get("EntityId").asString().result();
         if (optional.isPresent()) {
            Dynamic<?> dynamic = DataFixUtils.orElse(pDynamic.get("SpawnData").result(), pDynamic.emptyMap());
            dynamic = dynamic.set("id", dynamic.createString(optional.get().isEmpty() ? "Pig" : optional.get()));
            pDynamic = pDynamic.set("SpawnData", dynamic);
            pDynamic = pDynamic.remove("EntityId");
         }

         Optional<? extends Stream<? extends Dynamic<?>>> optional1 = pDynamic.get("SpawnPotentials").asStreamOpt().result();
         if (optional1.isPresent()) {
            pDynamic = pDynamic.set("SpawnPotentials", pDynamic.createList(optional1.get().map((p_16459_) -> {
               Optional<String> optional2 = p_16459_.get("Type").asString().result();
               if (optional2.isPresent()) {
                  Dynamic<?> dynamic1 = DataFixUtils.orElse(p_16459_.get("Properties").result(), p_16459_.emptyMap()).set("id", p_16459_.createString(optional2.get()));
                  return p_16459_.set("Entity", dynamic1).remove("Type").remove("Properties");
               } else {
                  return p_16459_;
               }
            })));
         }

         return pDynamic;
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
      return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(References.UNTAGGED_SPAWNER), type, (p_16455_) -> {
         Dynamic<?> dynamic = p_16455_.get(DSL.remainderFinder());
         dynamic = dynamic.set("id", dynamic.createString("MobSpawner"));
         DataResult<? extends Pair<? extends Typed<?>, ?>> dataresult = type.readTyped(this.fix(dynamic));
         return !dataresult.result().isPresent() ? p_16455_ : dataresult.result().get().getFirst();
      });
   }
}