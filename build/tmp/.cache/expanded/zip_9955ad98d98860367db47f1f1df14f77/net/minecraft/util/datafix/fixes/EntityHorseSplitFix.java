package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityHorseSplitFix extends EntityRenameFix {
   public EntityHorseSplitFix(Schema pOutputSchema, boolean pChangesType) {
      super("EntityHorseSplitFix", pOutputSchema, pChangesType);
   }

   protected Pair<String, Typed<?>> fix(String pEntityName, Typed<?> pTyped) {
      Dynamic<?> dynamic = pTyped.get(DSL.remainderFinder());
      if (Objects.equals("EntityHorse", pEntityName)) {
         int i = dynamic.get("Type").asInt(0);
         String s;
         switch (i) {
            case 0:
            default:
               s = "Horse";
               break;
            case 1:
               s = "Donkey";
               break;
            case 2:
               s = "Mule";
               break;
            case 3:
               s = "ZombieHorse";
               break;
            case 4:
               s = "SkeletonHorse";
         }

         dynamic.remove("Type");
         Type<?> type = this.getOutputSchema().findChoiceType(References.ENTITY).types().get(s);
         return Pair.of(s, (Typed<?>)((Pair)((com.mojang.serialization.DataResult<Dynamic<?>>)pTyped.write()).flatMap(type::readTyped).result().orElseThrow(() -> {
            return new IllegalStateException("Could not parse the new horse");
         })).getFirst());
      } else {
         return Pair.of(pEntityName, pTyped);
      }
   }
}