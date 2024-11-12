package net.minecraft.client.gui.components;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LerpingBossEvent extends BossEvent {
   private static final long LERP_MILLISECONDS = 100L;
   protected float targetPercent;
   protected long setTime;

   public LerpingBossEvent(UUID pId, Component pName, float pProgress, BossEvent.BossBarColor pColor, BossEvent.BossBarOverlay pOverlay, boolean pDarkenScreen, boolean pBossMusic, boolean pWorldFog) {
      super(pId, pName, pColor, pOverlay);
      this.targetPercent = pProgress;
      this.progress = pProgress;
      this.setTime = Util.getMillis();
      this.setDarkenScreen(pDarkenScreen);
      this.setPlayBossMusic(pBossMusic);
      this.setCreateWorldFog(pWorldFog);
   }

   public void setProgress(float pProgress) {
      this.progress = this.getProgress();
      this.targetPercent = pProgress;
      this.setTime = Util.getMillis();
   }

   public float getProgress() {
      long i = Util.getMillis() - this.setTime;
      float f = Mth.clamp((float)i / 100.0F, 0.0F, 1.0F);
      return Mth.lerp(f, this.progress, this.targetPercent);
   }
}