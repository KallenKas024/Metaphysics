package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class MapIndex extends SavedData {
   public static final String FILE_NAME = "idcounts";
   private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

   public MapIndex() {
      this.usedAuxIds.defaultReturnValue(-1);
   }

   public static MapIndex load(CompoundTag pCompoundTag) {
      MapIndex mapindex = new MapIndex();

      for(String s : pCompoundTag.getAllKeys()) {
         if (pCompoundTag.contains(s, 99)) {
            mapindex.usedAuxIds.put(s, pCompoundTag.getInt(s));
         }
      }

      return mapindex;
   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompoundTag the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundTag save(CompoundTag pCompound) {
      for(Object2IntMap.Entry<String> entry : this.usedAuxIds.object2IntEntrySet()) {
         pCompound.putInt(entry.getKey(), entry.getIntValue());
      }

      return pCompound;
   }

   public int getFreeAuxValueForMap() {
      int i = this.usedAuxIds.getInt("map") + 1;
      this.usedAuxIds.put("map", i);
      this.setDirty();
      return i;
   }
}