package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LegacyStructureDataHandler {
   private static final Map<String, String> CURRENT_TO_LEGACY_MAP = Util.make(Maps.newHashMap(), (p_71337_) -> {
      p_71337_.put("Village", "Village");
      p_71337_.put("Mineshaft", "Mineshaft");
      p_71337_.put("Mansion", "Mansion");
      p_71337_.put("Igloo", "Temple");
      p_71337_.put("Desert_Pyramid", "Temple");
      p_71337_.put("Jungle_Pyramid", "Temple");
      p_71337_.put("Swamp_Hut", "Temple");
      p_71337_.put("Stronghold", "Stronghold");
      p_71337_.put("Monument", "Monument");
      p_71337_.put("Fortress", "Fortress");
      p_71337_.put("EndCity", "EndCity");
   });
   private static final Map<String, String> LEGACY_TO_CURRENT_MAP = Util.make(Maps.newHashMap(), (p_71325_) -> {
      p_71325_.put("Iglu", "Igloo");
      p_71325_.put("TeDP", "Desert_Pyramid");
      p_71325_.put("TeJP", "Jungle_Pyramid");
      p_71325_.put("TeSH", "Swamp_Hut");
   });
   private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
   private final boolean hasLegacyData;
   private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
   private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
   private final List<String> legacyKeys;
   private final List<String> currentKeys;

   public LegacyStructureDataHandler(@Nullable DimensionDataStorage pStorage, List<String> pLegacyKeys, List<String> pCurrentKeys) {
      this.legacyKeys = pLegacyKeys;
      this.currentKeys = pCurrentKeys;
      this.populateCaches(pStorage);
      boolean flag = false;

      for(String s : this.currentKeys) {
         flag |= this.dataMap.get(s) != null;
      }

      this.hasLegacyData = flag;
   }

   public void removeIndex(long pPackedChunkPos) {
      for(String s : this.legacyKeys) {
         StructureFeatureIndexSavedData structurefeatureindexsaveddata = this.indexMap.get(s);
         if (structurefeatureindexsaveddata != null && structurefeatureindexsaveddata.hasUnhandledIndex(pPackedChunkPos)) {
            structurefeatureindexsaveddata.removeIndex(pPackedChunkPos);
            structurefeatureindexsaveddata.setDirty();
         }
      }

   }

   public CompoundTag updateFromLegacy(CompoundTag pTag) {
      CompoundTag compoundtag = pTag.getCompound("Level");
      ChunkPos chunkpos = new ChunkPos(compoundtag.getInt("xPos"), compoundtag.getInt("zPos"));
      if (this.isUnhandledStructureStart(chunkpos.x, chunkpos.z)) {
         pTag = this.updateStructureStart(pTag, chunkpos);
      }

      CompoundTag compoundtag1 = compoundtag.getCompound("Structures");
      CompoundTag compoundtag2 = compoundtag1.getCompound("References");

      for(String s : this.currentKeys) {
         boolean flag = OLD_STRUCTURE_REGISTRY_KEYS.contains(s.toLowerCase(Locale.ROOT));
         if (!compoundtag2.contains(s, 12) && flag) {
            int i = 8;
            LongList longlist = new LongArrayList();

            for(int j = chunkpos.x - 8; j <= chunkpos.x + 8; ++j) {
               for(int k = chunkpos.z - 8; k <= chunkpos.z + 8; ++k) {
                  if (this.hasLegacyStart(j, k, s)) {
                     longlist.add(ChunkPos.asLong(j, k));
                  }
               }
            }

            compoundtag2.putLongArray(s, longlist);
         }
      }

      compoundtag1.put("References", compoundtag2);
      compoundtag.put("Structures", compoundtag1);
      pTag.put("Level", compoundtag);
      return pTag;
   }

   private boolean hasLegacyStart(int pChunkX, int pChunkZ, String pKey) {
      if (!this.hasLegacyData) {
         return false;
      } else {
         return this.dataMap.get(pKey) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(pKey)).hasStartIndex(ChunkPos.asLong(pChunkX, pChunkZ));
      }
   }

   private boolean isUnhandledStructureStart(int pChunkX, int pChunkZ) {
      if (!this.hasLegacyData) {
         return false;
      } else {
         for(String s : this.currentKeys) {
            if (this.dataMap.get(s) != null && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(s)).hasUnhandledIndex(ChunkPos.asLong(pChunkX, pChunkZ))) {
               return true;
            }
         }

         return false;
      }
   }

   private CompoundTag updateStructureStart(CompoundTag pTag, ChunkPos pChunkPos) {
      CompoundTag compoundtag = pTag.getCompound("Level");
      CompoundTag compoundtag1 = compoundtag.getCompound("Structures");
      CompoundTag compoundtag2 = compoundtag1.getCompound("Starts");

      for(String s : this.currentKeys) {
         Long2ObjectMap<CompoundTag> long2objectmap = this.dataMap.get(s);
         if (long2objectmap != null) {
            long i = pChunkPos.toLong();
            if (this.indexMap.get(CURRENT_TO_LEGACY_MAP.get(s)).hasUnhandledIndex(i)) {
               CompoundTag compoundtag3 = long2objectmap.get(i);
               if (compoundtag3 != null) {
                  compoundtag2.put(s, compoundtag3);
               }
            }
         }
      }

      compoundtag1.put("Starts", compoundtag2);
      compoundtag.put("Structures", compoundtag1);
      pTag.put("Level", compoundtag);
      return pTag;
   }

   private void populateCaches(@Nullable DimensionDataStorage pStorage) {
      if (pStorage != null) {
         for(String s : this.legacyKeys) {
            CompoundTag compoundtag = new CompoundTag();

            try {
               compoundtag = pStorage.readTagFromDisk(s, 1493).getCompound("data").getCompound("Features");
               if (compoundtag.isEmpty()) {
                  continue;
               }
            } catch (IOException ioexception) {
            }

            for(String s1 : compoundtag.getAllKeys()) {
               CompoundTag compoundtag1 = compoundtag.getCompound(s1);
               long i = ChunkPos.asLong(compoundtag1.getInt("ChunkX"), compoundtag1.getInt("ChunkZ"));
               ListTag listtag = compoundtag1.getList("Children", 10);
               if (!listtag.isEmpty()) {
                  String s3 = listtag.getCompound(0).getString("id");
                  String s4 = LEGACY_TO_CURRENT_MAP.get(s3);
                  if (s4 != null) {
                     compoundtag1.putString("id", s4);
                  }
               }

               String s6 = compoundtag1.getString("id");
               this.dataMap.computeIfAbsent(s6, (p_71335_) -> {
                  return new Long2ObjectOpenHashMap();
               }).put(i, compoundtag1);
            }

            String s5 = s + "_index";
            StructureFeatureIndexSavedData structurefeatureindexsaveddata = pStorage.computeIfAbsent(StructureFeatureIndexSavedData::load, StructureFeatureIndexSavedData::new, s5);
            if (!structurefeatureindexsaveddata.getAll().isEmpty()) {
               this.indexMap.put(s, structurefeatureindexsaveddata);
            } else {
               StructureFeatureIndexSavedData structurefeatureindexsaveddata1 = new StructureFeatureIndexSavedData();
               this.indexMap.put(s, structurefeatureindexsaveddata1);

               for(String s2 : compoundtag.getAllKeys()) {
                  CompoundTag compoundtag2 = compoundtag.getCompound(s2);
                  structurefeatureindexsaveddata1.addIndex(ChunkPos.asLong(compoundtag2.getInt("ChunkX"), compoundtag2.getInt("ChunkZ")));
               }

               structurefeatureindexsaveddata1.setDirty();
            }
         }

      }
   }

   public static LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> pLevel, @Nullable DimensionDataStorage pStorage) {
      if (pLevel == Level.OVERWORLD) {
         return new LegacyStructureDataHandler(pStorage, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
      } else if (pLevel == Level.NETHER) {
         List<String> list1 = ImmutableList.of("Fortress");
         return new LegacyStructureDataHandler(pStorage, list1, list1);
      } else if (pLevel == Level.END) {
         List<String> list = ImmutableList.of("EndCity");
         return new LegacyStructureDataHandler(pStorage, list, list);
      } else {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown dimension type : %s", pLevel));
      }
   }
}