package net.minecraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Timer {
   public float partialTick;
   public float tickDelta;
   private long lastMs;
   private final float msPerTick;

   public Timer(float pTicksPerSecond, long pLastMs) {
      this.msPerTick = 1000.0F / pTicksPerSecond;
      this.lastMs = pLastMs;
   }

   public int advanceTime(long pGameTime) {
      this.tickDelta = (float)(pGameTime - this.lastMs) / this.msPerTick;
      this.lastMs = pGameTime;
      this.partialTick += this.tickDelta;
      int i = (int)this.partialTick;
      this.partialTick -= (float)i;
      return i;
   }
}