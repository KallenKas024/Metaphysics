package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.WorldOptions;

public interface WorldData {
   int ANVIL_VERSION_ID = 19133;
   int MCREGION_VERSION_ID = 19132;

   WorldDataConfiguration getDataConfiguration();

   void setDataConfiguration(WorldDataConfiguration pDataConfiguration);

   boolean wasModded();

   Set<String> getKnownServerBrands();

   Set<String> getRemovedFeatureFlags();

   void setModdedInfo(String pName, boolean pIsModded);

   default void fillCrashReportCategory(CrashReportCategory pCategory) {
      pCategory.setDetail("Known server brands", () -> {
         return String.join(", ", this.getKnownServerBrands());
      });
      pCategory.setDetail("Removed feature flags", () -> {
         return String.join(", ", this.getRemovedFeatureFlags());
      });
      pCategory.setDetail("Level was modded", () -> {
         return Boolean.toString(this.wasModded());
      });
      pCategory.setDetail("Level storage version", () -> {
         int i = this.getVersion();
         return String.format(Locale.ROOT, "0x%05X - %s", i, this.getStorageVersionName(i));
      });
   }

   default String getStorageVersionName(int pStorageVersionId) {
      switch (pStorageVersionId) {
         case 19132:
            return "McRegion";
         case 19133:
            return "Anvil";
         default:
            return "Unknown?";
      }
   }

   @Nullable
   CompoundTag getCustomBossEvents();

   void setCustomBossEvents(@Nullable CompoundTag pNbt);

   ServerLevelData overworldData();

   LevelSettings getLevelSettings();

   CompoundTag createTag(RegistryAccess pRegistries, @Nullable CompoundTag pHostPlayerNBT);

   /**
    * Returns {@code true} if hardcore mode is enabled, otherwise {@code false}.
    */
   boolean isHardcore();

   int getVersion();

   /**
    * Get current world name
    */
   String getLevelName();

   /**
    * Gets the GameType.
    */
   GameType getGameType();

   void setGameType(GameType pType);

   /**
    * Returns {@code true} if commands are allowed on this World.
    */
   boolean getAllowCommands();

   Difficulty getDifficulty();

   void setDifficulty(Difficulty pDifficulty);

   boolean isDifficultyLocked();

   void setDifficultyLocked(boolean pLocked);

   /**
    * Gets the GameRules class Instance.
    */
   GameRules getGameRules();

   @Nullable
   CompoundTag getLoadedPlayerTag();

   EndDragonFight.Data endDragonFightData();

   void setEndDragonFightData(EndDragonFight.Data pEndDragonFightData);

   WorldOptions worldGenOptions();

   boolean isFlatWorld();

   boolean isDebugWorld();

   Lifecycle worldGenSettingsLifecycle();

   default FeatureFlagSet enabledFeatures() {
      return this.getDataConfiguration().enabledFeatures();
   }
}