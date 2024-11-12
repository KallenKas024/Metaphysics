package net.minecraft.world.level.portal;

import net.minecraft.world.phys.Vec3;

public class PortalInfo {
   public final Vec3 pos;
   public final Vec3 speed;
   public final float yRot;
   public final float xRot;

   public PortalInfo(Vec3 pPos, Vec3 pSpeed, float pYRot, float pXRot) {
      this.pos = pPos;
      this.speed = pSpeed;
      this.yRot = pYRot;
      this.xRot = pXRot;
   }
}