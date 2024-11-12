package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidStructure extends SinglePieceStructure {
   public static final Codec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

   public DesertPyramidStructure(Structure.StructureSettings p_227418_) {
      super(DesertPyramidPiece::new, 21, 21, p_227418_);
   }

   public void afterPlace(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, RandomSource pRandom, BoundingBox pBoundingBox, ChunkPos pChunkPos, PiecesContainer pPieces) {
      Set<BlockPos> set = SortedArraySet.create(Vec3i::compareTo);

      for(StructurePiece structurepiece : pPieces.pieces()) {
         if (structurepiece instanceof DesertPyramidPiece desertpyramidpiece) {
            set.addAll(desertpyramidpiece.getPotentialSuspiciousSandWorldPositions());
            placeSuspiciousSand(pBoundingBox, pLevel, desertpyramidpiece.getRandomCollapsedRoofPos());
         }
      }

      ObjectArrayList<BlockPos> objectarraylist = new ObjectArrayList<>(set.stream().toList());
      RandomSource randomsource = RandomSource.create(pLevel.getSeed()).forkPositional().at(pPieces.calculateBoundingBox().getCenter());
      Util.shuffle(objectarraylist, randomsource);
      int i = Math.min(set.size(), randomsource.nextInt(5, 8));

      for(BlockPos blockpos : objectarraylist) {
         if (i > 0) {
            --i;
            placeSuspiciousSand(pBoundingBox, pLevel, blockpos);
         } else if (pBoundingBox.isInside(blockpos)) {
            pLevel.setBlock(blockpos, Blocks.SAND.defaultBlockState(), 2);
         }
      }

   }

   private static void placeSuspiciousSand(BoundingBox pBoundingBox, WorldGenLevel pWorldGenLevel, BlockPos pPos) {
      if (pBoundingBox.isInside(pPos)) {
         pWorldGenLevel.setBlock(pPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
         pWorldGenLevel.getBlockEntity(pPos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent((p_277328_) -> {
            p_277328_.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, pPos.asLong());
         });
      }

   }

   public StructureType<?> type() {
      return StructureType.DESERT_PYRAMID;
   }
}