package net.minecraft.world.entity.schedule;

public class Keyframe {
   private final int timeStamp;
   private final float value;

   public Keyframe(int pTimestamp, float pValue) {
      this.timeStamp = pTimestamp;
      this.value = pValue;
   }

   public int getTimeStamp() {
      return this.timeStamp;
   }

   public float getValue() {
      return this.value;
   }
}