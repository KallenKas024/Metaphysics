package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.RandomSource;

public class EntityZombieVillagerTypeFix extends NamedEntityFix {
   private static final int PROFESSION_MAX = 6;
   private static final RandomSource RANDOM = RandomSource.create();

   public EntityZombieVillagerTypeFix(Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType, "EntityZombieVillagerTypeFix", References.ENTITY, "Zombie");
   }

   public Dynamic<?> fixTag(Dynamic<?> p_15813_) {
      if (p_15813_.get("IsVillager").asBoolean(false)) {
         if (!p_15813_.get("ZombieType").result().isPresent()) {
            int i = this.getVillagerProfession(p_15813_.get("VillagerProfession").asInt(-1));
            if (i == -1) {
               i = this.getVillagerProfession(RANDOM.nextInt(6));
            }

            p_15813_ = p_15813_.set("ZombieType", p_15813_.createInt(i));
         }

         p_15813_ = p_15813_.remove("IsVillager");
      }

      return p_15813_;
   }

   private int getVillagerProfession(int pVillagerProfession) {
      return pVillagerProfession >= 0 && pVillagerProfession < 6 ? pVillagerProfession : -1;
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), this::fixTag);
   }
}