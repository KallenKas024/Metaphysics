package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public class DerivedLevelData implements ServerLevelData {
   private final WorldData worldData;
   private final ServerLevelData wrapped;

   public DerivedLevelData(WorldData pWorldData, ServerLevelData pWrapped) {
      this.worldData = pWorldData;
      this.wrapped = pWrapped;
   }

   /**
    * Returns the x spawn position
    */
   public int getXSpawn() {
      return this.wrapped.getXSpawn();
   }

   /**
    * Return the Y axis spawning point of the player.
    */
   public int getYSpawn() {
      return this.wrapped.getYSpawn();
   }

   /**
    * Returns the z spawn position
    */
   public int getZSpawn() {
      return this.wrapped.getZSpawn();
   }

   public float getSpawnAngle() {
      return this.wrapped.getSpawnAngle();
   }

   public long getGameTime() {
      return this.wrapped.getGameTime();
   }

   /**
    * Get current world time
    */
   public long getDayTime() {
      return this.wrapped.getDayTime();
   }

   /**
    * Get current world name
    */
   public String getLevelName() {
      return this.worldData.getLevelName();
   }

   public int getClearWeatherTime() {
      return this.wrapped.getClearWeatherTime();
   }

   public void setClearWeatherTime(int pTime) {
   }

   /**
    * Returns {@code true} if it is thundering, {@code false} otherwise.
    */
   public boolean isThundering() {
      return this.wrapped.isThundering();
   }

   /**
    * Returns the number of ticks until next thunderbolt.
    */
   public int getThunderTime() {
      return this.wrapped.getThunderTime();
   }

   /**
    * Returns {@code true} if it is raining, {@code false} otherwise.
    */
   public boolean isRaining() {
      return this.wrapped.isRaining();
   }

   /**
    * Return the number of ticks until rain.
    */
   public int getRainTime() {
      return this.wrapped.getRainTime();
   }

   /**
    * Gets the GameType.
    */
   public GameType getGameType() {
      return this.worldData.getGameType();
   }

   /**
    * Set the x spawn position to the passed in value
    */
   public void setXSpawn(int pX) {
   }

   /**
    * Sets the y spawn position
    */
   public void setYSpawn(int pY) {
   }

   /**
    * Set the z spawn position to the passed in value
    */
   public void setZSpawn(int pZ) {
   }

   public void setSpawnAngle(float pAngle) {
   }

   public void setGameTime(long pTime) {
   }

   /**
    * Set current world time
    */
   public void setDayTime(long pTime) {
   }

   public void setSpawn(BlockPos pSpawnPoint, float pAngle) {
   }

   /**
    * Sets whether it is thundering or not.
    */
   public void setThundering(boolean pThundering) {
   }

   /**
    * Defines the number of ticks until next thunderbolt.
    */
   public void setThunderTime(int pTime) {
   }

   /**
    * Sets whether it is raining or not.
    */
   public void setRaining(boolean pIsRaining) {
   }

   /**
    * Sets the number of ticks until rain.
    */
   public void setRainTime(int pTime) {
   }

   public void setGameType(GameType pType) {
   }

   /**
    * Returns {@code true} if hardcore mode is enabled, otherwise {@code false}.
    */
   public boolean isHardcore() {
      return this.worldData.isHardcore();
   }

   /**
    * Returns {@code true} if commands are allowed on this World.
    */
   public boolean getAllowCommands() {
      return this.worldData.getAllowCommands();
   }

   /**
    * Returns {@code true} if the World is initialized.
    */
   public boolean isInitialized() {
      return this.wrapped.isInitialized();
   }

   /**
    * Sets the initialization status of the World.
    */
   public void setInitialized(boolean pInitialized) {
   }

   /**
    * Gets the GameRules class Instance.
    */
   public GameRules getGameRules() {
      return this.worldData.getGameRules();
   }

   public WorldBorder.Settings getWorldBorder() {
      return this.wrapped.getWorldBorder();
   }

   public void setWorldBorder(WorldBorder.Settings pSerializer) {
   }

   public Difficulty getDifficulty() {
      return this.worldData.getDifficulty();
   }

   public boolean isDifficultyLocked() {
      return this.worldData.isDifficultyLocked();
   }

   public TimerQueue<MinecraftServer> getScheduledEvents() {
      return this.wrapped.getScheduledEvents();
   }

   public int getWanderingTraderSpawnDelay() {
      return 0;
   }

   public void setWanderingTraderSpawnDelay(int pDelay) {
   }

   public int getWanderingTraderSpawnChance() {
      return 0;
   }

   public void setWanderingTraderSpawnChance(int pChance) {
   }

   public UUID getWanderingTraderId() {
      return null;
   }

   public void setWanderingTraderId(UUID pId) {
   }

   public void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      pCrashReportCategory.setDetail("Derived", true);
      this.wrapped.fillCrashReportCategory(pCrashReportCategory, pLevel);
   }
}