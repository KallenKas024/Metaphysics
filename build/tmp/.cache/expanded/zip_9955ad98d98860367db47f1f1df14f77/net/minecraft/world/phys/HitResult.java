package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public abstract class HitResult {
   protected final Vec3 location;

   protected HitResult(Vec3 pLocation) {
      this.location = pLocation;
   }

   public double distanceTo(Entity pEntity) {
      double d0 = this.location.x - pEntity.getX();
      double d1 = this.location.y - pEntity.getY();
      double d2 = this.location.z - pEntity.getZ();
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public abstract HitResult.Type getType();

   /**
    * Returns the hit position of the raycast, in absolute world coordinates
    */
   public Vec3 getLocation() {
      return this.location;
   }

   public static enum Type {
      MISS,
      BLOCK,
      ENTITY;
   }
}