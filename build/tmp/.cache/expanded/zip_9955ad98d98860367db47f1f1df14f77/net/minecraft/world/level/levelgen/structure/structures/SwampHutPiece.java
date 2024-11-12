package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class SwampHutPiece extends ScatteredFeaturePiece {
   private boolean spawnedWitch;
   private boolean spawnedCat;

   public SwampHutPiece(RandomSource pRandom, int pX, int pZ) {
      super(StructurePieceType.SWAMPLAND_HUT, pX, 64, pZ, 7, 7, 9, getRandomHorizontalDirection(pRandom));
   }

   public SwampHutPiece(CompoundTag pTag) {
      super(StructurePieceType.SWAMPLAND_HUT, pTag);
      this.spawnedWitch = pTag.getBoolean("Witch");
      this.spawnedCat = pTag.getBoolean("Cat");
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      super.addAdditionalSaveData(pContext, pTag);
      pTag.putBoolean("Witch", this.spawnedWitch);
      pTag.putBoolean("Cat", this.spawnedCat);
   }

   public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
      if (this.updateAverageGroundHeight(pLevel, pBox, 0)) {
         this.generateBox(pLevel, pBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, pBox);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 1, 3, 4, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 5, 3, 4, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 5, 3, 5, pBox);
         this.placeBlock(pLevel, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, pBox);
         this.placeBlock(pLevel, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, pBox);
         this.placeBlock(pLevel, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, pBox);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, pBox);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, pBox);
         BlockState blockstate = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         BlockState blockstate1 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate2 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         BlockState blockstate3 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         this.generateBox(pLevel, pBox, 0, 4, 1, 6, 4, 1, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 0, 4, 2, 0, 4, 7, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 6, 4, 2, 6, 4, 7, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 0, 4, 8, 6, 4, 8, blockstate3, blockstate3, false);
         this.placeBlock(pLevel, blockstate.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, pBox);
         this.placeBlock(pLevel, blockstate.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, pBox);

         for(int i = 2; i <= 7; i += 5) {
            for(int j = 1; j <= 5; j += 4) {
               this.fillColumnDown(pLevel, Blocks.OAK_LOG.defaultBlockState(), j, -1, i, pBox);
            }
         }

         if (!this.spawnedWitch) {
            BlockPos blockpos = this.getWorldPos(2, 2, 5);
            if (pBox.isInside(blockpos)) {
               this.spawnedWitch = true;
               Witch witch = EntityType.WITCH.create(pLevel.getLevel());
               if (witch != null) {
                  witch.setPersistenceRequired();
                  witch.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
                  witch.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
                  pLevel.addFreshEntityWithPassengers(witch);
               }
            }
         }

         this.spawnCat(pLevel, pBox);
      }
   }

   private void spawnCat(ServerLevelAccessor pLevel, BoundingBox pBox) {
      if (!this.spawnedCat) {
         BlockPos blockpos = this.getWorldPos(2, 2, 5);
         if (pBox.isInside(blockpos)) {
            this.spawnedCat = true;
            Cat cat = EntityType.CAT.create(pLevel.getLevel());
            if (cat != null) {
               cat.setPersistenceRequired();
               cat.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
               cat.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
               pLevel.addFreshEntityWithPassengers(cat);
            }
         }
      }

   }
}