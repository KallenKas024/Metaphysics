package net.minecraft.world.level.storage;

import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public interface ServerLevelData extends WritableLevelData {
   /**
    * Get current world name
    */
   String getLevelName();

   /**
    * Sets whether it is thundering or not.
    */
   void setThundering(boolean pThundering);

   /**
    * Return the number of ticks until rain.
    */
   int getRainTime();

   /**
    * Sets the number of ticks until rain.
    */
   void setRainTime(int pTime);

   /**
    * Defines the number of ticks until next thunderbolt.
    */
   void setThunderTime(int pTime);

   /**
    * Returns the number of ticks until next thunderbolt.
    */
   int getThunderTime();

   default void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      WritableLevelData.super.fillCrashReportCategory(pCrashReportCategory, pLevel);
      pCrashReportCategory.setDetail("Level name", this::getLevelName);
      pCrashReportCategory.setDetail("Level game mode", () -> {
         return String.format(Locale.ROOT, "Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands());
      });
      pCrashReportCategory.setDetail("Level weather", () -> {
         return String.format(Locale.ROOT, "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering());
      });
   }

   int getClearWeatherTime();

   void setClearWeatherTime(int pTime);

   int getWanderingTraderSpawnDelay();

   void setWanderingTraderSpawnDelay(int pDelay);

   int getWanderingTraderSpawnChance();

   void setWanderingTraderSpawnChance(int pChance);

   @Nullable
   UUID getWanderingTraderId();

   void setWanderingTraderId(UUID pId);

   /**
    * Gets the GameType.
    */
   GameType getGameType();

   void setWorldBorder(WorldBorder.Settings pSerializer);

   WorldBorder.Settings getWorldBorder();

   /**
    * Returns {@code true} if the World is initialized.
    */
   boolean isInitialized();

   /**
    * Sets the initialization status of the World.
    */
   void setInitialized(boolean pInitialized);

   /**
    * Returns {@code true} if commands are allowed on this World.
    */
   boolean getAllowCommands();

   void setGameType(GameType pType);

   TimerQueue<MinecraftServer> getScheduledEvents();

   void setGameTime(long pTime);

   /**
    * Set current world time
    */
   void setDayTime(long pTime);
}