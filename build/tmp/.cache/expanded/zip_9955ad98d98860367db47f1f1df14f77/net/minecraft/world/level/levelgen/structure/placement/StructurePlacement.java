package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public abstract class StructurePlacement {
   public static final Codec<StructurePlacement> CODEC = BuiltInRegistries.STRUCTURE_PLACEMENT.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);
   private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
   private final Vec3i locateOffset;
   private final StructurePlacement.FrequencyReductionMethod frequencyReductionMethod;
   private final float frequency;
   private final int salt;
   private final Optional<StructurePlacement.ExclusionZone> exclusionZone;

   protected static <S extends StructurePlacement> Products.P5<RecordCodecBuilder.Mu<S>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> placementCodec(RecordCodecBuilder.Instance<S> pInstance) {
      return pInstance.group(Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(StructurePlacement::locateOffset), StructurePlacement.FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", StructurePlacement.FrequencyReductionMethod.DEFAULT).forGetter(StructurePlacement::frequencyReductionMethod), Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(StructurePlacement::frequency), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StructurePlacement::salt), StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::exclusionZone));
   }

   protected StructurePlacement(Vec3i pLocateOffset, StructurePlacement.FrequencyReductionMethod pFrequencyReductionMethod, float pFrequency, int pSalt, Optional<StructurePlacement.ExclusionZone> pExclusionZone) {
      this.locateOffset = pLocateOffset;
      this.frequencyReductionMethod = pFrequencyReductionMethod;
      this.frequency = pFrequency;
      this.salt = pSalt;
      this.exclusionZone = pExclusionZone;
   }

   protected Vec3i locateOffset() {
      return this.locateOffset;
   }

   protected StructurePlacement.FrequencyReductionMethod frequencyReductionMethod() {
      return this.frequencyReductionMethod;
   }

   protected float frequency() {
      return this.frequency;
   }

   protected int salt() {
      return this.salt;
   }

   protected Optional<StructurePlacement.ExclusionZone> exclusionZone() {
      return this.exclusionZone;
   }

   public boolean isStructureChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
      if (!this.isPlacementChunk(pStructureState, pX, pZ)) {
         return false;
      } else if (this.frequency < 1.0F && !this.frequencyReductionMethod.shouldGenerate(pStructureState.getLevelSeed(), this.salt, pX, pZ, this.frequency)) {
         return false;
      } else {
         return !this.exclusionZone.isPresent() || !this.exclusionZone.get().isPlacementForbidden(pStructureState, pX, pZ);
      }
   }

   protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ);

   public BlockPos getLocatePos(ChunkPos pChunkPos) {
      return (new BlockPos(pChunkPos.getMinBlockX(), 0, pChunkPos.getMinBlockZ())).offset(this.locateOffset());
   }

   public abstract StructurePlacementType<?> type();

   private static boolean probabilityReducer(long pLevelSeed, int pRegionX, int pRegionZ, int pSalt, float pProbability) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(pLevelSeed, pRegionX, pRegionZ, pSalt);
      return worldgenrandom.nextFloat() < pProbability;
   }

   private static boolean legacyProbabilityReducerWithDouble(long pBaseSeed, int p_227050_, int pChunkX, int pChunkZ, float pProbability) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureSeed(pBaseSeed, pChunkX, pChunkZ);
      return worldgenrandom.nextDouble() < (double)pProbability;
   }

   private static boolean legacyArbitrarySaltProbabilityReducer(long pLevelSeed, int p_227062_, int pRegionX, int pRegionZ, float pProbability) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(pLevelSeed, pRegionX, pRegionZ, 10387320);
      return worldgenrandom.nextFloat() < pProbability;
   }

   private static boolean legacyPillagerOutpostReducer(long pLevelSeed, int p_227068_, int pRegionX, int pRegionZ, float pProbability) {
      int i = pRegionX >> 4;
      int j = pRegionZ >> 4;
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setSeed((long)(i ^ j << 4) ^ pLevelSeed);
      worldgenrandom.nextInt();
      return worldgenrandom.nextInt((int)(1.0F / pProbability)) == 0;
   }

   /** @deprecated */
   @Deprecated
   public static record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
      public static final Codec<StructurePlacement.ExclusionZone> CODEC = RecordCodecBuilder.create((p_259015_) -> {
         return p_259015_.group(RegistryFileCodec.create(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false).fieldOf("other_set").forGetter(StructurePlacement.ExclusionZone::otherSet), Codec.intRange(1, 16).fieldOf("chunk_count").forGetter(StructurePlacement.ExclusionZone::chunkCount)).apply(p_259015_, StructurePlacement.ExclusionZone::new);
      });

      boolean isPlacementForbidden(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
         return pStructureState.hasStructureChunkInRange(this.otherSet, pX, pZ, this.chunkCount);
      }
   }

   @FunctionalInterface
   public interface FrequencyReducer {
      boolean shouldGenerate(long pLevelSeed, int p_227100_, int pRegionX, int pRegionZ, float pProbability);
   }

   public static enum FrequencyReductionMethod implements StringRepresentable {
      DEFAULT("default", StructurePlacement::probabilityReducer),
      LEGACY_TYPE_1("legacy_type_1", StructurePlacement::legacyPillagerOutpostReducer),
      LEGACY_TYPE_2("legacy_type_2", StructurePlacement::legacyArbitrarySaltProbabilityReducer),
      LEGACY_TYPE_3("legacy_type_3", StructurePlacement::legacyProbabilityReducerWithDouble);

      public static final Codec<StructurePlacement.FrequencyReductionMethod> CODEC = StringRepresentable.fromEnum(StructurePlacement.FrequencyReductionMethod::values);
      private final String name;
      private final StructurePlacement.FrequencyReducer reducer;

      private FrequencyReductionMethod(String pName, StructurePlacement.FrequencyReducer pReducer) {
         this.name = pName;
         this.reducer = pReducer;
      }

      public boolean shouldGenerate(long pLevelSeed, int p_227121_, int pRegionX, int pRegionZ, float pProbability) {
         return this.reducer.shouldGenerate(pLevelSeed, p_227121_, pRegionX, pRegionZ, pProbability);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}