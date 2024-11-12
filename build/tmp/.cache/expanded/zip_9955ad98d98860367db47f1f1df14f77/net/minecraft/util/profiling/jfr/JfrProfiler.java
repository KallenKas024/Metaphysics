package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.NetworkSummaryEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class JfrProfiler implements JvmProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String ROOT_CATEGORY = "Minecraft";
   public static final String WORLD_GEN_CATEGORY = "World Generation";
   public static final String TICK_CATEGORY = "Ticking";
   public static final String NETWORK_CATEGORY = "Network";
   private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(ChunkGenerationEvent.class, PacketReceivedEvent.class, PacketSentEvent.class, NetworkSummaryEvent.class, ServerTickTimeEvent.class, WorldLoadFinishedEvent.class);
   private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
   private static final DateTimeFormatter DATE_TIME_FORMATTER = (new DateTimeFormatterBuilder()).appendPattern("yyyy-MM-dd-HHmmss").toFormatter().withZone(ZoneId.systemDefault());
   private static final JfrProfiler INSTANCE = new JfrProfiler();
   @Nullable
   Recording recording;
   private float currentAverageTickTime;
   private final Map<String, NetworkSummaryEvent.SumAggregation> networkTrafficByAddress = new ConcurrentHashMap<>();

   private JfrProfiler() {
      CUSTOM_EVENTS.forEach(FlightRecorder::register);
      FlightRecorder.addPeriodicEvent(ServerTickTimeEvent.class, () -> {
         (new ServerTickTimeEvent(this.currentAverageTickTime)).commit();
      });
      FlightRecorder.addPeriodicEvent(NetworkSummaryEvent.class, () -> {
         Iterator<NetworkSummaryEvent.SumAggregation> iterator = this.networkTrafficByAddress.values().iterator();

         while(iterator.hasNext()) {
            iterator.next().commitEvent();
            iterator.remove();
         }

      });
   }

   public static JfrProfiler getInstance() {
      return INSTANCE;
   }

   public boolean start(Environment pEnvironment) {
      URL url = JfrProfiler.class.getResource("/flightrecorder-config.jfc");
      if (url == null) {
         LOGGER.warn("Could not find default flight recorder config at {}", (Object)"/flightrecorder-config.jfc");
         return false;
      } else {
         try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return this.start(bufferedreader, pEnvironment);
         } catch (IOException ioexception) {
            LOGGER.warn("Failed to start flight recorder using configuration at {}", url, ioexception);
            return false;
         }
      }
   }

   public Path stop() {
      if (this.recording == null) {
         throw new IllegalStateException("Not currently profiling");
      } else {
         this.networkTrafficByAddress.clear();
         Path path = this.recording.getDestination();
         this.recording.stop();
         return path;
      }
   }

   public boolean isRunning() {
      return this.recording != null;
   }

   public boolean isAvailable() {
      return FlightRecorder.isAvailable();
   }

   private boolean start(Reader pReader, Environment pEnvironment) {
      if (this.isRunning()) {
         LOGGER.warn("Profiling already in progress");
         return false;
      } else {
         try {
            Configuration configuration = Configuration.create(pReader);
            String s = DATE_TIME_FORMATTER.format(Instant.now());
            this.recording = Util.make(new Recording(configuration), (p_185311_) -> {
               CUSTOM_EVENTS.forEach(p_185311_::enable);
               p_185311_.setDumpOnExit(true);
               p_185311_.setToDisk(true);
               p_185311_.setName(String.format(Locale.ROOT, "%s-%s-%s", pEnvironment.getDescription(), SharedConstants.getCurrentVersion().getName(), s));
            });
            Path path = Paths.get(String.format(Locale.ROOT, "debug/%s-%s.jfr", pEnvironment.getDescription(), s));
            FileUtil.createDirectoriesSafe(path.getParent());
            this.recording.setDestination(path);
            this.recording.start();
            this.setupSummaryListener();
         } catch (ParseException | IOException ioexception) {
            LOGGER.warn("Failed to start jfr profiling", (Throwable)ioexception);
            return false;
         }

         LOGGER.info("Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command", this.recording.getId(), this.recording.getName(), this.recording.getDestination());
         return true;
      }
   }

   private void setupSummaryListener() {
      FlightRecorder.addListener(new FlightRecorderListener() {
         final SummaryReporter summaryReporter = new SummaryReporter(() -> {
            JfrProfiler.this.recording = null;
         });

         public void recordingStateChanged(Recording p_185339_) {
            if (p_185339_ == JfrProfiler.this.recording && p_185339_.getState() == RecordingState.STOPPED) {
               this.summaryReporter.recordingStopped(p_185339_.getDestination());
               FlightRecorder.removeListener(this);
            }
         }
      });
   }

   public void onServerTick(float pCurrentAverageTickTime) {
      if (ServerTickTimeEvent.TYPE.isEnabled()) {
         this.currentAverageTickTime = pCurrentAverageTickTime;
      }

   }

   public void onPacketReceived(int pProtocolId, int pPacketId, SocketAddress pRemoteAddress, int pBytes) {
      if (PacketReceivedEvent.TYPE.isEnabled()) {
         (new PacketReceivedEvent(pProtocolId, pPacketId, pRemoteAddress, pBytes)).commit();
      }

      if (NetworkSummaryEvent.TYPE.isEnabled()) {
         this.networkStatFor(pRemoteAddress).trackReceivedPacket(pBytes);
      }

   }

   public void onPacketSent(int pProtocolId, int pPacketId, SocketAddress pRemoteAddress, int pBytes) {
      if (PacketSentEvent.TYPE.isEnabled()) {
         (new PacketSentEvent(pProtocolId, pPacketId, pRemoteAddress, pBytes)).commit();
      }

      if (NetworkSummaryEvent.TYPE.isEnabled()) {
         this.networkStatFor(pRemoteAddress).trackSentPacket(pBytes);
      }

   }

   private NetworkSummaryEvent.SumAggregation networkStatFor(SocketAddress pRemoteAddress) {
      return this.networkTrafficByAddress.computeIfAbsent(pRemoteAddress.toString(), NetworkSummaryEvent.SumAggregation::new);
   }

   @Nullable
   public ProfiledDuration onWorldLoadedStarted() {
      if (!WorldLoadFinishedEvent.TYPE.isEnabled()) {
         return null;
      } else {
         WorldLoadFinishedEvent worldloadfinishedevent = new WorldLoadFinishedEvent();
         worldloadfinishedevent.begin();
         return worldloadfinishedevent::commit;
      }
   }

   @Nullable
   public ProfiledDuration onChunkGenerate(ChunkPos pChunkPos, ResourceKey<Level> pLevel, String pName) {
      if (!ChunkGenerationEvent.TYPE.isEnabled()) {
         return null;
      } else {
         ChunkGenerationEvent chunkgenerationevent = new ChunkGenerationEvent(pChunkPos, pLevel, pName);
         chunkgenerationevent.begin();
         return chunkgenerationevent::commit;
      }
   }
}