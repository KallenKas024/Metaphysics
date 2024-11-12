package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityZombieSplitFix extends SimpleEntityRenameFix {
   public EntityZombieSplitFix(Schema pOutputSchema, boolean pChangesType) {
      super("EntityZombieSplitFix", pOutputSchema, pChangesType);
   }

   protected Pair<String, Dynamic<?>> getNewNameAndTag(String pName, Dynamic<?> pTag) {
      if (Objects.equals("Zombie", pName)) {
         String s = "Zombie";
         int i = pTag.get("ZombieType").asInt(0);
         switch (i) {
            case 0:
            default:
               break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
               s = "ZombieVillager";
               pTag = pTag.set("Profession", pTag.createInt(i - 1));
               break;
            case 6:
               s = "Husk";
         }

         pTag = pTag.remove("ZombieType");
         return Pair.of(s, pTag);
      } else {
         return Pair.of(pName, pTag);
      }
   }
}