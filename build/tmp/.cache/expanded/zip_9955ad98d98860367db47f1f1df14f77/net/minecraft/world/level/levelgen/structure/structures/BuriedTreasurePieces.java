package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {
   public static class BuriedTreasurePiece extends StructurePiece {
      public BuriedTreasurePiece(BlockPos pPos) {
         super(StructurePieceType.BURIED_TREASURE_PIECE, 0, new BoundingBox(pPos));
      }

      public BuriedTreasurePiece(CompoundTag pTag) {
         super(StructurePieceType.BURIED_TREASURE_PIECE, pTag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         int i = pLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.minX(), this.boundingBox.minZ());
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(this.boundingBox.minX(), i, this.boundingBox.minZ());

         while(blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight()) {
            BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
            BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos.below());
            if (blockstate1 == Blocks.SANDSTONE.defaultBlockState() || blockstate1 == Blocks.STONE.defaultBlockState() || blockstate1 == Blocks.ANDESITE.defaultBlockState() || blockstate1 == Blocks.GRANITE.defaultBlockState() || blockstate1 == Blocks.DIORITE.defaultBlockState()) {
               BlockState blockstate2 = !blockstate.isAir() && !this.isLiquid(blockstate) ? blockstate : Blocks.SAND.defaultBlockState();

               for(Direction direction : Direction.values()) {
                  BlockPos blockpos = blockpos$mutableblockpos.relative(direction);
                  BlockState blockstate3 = pLevel.getBlockState(blockpos);
                  if (blockstate3.isAir() || this.isLiquid(blockstate3)) {
                     BlockPos blockpos1 = blockpos.below();
                     BlockState blockstate4 = pLevel.getBlockState(blockpos1);
                     if ((blockstate4.isAir() || this.isLiquid(blockstate4)) && direction != Direction.UP) {
                        pLevel.setBlock(blockpos, blockstate1, 3);
                     } else {
                        pLevel.setBlock(blockpos, blockstate2, 3);
                     }
                  }
               }

               this.boundingBox = new BoundingBox(blockpos$mutableblockpos);
               this.createChest(pLevel, pBox, pRandom, blockpos$mutableblockpos, BuiltInLootTables.BURIED_TREASURE, (BlockState)null);
               return;
            }

            blockpos$mutableblockpos.move(0, -1, 0);
         }

      }

      private boolean isLiquid(BlockState pState) {
         return pState == Blocks.WATER.defaultBlockState() || pState == Blocks.LAVA.defaultBlockState();
      }
   }
}