package net.minecraft.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements CommandSource, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String VANILLA_BRAND = "vanilla";
   private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
   private static final int TICK_STATS_SPAN = 100;
   public static final int MS_PER_TICK = 50;
   private static final int OVERLOADED_THRESHOLD = 2000;
   private static final int OVERLOADED_WARNING_INTERVAL = 15000;
   private static final long STATUS_EXPIRE_TIME_NS = 5000000000L;
   private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
   public static final int START_CHUNK_RADIUS = 11;
   private static final int START_TICKING_CHUNK_COUNT = 441;
   private static final int AUTOSAVE_INTERVAL = 6000;
   private static final int MAX_TICK_LATENCY = 3;
   public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
   public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT);
   private static final long DELAYED_TASKS_TICK_EXTENSION = 50L;
   public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(Util.NIL_UUID, "Anonymous Player");
   protected final LevelStorageSource.LevelStorageAccess storageSource;
   protected final PlayerDataStorage playerDataStorage;
   private final List<Runnable> tickables = Lists.newArrayList();
   private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
   private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
   private Consumer<ProfileResults> onMetricsRecordingStopped = (p_177903_) -> {
      this.stopRecordingMetrics();
   };
   private Consumer<Path> onMetricsRecordingFinished = (p_177954_) -> {
   };
   private boolean willStartRecordingMetrics;
   @Nullable
   private MinecraftServer.TimeProfiler debugCommandProfiler;
   private boolean debugCommandProfilerDelayStart;
   private final ServerConnectionListener connection;
   private final ChunkProgressListenerFactory progressListenerFactory;
   @Nullable
   private ServerStatus status;
   @Nullable
   private ServerStatus.Favicon statusIcon;
   private final RandomSource random = RandomSource.create();
   private final DataFixer fixerUpper;
   private String localIp;
   private int port = -1;
   private final LayeredRegistryAccess<RegistryLayer> registries;
   private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
   private PlayerList playerList;
   private volatile boolean running = true;
   private boolean stopped;
   private int tickCount;
   protected final Proxy proxy;
   private boolean onlineMode;
   private boolean preventProxyConnections;
   private boolean pvp;
   private boolean allowFlight;
   @Nullable
   private String motd;
   private int playerIdleTimeout;
   public final long[] tickTimes = new long[100];
   @Nullable
   private KeyPair keyPair;
   @Nullable
   private GameProfile singleplayerProfile;
   private boolean isDemo;
   private volatile boolean isReady;
   private long lastOverloadWarning;
   protected final Services services;
   private long lastServerStatus;
   private final Thread serverThread;
   protected long nextTickTime = Util.getMillis();
   private long delayedTasksMaxNextTickTime;
   private boolean mayHaveDelayedTasks;
   private final PackRepository packRepository;
   private final ServerScoreboard scoreboard = new ServerScoreboard(this);
   @Nullable
   private CommandStorage commandStorage;
   private final CustomBossEvents customBossEvents = new CustomBossEvents();
   private final ServerFunctionManager functionManager;
   private final FrameTimer frameTimer = new FrameTimer();
   private boolean enforceWhitelist;
   private float averageTickTime;
   private final Executor executor;
   @Nullable
   private String serverId;
   private MinecraftServer.ReloadableResources resources;
   private final StructureTemplateManager structureTemplateManager;
   protected final WorldData worldData;
   private volatile boolean isSaving;

   public static <S extends MinecraftServer> S spin(Function<Thread, S> pThreadFunction) {
      AtomicReference<S> atomicreference = new AtomicReference<>();
      Thread thread = new Thread(net.minecraftforge.fml.util.thread.SidedThreadGroups.SERVER, () -> {
         atomicreference.get().runServer();
      }, "Server thread");
      thread.setUncaughtExceptionHandler((p_177909_, p_177910_) -> {
         LOGGER.error("Uncaught exception in server thread", p_177910_);
      });
      if (Runtime.getRuntime().availableProcessors() > 4) {
         thread.setPriority(8);
      }

      S s = pThreadFunction.apply(thread);
      atomicreference.set(s);
      thread.start();
      return s;
   }

   public MinecraftServer(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, Proxy pProxy, DataFixer pFixerUpper, Services pServices, ChunkProgressListenerFactory pProgressListenerFactory) {
      super("Server");
      this.registries = pWorldStem.registries();
      this.worldData = pWorldStem.worldData();
      if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
         throw new IllegalStateException("Missing Overworld dimension data");
      } else {
         this.proxy = pProxy;
         this.packRepository = pPackRepository;
         this.resources = new MinecraftServer.ReloadableResources(pWorldStem.resourceManager(), pWorldStem.dataPackResources());
         this.services = pServices;
         if (pServices.profileCache() != null) {
            pServices.profileCache().setExecutor(this);
         }

         this.connection = new ServerConnectionListener(this);
         this.progressListenerFactory = pProgressListenerFactory;
         this.storageSource = pStorageSource;
         this.playerDataStorage = pStorageSource.createPlayerStorage();
         this.fixerUpper = pFixerUpper;
         this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
         HolderGetter<Block> holdergetter = this.registries.compositeAccess().registryOrThrow(Registries.BLOCK).asLookup().filterFeatures(this.worldData.enabledFeatures());
         this.structureTemplateManager = new StructureTemplateManager(pWorldStem.resourceManager(), pStorageSource, pFixerUpper, holdergetter);
         this.serverThread = pServerThread;
         this.executor = Util.backgroundExecutor();
      }
   }

   private void readScoreboard(DimensionDataStorage pDataStorage) {
      pDataStorage.computeIfAbsent(this.getScoreboard()::createData, this.getScoreboard()::createData, "scoreboard");
   }

   /**
    * Initialises the server and starts it.
    */
   protected abstract boolean initServer() throws IOException;

   protected void loadLevel() {
      if (!JvmProfiler.INSTANCE.isRunning()) {
      }

      boolean flag = false;
      ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onWorldLoadedStarted();
      this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
      ChunkProgressListener chunkprogresslistener = this.progressListenerFactory.create(11);
      this.createLevels(chunkprogresslistener);
      this.forceDifficulty();
      this.prepareLevels(chunkprogresslistener);
      if (profiledduration != null) {
         profiledduration.finish();
      }

      if (flag) {
         try {
            JvmProfiler.INSTANCE.stop();
         } catch (Throwable throwable) {
            LOGGER.warn("Failed to stop JFR profiling", throwable);
         }
      }

   }

   protected void forceDifficulty() {
   }

   protected void createLevels(ChunkProgressListener pListener) {
      ServerLevelData serverleveldata = this.worldData.overworldData();
      boolean flag = this.worldData.isDebugWorld();
      Registry<LevelStem> registry = this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
      WorldOptions worldoptions = this.worldData.worldGenOptions();
      long i = worldoptions.seed();
      long j = BiomeManager.obfuscateSeed(i);
      List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverleveldata));
      LevelStem levelstem = registry.get(LevelStem.OVERWORLD);
      ServerLevel serverlevel = new ServerLevel(this, this.executor, this.storageSource, serverleveldata, Level.OVERWORLD, levelstem, pListener, flag, j, list, true, (RandomSequences)null);
      this.levels.put(Level.OVERWORLD, serverlevel);
      DimensionDataStorage dimensiondatastorage = serverlevel.getDataStorage();
      this.readScoreboard(dimensiondatastorage);
      this.commandStorage = new CommandStorage(dimensiondatastorage);
      WorldBorder worldborder = serverlevel.getWorldBorder();
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Load(levels.get(Level.OVERWORLD)));
      if (!serverleveldata.isInitialized()) {
         try {
            setInitialSpawn(serverlevel, serverleveldata, worldoptions.generateBonusChest(), flag);
            serverleveldata.setInitialized(true);
            if (flag) {
               this.setupDebugLevel(this.worldData);
            }
         } catch (Throwable throwable1) {
            CrashReport crashreport = CrashReport.forThrowable(throwable1, "Exception initializing level");

            try {
               serverlevel.fillReportDetails(crashreport);
            } catch (Throwable throwable) {
            }

            throw new ReportedException(crashreport);
         }

         serverleveldata.setInitialized(true);
      }

      this.getPlayerList().addWorldborderListener(serverlevel);
      if (this.worldData.getCustomBossEvents() != null) {
         this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
      }

      RandomSequences randomsequences = serverlevel.getRandomSequences();

      for(Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
         ResourceKey<LevelStem> resourcekey = entry.getKey();
         if (resourcekey != LevelStem.OVERWORLD) {
            ResourceKey<Level> resourcekey1 = ResourceKey.create(Registries.DIMENSION, resourcekey.location());
            DerivedLevelData derivedleveldata = new DerivedLevelData(this.worldData, serverleveldata);
            ServerLevel serverlevel1 = new ServerLevel(this, this.executor, this.storageSource, derivedleveldata, resourcekey1, entry.getValue(), pListener, flag, j, ImmutableList.of(), false, randomsequences);
            worldborder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverlevel1.getWorldBorder()));
            this.levels.put(resourcekey1, serverlevel1);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Load(levels.get(resourcekey)));
         }
      }

      worldborder.applySettings(serverleveldata.getWorldBorder());
   }

   private static void setInitialSpawn(ServerLevel pLevel, ServerLevelData pLevelData, boolean pGenerateBonusChest, boolean pDebug) {
      if (pDebug) {
         pLevelData.setSpawn(BlockPos.ZERO.above(80), 0.0F);
      } else {
         ServerChunkCache serverchunkcache = pLevel.getChunkSource();
         if (net.minecraftforge.event.ForgeEventFactory.onCreateWorldSpawn(pLevel, pLevelData)) return;
         ChunkPos chunkpos = new ChunkPos(serverchunkcache.randomState().sampler().findSpawnPosition());
         int i = serverchunkcache.getGenerator().getSpawnHeight(pLevel);
         if (i < pLevel.getMinBuildHeight()) {
            BlockPos blockpos = chunkpos.getWorldPosition();
            i = pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos.getX() + 8, blockpos.getZ() + 8);
         }

         pLevelData.setSpawn(chunkpos.getWorldPosition().offset(8, i, 8), 0.0F);
         int k1 = 0;
         int j = 0;
         int k = 0;
         int l = -1;
         int i1 = 5;

         for(int j1 = 0; j1 < Mth.square(11); ++j1) {
            if (k1 >= -5 && k1 <= 5 && j >= -5 && j <= 5) {
               BlockPos blockpos1 = PlayerRespawnLogic.getSpawnPosInChunk(pLevel, new ChunkPos(chunkpos.x + k1, chunkpos.z + j));
               if (blockpos1 != null) {
                  pLevelData.setSpawn(blockpos1, 0.0F);
                  break;
               }
            }

            if (k1 == j || k1 < 0 && k1 == -j || k1 > 0 && k1 == 1 - j) {
               int l1 = k;
               k = -l;
               l = l1;
            }

            k1 += k;
            j += l;
         }

         if (pGenerateBonusChest) {
            pLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((p_258226_) -> {
               return p_258226_.getHolder(MiscOverworldFeatures.BONUS_CHEST);
            }).ifPresent((p_264729_) -> {
               p_264729_.value().place(pLevel, serverchunkcache.getGenerator(), pLevel.random, new BlockPos(pLevelData.getXSpawn(), pLevelData.getYSpawn(), pLevelData.getZSpawn()));
            });
         }

      }
   }

   private void setupDebugLevel(WorldData pWorldData) {
      pWorldData.setDifficulty(Difficulty.PEACEFUL);
      pWorldData.setDifficultyLocked(true);
      ServerLevelData serverleveldata = pWorldData.overworldData();
      serverleveldata.setRaining(false);
      serverleveldata.setThundering(false);
      serverleveldata.setClearWeatherTime(1000000000);
      serverleveldata.setDayTime(6000L);
      serverleveldata.setGameType(GameType.SPECTATOR);
   }

   /**
    * Loads the spawn chunks and any forced chunks
    */
   private void prepareLevels(ChunkProgressListener pListener) {
      ServerLevel serverlevel = this.overworld();
      LOGGER.info("Preparing start region for dimension {}", (Object)serverlevel.dimension().location());
      BlockPos blockpos = serverlevel.getSharedSpawnPos();
      pListener.updateSpawnPos(new ChunkPos(blockpos));
      ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
      this.nextTickTime = Util.getMillis();
      serverchunkcache.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

      while(serverchunkcache.getTickingGenerated() != 441) {
         this.nextTickTime = Util.getMillis() + 10L;
         this.waitUntilNextTick();
      }

      this.nextTickTime = Util.getMillis() + 10L;
      this.waitUntilNextTick();

      for(ServerLevel serverlevel1 : this.levels.values()) {
         ForcedChunksSavedData forcedchunkssaveddata = serverlevel1.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
         if (forcedchunkssaveddata != null) {
            LongIterator longiterator = forcedchunkssaveddata.getChunks().iterator();

            while(longiterator.hasNext()) {
               long i = longiterator.nextLong();
               ChunkPos chunkpos = new ChunkPos(i);
               serverlevel1.getChunkSource().updateChunkForced(chunkpos, true);
            }
            net.minecraftforge.common.world.ForgeChunkManager.reinstatePersistentChunks(serverlevel1, forcedchunkssaveddata);
         }
      }

      this.nextTickTime = Util.getMillis() + 10L;
      this.waitUntilNextTick();
      pListener.stop();
      this.updateMobSpawningFlags();
   }

   public GameType getDefaultGameType() {
      return this.worldData.getGameType();
   }

   /**
    * Defaults to false.
    */
   public boolean isHardcore() {
      return this.worldData.isHardcore();
   }

   public abstract int getOperatorUserPermissionLevel();

   public abstract int getFunctionCompilationLevel();

   public abstract boolean shouldRconBroadcast();

   public boolean saveAllChunks(boolean pSuppressLog, boolean pFlush, boolean pForced) {
      boolean flag = false;

      for(ServerLevel serverlevel : this.getAllLevels()) {
         if (!pSuppressLog) {
            LOGGER.info("Saving chunks for level '{}'/{}", serverlevel, serverlevel.dimension().location());
         }

         serverlevel.save((ProgressListener)null, pFlush, serverlevel.noSave && !pForced);
         flag = true;
      }

      ServerLevel serverlevel2 = this.overworld();
      ServerLevelData serverleveldata = this.worldData.overworldData();
      serverleveldata.setWorldBorder(serverlevel2.getWorldBorder().createSettings());
      this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
      this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
      if (pFlush) {
         for(ServerLevel serverlevel1 : this.getAllLevels()) {
            LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", (Object)serverlevel1.getChunkSource().chunkMap.getStorageName());
         }

         LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
      }

      return flag;
   }

   public boolean saveEverything(boolean pSuppressLog, boolean pFlush, boolean pForced) {
      boolean flag;
      try {
         this.isSaving = true;
         this.getPlayerList().saveAll();
         flag = this.saveAllChunks(pSuppressLog, pFlush, pForced);
      } finally {
         this.isSaving = false;
      }

      return flag;
   }

   public void close() {
      this.stopServer();
   }

   /**
    * Saves all necessary data as preparation for stopping the server.
    */
   public void stopServer() {
      if (this.metricsRecorder.isRecording()) {
         this.cancelRecordingMetrics();
      }

      LOGGER.info("Stopping server");
      if (this.getConnection() != null) {
         this.getConnection().stop();
      }

      this.isSaving = true;
      if (this.playerList != null) {
         LOGGER.info("Saving players");
         this.playerList.saveAll();
         this.playerList.removeAll();
      }

      LOGGER.info("Saving worlds");

      for(ServerLevel serverlevel : this.getAllLevels()) {
         if (serverlevel != null) {
            serverlevel.noSave = false;
         }
      }

      while(this.levels.values().stream().anyMatch((p_202480_) -> {
         return p_202480_.getChunkSource().chunkMap.hasWork();
      })) {
         this.nextTickTime = Util.getMillis() + 1L;

         for(ServerLevel serverlevel1 : this.getAllLevels()) {
            serverlevel1.getChunkSource().removeTicketsOnClosing();
            serverlevel1.getChunkSource().tick(() -> {
               return true;
            }, false);
         }

         this.waitUntilNextTick();
      }

      this.saveAllChunks(false, true, false);

      for(ServerLevel serverlevel2 : this.getAllLevels()) {
         if (serverlevel2 != null) {
            try {
               net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Unload(serverlevel2));
               serverlevel2.close();
            } catch (IOException ioexception1) {
               LOGGER.error("Exception closing the level", (Throwable)ioexception1);
            }
         }
      }

      this.isSaving = false;
      this.resources.close();

      try {
         this.storageSource.close();
      } catch (IOException ioexception) {
         LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), ioexception);
      }

   }

   /**
    * "getHostname" is already taken, but both return the hostname.
    */
   public String getLocalIp() {
      return this.localIp;
   }

   public void setLocalIp(String pHost) {
      this.localIp = pHost;
   }

   public boolean isRunning() {
      return this.running;
   }

   /**
    * Sets the serverRunning variable to false, in order to get the server to shut down.
    */
   public void halt(boolean pWaitForServer) {
      this.running = false;
      if (pWaitForServer) {
         try {
            this.serverThread.join();
         } catch (InterruptedException interruptedexception) {
            LOGGER.error("Error while shutting down", (Throwable)interruptedexception);
         }
      }

   }

   protected void runServer() {
      try {
         if (!this.initServer()) {
            throw new IllegalStateException("Failed to initialize server");
         }

         net.minecraftforge.server.ServerLifecycleHooks.handleServerStarted(this);
         this.nextTickTime = Util.getMillis();
         this.statusIcon = this.loadStatusIcon().orElse((ServerStatus.Favicon)null);
         this.status = this.buildServerStatus();
         resetStatusCache(status);

         while(this.running) {
            long i = Util.getMillis() - this.nextTickTime;
            if (i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
               long j = i / 50L;
               LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
               this.nextTickTime += j * 50L;
               this.lastOverloadWarning = this.nextTickTime;
            }

            if (this.debugCommandProfilerDelayStart) {
               this.debugCommandProfilerDelayStart = false;
               this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
            }

            this.nextTickTime += 50L;
            this.startMetricsRecordingTick();
            this.profiler.push("tick");
            this.tickServer(this::haveTime);
            this.profiler.popPush("nextTickWait");
            this.mayHaveDelayedTasks = true;
            this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
            this.waitUntilNextTick();
            this.profiler.pop();
            this.endMetricsRecordingTick();
            this.isReady = true;
            JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
         }
         net.minecraftforge.server.ServerLifecycleHooks.handleServerStopping(this);
         net.minecraftforge.server.ServerLifecycleHooks.expectServerStopped(); // Forge: Has to come before MinecraftServer#onServerCrash to avoid race conditions
      } catch (Throwable throwable1) {
         LOGGER.error("Encountered an unexpected exception", throwable1);
         CrashReport crashreport = constructOrExtractCrashReport(throwable1);
         this.fillSystemReport(crashreport.getSystemReport());
         File file1 = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
         if (crashreport.saveToFile(file1)) {
            LOGGER.error("This crash report has been saved to: {}", (Object)file1.getAbsolutePath());
         } else {
            LOGGER.error("We were unable to save this crash report to disk.");
         }

         net.minecraftforge.server.ServerLifecycleHooks.expectServerStopped(); // Forge: Has to come before MinecraftServer#onServerCrash to avoid race conditions
         this.onServerCrash(crashreport);
      } finally {
         try {
            this.stopped = true;
            this.stopServer();
         } catch (Throwable throwable) {
            LOGGER.error("Exception stopping the server", throwable);
         } finally {
            if (this.services.profileCache() != null) {
               this.services.profileCache().clearExecutor();
            }

            net.minecraftforge.server.ServerLifecycleHooks.handleServerStopped(this);
            this.onServerExit();
         }

      }

   }

   private static CrashReport constructOrExtractCrashReport(Throwable pCause) {
      ReportedException reportedexception = null;

      for(Throwable throwable = pCause; throwable != null; throwable = throwable.getCause()) {
         if (throwable instanceof ReportedException reportedexception1) {
            reportedexception = reportedexception1;
         }
      }

      CrashReport crashreport;
      if (reportedexception != null) {
         crashreport = reportedexception.getReport();
         if (reportedexception != pCause) {
            crashreport.addCategory("Wrapped in").setDetailError("Wrapping exception", pCause);
         }
      } else {
         crashreport = new CrashReport("Exception in server tick loop", pCause);
      }

      return crashreport;
   }

   private boolean haveTime() {
      return this.runningTask() || Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
   }

   /**
    * Runs all pending tasks and waits for more tasks until serverTime is reached.
    */
   protected void waitUntilNextTick() {
      this.runAllTasks();
      this.managedBlock(() -> {
         return !this.haveTime();
      });
   }

   /**
    * Wraps the given runnable task. In this case, the original runnable is returned as-is.
    * <p>
    * @return The wrapped runnable task
    * @param pRunnable The original runnable task
    */
   protected TickTask wrapRunnable(Runnable pRunnable) {
      return new TickTask(this.tickCount, pRunnable);
   }

   /**
    * Determines whether the given runnable task should be run or not.
    * It depends on the shutdown state of the SoundEngineExecutor.
    * <p>
    * @return true if the task should run, false otherwise
    * @param pRunnable The runnable task
    */
   protected boolean shouldRun(TickTask pRunnable) {
      return pRunnable.getTick() + 3 < this.tickCount || this.haveTime();
   }

   public boolean pollTask() {
      boolean flag = this.pollTaskInternal();
      this.mayHaveDelayedTasks = flag;
      return flag;
   }

   private boolean pollTaskInternal() {
      if (super.pollTask()) {
         return true;
      } else {
         if (this.haveTime()) {
            for(ServerLevel serverlevel : this.getAllLevels()) {
               if (serverlevel.getChunkSource().pollTask()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void doRunTask(TickTask pTask) {
      this.getProfiler().incrementCounter("runTask");
      super.doRunTask(pTask);
   }

   private Optional<ServerStatus.Favicon> loadStatusIcon() {
      Optional<Path> optional = Optional.of(this.getFile("server-icon.png").toPath()).filter((p_272387_) -> {
         return Files.isRegularFile(p_272387_);
      }).or(() -> {
         return this.storageSource.getIconFile().filter((p_272385_) -> {
            return Files.isRegularFile(p_272385_);
         });
      });
      return optional.flatMap((p_272386_) -> {
         try {
            BufferedImage bufferedimage = ImageIO.read(p_272386_.toFile());
            Preconditions.checkState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
            Preconditions.checkState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            ImageIO.write(bufferedimage, "PNG", bytearrayoutputstream);
            return Optional.of(new ServerStatus.Favicon(bytearrayoutputstream.toByteArray()));
         } catch (Exception exception) {
            LOGGER.error("Couldn't load server icon", (Throwable)exception);
            return Optional.empty();
         }
      });
   }

   public Optional<Path> getWorldScreenshotFile() {
      return this.storageSource.getIconFile();
   }

   public File getServerDirectory() {
      return new File(".");
   }

   /**
    * Called on exit from the main run() loop.
    */
   public void onServerCrash(CrashReport pReport) {
   }

   /**
    * Directly calls System.exit(0), instantly killing the program.
    */
   public void onServerExit() {
   }

   /**
    * Main function called by run() every loop.
    */
   public void tickServer(BooleanSupplier pHasTimeLeft) {
      long i = Util.getNanos();
      net.minecraftforge.event.ForgeEventFactory.onPreServerTick(pHasTimeLeft, this);
      ++this.tickCount;
      this.tickChildren(pHasTimeLeft);
      if (i - this.lastServerStatus >= 5000000000L) {
         this.lastServerStatus = i;
         this.status = this.buildServerStatus();
         resetStatusCache(status);
      }

      if (this.tickCount % 6000 == 0) {
         LOGGER.debug("Autosave started");
         this.profiler.push("save");
         this.saveEverything(true, false, false);
         this.profiler.pop();
         LOGGER.debug("Autosave finished");
      }

      this.profiler.push("tallying");
      long j = this.tickTimes[this.tickCount % 100] = Util.getNanos() - i;
      this.averageTickTime = this.averageTickTime * 0.8F + (float)j / 1000000.0F * 0.19999999F;
      long k = Util.getNanos();
      this.frameTimer.logFrameDuration(k - i);
      this.profiler.pop();
      net.minecraftforge.event.ForgeEventFactory.onPostServerTick(pHasTimeLeft, this);
   }

   private static final com.google.gson.Gson GSON = new com.google.gson.Gson();
   private String cachedServerStatus; // FORGE: cache the server status json in case a client spams requests
   private void resetStatusCache(ServerStatus status) {
      this.cachedServerStatus = GSON.toJson(ServerStatus.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, status)
              .result().orElseThrow());
   }
   public String getStatusJson() {
      return cachedServerStatus;
   }

   private ServerStatus buildServerStatus() {
      ServerStatus.Players serverstatus$players = this.buildPlayerStatus();
      return new ServerStatus(Component.nullToEmpty(this.motd), Optional.of(serverstatus$players), Optional.of(ServerStatus.Version.current()), Optional.ofNullable(this.statusIcon), this.enforceSecureProfile(), Optional.of(new net.minecraftforge.network.ServerStatusPing()));
   }

   private ServerStatus.Players buildPlayerStatus() {
      List<ServerPlayer> list = this.playerList.getPlayers();
      int i = this.getMaxPlayers();
      if (this.hidesOnlinePlayers()) {
         return new ServerStatus.Players(i, list.size(), List.of());
      } else {
         int j = Math.min(list.size(), 12);
         ObjectArrayList<GameProfile> objectarraylist = new ObjectArrayList<>(j);
         int k = Mth.nextInt(this.random, 0, list.size() - j);

         for(int l = 0; l < j; ++l) {
            ServerPlayer serverplayer = list.get(k + l);
            objectarraylist.add(serverplayer.allowsListing() ? serverplayer.getGameProfile() : ANONYMOUS_PLAYER_PROFILE);
         }

         Util.shuffle(objectarraylist, this.random);
         return new ServerStatus.Players(i, list.size(), objectarraylist);
      }
   }

   public void tickChildren(BooleanSupplier pHasTimeLeft) {
      this.profiler.push("commandFunctions");
      this.getFunctions().tick();
      this.profiler.popPush("levels");

      for(ServerLevel serverlevel : this.getWorldArray()) {
         long tickStart = Util.getNanos();
         this.profiler.push(() -> {
            return serverlevel + " " + serverlevel.dimension().location();
         });
         if (this.tickCount % 20 == 0) {
            this.profiler.push("timeSync");
            this.synchronizeTime(serverlevel);
            this.profiler.pop();
         }

         this.profiler.push("tick");
         net.minecraftforge.event.ForgeEventFactory.onPreLevelTick(serverlevel, pHasTimeLeft);

         try {
            serverlevel.tick(pHasTimeLeft);
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
            serverlevel.fillReportDetails(crashreport);
            throw new ReportedException(crashreport);
         }
         net.minecraftforge.event.ForgeEventFactory.onPostLevelTick(serverlevel, pHasTimeLeft);

         this.profiler.pop();
         this.profiler.pop();
         perWorldTickTimes.computeIfAbsent(serverlevel.dimension(), k -> new long[100])[this.tickCount % 100] = Util.getNanos() - tickStart;
      }

      this.profiler.popPush("connection");
      this.getConnection().tick();
      this.profiler.popPush("players");
      this.playerList.tick();
      if (net.minecraftforge.gametest.ForgeGameTestHooks.isGametestEnabled()) {
         GameTestTicker.SINGLETON.tick();
      }

      this.profiler.popPush("server gui refresh");

      for(int i = 0; i < this.tickables.size(); ++i) {
         this.tickables.get(i).run();
      }

      this.profiler.pop();
   }

   private void synchronizeTime(ServerLevel pLevel) {
      this.playerList.broadcastAll(new ClientboundSetTimePacket(pLevel.getGameTime(), pLevel.getDayTime(), pLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), pLevel.dimension());
   }

   public void forceTimeSynchronization() {
      this.profiler.push("timeSync");

      for(ServerLevel serverlevel : this.getAllLevels()) {
         this.synchronizeTime(serverlevel);
      }

      this.profiler.pop();
   }

   public boolean isNetherEnabled() {
      return true;
   }

   public void addTickable(Runnable pTickable) {
      this.tickables.add(pTickable);
   }

   protected void setId(String pServerId) {
      this.serverId = pServerId;
   }

   public boolean isShutdown() {
      return !this.serverThread.isAlive();
   }

   /**
    * Returns a File object from the specified string.
    */
   public File getFile(String pFileName) {
      return new File(this.getServerDirectory(), pFileName);
   }

   public final ServerLevel overworld() {
      return this.levels.get(Level.OVERWORLD);
   }

   /**
    * Gets the worldServer by the given dimension.
    */
   @Nullable
   public ServerLevel getLevel(ResourceKey<Level> pDimension) {
      return this.levels.get(pDimension);
   }

   public Set<ResourceKey<Level>> levelKeys() {
      return this.levels.keySet();
   }

   public Iterable<ServerLevel> getAllLevels() {
      return this.levels.values();
   }

   /**
    * Returns the server's Minecraft version as string.
    */
   public String getServerVersion() {
      return SharedConstants.getCurrentVersion().getName();
   }

   /**
    * Returns the number of players currently on the server.
    */
   public int getPlayerCount() {
      return this.playerList.getPlayerCount();
   }

   /**
    * Returns the maximum number of players allowed on the server.
    */
   public int getMaxPlayers() {
      return this.playerList.getMaxPlayers();
   }

   /**
    * Returns an array of the usernames of all the connected players.
    */
   public String[] getPlayerNames() {
      return this.playerList.getPlayerNamesArray();
   }

   @DontObfuscate
   public String getServerModName() {
      return net.minecraftforge.internal.BrandingControl.getServerBranding();
   }

   public SystemReport fillSystemReport(SystemReport pSystemReport) {
      pSystemReport.setDetail("Server Running", () -> {
         return Boolean.toString(this.running);
      });
      if (this.playerList != null) {
         pSystemReport.setDetail("Player Count", () -> {
            return this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers();
         });
      }

      pSystemReport.setDetail("Data Packs", () -> {
         return this.packRepository.getSelectedPacks().stream().map((p_248087_) -> {
            return p_248087_.getId() + (p_248087_.getCompatibility().isCompatible() ? "" : " (incompatible)");
         }).collect(Collectors.joining(", "));
      });
      pSystemReport.setDetail("Enabled Feature Flags", () -> {
         return FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
      });
      pSystemReport.setDetail("World Generation", () -> {
         return this.worldData.worldGenSettingsLifecycle().toString();
      });
      if (this.serverId != null) {
         pSystemReport.setDetail("Server Id", () -> {
            return this.serverId;
         });
      }

      return this.fillServerSystemReport(pSystemReport);
   }

   public abstract SystemReport fillServerSystemReport(SystemReport pReport);

   public ModCheck getModdedStatus() {
      return ModCheck.identify("vanilla", this::getServerModName, "Server", MinecraftServer.class);
   }

   public void sendSystemMessage(Component pComponent) {
      LOGGER.info(pComponent.getString());
   }

   /**
    * Gets KeyPair instanced in MinecraftServer.
    */
   public KeyPair getKeyPair() {
      return this.keyPair;
   }

   /**
    * Gets serverPort.
    */
   public int getPort() {
      return this.port;
   }

   public void setPort(int pPort) {
      this.port = pPort;
   }

   @Nullable
   public GameProfile getSingleplayerProfile() {
      return this.singleplayerProfile;
   }

   public void setSingleplayerProfile(@Nullable GameProfile pSingleplayerProfile) {
      this.singleplayerProfile = pSingleplayerProfile;
   }

   public boolean isSingleplayer() {
      return this.singleplayerProfile != null;
   }

   protected void initializeKeyPair() {
      LOGGER.info("Generating keypair");

      try {
         this.keyPair = Crypt.generateKeyPair();
      } catch (CryptException cryptexception) {
         throw new IllegalStateException("Failed to generate key pair", cryptexception);
      }
   }

   public void setDifficulty(Difficulty pDifficulty, boolean pForced) {
      if (pForced || !this.worldData.isDifficultyLocked()) {
         this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : pDifficulty);
         this.updateMobSpawningFlags();
         this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
      }
   }

   public int getScaledTrackingDistance(int pTrackingDistance) {
      return pTrackingDistance;
   }

   private void updateMobSpawningFlags() {
      for(ServerLevel serverlevel : this.getAllLevels()) {
         serverlevel.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
      }

   }

   public void setDifficultyLocked(boolean pLocked) {
      this.worldData.setDifficultyLocked(pLocked);
      this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
   }

   private void sendDifficultyUpdate(ServerPlayer p_129939_) {
      LevelData leveldata = p_129939_.level().getLevelData();
      p_129939_.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
   }

   public boolean isSpawningMonsters() {
      return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
   }

   /**
    * Gets whether this is a demo or not.
    */
   public boolean isDemo() {
      return this.isDemo;
   }

   /**
    * Sets whether this is a demo or not.
    */
   public void setDemo(boolean pDemo) {
      this.isDemo = pDemo;
   }

   public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
      return Optional.empty();
   }

   public boolean isResourcePackRequired() {
      return this.getServerResourcePack().filter(MinecraftServer.ServerResourcePackInfo::isRequired).isPresent();
   }

   public abstract boolean isDedicatedServer();

   public abstract int getRateLimitPacketsPerSecond();

   public boolean usesAuthentication() {
      return this.onlineMode;
   }

   public void setUsesAuthentication(boolean pOnline) {
      this.onlineMode = pOnline;
   }

   public boolean getPreventProxyConnections() {
      return this.preventProxyConnections;
   }

   public void setPreventProxyConnections(boolean pPreventProxyConnections) {
      this.preventProxyConnections = pPreventProxyConnections;
   }

   public boolean isSpawningAnimals() {
      return true;
   }

   public boolean areNpcsEnabled() {
      return true;
   }

   /**
    * Get if native transport should be used. Native transport means linux server performance improvements and optimized
    * packet sending/receiving on linux
    */
   public abstract boolean isEpollEnabled();

   public boolean isPvpAllowed() {
      return this.pvp;
   }

   public void setPvpAllowed(boolean pAllowPvp) {
      this.pvp = pAllowPvp;
   }

   public boolean isFlightAllowed() {
      return this.allowFlight;
   }

   public void setFlightAllowed(boolean pAllow) {
      this.allowFlight = pAllow;
   }

   /**
    * Return whether command blocks are enabled.
    */
   public abstract boolean isCommandBlockEnabled();

   public String getMotd() {
      return this.motd;
   }

   public void setMotd(String pMotd) {
      this.motd = pMotd;
   }

   public boolean isStopped() {
      return this.stopped;
   }

   public PlayerList getPlayerList() {
      return this.playerList;
   }

   public void setPlayerList(PlayerList pList) {
      this.playerList = pList;
   }

   /**
    * Returns {@code true} if this integrated server is open to LAN
    */
   public abstract boolean isPublished();

   /**
    * Sets the game type for all worlds.
    */
   public void setDefaultGameType(GameType pGameMode) {
      this.worldData.setGameType(pGameMode);
   }

   @Nullable
   public ServerConnectionListener getConnection() {
      return this.connection;
   }

   public boolean isReady() {
      return this.isReady;
   }

   public boolean hasGui() {
      return false;
   }

   public boolean publishServer(@Nullable GameType pGameMode, boolean pCheats, int pPort) {
      return false;
   }

   public int getTickCount() {
      return this.tickCount;
   }

   /**
    * Return the spawn protection area's size.
    */
   public int getSpawnProtectionRadius() {
      return 16;
   }

   public boolean isUnderSpawnProtection(ServerLevel pLevel, BlockPos pPos, Player pPlayer) {
      return false;
   }

   public boolean repliesToStatus() {
      return true;
   }

   public boolean hidesOnlinePlayers() {
      return false;
   }

   public Proxy getProxy() {
      return this.proxy;
   }

   public int getPlayerIdleTimeout() {
      return this.playerIdleTimeout;
   }

   public void setPlayerIdleTimeout(int pIdleTimeout) {
      this.playerIdleTimeout = pIdleTimeout;
   }

   public MinecraftSessionService getSessionService() {
      return this.services.sessionService();
   }

   @Nullable
   public SignatureValidator getProfileKeySignatureValidator() {
      return this.services.profileKeySignatureValidator();
   }

   public GameProfileRepository getProfileRepository() {
      return this.services.profileRepository();
   }

   @Nullable
   public GameProfileCache getProfileCache() {
      return this.services.profileCache();
   }

   @Nullable
   public ServerStatus getStatus() {
      return this.status;
   }

   public void invalidateStatus() {
      this.lastServerStatus = 0L;
   }

   public int getAbsoluteMaxWorldSize() {
      return 29999984;
   }

   public boolean scheduleExecutables() {
      return super.scheduleExecutables() && !this.isStopped();
   }

   public void executeIfPossible(Runnable pTask) {
      if (this.isStopped()) {
         throw new RejectedExecutionException("Server already shutting down");
      } else {
         super.executeIfPossible(pTask);
      }
   }

   /**
    * {@return The currently running thread}
    */
   public Thread getRunningThread() {
      return this.serverThread;
   }

   /**
    * The compression threshold. If the packet is larger than the specified amount of bytes, it will be compressed
    */
   public int getCompressionThreshold() {
      return 256;
   }

   public boolean enforceSecureProfile() {
      return false;
   }

   public long getNextTickTime() {
      return this.nextTickTime;
   }

   public DataFixer getFixerUpper() {
      return this.fixerUpper;
   }

   public int getSpawnRadius(@Nullable ServerLevel pLevel) {
      return pLevel != null ? pLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
   }

   public ServerAdvancementManager getAdvancements() {
      return this.resources.managers.getAdvancements();
   }

   public ServerFunctionManager getFunctions() {
      return this.functionManager;
   }

   /**
    * Replaces currently selected list of datapacks, reloads them, and sends new data to players.
    */
   public CompletableFuture<Void> reloadResources(Collection<String> pSelectedIds) {
      RegistryAccess.Frozen registryaccess$frozen = this.registries.getAccessForLoading(RegistryLayer.RELOADABLE);
      CompletableFuture<Void> completablefuture = CompletableFuture.supplyAsync(() -> {
         return pSelectedIds.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList());
      }, this).thenCompose((p_248092_) -> {
         CloseableResourceManager closeableresourcemanager = new MultiPackResourceManager(PackType.SERVER_DATA, p_248092_);
         return ReloadableServerResources.loadResources(closeableresourcemanager, registryaccess$frozen, this.worldData.enabledFeatures(), this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this).whenComplete((p_212907_, p_212908_) -> {
            if (p_212908_ != null) {
               closeableresourcemanager.close();
            }

         }).thenApply((p_212904_) -> {
            return new MinecraftServer.ReloadableResources(closeableresourcemanager, p_212904_);
         });
      }).thenAcceptAsync((p_248090_) -> {
         this.resources.close();
         this.resources = p_248090_;
         this.packRepository.setSelected(pSelectedIds);
         WorldDataConfiguration worlddataconfiguration = new WorldDataConfiguration(getSelectedPacks(this.packRepository), this.worldData.enabledFeatures());
         this.worldData.setDataConfiguration(worlddataconfiguration);
         this.resources.managers.updateRegistryTags(this.registryAccess());
         this.getPlayerList().saveAll();
         this.getPlayerList().reloadResources();
         this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
         this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
         this.getPlayerList().getPlayers().forEach(this.getPlayerList()::sendPlayerPermissionLevel); //Forge: Fix newly added/modified commands not being sent to the client when commands reload.
      }, this);
      if (this.isSameThread()) {
         this.managedBlock(completablefuture::isDone);
      }

      return completablefuture;
   }

   public static WorldDataConfiguration configurePackRepository(PackRepository pPackRepository, DataPackConfig pDataPackConfig, boolean pSafeMode, FeatureFlagSet pEnabledFeatures) {
      net.minecraftforge.resource.ResourcePackLoader.loadResourcePacks(pPackRepository, net.minecraftforge.server.ServerLifecycleHooks::buildPackFinder);
      pPackRepository.reload();
      DataPackConfig.DEFAULT.addModPacks(net.minecraftforge.common.ForgeHooks.getModPacks());
      pDataPackConfig.addModPacks(net.minecraftforge.common.ForgeHooks.getModPacks());
      if (pSafeMode) {
         pPackRepository.setSelected(net.minecraftforge.common.ForgeHooks.getModPacksWithVanilla());
         return new WorldDataConfiguration(new DataPackConfig(net.minecraftforge.common.ForgeHooks.getModPacksWithVanilla(), ImmutableList.of()), FeatureFlags.DEFAULT_FLAGS);
      } else {
         Set<String> set = Sets.newLinkedHashSet();

         for(String s : pDataPackConfig.getEnabled()) {
            if (pPackRepository.isAvailable(s)) {
               set.add(s);
            } else {
               LOGGER.warn("Missing data pack {}", (Object)s);
            }
         }

         for(Pack pack : pPackRepository.getAvailablePacks()) {
            String s1 = pack.getId();
            if (!pDataPackConfig.getDisabled().contains(s1)) {
               FeatureFlagSet featureflagset = pack.getRequestedFeatures();
               boolean flag = set.contains(s1);
               if (!flag && pack.getPackSource().shouldAddAutomatically()) {
                  if (featureflagset.isSubsetOf(pEnabledFeatures)) {
                     LOGGER.info("Found new data pack {}, loading it automatically", (Object)s1);
                     set.add(s1);
                  } else {
                     LOGGER.info("Found new data pack {}, but can't load it due to missing features {}", s1, FeatureFlags.printMissingFlags(pEnabledFeatures, featureflagset));
                  }
               }

               if (flag && !featureflagset.isSubsetOf(pEnabledFeatures)) {
                  LOGGER.warn("Pack {} requires features {} that are not enabled for this world, disabling pack.", s1, FeatureFlags.printMissingFlags(pEnabledFeatures, featureflagset));
                  set.remove(s1);
               }
            }
         }

         if (set.isEmpty()) {
            LOGGER.info("No datapacks selected, forcing vanilla");
            set.add("vanilla");
         }

         pPackRepository.setSelected(set);
         DataPackConfig datapackconfig = getSelectedPacks(pPackRepository);
         FeatureFlagSet featureflagset1 = pPackRepository.getRequestedFeatureFlags();
         return new WorldDataConfiguration(datapackconfig, featureflagset1);
      }
   }

   private static DataPackConfig getSelectedPacks(PackRepository pPackRepository) {
      Collection<String> collection = pPackRepository.getSelectedIds();
      List<String> list = ImmutableList.copyOf(collection);
      List<String> list1 = pPackRepository.getAvailableIds().stream().filter((p_212916_) -> {
         return !collection.contains(p_212916_);
      }).collect(ImmutableList.toImmutableList());
      return new DataPackConfig(list, list1);
   }

   public void kickUnlistedPlayers(CommandSourceStack pCommandSource) {
      if (this.isEnforceWhitelist()) {
         PlayerList playerlist = pCommandSource.getServer().getPlayerList();
         UserWhiteList userwhitelist = playerlist.getWhiteList();

         for(ServerPlayer serverplayer : Lists.newArrayList(playerlist.getPlayers())) {
            if (!userwhitelist.isWhiteListed(serverplayer.getGameProfile())) {
               serverplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
            }
         }

      }
   }

   public PackRepository getPackRepository() {
      return this.packRepository;
   }

   public Commands getCommands() {
      return this.resources.managers.getCommands();
   }

   public CommandSourceStack createCommandSourceStack() {
      ServerLevel serverlevel = this.overworld();
      return new CommandSourceStack(this, serverlevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverlevel.getSharedSpawnPos()), Vec2.ZERO, serverlevel, 4, "Server", Component.literal("Server"), this, (Entity)null);
   }

   public boolean acceptsSuccess() {
      return true;
   }

   public boolean acceptsFailure() {
      return true;
   }

   public abstract boolean shouldInformAdmins();

   public RecipeManager getRecipeManager() {
      return this.resources.managers.getRecipeManager();
   }

   public ServerScoreboard getScoreboard() {
      return this.scoreboard;
   }

   public CommandStorage getCommandStorage() {
      if (this.commandStorage == null) {
         throw new NullPointerException("Called before server init");
      } else {
         return this.commandStorage;
      }
   }

   public LootDataManager getLootData() {
      return this.resources.managers.getLootData();
   }

   public GameRules getGameRules() {
      return this.overworld().getGameRules();
   }

   public CustomBossEvents getCustomBossEvents() {
      return this.customBossEvents;
   }

   public boolean isEnforceWhitelist() {
      return this.enforceWhitelist;
   }

   public void setEnforceWhitelist(boolean pWhitelistEnabled) {
      this.enforceWhitelist = pWhitelistEnabled;
   }

   public float getAverageTickTime() {
      return this.averageTickTime;
   }

   public int getProfilePermissions(GameProfile pProfile) {
      if (this.getPlayerList().isOp(pProfile)) {
         ServerOpListEntry serveroplistentry = this.getPlayerList().getOps().get(pProfile);
         if (serveroplistentry != null) {
            return serveroplistentry.getLevel();
         } else if (this.isSingleplayerOwner(pProfile)) {
            return 4;
         } else if (this.isSingleplayer()) {
            return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
         } else {
            return this.getOperatorUserPermissionLevel();
         }
      } else {
         return 0;
      }
   }

   public FrameTimer getFrameTimer() {
      return this.frameTimer;
   }

   public ProfilerFiller getProfiler() {
      return this.profiler;
   }

   public abstract boolean isSingleplayerOwner(GameProfile pProfile);

   private Map<ResourceKey<Level>, long[]> perWorldTickTimes = Maps.newIdentityHashMap();
   @Nullable
   public long[] getTickTime(ResourceKey<Level> dim) {
      return perWorldTickTimes.get(dim);
   }

   @Deprecated //Forge Internal use Only, You can screw up a lot of things if you mess with this map.
   public synchronized Map<ResourceKey<Level>, ServerLevel> forgeGetWorldMap() {
      return this.levels;
   }
   private int worldArrayMarker = 0;
   private int worldArrayLast = -1;
   private ServerLevel[] worldArray;
   @Deprecated //Forge Internal use Only, use to protect against concurrent modifications in the world tick loop.
   public synchronized void markWorldsDirty() {
      worldArrayMarker++;
   }
   private ServerLevel[] getWorldArray() {
      if (worldArrayMarker == worldArrayLast && worldArray != null)
         return worldArray;
      worldArray = this.levels.values().stream().toArray(x -> new ServerLevel[x]);
      worldArrayLast = worldArrayMarker;
      return worldArray;
   }

   public void dumpServerProperties(Path pPath) throws IOException {
   }

   private void saveDebugReport(Path pPath) {
      Path path = pPath.resolve("levels");

      try {
         for(Map.Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey().location();
            Path path1 = path.resolve(resourcelocation.getNamespace()).resolve(resourcelocation.getPath());
            Files.createDirectories(path1);
            entry.getValue().saveDebugReport(path1);
         }

         this.dumpGameRules(pPath.resolve("gamerules.txt"));
         this.dumpClasspath(pPath.resolve("classpath.txt"));
         this.dumpMiscStats(pPath.resolve("stats.txt"));
         this.dumpThreads(pPath.resolve("threads.txt"));
         this.dumpServerProperties(pPath.resolve("server.properties.txt"));
         this.dumpNativeModules(pPath.resolve("modules.txt"));
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to save debug report", (Throwable)ioexception);
      }

   }

   private void dumpMiscStats(Path pPath) throws IOException {
      try (Writer writer = Files.newBufferedWriter(pPath)) {
         writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
         writer.write(String.format(Locale.ROOT, "average_tick_time: %f\n", this.getAverageTickTime()));
         writer.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimes)));
         writer.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
      }

   }

   private void dumpGameRules(Path pPath) throws IOException {
      try (Writer writer = Files.newBufferedWriter(pPath)) {
         final List<String> list = Lists.newArrayList();
         final GameRules gamerules = this.getGameRules();
         GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> p_195531_, GameRules.Type<T> p_195532_) {
               list.add(String.format(Locale.ROOT, "%s=%s\n", p_195531_.getId(), gamerules.<T>getRule(p_195531_)));
            }
         });

         for(String s : list) {
            writer.write(s);
         }
      }

   }

   private void dumpClasspath(Path pPath) throws IOException {
      try (Writer writer = Files.newBufferedWriter(pPath)) {
         String s = System.getProperty("java.class.path");
         String s1 = System.getProperty("path.separator");

         for(String s2 : Splitter.on(s1).split(s)) {
            writer.write(s2);
            writer.write("\n");
         }
      }

   }

   private void dumpThreads(Path pPath) throws IOException {
      ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
      ThreadInfo[] athreadinfo = threadmxbean.dumpAllThreads(true, true);
      Arrays.sort(athreadinfo, Comparator.comparing(ThreadInfo::getThreadName));

      try (Writer writer = Files.newBufferedWriter(pPath)) {
         for(ThreadInfo threadinfo : athreadinfo) {
            writer.write(threadinfo.toString());
            writer.write(10);
         }
      }

   }

   private void dumpNativeModules(Path pPath) throws IOException {
      try (Writer writer = Files.newBufferedWriter(pPath)) {
         List<NativeModuleLister.NativeModuleInfo> list;
         try {
            list = Lists.newArrayList(NativeModuleLister.listModules());
         } catch (Throwable throwable) {
            LOGGER.warn("Failed to list native modules", throwable);
            return;
         }

         list.sort(Comparator.comparing((p_212910_) -> {
            return p_212910_.name;
         }));

         for(NativeModuleLister.NativeModuleInfo nativemodulelister$nativemoduleinfo : list) {
            writer.write(nativemodulelister$nativemoduleinfo.toString());
            writer.write(10);
         }

      }
   }

   private void startMetricsRecordingTick() {
      if (this.willStartRecordingMetrics) {
         this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, (p_212927_) -> {
            this.executeBlocking(() -> {
               this.saveDebugReport(p_212927_.resolve("server"));
            });
            this.onMetricsRecordingFinished.accept(p_212927_);
         });
         this.willStartRecordingMetrics = false;
      }

      this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
      this.metricsRecorder.startTick();
      this.profiler.startTick();
   }

   private void endMetricsRecordingTick() {
      this.profiler.endTick();
      this.metricsRecorder.endTick();
   }

   public boolean isRecordingMetrics() {
      return this.metricsRecorder.isRecording();
   }

   public void startRecordingMetrics(Consumer<ProfileResults> pOutput, Consumer<Path> pOnMetricsRecordingFinished) {
      this.onMetricsRecordingStopped = (p_212922_) -> {
         this.stopRecordingMetrics();
         pOutput.accept(p_212922_);
      };
      this.onMetricsRecordingFinished = pOnMetricsRecordingFinished;
      this.willStartRecordingMetrics = true;
   }

   public void stopRecordingMetrics() {
      this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
   }

   public void finishRecordingMetrics() {
      this.metricsRecorder.end();
   }

   public void cancelRecordingMetrics() {
      this.metricsRecorder.cancel();
      this.profiler = this.metricsRecorder.getProfiler();
   }

   public Path getWorldPath(LevelResource pLevelResource) {
      return this.storageSource.getLevelPath(pLevelResource);
   }

   public boolean forceSynchronousWrites() {
      return true;
   }

   public StructureTemplateManager getStructureManager() {
      return this.structureTemplateManager;
   }

   public WorldData getWorldData() {
      return this.worldData;
   }

   public MinecraftServer.ReloadableResources getServerResources() {
       return resources;
   }

   public RegistryAccess.Frozen registryAccess() {
      return this.registries.compositeAccess();
   }

   public LayeredRegistryAccess<RegistryLayer> registries() {
      return this.registries;
   }

   public TextFilter createTextFilterForPlayer(ServerPlayer pPlayer) {
      return TextFilter.DUMMY;
   }

   public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer pPlayer) {
      return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(pPlayer) : new ServerPlayerGameMode(pPlayer));
   }

   @Nullable
   public GameType getForcedGameType() {
      return null;
   }

   public ResourceManager getResourceManager() {
      return this.resources.resourceManager;
   }

   public boolean isCurrentlySaving() {
      return this.isSaving;
   }

   public boolean isTimeProfilerRunning() {
      return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
   }

   public void startTimeProfiler() {
      this.debugCommandProfilerDelayStart = true;
   }

   public ProfileResults stopTimeProfiler() {
      if (this.debugCommandProfiler == null) {
         return EmptyProfileResults.EMPTY;
      } else {
         ProfileResults profileresults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
         this.debugCommandProfiler = null;
         return profileresults;
      }
   }

   public int getMaxChainedNeighborUpdates() {
      return 1000000;
   }

   public void logChatMessage(Component pContent, ChatType.Bound pBoundChatType, @Nullable String pHeader) {
      String s = pBoundChatType.decorate(pContent).getString();
      if (pHeader != null) {
         LOGGER.info("[{}] {}", pHeader, s);
      } else {
         LOGGER.info("{}", (Object)s);
      }

   }

   public ChatDecorator getChatDecorator() {
      return ChatDecorator.PLAIN;
   }

   public static record ReloadableResources(CloseableResourceManager resourceManager, ReloadableServerResources managers) implements AutoCloseable {
      public void close() {
         this.resourceManager.close();
      }
   }

   public static record ServerResourcePackInfo(String url, String hash, boolean isRequired, @Nullable Component prompt) {
   }

   static class TimeProfiler {
      final long startNanos;
      final int startTick;

      TimeProfiler(long pStartNanos, int pStartTick) {
         this.startNanos = pStartNanos;
         this.startTick = pStartTick;
      }

      ProfileResults stop(final long pEndTimeNano, final int pEndTimeTicks) {
         return new ProfileResults() {
            public List<ResultField> getTimes(String p_177972_) {
               return Collections.emptyList();
            }

            public boolean saveResults(Path p_177974_) {
               return false;
            }

            public long getStartTimeNano() {
               return TimeProfiler.this.startNanos;
            }

            public int getStartTimeTicks() {
               return TimeProfiler.this.startTick;
            }

            public long getEndTimeNano() {
               return pEndTimeNano;
            }

            public int getEndTimeTicks() {
               return pEndTimeTicks;
            }

            public String getProfilerResults() {
               return "";
            }
         };
      }
   }
}
