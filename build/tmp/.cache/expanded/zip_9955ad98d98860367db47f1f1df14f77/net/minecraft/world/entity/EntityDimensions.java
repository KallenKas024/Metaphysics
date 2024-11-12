package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityDimensions {
   public final float width;
   public final float height;
   public final boolean fixed;

   public EntityDimensions(float pWidth, float pHeight, boolean pFixed) {
      this.width = pWidth;
      this.height = pHeight;
      this.fixed = pFixed;
   }

   public AABB makeBoundingBox(Vec3 pPos) {
      return this.makeBoundingBox(pPos.x, pPos.y, pPos.z);
   }

   public AABB makeBoundingBox(double pX, double pY, double pZ) {
      float f = this.width / 2.0F;
      float f1 = this.height;
      return new AABB(pX - (double)f, pY, pZ - (double)f, pX + (double)f, pY + (double)f1, pZ + (double)f);
   }

   public EntityDimensions scale(float pFactor) {
      return this.scale(pFactor, pFactor);
   }

   public EntityDimensions scale(float pWidthFactor, float pHeightFactor) {
      return !this.fixed && (pWidthFactor != 1.0F || pHeightFactor != 1.0F) ? scalable(this.width * pWidthFactor, this.height * pHeightFactor) : this;
   }

   public static EntityDimensions scalable(float pWidth, float pHeight) {
      return new EntityDimensions(pWidth, pHeight, false);
   }

   public static EntityDimensions fixed(float pWidth, float pHeight) {
      return new EntityDimensions(pWidth, pHeight, true);
   }

   public String toString() {
      return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
   }
}