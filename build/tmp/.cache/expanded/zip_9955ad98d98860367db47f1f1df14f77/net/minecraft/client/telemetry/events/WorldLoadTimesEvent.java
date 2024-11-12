package net.minecraft.client.telemetry.events;

import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldLoadTimesEvent {
   private final boolean newWorld;
   @Nullable
   private final Duration worldLoadDuration;

   public WorldLoadTimesEvent(boolean pNewWorld, @Nullable Duration pWordLoadDuration) {
      this.worldLoadDuration = pWordLoadDuration;
      this.newWorld = pNewWorld;
   }

   public void send(TelemetryEventSender pSender) {
      if (this.worldLoadDuration != null) {
         pSender.send(TelemetryEventType.WORLD_LOAD_TIMES, (p_261740_) -> {
            p_261740_.put(TelemetryProperty.WORLD_LOAD_TIME_MS, (int)this.worldLoadDuration.toMillis());
            p_261740_.put(TelemetryProperty.NEW_WORLD, this.newWorld);
         });
      }

   }
}