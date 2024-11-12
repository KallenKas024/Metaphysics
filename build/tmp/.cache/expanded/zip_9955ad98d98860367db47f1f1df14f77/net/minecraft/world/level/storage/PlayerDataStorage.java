package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class PlayerDataStorage {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final File playerDir;
   protected final DataFixer fixerUpper;

   public PlayerDataStorage(LevelStorageSource.LevelStorageAccess pLevelStorageAccess, DataFixer pFixerUpper) {
      this.fixerUpper = pFixerUpper;
      this.playerDir = pLevelStorageAccess.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
      this.playerDir.mkdirs();
   }

   public void save(Player pPlayer) {
      try {
         CompoundTag compoundtag = pPlayer.saveWithoutId(new CompoundTag());
         File file1 = File.createTempFile(pPlayer.getStringUUID() + "-", ".dat", this.playerDir);
         NbtIo.writeCompressed(compoundtag, file1);
         File file2 = new File(this.playerDir, pPlayer.getStringUUID() + ".dat");
         File file3 = new File(this.playerDir, pPlayer.getStringUUID() + ".dat_old");
         Util.safeReplaceFile(file2, file1, file3);
         net.minecraftforge.event.ForgeEventFactory.firePlayerSavingEvent(pPlayer, playerDir, pPlayer.getStringUUID());
      } catch (Exception exception) {
         LOGGER.warn("Failed to save player data for {}", (Object)pPlayer.getName().getString());
      }

   }

   @Nullable
   public CompoundTag load(Player pPlayer) {
      CompoundTag compoundtag = null;

      try {
         File file1 = new File(this.playerDir, pPlayer.getStringUUID() + ".dat");
         if (file1.exists() && file1.isFile()) {
            compoundtag = NbtIo.readCompressed(file1);
         }
      } catch (Exception exception) {
         LOGGER.warn("Failed to load player data for {}", (Object)pPlayer.getName().getString());
      }

      if (compoundtag != null) {
         int i = NbtUtils.getDataVersion(compoundtag, -1);
         pPlayer.load(DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, compoundtag, i));
      }
      net.minecraftforge.event.ForgeEventFactory.firePlayerLoadingEvent(pPlayer, playerDir, pPlayer.getStringUUID());

      return compoundtag;
   }

   public String[] getSeenPlayers() {
      String[] astring = this.playerDir.list();
      if (astring == null) {
         astring = new String[0];
      }

      for(int i = 0; i < astring.length; ++i) {
         if (astring[i].endsWith(".dat")) {
            astring[i] = astring[i].substring(0, astring[i].length() - 4);
         }
      }

      return astring;
   }

   public File getPlayerDataFolder() {
      return playerDir;
   }
}
