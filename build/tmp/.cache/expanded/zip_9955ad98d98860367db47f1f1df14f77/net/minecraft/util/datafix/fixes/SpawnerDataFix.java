package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;

public class SpawnerDataFix extends DataFix {
   public SpawnerDataFix(Schema pOutputSchema) {
      super(pOutputSchema, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.UNTAGGED_SPAWNER);
      Type<?> type1 = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
      OpticFinder<?> opticfinder = type.findField("SpawnData");
      Type<?> type2 = type1.findField("SpawnData").type();
      OpticFinder<?> opticfinder1 = type.findField("SpawnPotentials");
      Type<?> type3 = type1.findField("SpawnPotentials").type();
      return this.fixTypeEverywhereTyped("Fix mob spawner data structure", type, type1, (p_185139_) -> {
         return p_185139_.updateTyped(opticfinder, type2, (p_185154_) -> {
            return this.wrapEntityToSpawnData(type2, p_185154_);
         }).updateTyped(opticfinder1, type3, (p_185151_) -> {
            return this.wrapSpawnPotentialsToWeightedEntries(type3, p_185151_);
         });
      });
   }

   private <T> Typed<T> wrapEntityToSpawnData(Type<T> pType, Typed<?> pTyped) {
      DynamicOps<?> dynamicops = pTyped.getOps();
      return new Typed<>(pType, dynamicops, (T)Pair.<Object, Dynamic<?>>of(pTyped.getValue(), new Dynamic<>(dynamicops)));
   }

   private <T> Typed<T> wrapSpawnPotentialsToWeightedEntries(Type<T> pType, Typed<?> pTyped) {
      DynamicOps<?> dynamicops = pTyped.getOps();
      List<?> list = (List)pTyped.getValue();
      List<?> list1 = list.stream().map((p_185145_) -> {
         Pair<Object, Dynamic<?>> pair = (Pair)p_185145_;
         int i = pair.getSecond().get("Weight").asNumber().result().orElse(1).intValue();
         Dynamic<?> dynamic = new Dynamic<>(dynamicops);
         dynamic = dynamic.set("weight", dynamic.createInt(i));
         Dynamic<?> dynamic1 = pair.getSecond().remove("Weight").remove("Entity");
         return Pair.of(Pair.of(pair.getFirst(), dynamic1), dynamic);
      }).toList();
      return new Typed<>(pType, dynamicops, (T)list1);
   }
}