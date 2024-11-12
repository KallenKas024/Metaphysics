package net.minecraft.world.entity;

import net.minecraft.util.Mth;

public class WalkAnimationState {
   private float speedOld;
   private float speed;
   private float position;

   public void setSpeed(float pSpeed) {
      this.speed = pSpeed;
   }

   public void update(float pNewSpeed, float pPartialTick) {
      this.speedOld = this.speed;
      this.speed += (pNewSpeed - this.speed) * pPartialTick;
      this.position += this.speed;
   }

   public float speed() {
      return this.speed;
   }

   public float speed(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.speedOld, this.speed);
   }

   public float position() {
      return this.position;
   }

   public float position(float pPartialTick) {
      return this.position - this.speed * (1.0F - pPartialTick);
   }

   public boolean isMoving() {
      return this.speed > 1.0E-5F;
   }
}