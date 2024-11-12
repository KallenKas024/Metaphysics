package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

public class ConcentricRingsStructurePlacement extends StructurePlacement {
   public static final Codec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.create((p_204960_) -> {
      return codec(p_204960_).apply(p_204960_, ConcentricRingsStructurePlacement::new);
   });
   private final int distance;
   private final int spread;
   private final int count;
   private final HolderSet<Biome> preferredBiomes;

   private static Products.P9<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, HolderSet<Biome>> codec(RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement> pInstance) {
      Products.P5<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> p5 = placementCodec(pInstance);
      Products.P4<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Integer, Integer, Integer, HolderSet<Biome>> p4 = pInstance.group(Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance), Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread), Codec.intRange(1, 4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count), RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(ConcentricRingsStructurePlacement::preferredBiomes));
      return new Products.P9<>(p5.t1(), p5.t2(), p5.t3(), p5.t4(), p5.t5(), p4.t1(), p4.t2(), p4.t3(), p4.t4());
   }

   public ConcentricRingsStructurePlacement(Vec3i p_226981_, StructurePlacement.FrequencyReductionMethod p_226982_, float p_226983_, int p_226984_, Optional<StructurePlacement.ExclusionZone> p_226985_, int p_226986_, int p_226987_, int p_226988_, HolderSet<Biome> p_226989_) {
      super(p_226981_, p_226982_, p_226983_, p_226984_, p_226985_);
      this.distance = p_226986_;
      this.spread = p_226987_;
      this.count = p_226988_;
      this.preferredBiomes = p_226989_;
   }

   public ConcentricRingsStructurePlacement(int pDistance, int pSpread, int pCount, HolderSet<Biome> pPreferrredBiomes) {
      this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty(), pDistance, pSpread, pCount, pPreferrredBiomes);
   }

   public int distance() {
      return this.distance;
   }

   public int spread() {
      return this.spread;
   }

   public int count() {
      return this.count;
   }

   public HolderSet<Biome> preferredBiomes() {
      return this.preferredBiomes;
   }

   protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
      List<ChunkPos> list = pStructureState.getRingPositionsFor(this);
      return list == null ? false : list.contains(new ChunkPos(pX, pZ));
   }

   public StructurePlacementType<?> type() {
      return StructurePlacementType.CONCENTRIC_RINGS;
   }
}