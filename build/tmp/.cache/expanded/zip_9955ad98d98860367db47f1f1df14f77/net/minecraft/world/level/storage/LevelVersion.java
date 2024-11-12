package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;

public class LevelVersion {
   private final int levelDataVersion;
   private final long lastPlayed;
   private final String minecraftVersionName;
   private final DataVersion minecraftVersion;
   private final boolean snapshot;

   private LevelVersion(int pLevelDataVersion, long pLastPlayed, String pMinecraftVersionName, int pMinecraftVersion, String pSeries, boolean pSnapshot) {
      this.levelDataVersion = pLevelDataVersion;
      this.lastPlayed = pLastPlayed;
      this.minecraftVersionName = pMinecraftVersionName;
      this.minecraftVersion = new DataVersion(pMinecraftVersion, pSeries);
      this.snapshot = pSnapshot;
   }

   public static LevelVersion parse(Dynamic<?> pNbt) {
      int i = pNbt.get("version").asInt(0);
      long j = pNbt.get("LastPlayed").asLong(0L);
      OptionalDynamic<?> optionaldynamic = pNbt.get("Version");
      return optionaldynamic.result().isPresent() ? new LevelVersion(i, j, optionaldynamic.get("Name").asString(SharedConstants.getCurrentVersion().getName()), optionaldynamic.get("Id").asInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion()), optionaldynamic.get("Series").asString(DataVersion.MAIN_SERIES), optionaldynamic.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().isStable())) : new LevelVersion(i, j, "", 0, DataVersion.MAIN_SERIES, false);
   }

   public int levelDataVersion() {
      return this.levelDataVersion;
   }

   public long lastPlayed() {
      return this.lastPlayed;
   }

   public String minecraftVersionName() {
      return this.minecraftVersionName;
   }

   public DataVersion minecraftVersion() {
      return this.minecraftVersion;
   }

   public boolean snapshot() {
      return this.snapshot;
   }
}