package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckStructure extends Structure {
   public static final Codec<ShipwreckStructure> CODEC = RecordCodecBuilder.create((p_229401_) -> {
      return p_229401_.group(settingsCodec(p_229401_), Codec.BOOL.fieldOf("is_beached").forGetter((p_229399_) -> {
         return p_229399_.isBeached;
      })).apply(p_229401_, ShipwreckStructure::new);
   });
   public final boolean isBeached;

   public ShipwreckStructure(Structure.StructureSettings p_229388_, boolean p_229389_) {
      super(p_229388_);
      this.isBeached = p_229389_;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      Heightmap.Types heightmap$types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
      return onTopOfChunkCenter(pContext, heightmap$types, (p_229394_) -> {
         this.generatePieces(p_229394_, pContext);
      });
   }

   private void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext) {
      Rotation rotation = Rotation.getRandom(pContext.random());
      BlockPos blockpos = new BlockPos(pContext.chunkPos().getMinBlockX(), 90, pContext.chunkPos().getMinBlockZ());
      ShipwreckPieces.addPieces(pContext.structureTemplateManager(), blockpos, rotation, pBuilder, pContext.random(), this.isBeached);
   }

   public StructureType<?> type() {
      return StructureType.SHIPWRECK;
   }
}