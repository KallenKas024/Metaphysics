package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.slf4j.Logger;

public class ChunkSerializer {
   private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TAG_UPGRADE_DATA = "UpgradeData";
   private static final String BLOCK_TICKS_TAG = "block_ticks";
   private static final String FLUID_TICKS_TAG = "fluid_ticks";
   public static final String X_POS_TAG = "xPos";
   public static final String Z_POS_TAG = "zPos";
   public static final String HEIGHTMAPS_TAG = "Heightmaps";
   public static final String IS_LIGHT_ON_TAG = "isLightOn";
   public static final String SECTIONS_TAG = "sections";
   public static final String BLOCK_LIGHT_TAG = "BlockLight";
   public static final String SKY_LIGHT_TAG = "SkyLight";

   public static ProtoChunk read(ServerLevel pLevel, PoiManager pPoiManager, ChunkPos pPos, CompoundTag pTag) {
      ChunkPos chunkpos = new ChunkPos(pTag.getInt("xPos"), pTag.getInt("zPos"));
      if (!Objects.equals(pPos, chunkpos)) {
         LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", pPos, pPos, chunkpos);
      }

      UpgradeData upgradedata = pTag.contains("UpgradeData", 10) ? new UpgradeData(pTag.getCompound("UpgradeData"), pLevel) : UpgradeData.EMPTY;
      boolean flag = pTag.getBoolean("isLightOn");
      ListTag listtag = pTag.getList("sections", 10);
      int i = pLevel.getSectionsCount();
      LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
      boolean flag1 = pLevel.dimensionType().hasSkyLight();
      ChunkSource chunksource = pLevel.getChunkSource();
      LevelLightEngine levellightengine = chunksource.getLightEngine();
      Registry<Biome> registry = pLevel.registryAccess().registryOrThrow(Registries.BIOME);
      Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);
      boolean flag2 = false;

      for(int j = 0; j < listtag.size(); ++j) {
         CompoundTag compoundtag = listtag.getCompound(j);
         int k = compoundtag.getByte("Y");
         int l = pLevel.getSectionIndexFromSectionY(k);
         if (l >= 0 && l < alevelchunksection.length) {
            PalettedContainer<BlockState> palettedcontainer;
            if (compoundtag.contains("block_states", 10)) {
               palettedcontainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundtag.getCompound("block_states")).promotePartial((p_188283_) -> {
                  logErrors(pPos, k, p_188283_);
               }).getOrThrow(false, LOGGER::error);
            } else {
               palettedcontainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            }

            PalettedContainerRO<Holder<Biome>> palettedcontainerro;
            if (compoundtag.contains("biomes", 10)) {
               palettedcontainerro = codec.parse(NbtOps.INSTANCE, compoundtag.getCompound("biomes")).promotePartial((p_188274_) -> {
                  logErrors(pPos, k, p_188274_);
               }).getOrThrow(false, LOGGER::error);
            } else {
               palettedcontainerro = new PalettedContainer<>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
            }

            LevelChunkSection levelchunksection = new LevelChunkSection(palettedcontainer, palettedcontainerro);
            alevelchunksection[l] = levelchunksection;
            SectionPos sectionpos = SectionPos.of(pPos, k);
            pPoiManager.checkConsistencyWithBlocks(sectionpos, levelchunksection);
         }

         boolean flag3 = compoundtag.contains("BlockLight", 7);
         boolean flag4 = flag1 && compoundtag.contains("SkyLight", 7);
         if (flag3 || flag4) {
            if (!flag2) {
               levellightengine.retainData(pPos, true);
               flag2 = true;
            }

            if (flag3) {
               levellightengine.queueSectionData(LightLayer.BLOCK, SectionPos.of(pPos, k), new DataLayer(compoundtag.getByteArray("BlockLight")));
            }

            if (flag4) {
               levellightengine.queueSectionData(LightLayer.SKY, SectionPos.of(pPos, k), new DataLayer(compoundtag.getByteArray("SkyLight")));
            }
         }
      }

      long k1 = pTag.getLong("InhabitedTime");
      ChunkStatus.ChunkType chunkstatus$chunktype = getChunkTypeFromTag(pTag);
      BlendingData blendingdata;
      if (pTag.contains("blending_data", 10)) {
         blendingdata = BlendingData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pTag.getCompound("blending_data"))).resultOrPartial(LOGGER::error).orElse((BlendingData)null);
      } else {
         blendingdata = null;
      }

      ChunkAccess chunkaccess;
      if (chunkstatus$chunktype == ChunkStatus.ChunkType.LEVELCHUNK) {
         LevelChunkTicks<Block> levelchunkticks = LevelChunkTicks.load(pTag.getList("block_ticks", 10), (p_258988_) -> {
            return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(p_258988_));
         }, pPos);
         LevelChunkTicks<Fluid> levelchunkticks1 = LevelChunkTicks.load(pTag.getList("fluid_ticks", 10), (p_258990_) -> {
            return BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(p_258990_));
         }, pPos);
         chunkaccess = new LevelChunk(pLevel.getLevel(), pPos, upgradedata, levelchunkticks, levelchunkticks1, k1, alevelchunksection, postLoadChunk(pLevel, pTag), blendingdata);
         if (pTag.contains("ForgeCaps")) ((LevelChunk)chunkaccess).readCapsFromNBT(pTag.getCompound("ForgeCaps"));
      } else {
         ProtoChunkTicks<Block> protochunkticks = ProtoChunkTicks.load(pTag.getList("block_ticks", 10), (p_258992_) -> {
            return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(p_258992_));
         }, pPos);
         ProtoChunkTicks<Fluid> protochunkticks1 = ProtoChunkTicks.load(pTag.getList("fluid_ticks", 10), (p_258991_) -> {
            return BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(p_258991_));
         }, pPos);
         ProtoChunk protochunk = new ProtoChunk(pPos, upgradedata, alevelchunksection, protochunkticks, protochunkticks1, pLevel, registry, blendingdata);
         chunkaccess = protochunk;
         protochunk.setInhabitedTime(k1);
         if (pTag.contains("below_zero_retrogen", 10)) {
            BelowZeroRetrogen.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pTag.getCompound("below_zero_retrogen"))).resultOrPartial(LOGGER::error).ifPresent(protochunk::setBelowZeroRetrogen);
         }

         ChunkStatus chunkstatus = ChunkStatus.byName(pTag.getString("Status"));
         protochunk.setStatus(chunkstatus);
         if (chunkstatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
            protochunk.setLightEngine(levellightengine);
         }
      }

      chunkaccess.setLightCorrect(flag);
      CompoundTag compoundtag2 = pTag.getCompound("Heightmaps");
      EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);

      for(Heightmap.Types heightmap$types : chunkaccess.getStatus().heightmapsAfter()) {
         String s = heightmap$types.getSerializationKey();
         if (compoundtag2.contains(s, 12)) {
            chunkaccess.setHeightmap(heightmap$types, compoundtag2.getLongArray(s));
         } else {
            enumset.add(heightmap$types);
         }
      }

      Heightmap.primeHeightmaps(chunkaccess, enumset);
      CompoundTag compoundtag3 = pTag.getCompound("structures");
      chunkaccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(pLevel), compoundtag3, pLevel.getSeed()));
      chunkaccess.setAllReferences(unpackStructureReferences(pLevel.registryAccess(), pPos, compoundtag3));
      if (pTag.getBoolean("shouldSave")) {
         chunkaccess.setUnsaved(true);
      }

      ListTag listtag2 = pTag.getList("PostProcessing", 9);

      for(int l1 = 0; l1 < listtag2.size(); ++l1) {
         ListTag listtag1 = listtag2.getList(l1);

         for(int i1 = 0; i1 < listtag1.size(); ++i1) {
            chunkaccess.addPackedPostProcess(listtag1.getShort(i1), l1);
         }
      }

      if (chunkstatus$chunktype == ChunkStatus.ChunkType.LEVELCHUNK) {
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.ChunkDataEvent.Load(chunkaccess, pTag, chunkstatus$chunktype));
         return new ImposterProtoChunk((LevelChunk)chunkaccess, false);
      } else {
         ProtoChunk protochunk1 = (ProtoChunk)chunkaccess;
         ListTag listtag3 = pTag.getList("entities", 10);

         for(int i2 = 0; i2 < listtag3.size(); ++i2) {
            protochunk1.addEntity(listtag3.getCompound(i2));
         }

         ListTag listtag4 = pTag.getList("block_entities", 10);

         for(int j1 = 0; j1 < listtag4.size(); ++j1) {
            CompoundTag compoundtag1 = listtag4.getCompound(j1);
            chunkaccess.setBlockEntityNbt(compoundtag1);
         }

         CompoundTag compoundtag4 = pTag.getCompound("CarvingMasks");

         for(String s1 : compoundtag4.getAllKeys()) {
            GenerationStep.Carving generationstep$carving = GenerationStep.Carving.valueOf(s1);
            protochunk1.setCarvingMask(generationstep$carving, new CarvingMask(compoundtag4.getLongArray(s1), chunkaccess.getMinBuildHeight()));
         }

         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.ChunkDataEvent.Load(chunkaccess, pTag, chunkstatus$chunktype));
         return protochunk1;
      }
   }

   private static void logErrors(ChunkPos pChunkPos, int pChunkSectionY, String pErrorMessage) {
      LOGGER.error("Recoverable errors when loading section [" + pChunkPos.x + ", " + pChunkSectionY + ", " + pChunkPos.z + "]: " + pErrorMessage);
   }

   private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> pBiomeRegistry) {
      return PalettedContainer.codecRO(pBiomeRegistry.asHolderIdMap(), pBiomeRegistry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, pBiomeRegistry.getHolderOrThrow(Biomes.PLAINS));
   }

   public static CompoundTag write(ServerLevel pLevel, ChunkAccess pChunk) {
      ChunkPos chunkpos = pChunk.getPos();
      CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
      compoundtag.putInt("xPos", chunkpos.x);
      compoundtag.putInt("yPos", pChunk.getMinSection());
      compoundtag.putInt("zPos", chunkpos.z);
      compoundtag.putLong("LastUpdate", pLevel.getGameTime());
      compoundtag.putLong("InhabitedTime", pChunk.getInhabitedTime());
      compoundtag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(pChunk.getStatus()).toString());
      BlendingData blendingdata = pChunk.getBlendingData();
      if (blendingdata != null) {
         BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, blendingdata).resultOrPartial(LOGGER::error).ifPresent((p_196909_) -> {
            compoundtag.put("blending_data", p_196909_);
         });
      }

      BelowZeroRetrogen belowzeroretrogen = pChunk.getBelowZeroRetrogen();
      if (belowzeroretrogen != null) {
         BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, belowzeroretrogen).resultOrPartial(LOGGER::error).ifPresent((p_188279_) -> {
            compoundtag.put("below_zero_retrogen", p_188279_);
         });
      }

      UpgradeData upgradedata = pChunk.getUpgradeData();
      if (!upgradedata.isEmpty()) {
         compoundtag.put("UpgradeData", upgradedata.write());
      }

      LevelChunkSection[] alevelchunksection = pChunk.getSections();
      ListTag listtag = new ListTag();
      LevelLightEngine levellightengine = pLevel.getChunkSource().getLightEngine();
      Registry<Biome> registry = pLevel.registryAccess().registryOrThrow(Registries.BIOME);
      Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);
      boolean flag = pChunk.isLightCorrect();

      for(int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
         int j = pChunk.getSectionIndexFromSectionY(i);
         boolean flag1 = j >= 0 && j < alevelchunksection.length;
         DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
         DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
         if (flag1 || datalayer != null || datalayer1 != null) {
            CompoundTag compoundtag1 = new CompoundTag();
            if (flag1) {
               LevelChunkSection levelchunksection = alevelchunksection[j];
               compoundtag1.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow(false, LOGGER::error));
               compoundtag1.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelchunksection.getBiomes()).getOrThrow(false, LOGGER::error));
            }

            if (datalayer != null && !datalayer.isEmpty()) {
               compoundtag1.putByteArray("BlockLight", datalayer.getData());
            }

            if (datalayer1 != null && !datalayer1.isEmpty()) {
               compoundtag1.putByteArray("SkyLight", datalayer1.getData());
            }

            if (!compoundtag1.isEmpty()) {
               compoundtag1.putByte("Y", (byte)i);
               listtag.add(compoundtag1);
            }
         }
      }

      compoundtag.put("sections", listtag);
      if (flag) {
         compoundtag.putBoolean("isLightOn", true);
      }

      ListTag listtag1 = new ListTag();

      for(BlockPos blockpos : pChunk.getBlockEntitiesPos()) {
         CompoundTag compoundtag3 = pChunk.getBlockEntityNbtForSaving(blockpos);
         if (compoundtag3 != null) {
            listtag1.add(compoundtag3);
         }
      }

      compoundtag.put("block_entities", listtag1);
      if (pChunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
         ProtoChunk protochunk = (ProtoChunk)pChunk;
         ListTag listtag2 = new ListTag();
         listtag2.addAll(protochunk.getEntities());
         compoundtag.put("entities", listtag2);
         CompoundTag compoundtag4 = new CompoundTag();

         for(GenerationStep.Carving generationstep$carving : GenerationStep.Carving.values()) {
            CarvingMask carvingmask = protochunk.getCarvingMask(generationstep$carving);
            if (carvingmask != null) {
               compoundtag4.putLongArray(generationstep$carving.toString(), carvingmask.toArray());
            }
         }

         compoundtag.put("CarvingMasks", compoundtag4);
      }
      else if (pChunk instanceof LevelChunk levelChunk){
          try {
              final CompoundTag capTag = levelChunk.writeCapsToNBT();
              if (capTag != null) compoundtag.put("ForgeCaps", capTag);
          } catch (Exception exception) {
              LOGGER.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", exception);
          }
      }

      saveTicks(pLevel, compoundtag, pChunk.getTicksForSerialization());
      compoundtag.put("PostProcessing", packOffsets(pChunk.getPostProcessing()));
      CompoundTag compoundtag2 = new CompoundTag();

      for(Map.Entry<Heightmap.Types, Heightmap> entry : pChunk.getHeightmaps()) {
         if (pChunk.getStatus().heightmapsAfter().contains(entry.getKey())) {
            compoundtag2.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
         }
      }

      compoundtag.put("Heightmaps", compoundtag2);
      compoundtag.put("structures", packStructureData(StructurePieceSerializationContext.fromLevel(pLevel), chunkpos, pChunk.getAllStarts(), pChunk.getAllReferences()));
      return compoundtag;
   }

   private static void saveTicks(ServerLevel pLevel, CompoundTag pTag, ChunkAccess.TicksToSave pTicksToSave) {
      long i = pLevel.getLevelData().getGameTime();
      pTag.put("block_ticks", pTicksToSave.blocks().save(i, (p_258987_) -> {
         return BuiltInRegistries.BLOCK.getKey(p_258987_).toString();
      }));
      pTag.put("fluid_ticks", pTicksToSave.fluids().save(i, (p_258989_) -> {
         return BuiltInRegistries.FLUID.getKey(p_258989_).toString();
      }));
   }

   public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag pChunkNBT) {
      return pChunkNBT != null ? ChunkStatus.byName(pChunkNBT.getString("Status")).getChunkType() : ChunkStatus.ChunkType.PROTOCHUNK;
   }

   @Nullable
   private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel pLevel, CompoundTag pTag) {
      ListTag listtag = getListOfCompoundsOrNull(pTag, "entities");
      ListTag listtag1 = getListOfCompoundsOrNull(pTag, "block_entities");
      return listtag == null && listtag1 == null ? null : (p_196904_) -> {
         if (listtag != null) {
            pLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listtag, pLevel));
         }

         if (listtag1 != null) {
            for(int i = 0; i < listtag1.size(); ++i) {
               CompoundTag compoundtag = listtag1.getCompound(i);
               boolean flag = compoundtag.getBoolean("keepPacked");
               if (flag) {
                  p_196904_.setBlockEntityNbt(compoundtag);
               } else {
                  BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag);
                  BlockEntity blockentity = BlockEntity.loadStatic(blockpos, p_196904_.getBlockState(blockpos), compoundtag);
                  if (blockentity != null) {
                     p_196904_.setBlockEntity(blockentity);
                  }
               }
            }
         }

      };
   }

   @Nullable
   private static ListTag getListOfCompoundsOrNull(CompoundTag pTag, String pKey) {
      ListTag listtag = pTag.getList(pKey, 10);
      return listtag.isEmpty() ? null : listtag;
   }

   private static CompoundTag packStructureData(StructurePieceSerializationContext pContext, ChunkPos pPos, Map<Structure, StructureStart> pStructureMap, Map<Structure, LongSet> pReferenceMap) {
      CompoundTag compoundtag = new CompoundTag();
      CompoundTag compoundtag1 = new CompoundTag();
      Registry<Structure> registry = pContext.registryAccess().registryOrThrow(Registries.STRUCTURE);

      for(Map.Entry<Structure, StructureStart> entry : pStructureMap.entrySet()) {
         ResourceLocation resourcelocation = registry.getKey(entry.getKey());
         compoundtag1.put(resourcelocation.toString(), entry.getValue().createTag(pContext, pPos));
      }

      compoundtag.put("starts", compoundtag1);
      CompoundTag compoundtag2 = new CompoundTag();

      for(Map.Entry<Structure, LongSet> entry1 : pReferenceMap.entrySet()) {
         if (!entry1.getValue().isEmpty()) {
            ResourceLocation resourcelocation1 = registry.getKey(entry1.getKey());
            compoundtag2.put(resourcelocation1.toString(), new LongArrayTag(entry1.getValue()));
         }
      }

      compoundtag.put("References", compoundtag2);
      return compoundtag;
   }

   private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext pContext, CompoundTag pTag, long pSeed) {
      Map<Structure, StructureStart> map = Maps.newHashMap();
      Registry<Structure> registry = pContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
      CompoundTag compoundtag = pTag.getCompound("starts");

      for(String s : compoundtag.getAllKeys()) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         Structure structure = registry.get(resourcelocation);
         if (structure == null) {
            LOGGER.error("Unknown structure start: {}", (Object)resourcelocation);
         } else {
            StructureStart structurestart = StructureStart.loadStaticStart(pContext, compoundtag.getCompound(s), pSeed);
            if (structurestart != null) {
               map.put(structure, structurestart);
            }
         }
      }

      return map;
   }

   private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess pRegistryAccess, ChunkPos pPos, CompoundTag pTag) {
      Map<Structure, LongSet> map = Maps.newHashMap();
      Registry<Structure> registry = pRegistryAccess.registryOrThrow(Registries.STRUCTURE);
      CompoundTag compoundtag = pTag.getCompound("References");

      for(String s : compoundtag.getAllKeys()) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         Structure structure = registry.get(resourcelocation);
         if (structure == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", resourcelocation, pPos);
         } else {
            long[] along = compoundtag.getLongArray(s);
            if (along.length != 0) {
               map.put(structure, new LongOpenHashSet(Arrays.stream(along).filter((p_208153_) -> {
                  ChunkPos chunkpos = new ChunkPos(p_208153_);
                  if (chunkpos.getChessboardDistance(pPos) > 8) {
                     LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", resourcelocation, chunkpos, pPos);
                     return false;
                  } else {
                     return true;
                  }
               }).toArray()));
            }
         }
      }

      return map;
   }

   public static ListTag packOffsets(ShortList[] pList) {
      ListTag listtag = new ListTag();

      for(ShortList shortlist : pList) {
         ListTag listtag1 = new ListTag();
         if (shortlist != null) {
            for(Short oshort : shortlist) {
               listtag1.add(ShortTag.valueOf(oshort));
            }
         }

         listtag.add(listtag1);
      }

      return listtag;
   }
}
