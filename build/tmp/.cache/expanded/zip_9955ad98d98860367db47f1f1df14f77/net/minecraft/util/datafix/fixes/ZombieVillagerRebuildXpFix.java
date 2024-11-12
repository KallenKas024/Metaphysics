package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;

public class ZombieVillagerRebuildXpFix extends NamedEntityFix {
   public ZombieVillagerRebuildXpFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "Zombie Villager XP rebuild", References.ENTITY, "minecraft:zombie_villager");
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), (p_17303_) -> {
         Optional<Number> optional = p_17303_.get("Xp").asNumber().result();
         if (!optional.isPresent()) {
            int i = p_17303_.get("VillagerData").get("level").asInt(1);
            return p_17303_.set("Xp", p_17303_.createInt(VillagerRebuildLevelAndXpFix.getMinXpPerLevel(i)));
         } else {
            return p_17303_;
         }
      });
   }
}