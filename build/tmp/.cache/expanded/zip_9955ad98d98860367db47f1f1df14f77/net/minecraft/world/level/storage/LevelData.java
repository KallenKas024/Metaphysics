package net.minecraft.world.level.storage;

import java.util.Locale;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelHeightAccessor;

public interface LevelData {
   /**
    * Returns the x spawn position
    */
   int getXSpawn();

   /**
    * Return the Y axis spawning point of the player.
    */
   int getYSpawn();

   /**
    * Returns the z spawn position
    */
   int getZSpawn();

   float getSpawnAngle();

   long getGameTime();

   /**
    * Get current world time
    */
   long getDayTime();

   /**
    * Returns {@code true} if it is thundering, {@code false} otherwise.
    */
   boolean isThundering();

   /**
    * Returns {@code true} if it is raining, {@code false} otherwise.
    */
   boolean isRaining();

   /**
    * Sets whether it is raining or not.
    */
   void setRaining(boolean pRaining);

   /**
    * Returns {@code true} if hardcore mode is enabled, otherwise {@code false}.
    */
   boolean isHardcore();

   /**
    * Gets the GameRules class Instance.
    */
   GameRules getGameRules();

   Difficulty getDifficulty();

   boolean isDifficultyLocked();

   default void fillCrashReportCategory(CrashReportCategory pCrashReportCategory, LevelHeightAccessor pLevel) {
      pCrashReportCategory.setDetail("Level spawn location", () -> {
         return CrashReportCategory.formatLocation(pLevel, this.getXSpawn(), this.getYSpawn(), this.getZSpawn());
      });
      pCrashReportCategory.setDetail("Level time", () -> {
         return String.format(Locale.ROOT, "%d game time, %d day time", this.getGameTime(), this.getDayTime());
      });
   }
}