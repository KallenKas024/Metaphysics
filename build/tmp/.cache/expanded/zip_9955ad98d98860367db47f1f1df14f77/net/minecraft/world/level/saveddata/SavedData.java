package net.minecraft.world.level.saveddata;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public abstract class SavedData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private boolean dirty;

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompoundTag the {@code CompoundTag} to save the {@code SavedData} to
    */
   public abstract CompoundTag save(CompoundTag pCompoundTag);

   /**
    * Marks this {@code SavedData} dirty, to be saved to disk when the level next saves.
    */
   public void setDirty() {
      this.setDirty(true);
   }

   /**
    * Sets the dirty state of this {@code SavedData}, whether it needs saving to disk.
    */
   public void setDirty(boolean pDirty) {
      this.dirty = pDirty;
   }

   /**
    * Whether this {@code SavedData} needs saving to disk.
    */
   public boolean isDirty() {
      return this.dirty;
   }

   /**
    * Saves the {@code SavedData} to disc
    * @param pFile the passed {@code java.io.File} to write the {@code SavedData} to
    */
   public void save(File pFile) {
      if (this.isDirty()) {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.put("data", this.save(new CompoundTag()));
         NbtUtils.addCurrentDataVersion(compoundtag);

         try {
            NbtIo.writeCompressed(compoundtag, pFile);
         } catch (IOException ioexception) {
            LOGGER.error("Could not save data {}", this, ioexception);
         }

         this.setDirty(false);
      }
   }
}