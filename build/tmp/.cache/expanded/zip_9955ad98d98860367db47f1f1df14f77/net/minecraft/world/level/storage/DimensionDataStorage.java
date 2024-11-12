package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<String, SavedData> cache = Maps.newHashMap();
   private final DataFixer fixerUpper;
   private final File dataFolder;

   public DimensionDataStorage(File pDataFolder, DataFixer pFixerUpper) {
      this.fixerUpper = pFixerUpper;
      this.dataFolder = pDataFolder;
   }

   private File getDataFile(String pName) {
      return new File(this.dataFolder, pName + ".dat");
   }

   public <T extends SavedData> T computeIfAbsent(Function<CompoundTag, T> pLoadFunction, Supplier<T> pCreateFunction, String pName) {
      T t = this.get(pLoadFunction, pName);
      if (t != null) {
         return t;
      } else {
         T t1 = pCreateFunction.get();
         this.set(pName, t1);
         return t1;
      }
   }

   @Nullable
   public <T extends SavedData> T get(Function<CompoundTag, T> pLoadFunction, String pName) {
      SavedData saveddata = this.cache.get(pName);
      if (saveddata == net.minecraftforge.common.util.DummySavedData.DUMMY) return null;
      if (saveddata == null && !this.cache.containsKey(pName)) {
         saveddata = this.readSavedData(pLoadFunction, pName);
         this.cache.put(pName, saveddata);
      } else if (saveddata == null) {
         this.cache.put(pName, net.minecraftforge.common.util.DummySavedData.DUMMY);
         return null;
      }

      return (T)saveddata;
   }

   @Nullable
   private <T extends SavedData> T readSavedData(Function<CompoundTag, T> pLoadFunction, String pName) {
      try {
         File file1 = this.getDataFile(pName);
         if (file1.exists()) {
            CompoundTag compoundtag = this.readTagFromDisk(pName, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            return pLoadFunction.apply(compoundtag.getCompound("data"));
         }
      } catch (Exception exception) {
         LOGGER.error("Error loading saved data: {}", pName, exception);
      }

      return (T)null;
   }

   public void set(String pName, SavedData pSavedData) {
      this.cache.put(pName, pSavedData);
   }

   public CompoundTag readTagFromDisk(String pName, int pLevelVersion) throws IOException {
      File file1 = this.getDataFile(pName);

      CompoundTag compoundtag1;
      try (
         FileInputStream fileinputstream = new FileInputStream(file1);
         PushbackInputStream pushbackinputstream = new PushbackInputStream(fileinputstream, 2);
      ) {
         CompoundTag compoundtag;
         if (this.isGzip(pushbackinputstream)) {
            compoundtag = NbtIo.readCompressed(pushbackinputstream);
         } else {
            try (DataInputStream datainputstream = new DataInputStream(pushbackinputstream)) {
               compoundtag = NbtIo.read(datainputstream);
            }
         }

         int i = NbtUtils.getDataVersion(compoundtag, 1343);
         compoundtag1 = DataFixTypes.SAVED_DATA.update(this.fixerUpper, compoundtag, i, pLevelVersion);
      }

      return compoundtag1;
   }

   private boolean isGzip(PushbackInputStream pInputStream) throws IOException {
      byte[] abyte = new byte[2];
      boolean flag = false;
      int i = pInputStream.read(abyte, 0, 2);
      if (i == 2) {
         int j = (abyte[1] & 255) << 8 | abyte[0] & 255;
         if (j == 35615) {
            flag = true;
         }
      }

      if (i != 0) {
         pInputStream.unread(abyte, 0, i);
      }

      return flag;
   }

   public void save() {
      this.cache.forEach((p_164866_, p_164867_) -> {
         if (p_164867_ != null) {
            p_164867_.save(this.getDataFile(p_164866_));
         }

      });
   }
}
