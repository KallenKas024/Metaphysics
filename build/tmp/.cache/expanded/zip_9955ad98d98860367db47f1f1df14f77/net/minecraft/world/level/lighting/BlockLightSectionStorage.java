package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
   protected BlockLightSectionStorage(LightChunkGetter pChunkSource) {
      super(LightLayer.BLOCK, pChunkSource, new BlockLightSectionStorage.BlockDataLayerStorageMap(new Long2ObjectOpenHashMap<>()));
   }

   protected int getLightValue(long pLevelPos) {
      long i = SectionPos.blockToSection(pLevelPos);
      DataLayer datalayer = this.getDataLayer(i, false);
      return datalayer == null ? 0 : datalayer.get(SectionPos.sectionRelative(BlockPos.getX(pLevelPos)), SectionPos.sectionRelative(BlockPos.getY(pLevelPos)), SectionPos.sectionRelative(BlockPos.getZ(pLevelPos)));
   }

   protected static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockLightSectionStorage.BlockDataLayerStorageMap> {
      public BlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> p_75515_) {
         super(p_75515_);
      }

      public BlockLightSectionStorage.BlockDataLayerStorageMap copy() {
         return new BlockLightSectionStorage.BlockDataLayerStorageMap(this.map.clone());
      }
   }
}