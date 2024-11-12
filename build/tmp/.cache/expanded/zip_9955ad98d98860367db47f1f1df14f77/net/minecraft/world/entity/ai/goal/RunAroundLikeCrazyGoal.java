package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RunAroundLikeCrazyGoal extends Goal {
   private final AbstractHorse horse;
   private final double speedModifier;
   private double posX;
   private double posY;
   private double posZ;

   public RunAroundLikeCrazyGoal(AbstractHorse pHorse, double pSpeedModifier) {
      this.horse = pHorse;
      this.speedModifier = pSpeedModifier;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!this.horse.isTamed() && this.horse.isVehicle()) {
         Vec3 vec3 = DefaultRandomPos.getPos(this.horse, 5, 4);
         if (vec3 == null) {
            return false;
         } else {
            this.posX = vec3.x;
            this.posY = vec3.y;
            this.posZ = vec3.z;
            return true;
         }
      } else {
         return false;
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.horse.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return !this.horse.isTamed() && !this.horse.getNavigation().isDone() && this.horse.isVehicle();
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (!this.horse.isTamed() && this.horse.getRandom().nextInt(this.adjustedTickDelay(50)) == 0) {
         Entity entity = this.horse.getPassengers().get(0);
         if (entity == null) {
            return;
         }

         if (entity instanceof Player) {
            int i = this.horse.getTemper();
            int j = this.horse.getMaxTemper();
            if (j > 0 && this.horse.getRandom().nextInt(j) < i && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(horse, (Player)entity)) {
               this.horse.tameWithName((Player)entity);
               return;
            }

            this.horse.modifyTemper(5);
         }

         this.horse.ejectPassengers();
         this.horse.makeMad();
         this.horse.level().broadcastEntityEvent(this.horse, (byte)6);
      }

   }
}
