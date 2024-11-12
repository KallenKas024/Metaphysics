package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class DefendVillageTargetGoal extends TargetGoal {
   private final IronGolem golem;
   @Nullable
   private LivingEntity potentialTarget;
   private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);

   public DefendVillageTargetGoal(IronGolem pGolem) {
      super(pGolem, false, true);
      this.golem = pGolem;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      AABB aabb = this.golem.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
      List<? extends LivingEntity> list = this.golem.level().getNearbyEntities(Villager.class, this.attackTargeting, this.golem, aabb);
      List<Player> list1 = this.golem.level().getNearbyPlayers(this.attackTargeting, this.golem, aabb);

      for(LivingEntity livingentity : list) {
         Villager villager = (Villager)livingentity;

         for(Player player : list1) {
            int i = villager.getPlayerReputation(player);
            if (i <= -100) {
               this.potentialTarget = player;
            }
         }
      }

      if (this.potentialTarget == null) {
         return false;
      } else {
         return !(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player)this.potentialTarget).isCreative();
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.golem.setTarget(this.potentialTarget);
      super.start();
   }
}