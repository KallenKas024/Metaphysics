package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.slf4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final String PLAYER = "Player";
   protected static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
   private LevelSettings settings;
   private final WorldOptions worldOptions;
   private final PrimaryLevelData.SpecialWorldProperty specialWorldProperty;
   private final Lifecycle worldGenSettingsLifecycle;
   private int xSpawn;
   private int ySpawn;
   private int zSpawn;
   private float spawnAngle;
   private long gameTime;
   private long dayTime;
   @Nullable
   private final DataFixer fixerUpper;
   private final int playerDataVersion;
   private boolean upgradedPlayerTag;
   @Nullable
   private CompoundTag loadedPlayerTag;
   private final int version;
   private int clearWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private boolean initialized;
   private boolean difficultyLocked;
   private WorldBorder.Settings worldBorder;
   private EndDragonFight.Data endDragonFightData;
   @Nullable
   private CompoundTag customBossEvents;
   private int wanderingTraderSpawnDelay;
   private int wanderingTraderSpawnChance;
   @Nullable
   private UUID wanderingTraderId;
   private final Set<String> knownServerBrands;
   private boolean wasModded;
   private final Set<String> removedFeatureFlags;
   private final TimerQueue<MinecraftServer> scheduledEvents;
   private boolean confirmedExperimentalWarning = false;

   private PrimaryLevelData(@Nullable DataFixer pFixerUpper, int pPlayerDataVersion, @Nullable CompoundTag pLoadedPlayerTag, boolean pWasModded, int pXSpawn, int pYSpawn, int pZSpawn, float pSpawnAngle, long pGameTime, long pDayTime, int pVersion, int pClearWeatherTime, int pRainTime, boolean pRaining, int pThunderTime, boolean pThundering, boolean pInitialized, boolean pDifficultyLocked, WorldBorder.Settings pWorldBorder, int pWanderingTraderSpawnDelay, int pWanderingTraderSpawnChance, @Nullable UUID pWanderingTraderId, Set<String> pKnownServerBrands, Set<String> pRemoveFeatureFlags, TimerQueue<MinecraftServer> pScheduledEvents, @Nullable CompoundTag pCustomBossEvents, EndDragonFight.Data pEndDragonFightData, LevelSettings pSettings, WorldOptions pWorldOptions, PrimaryLevelData.SpecialWorldProperty pSpecialWorldProperty, Lifecycle pWorldGenSettingsLifecycle) {
      this.fixerUpper = pFixerUpper;
      this.wasModded = pWasModded;
      this.xSpawn = pXSpawn;
      this.ySpawn = pYSpawn;
      this.zSpawn = pZSpawn;
      this.spawnAngle = pSpawnAngle;
      this.gameTime = pGameTime;
      this.dayTime = pDayTime;
      this.version = pVersion;
      this.clearWeatherTime = pClearWeatherTime;
      this.rainTime = pRainTime;
      this.raining = pRaining;
      this.thunderTime = pThunderTime;
      this.thundering = pThundering;
      this.initialized = pInitialized;
      this.difficultyLocked = pDifficultyLocked;
      this.worldBorder = pWorldBorder;
      this.wanderingTraderSpawnDelay = pWanderingTraderSpawnDelay;
      this.wanderingTraderSpawnChance = pWanderingTraderSpawnChance;
      this.wanderingTraderId = pWanderingTraderId;
      this.knownServerBrands = pKnownServerBrands;
      this.removedFeatureFlags = pRemoveFeatureFlags;
      this.loadedPlayerTag = pLoadedPlayerTag;
      this.playerDataVersion = pPlayerDataVersion;
      this.scheduledEvents = pScheduledEvents;
      this.customBossEvents = pCustomBossEvents;
      this.endDragonFightData = pEndDragonFightData;
      this.settings = pSettings;
      this.worldOptions = pWorldOptions;
      this.specialWorldProperty = pSpecialWorldProperty;
      this.worldGenSettingsLifecycle = pWorldGenSettingsLifecycle;
   }

   public PrimaryLevelData(LevelSettings pSettings, WorldOptions pWorldOptions, PrimaryLevelData.SpecialWorldProperty pSpecialWorldProperty, Lifecycle pWorldGenSettingsLifecycle) {
      this((DataFixer)null, SharedConstants.getCurrentVersion().getDataVersion().getVersion(), (CompoundTag)null, false, 0, 0, 0, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, (UUID)null, Sets.newLinkedHashSet(), new HashSet<>(), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS), (CompoundTag)null, EndDragonFight.Data.DEFAULT, pSettings.copy(), pWorldOptions, pSpecialWorldProperty, pWorldGenSettingsLifecycle);
   }

   public static <T> PrimaryLevelData parse(Dynamic<T> pDynamic, DataFixer pFixerUpper, int pPlayerDataVersion, @Nullable CompoundTag pLoadedPlayerTag, LevelSettings pSettings, LevelVersion pLevelVersion, PrimaryLevelData.SpecialWorldProperty pSpecialWorldProperty, WorldOptions pWorldOptions, Lifecycle pWorldGenSettingsLifecycle) {
      long i = pDynamic.get("Time").asLong(0L);
      return new PrimaryLevelData(pFixerUpper, pPlayerDataVersion, pLoadedPlayerTag, pDynamic.get("WasModded").asBoolean(false), pDynamic.get("SpawnX").asInt(0), pDynamic.get("SpawnY").asInt(0), pDynamic.get("SpawnZ").asInt(0), pDynamic.get("SpawnAngle").asFloat(0.0F), i, pDynamic.get("DayTime").asLong(i), pLevelVersion.levelDataVersion(), pDynamic.get("clearWeatherTime").asInt(0), pDynamic.get("rainTime").asInt(0), pDynamic.get("raining").asBoolean(false), pDynamic.get("thunderTime").asInt(0), pDynamic.get("thundering").asBoolean(false), pDynamic.get("initialized").asBoolean(true), pDynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Settings.read(pDynamic, WorldBorder.DEFAULT_SETTINGS), pDynamic.get("WanderingTraderSpawnDelay").asInt(0), pDynamic.get("WanderingTraderSpawnChance").asInt(0), pDynamic.get("WanderingTraderId").read(UUIDUtil.CODEC).result().orElse((UUID)null), pDynamic.get("ServerBrands").asStream().flatMap((p_78529_) -> {
         return p_78529_.asString().result().stream();
      }).collect(Collectors.toCollection(Sets::newLinkedHashSet)), pDynamic.get("removed_features").asStream().flatMap((p_277335_) -> {
         return p_277335_.asString().result().stream();
      }).collect(Collectors.toSet()), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, pDynamic.get("ScheduledEvents").asStream()), (CompoundTag)pDynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), pDynamic.get("DragonFight").read(EndDragonFight.Data.CODEC).resultOrPartial(LOGGER::error).orElse(EndDragonFight.Data.DEFAULT), pSettings, pWorldOptions, pSpecialWorldProperty, pWorldGenSettingsLifecycle).withConfirmedWarning(pWorldGenSettingsLifecycle != Lifecycle.stable() && pDynamic.get("confirmedExperimentalSettings").asBoolean(false));
   }

   public CompoundTag createTag(RegistryAccess pRegistries, @Nullable CompoundTag pHostPlayerNBT) {
      this.updatePlayerTag();
      if (pHostPlayerNBT == null) {
         pHostPlayerNBT = this.loadedPlayerTag;
      }

      CompoundTag compoundtag = new CompoundTag();
      this.setTagData(pRegistries, compoundtag, pHostPlayerNBT);
      return compoundtag;
   }

   private void setTagData(RegistryAccess pRegistry, CompoundTag pNbt, @Nullable CompoundTag pPlayerNBT) {
      pNbt.put("ServerBrands", stringCollectionToTag(this.knownServerBrands));
      pNbt.putBoolean("WasModded", this.wasModded);
      if (!this.removedFeatureFlags.isEmpty()) {
         pNbt.put("removed_features", stringCollectionToTag(this.removedFeatureFlags));
      }

      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("Name", SharedConstants.getCurrentVersion().getName());
      compoundtag.putInt("Id", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
      compoundtag.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
      compoundtag.putString("Series", SharedConstants.getCurrentVersion().getDataVersion().getSeries());
      pNbt.put("Version", compoundtag);
      NbtUtils.addCurrentDataVersion(pNbt);
      DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, pRegistry);
      WorldGenSettings.encode(dynamicops, this.worldOptions, pRegistry).resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).ifPresent((p_78574_) -> {
         pNbt.put("WorldGenSettings", p_78574_);
      });
      pNbt.putInt("GameType", this.settings.gameType().getId());
      pNbt.putInt("SpawnX", this.xSpawn);
      pNbt.putInt("SpawnY", this.ySpawn);
      pNbt.putInt("SpawnZ", this.zSpawn);
      pNbt.putFloat("SpawnAngle", this.spawnAngle);
      pNbt.putLong("Time", this.gameTime);
      pNbt.putLong("DayTime", this.dayTime);
      pNbt.putLong("LastPlayed", Util.getEpochMillis());
      pNbt.putString("LevelName", this.settings.levelName());
      pNbt.putInt("version", 19133);
      pNbt.putInt("clearWeatherTime", this.clearWeatherTime);
      pNbt.putInt("rainTime", this.rainTime);
      pNbt.putBoolean("raining", this.raining);
      pNbt.putInt("thunderTime", this.thunderTime);
      pNbt.putBoolean("thundering", this.thundering);
      pNbt.putBoolean("hardcore", this.settings.hardcore());
      pNbt.putBoolean("allowCommands", this.settings.allowCommands());
      pNbt.putBoolean("initialized", this.initialized);
      this.worldBorder.write(pNbt);
      pNbt.putByte("Difficulty", (byte)this.settings.difficulty().getId());
      pNbt.putBoolean("DifficultyLocked", this.difficultyLocked);
      pNbt.put("GameRules", this.settings.gameRules().createTag());
      pNbt.put("DragonFight", Util.getOrThrow(EndDragonFight.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.endDragonFightData), IllegalStateException::new));
      if (pPlayerNBT != null) {
         pNbt.put("Player", pPlayerNBT);
      }

      DataResult<Tag> dataresult = WorldDataConfiguration.CODEC.encodeStart(NbtOps.INSTANCE, this.settings.getDataConfiguration());
      dataresult.get().ifLeft((p_248505_) -> {
         pNbt.merge((CompoundTag)p_248505_);
      }).ifRight((p_248506_) -> {
         LOGGER.warn("Failed to encode configuration {}", (Object)p_248506_.message());
      });
      if (this.customBossEvents != null) {
         pNbt.put("CustomBossEvents", this.customBossEvents);
      }

      pNbt.put("ScheduledEvents", this.scheduledEvents.store());
      pNbt.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
      pNbt.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
      if (this.wanderingTraderId != null) {
         pNbt.putUUID("WanderingTraderId", this.wanderingTraderId);
      }

      pNbt.putString("forgeLifecycle", net.minecraftforge.common.ForgeHooks.encodeLifecycle(this.settings.getLifecycle()));
      pNbt.putBoolean("confirmedExperimentalSettings", this.confirmedExperimentalWarning);
   }

   private static ListTag stringCollectionToTag(Set<String> pStringCollection) {
      ListTag listtag = new ListTag();
      pStringCollection.stream().map(StringTag::valueOf).forEach(listtag::add);
      return listtag;
   }

   /**
    * Returns the x spawn position
    */
   public int getXSpawn() {
      return this.xSpawn;
   }

   /**
    * Return the Y axis spawning point of the player.
    */
   public int getYSpawn() {
      return this.ySpawn;
   }

   /**
    * Returns the z spawn position
    */
   public int getZSpawn() {
      return this.zSpawn;
   }

   public float getSpawnAngle() {
      return this.spawnAngle;
   }

   public long getGameTime() {
      return this.gameTime;
   }

   /**
    * Get current world time
    */
   public long getDayTime() {
      return this.dayTime;
   }

   private void updatePlayerTag() {
      if (!this.upgradedPlayerTag && this.loadedPlayerTag != null) {
         if (this.playerDataVersion < SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
            if (this.fixerUpper == null) {
               throw (NullPointerException)Util.pauseInIde(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
            }

            this.loadedPlayerTag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, this.loadedPlayerTag, this.playerDataVersion);
         }

         this.upgradedPlayerTag = true;
      }
   }

   public CompoundTag getLoadedPlayerTag() {
      this.updatePlayerTag();
      return this.loadedPlayerTag;
   }

   /**
    * Set the x spawn position to the passed in value
    */
   public void setXSpawn(int pX) {
      this.xSpawn = pX;
   }

   /**
    * Sets the y spawn position
    */
   public void setYSpawn(int pY) {
      this.ySpawn = pY;
   }

   /**
    * Set the z spawn position to the passed in value
    */
   public void setZSpawn(int pZ) {
      this.zSpawn = pZ;
   }

   public void setSpawnAngle(float pAngle) {
      this.spawnAngle = pAngle;
   }

   public void setGameTime(long pTime) {
      this.gameTime = pTime;
   }

   /**
    * Set current world time
    */
   public void setDayTime(long pTime) {
      this.dayTime = pTime;
   }

   public void setSpawn(BlockPos pSpawnPoint, float pAngle) {
      this.xSpawn = pSpawnPoint.getX();
      this.ySpawn = pSpawnPoint.getY();
      this.zSpawn = pSpawnPoint.getZ();
      this.spawnAngle = pAngle;
   }

   /**
    * Get current world name
    */
   public String getLevelName() {
      return this.settings.levelName();
   }

   public int getVersion() {
      return this.version;
   }

   public int getClearWeatherTime() {
      return this.clearWeatherTime;
   }

   public void setClearWeatherTime(int pTime) {
      this.clearWeatherTime = pTime;
   }

   /**
    * Returns {@code true} if it is thundering, {@code false} otherwise.
    */
   public boolean isThundering() {
      return this.thundering;
   }

   /**
    * Sets whether it is thundering or not.
    */
   public void setThundering(boolean pThundering) {
      this.thundering = pThundering;
   }

   /**
    * Returns the number of ticks until next thunderbolt.
    */
   public int getThunderTime() {
      return this.thunderTime;
   }

   /**
    * Defines the number of ticks until next thunderbolt.
    */
   public void setThunderTime(int pTime) {
      this.thunderTime = pTime;
   }

   /**
    * Returns {@code true} if it is raining, {@code false} otherwise.
    */
   public boolean isRaining() {
      return this.raining;
   }

   /**
    * Sets whether it is raining or not.
    */
   public void setRaining(boolean pIsRaining) {
      this.raining = pIsRaining;
   }

   /**
    * Return the number of ticks until rain.
    */
   public int getRainTime() {
      return this.rainTime;
   }

   /**
    * Sets the number of ticks until rain.
    */
   public void setRainTime(int pTime) {
      this.rainTime = pTime;
   }

   /**
    * Gets the GameType.
    */
   public GameType getGameType() {
      return this.settings.gameType();
   }

   public void setGameType(GameType pType) {
      this.settings = this.settings.withGameType(pType);
   }

   /**
    * Returns {@code true} if hardcore mode is enabled, otherwise {@code false}.
    */
   public boolean isHardcore() {
      return this.settings.hardcore();
   }

   /**
    * Returns {@code true} if commands are allowed on this World.
    */
   public boolean getAllowCommands() {
      return this.settings.allowCommands();
   }

   /**
    * Returns {@code true} if the World is initialized.
    */
   public boolean isInitialized() {
      return this.initialized;
   }

   /**
    * Sets the initialization status of the World.
    */
   public void setInitialized(boolean pInitialized) {
      this.initialized = pInitialized;
   }

   /**
    * Gets the GameRules class Instance.
    */
   public GameRules getGameRules() {
      return this.settings.gameRules();
   }

   public WorldBorder.Settings getWorldBorder() {
      return this.worldBorder;
   }

   public void setWorldBorder(WorldBorder.Settings pSerializer) {
      this.worldBorder = pSerializer;
   }

   public Difficulty getDifficulty() {
      return this.settings.difficulty();
   }

   public void setDifficulty(Difficulty pDifficulty) {
      this.settings = this.settings.withDifficulty(pDifficulty);
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   public void setDifficultyLocked(boolean pLocked) {
      this.difficultyLocked = pLocked;
   }

   public TimerQueue<MinecraftServer> getScheduledEvents() {
      return this.scheduledEvents;
   }

   public void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      ServerLevelData.super.fillCrashReportCategory(pCrashReportCategory, pLevel);
      WorldData.super.fillCrashReportCategory(pCrashReportCategory);
   }

   public WorldOptions worldGenOptions() {
      return this.worldOptions;
   }

   public boolean isFlatWorld() {
      return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.FLAT;
   }

   public boolean isDebugWorld() {
      return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
   }

   public Lifecycle worldGenSettingsLifecycle() {
      return this.worldGenSettingsLifecycle;
   }

   public EndDragonFight.Data endDragonFightData() {
      return this.endDragonFightData;
   }

   public void setEndDragonFightData(EndDragonFight.Data pEndDragonFightData) {
      this.endDragonFightData = pEndDragonFightData;
   }

   public WorldDataConfiguration getDataConfiguration() {
      return this.settings.getDataConfiguration();
   }

   public void setDataConfiguration(WorldDataConfiguration pDataConfiguration) {
      this.settings = this.settings.withDataConfiguration(pDataConfiguration);
   }

   @Nullable
   public CompoundTag getCustomBossEvents() {
      return this.customBossEvents;
   }

   public void setCustomBossEvents(@Nullable CompoundTag pNbt) {
      this.customBossEvents = pNbt;
   }

   public int getWanderingTraderSpawnDelay() {
      return this.wanderingTraderSpawnDelay;
   }

   public void setWanderingTraderSpawnDelay(int pDelay) {
      this.wanderingTraderSpawnDelay = pDelay;
   }

   public int getWanderingTraderSpawnChance() {
      return this.wanderingTraderSpawnChance;
   }

   public void setWanderingTraderSpawnChance(int pChance) {
      this.wanderingTraderSpawnChance = pChance;
   }

   @Nullable
   public UUID getWanderingTraderId() {
      return this.wanderingTraderId;
   }

   public void setWanderingTraderId(UUID pId) {
      this.wanderingTraderId = pId;
   }

   public void setModdedInfo(String pName, boolean pIsModded) {
      this.knownServerBrands.add(pName);
      this.wasModded |= pIsModded;
   }

   public boolean wasModded() {
      return this.wasModded;
   }

   public Set<String> getKnownServerBrands() {
      return ImmutableSet.copyOf(this.knownServerBrands);
   }

   public Set<String> getRemovedFeatureFlags() {
      return Set.copyOf(this.removedFeatureFlags);
   }

   public ServerLevelData overworldData() {
      return this;
   }

   public LevelSettings getLevelSettings() {
      return this.settings.copy();
   }

   public boolean hasConfirmedExperimentalWarning() {
      return this.confirmedExperimentalWarning;
   }

   public PrimaryLevelData withConfirmedWarning(boolean confirmedWarning) { // Builder-like to not patch ctor
      this.confirmedExperimentalWarning = confirmedWarning;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public static enum SpecialWorldProperty {
      NONE,
      FLAT,
      DEBUG;
   }
}
