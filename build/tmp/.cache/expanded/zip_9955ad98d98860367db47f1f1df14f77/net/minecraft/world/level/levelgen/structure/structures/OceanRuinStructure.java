package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinStructure extends Structure {
   public static final Codec<OceanRuinStructure> CODEC = RecordCodecBuilder.create((p_229075_) -> {
      return p_229075_.group(settingsCodec(p_229075_), OceanRuinStructure.Type.CODEC.fieldOf("biome_temp").forGetter((p_229079_) -> {
         return p_229079_.biomeTemp;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter((p_229077_) -> {
         return p_229077_.largeProbability;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter((p_229073_) -> {
         return p_229073_.clusterProbability;
      })).apply(p_229075_, OceanRuinStructure::new);
   });
   public final OceanRuinStructure.Type biomeTemp;
   public final float largeProbability;
   public final float clusterProbability;

   public OceanRuinStructure(Structure.StructureSettings p_229060_, OceanRuinStructure.Type p_229061_, float p_229062_, float p_229063_) {
      super(p_229060_);
      this.biomeTemp = p_229061_;
      this.largeProbability = p_229062_;
      this.clusterProbability = p_229063_;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      return onTopOfChunkCenter(pContext, Heightmap.Types.OCEAN_FLOOR_WG, (p_229068_) -> {
         this.generatePieces(p_229068_, pContext);
      });
   }

   private void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext) {
      BlockPos blockpos = new BlockPos(pContext.chunkPos().getMinBlockX(), 90, pContext.chunkPos().getMinBlockZ());
      Rotation rotation = Rotation.getRandom(pContext.random());
      OceanRuinPieces.addPieces(pContext.structureTemplateManager(), blockpos, rotation, pBuilder, pContext.random(), this);
   }

   public StructureType<?> type() {
      return StructureType.OCEAN_RUIN;
   }

   public static enum Type implements StringRepresentable {
      WARM("warm"),
      COLD("cold");

      public static final Codec<OceanRuinStructure.Type> CODEC = StringRepresentable.fromEnum(OceanRuinStructure.Type::values);
      private final String name;

      private Type(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}