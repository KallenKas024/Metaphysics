package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class RandomSpreadStructurePlacement extends StructurePlacement {
   public static final Codec<RandomSpreadStructurePlacement> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec((p_204996_) -> {
      return placementCodec(p_204996_).and(p_204996_.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType))).apply(p_204996_, RandomSpreadStructurePlacement::new);
   }), RandomSpreadStructurePlacement::validate).codec();
   private final int spacing;
   private final int separation;
   private final RandomSpreadType spreadType;

   private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement p_286361_) {
      return p_286361_.spacing <= p_286361_.separation ? DataResult.error(() -> {
         return "Spacing has to be larger than separation";
      }) : DataResult.success(p_286361_);
   }

   public RandomSpreadStructurePlacement(Vec3i p_227000_, StructurePlacement.FrequencyReductionMethod p_227001_, float p_227002_, int p_227003_, Optional<StructurePlacement.ExclusionZone> p_227004_, int p_227005_, int p_227006_, RandomSpreadType p_227007_) {
      super(p_227000_, p_227001_, p_227002_, p_227003_, p_227004_);
      this.spacing = p_227005_;
      this.separation = p_227006_;
      this.spreadType = p_227007_;
   }

   public RandomSpreadStructurePlacement(int pSpacing, int pSeparation, RandomSpreadType pSpreadType, int pSalt) {
      this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, pSalt, Optional.empty(), pSpacing, pSeparation, pSpreadType);
   }

   public int spacing() {
      return this.spacing;
   }

   public int separation() {
      return this.separation;
   }

   public RandomSpreadType spreadType() {
      return this.spreadType;
   }

   public ChunkPos getPotentialStructureChunk(long pSeed, int pRegionX, int pRegionZ) {
      int i = Math.floorDiv(pRegionX, this.spacing);
      int j = Math.floorDiv(pRegionZ, this.spacing);
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(pSeed, i, j, this.salt());
      int k = this.spacing - this.separation;
      int l = this.spreadType.evaluate(worldgenrandom, k);
      int i1 = this.spreadType.evaluate(worldgenrandom, k);
      return new ChunkPos(i * this.spacing + l, j * this.spacing + i1);
   }

   protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
      ChunkPos chunkpos = this.getPotentialStructureChunk(pStructureState.getLevelSeed(), pX, pZ);
      return chunkpos.x == pX && chunkpos.z == pZ;
   }

   public StructurePlacementType<?> type() {
      return StructurePlacementType.RANDOM_SPREAD;
   }
}