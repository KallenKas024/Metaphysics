package net.minecraft.world.entity.player;

import net.minecraft.nbt.CompoundTag;

public class Abilities {
   public boolean invulnerable;
   public boolean flying;
   public boolean mayfly;
   public boolean instabuild;
   public boolean mayBuild = true;
   private float flyingSpeed = 0.05F;
   private float walkingSpeed = 0.1F;

   public void addSaveData(CompoundTag pCompound) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putBoolean("invulnerable", this.invulnerable);
      compoundtag.putBoolean("flying", this.flying);
      compoundtag.putBoolean("mayfly", this.mayfly);
      compoundtag.putBoolean("instabuild", this.instabuild);
      compoundtag.putBoolean("mayBuild", this.mayBuild);
      compoundtag.putFloat("flySpeed", this.flyingSpeed);
      compoundtag.putFloat("walkSpeed", this.walkingSpeed);
      pCompound.put("abilities", compoundtag);
   }

   public void loadSaveData(CompoundTag pCompound) {
      if (pCompound.contains("abilities", 10)) {
         CompoundTag compoundtag = pCompound.getCompound("abilities");
         this.invulnerable = compoundtag.getBoolean("invulnerable");
         this.flying = compoundtag.getBoolean("flying");
         this.mayfly = compoundtag.getBoolean("mayfly");
         this.instabuild = compoundtag.getBoolean("instabuild");
         if (compoundtag.contains("flySpeed", 99)) {
            this.flyingSpeed = compoundtag.getFloat("flySpeed");
            this.walkingSpeed = compoundtag.getFloat("walkSpeed");
         }

         if (compoundtag.contains("mayBuild", 1)) {
            this.mayBuild = compoundtag.getBoolean("mayBuild");
         }
      }

   }

   public float getFlyingSpeed() {
      return this.flyingSpeed;
   }

   public void setFlyingSpeed(float pFlyingSpeed) {
      this.flyingSpeed = pFlyingSpeed;
   }

   public float getWalkingSpeed() {
      return this.walkingSpeed;
   }

   public void setWalkingSpeed(float pWalkingSpeed) {
      this.walkingSpeed = pWalkingSpeed;
   }
}