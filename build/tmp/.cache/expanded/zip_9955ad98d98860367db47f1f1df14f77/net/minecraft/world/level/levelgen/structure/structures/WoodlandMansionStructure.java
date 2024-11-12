package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionStructure extends Structure {
   public static final Codec<WoodlandMansionStructure> CODEC = simpleCodec(WoodlandMansionStructure::new);

   public WoodlandMansionStructure(Structure.StructureSettings p_230225_) {
      super(p_230225_);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
      Rotation rotation = Rotation.getRandom(pContext.random());
      BlockPos blockpos = this.getLowestYIn5by5BoxOffset7Blocks(pContext, rotation);
      return blockpos.getY() < 60 ? Optional.empty() : Optional.of(new Structure.GenerationStub(blockpos, (p_230240_) -> {
         this.generatePieces(p_230240_, pContext, blockpos, rotation);
      }));
   }

   private void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext, BlockPos pPos, Rotation pRotation) {
      List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.newLinkedList();
      WoodlandMansionPieces.generateMansion(pContext.structureTemplateManager(), pPos, pRotation, list, pContext.random());
      list.forEach(pBuilder::addPiece);
   }

   public void afterPlace(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, RandomSource pRandom, BoundingBox pBoundingBox, ChunkPos pChunkPos, PiecesContainer pPieces) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i = pLevel.getMinBuildHeight();
      BoundingBox boundingbox = pPieces.calculateBoundingBox();
      int j = boundingbox.minY();

      for(int k = pBoundingBox.minX(); k <= pBoundingBox.maxX(); ++k) {
         for(int l = pBoundingBox.minZ(); l <= pBoundingBox.maxZ(); ++l) {
            blockpos$mutableblockpos.set(k, j, l);
            if (!pLevel.isEmptyBlock(blockpos$mutableblockpos) && boundingbox.isInside(blockpos$mutableblockpos) && pPieces.isInsidePiece(blockpos$mutableblockpos)) {
               for(int i1 = j - 1; i1 > i; --i1) {
                  blockpos$mutableblockpos.setY(i1);
                  if (!pLevel.isEmptyBlock(blockpos$mutableblockpos) && !pLevel.getBlockState(blockpos$mutableblockpos).liquid()) {
                     break;
                  }

                  pLevel.setBlock(blockpos$mutableblockpos, Blocks.COBBLESTONE.defaultBlockState(), 2);
               }
            }
         }
      }

   }

   public StructureType<?> type() {
      return StructureType.WOODLAND_MANSION;
   }
}