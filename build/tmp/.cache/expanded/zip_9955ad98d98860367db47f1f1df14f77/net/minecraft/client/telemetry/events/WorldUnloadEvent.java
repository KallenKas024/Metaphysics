package net.minecraft.client.telemetry.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldUnloadEvent {
   private static final int NOT_TRACKING_TIME = -1;
   private Optional<Instant> worldLoadedTime = Optional.empty();
   private long totalTicks;
   private long lastGameTime;

   public void onPlayerInfoReceived() {
      this.lastGameTime = -1L;
      if (this.worldLoadedTime.isEmpty()) {
         this.worldLoadedTime = Optional.of(Instant.now());
      }

   }

   public void setTime(long pTime) {
      if (this.lastGameTime != -1L) {
         this.totalTicks += Math.max(0L, pTime - this.lastGameTime);
      }

      this.lastGameTime = pTime;
   }

   private int getTimeInSecondsSinceLoad(Instant pWordLoadedTime) {
      Duration duration = Duration.between(pWordLoadedTime, Instant.now());
      return (int)duration.toSeconds();
   }

   public void send(TelemetryEventSender pSender) {
      this.worldLoadedTime.ifPresent((p_261953_) -> {
         pSender.send(TelemetryEventType.WORLD_UNLOADED, (p_261597_) -> {
            p_261597_.put(TelemetryProperty.SECONDS_SINCE_LOAD, this.getTimeInSecondsSinceLoad(p_261953_));
            p_261597_.put(TelemetryProperty.TICKS_SINCE_LOAD, (int)this.totalTicks);
         });
      });
   }
}