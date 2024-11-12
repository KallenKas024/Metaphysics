package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class SkyLightSectionStorage extends LayerLightSectionStorage<SkyLightSectionStorage.SkyDataLayerStorageMap> {
   protected SkyLightSectionStorage(LightChunkGetter pChunkSource) {
      super(LightLayer.SKY, pChunkSource, new SkyLightSectionStorage.SkyDataLayerStorageMap(new Long2ObjectOpenHashMap<>(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
   }

   protected int getLightValue(long pLevelPos) {
      return this.getLightValue(pLevelPos, false);
   }

   protected int getLightValue(long pPackedPos, boolean pUpdateAll) {
      long i = SectionPos.blockToSection(pPackedPos);
      int j = SectionPos.y(i);
      SkyLightSectionStorage.SkyDataLayerStorageMap skylightsectionstorage$skydatalayerstoragemap = pUpdateAll ? this.updatingSectionData : this.visibleSectionData;
      int k = skylightsectionstorage$skydatalayerstoragemap.topSections.get(SectionPos.getZeroNode(i));
      if (k != skylightsectionstorage$skydatalayerstoragemap.currentLowestY && j < k) {
         DataLayer datalayer = this.getDataLayer(skylightsectionstorage$skydatalayerstoragemap, i);
         if (datalayer == null) {
            for(pPackedPos = BlockPos.getFlatIndex(pPackedPos); datalayer == null; datalayer = this.getDataLayer(skylightsectionstorage$skydatalayerstoragemap, i)) {
               ++j;
               if (j >= k) {
                  return 15;
               }

               i = SectionPos.offset(i, Direction.UP);
            }
         }

         return datalayer.get(SectionPos.sectionRelative(BlockPos.getX(pPackedPos)), SectionPos.sectionRelative(BlockPos.getY(pPackedPos)), SectionPos.sectionRelative(BlockPos.getZ(pPackedPos)));
      } else {
         return pUpdateAll && !this.lightOnInSection(i) ? 0 : 15;
      }
   }

   protected void onNodeAdded(long pSectionPos) {
      int i = SectionPos.y(pSectionPos);
      if ((this.updatingSectionData).currentLowestY > i) {
         (this.updatingSectionData).currentLowestY = i;
         (this.updatingSectionData).topSections.defaultReturnValue((this.updatingSectionData).currentLowestY);
      }

      long j = SectionPos.getZeroNode(pSectionPos);
      int k = (this.updatingSectionData).topSections.get(j);
      if (k < i + 1) {
         (this.updatingSectionData).topSections.put(j, i + 1);
      }

   }

   protected void onNodeRemoved(long pSectionPos) {
      long i = SectionPos.getZeroNode(pSectionPos);
      int j = SectionPos.y(pSectionPos);
      if ((this.updatingSectionData).topSections.get(i) == j + 1) {
         long k;
         for(k = pSectionPos; !this.storingLightForSection(k) && this.hasLightDataAtOrBelow(j); k = SectionPos.offset(k, Direction.DOWN)) {
            --j;
         }

         if (this.storingLightForSection(k)) {
            (this.updatingSectionData).topSections.put(i, j + 1);
         } else {
            (this.updatingSectionData).topSections.remove(i);
         }
      }

   }

   protected DataLayer createDataLayer(long pSectionPos) {
      DataLayer datalayer = this.queuedSections.get(pSectionPos);
      if (datalayer != null) {
         return datalayer;
      } else {
         int i = (this.updatingSectionData).topSections.get(SectionPos.getZeroNode(pSectionPos));
         if (i != (this.updatingSectionData).currentLowestY && SectionPos.y(pSectionPos) < i) {
            DataLayer datalayer1;
            for(long j = SectionPos.offset(pSectionPos, Direction.UP); (datalayer1 = this.getDataLayer(j, true)) == null; j = SectionPos.offset(j, Direction.UP)) {
            }

            return repeatFirstLayer(datalayer1);
         } else {
            return this.lightOnInSection(pSectionPos) ? new DataLayer(15) : new DataLayer();
         }
      }
   }

   private static DataLayer repeatFirstLayer(DataLayer pDataLayer) {
      if (pDataLayer.isDefinitelyHomogenous()) {
         return pDataLayer.copy();
      } else {
         byte[] abyte = pDataLayer.getData();
         byte[] abyte1 = new byte[2048];

         for(int i = 0; i < 16; ++i) {
            System.arraycopy(abyte, 0, abyte1, i * 128, 128);
         }

         return new DataLayer(abyte1);
      }
   }

   protected boolean hasLightDataAtOrBelow(int pY) {
      return pY >= (this.updatingSectionData).currentLowestY;
   }

   protected boolean isAboveData(long pSectionPos) {
      long i = SectionPos.getZeroNode(pSectionPos);
      int j = (this.updatingSectionData).topSections.get(i);
      return j == (this.updatingSectionData).currentLowestY || SectionPos.y(pSectionPos) >= j;
   }

   protected int getTopSectionY(long pSectionPos) {
      return (this.updatingSectionData).topSections.get(pSectionPos);
   }

   protected int getBottomSectionY() {
      return (this.updatingSectionData).currentLowestY;
   }

   protected static final class SkyDataLayerStorageMap extends DataLayerStorageMap<SkyLightSectionStorage.SkyDataLayerStorageMap> {
      int currentLowestY;
      final Long2IntOpenHashMap topSections;

      public SkyDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> pMap, Long2IntOpenHashMap pTopSections, int pCurrentLowestY) {
         super(pMap);
         this.topSections = pTopSections;
         pTopSections.defaultReturnValue(pCurrentLowestY);
         this.currentLowestY = pCurrentLowestY;
      }

      public SkyLightSectionStorage.SkyDataLayerStorageMap copy() {
         return new SkyLightSectionStorage.SkyDataLayerStorageMap(this.map.clone(), this.topSections.clone(), this.currentLowestY);
      }
   }
}