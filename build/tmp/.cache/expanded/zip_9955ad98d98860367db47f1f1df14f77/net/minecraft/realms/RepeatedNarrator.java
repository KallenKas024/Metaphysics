package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RepeatedNarrator {
   private final float permitsPerSecond;
   private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference<>();

   public RepeatedNarrator(Duration pDuration) {
      this.permitsPerSecond = 1000.0F / (float)pDuration.toMillis();
   }

   public void narrate(GameNarrator pNarrator, Component pNarration) {
      RepeatedNarrator.Params repeatednarrator$params = this.params.updateAndGet((p_175080_) -> {
         return p_175080_ != null && pNarration.equals(p_175080_.narration) ? p_175080_ : new RepeatedNarrator.Params(pNarration, RateLimiter.create((double)this.permitsPerSecond));
      });
      if (repeatednarrator$params.rateLimiter.tryAcquire(1)) {
         pNarrator.sayNow(pNarration);
      }

   }

   @OnlyIn(Dist.CLIENT)
   static class Params {
      final Component narration;
      final RateLimiter rateLimiter;

      Params(Component pNarration, RateLimiter pRateLimiter) {
         this.narration = pNarration;
         this.rateLimiter = pRateLimiter;
      }
   }
}