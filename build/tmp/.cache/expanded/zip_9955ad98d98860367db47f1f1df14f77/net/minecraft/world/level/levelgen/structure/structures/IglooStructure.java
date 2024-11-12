package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooStructure extends Structure {
   public static final Codec<IglooStructure> CODEC = simpleCodec(IglooStructure::new);

   public IglooStructure(Structure.StructureSettings p_227593_) {
      super(p_227593_);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      return onTopOfChunkCenter(pContext, Heightmap.Types.WORLD_SURFACE_WG, (p_227598_) -> {
         this.generatePieces(p_227598_, pContext);
      });
   }

   private void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext) {
      ChunkPos chunkpos = pContext.chunkPos();
      WorldgenRandom worldgenrandom = pContext.random();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 90, chunkpos.getMinBlockZ());
      Rotation rotation = Rotation.getRandom(worldgenrandom);
      IglooPieces.addPieces(pContext.structureTemplateManager(), blockpos, rotation, pBuilder, worldgenrandom);
   }

   public StructureType<?> type() {
      return StructureType.IGLOO;
   }
}