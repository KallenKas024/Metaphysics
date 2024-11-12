package net.minecraft.world.entity.ai.sensing;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class Sensing {
   private final Mob mob;
   private final IntSet seen = new IntOpenHashSet();
   private final IntSet unseen = new IntOpenHashSet();

   public Sensing(Mob pMob) {
      this.mob = pMob;
   }

   /**
    * Clears seen and unseen.
    */
   public void tick() {
      this.seen.clear();
      this.unseen.clear();
   }

   /**
    * Updates list of visible and not visible entities for the given entity
    */
   public boolean hasLineOfSight(Entity pEntity) {
      int i = pEntity.getId();
      if (this.seen.contains(i)) {
         return true;
      } else if (this.unseen.contains(i)) {
         return false;
      } else {
         this.mob.level().getProfiler().push("hasLineOfSight");
         boolean flag = this.mob.hasLineOfSight(pEntity);
         this.mob.level().getProfiler().pop();
         if (flag) {
            this.seen.add(i);
         } else {
            this.unseen.add(i);
         }

         return flag;
      }
   }
}