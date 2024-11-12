package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.chunk.DataLayer;

public abstract class DataLayerStorageMap<M extends DataLayerStorageMap<M>> {
   private static final int CACHE_SIZE = 2;
   private final long[] lastSectionKeys = new long[2];
   private final DataLayer[] lastSections = new DataLayer[2];
   private boolean cacheEnabled;
   protected final Long2ObjectOpenHashMap<DataLayer> map;

   protected DataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> pMap) {
      this.map = pMap;
      this.clearCache();
      this.cacheEnabled = true;
   }

   public abstract M copy();

   public DataLayer copyDataLayer(long pIndex) {
      DataLayer datalayer = this.map.get(pIndex).copy();
      this.map.put(pIndex, datalayer);
      this.clearCache();
      return datalayer;
   }

   public boolean hasLayer(long pSectionPos) {
      return this.map.containsKey(pSectionPos);
   }

   @Nullable
   public DataLayer getLayer(long pSectionPos) {
      if (this.cacheEnabled) {
         for(int i = 0; i < 2; ++i) {
            if (pSectionPos == this.lastSectionKeys[i]) {
               return this.lastSections[i];
            }
         }
      }

      DataLayer datalayer = this.map.get(pSectionPos);
      if (datalayer == null) {
         return null;
      } else {
         if (this.cacheEnabled) {
            for(int j = 1; j > 0; --j) {
               this.lastSectionKeys[j] = this.lastSectionKeys[j - 1];
               this.lastSections[j] = this.lastSections[j - 1];
            }

            this.lastSectionKeys[0] = pSectionPos;
            this.lastSections[0] = datalayer;
         }

         return datalayer;
      }
   }

   @Nullable
   public DataLayer removeLayer(long pSectionPos) {
      return this.map.remove(pSectionPos);
   }

   public void setLayer(long pSectionPos, DataLayer pArray) {
      this.map.put(pSectionPos, pArray);
   }

   public void clearCache() {
      for(int i = 0; i < 2; ++i) {
         this.lastSectionKeys[i] = Long.MAX_VALUE;
         this.lastSections[i] = null;
      }

   }

   public void disableCache() {
      this.cacheEnabled = false;
   }
}