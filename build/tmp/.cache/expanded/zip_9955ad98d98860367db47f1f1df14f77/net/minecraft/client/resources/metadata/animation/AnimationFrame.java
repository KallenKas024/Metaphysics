package net.minecraft.client.resources.metadata.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationFrame {
   public static final int UNKNOWN_FRAME_TIME = -1;
   private final int index;
   private final int time;

   public AnimationFrame(int pIndex) {
      this(pIndex, -1);
   }

   public AnimationFrame(int pIndex, int pTime) {
      this.index = pIndex;
      this.time = pTime;
   }

   public int getTime(int pDefaultValue) {
      return this.time == -1 ? pDefaultValue : this.time;
   }

   public int getIndex() {
      return this.index;
   }
}