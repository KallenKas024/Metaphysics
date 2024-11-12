package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class JungleTemplePiece extends ScatteredFeaturePiece {
   public static final int WIDTH = 12;
   public static final int DEPTH = 15;
   private boolean placedMainChest;
   private boolean placedHiddenChest;
   private boolean placedTrap1;
   private boolean placedTrap2;
   private static final JungleTemplePiece.MossStoneSelector STONE_SELECTOR = new JungleTemplePiece.MossStoneSelector();

   public JungleTemplePiece(RandomSource pRandom, int pX, int pZ) {
      super(StructurePieceType.JUNGLE_PYRAMID_PIECE, pX, 64, pZ, 12, 10, 15, getRandomHorizontalDirection(pRandom));
   }

   public JungleTemplePiece(CompoundTag pTag) {
      super(StructurePieceType.JUNGLE_PYRAMID_PIECE, pTag);
      this.placedMainChest = pTag.getBoolean("placedMainChest");
      this.placedHiddenChest = pTag.getBoolean("placedHiddenChest");
      this.placedTrap1 = pTag.getBoolean("placedTrap1");
      this.placedTrap2 = pTag.getBoolean("placedTrap2");
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      super.addAdditionalSaveData(pContext, pTag);
      pTag.putBoolean("placedMainChest", this.placedMainChest);
      pTag.putBoolean("placedHiddenChest", this.placedHiddenChest);
      pTag.putBoolean("placedTrap1", this.placedTrap1);
      pTag.putBoolean("placedTrap2", this.placedTrap2);
   }

   public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
      if (this.updateAverageGroundHeight(pLevel, pBox, 0)) {
         this.generateBox(pLevel, pBox, 0, -4, 0, this.width - 1, 0, this.depth - 1, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, 1, 2, 9, 2, 2, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, 1, 12, 9, 2, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, 1, 3, 2, 2, 11, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 9, 1, 3, 9, 2, 11, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 3, 1, 10, 6, 1, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 3, 13, 10, 6, 13, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 3, 2, 1, 6, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 10, 3, 2, 10, 6, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, 3, 2, 9, 3, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, 6, 2, 9, 6, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 3, 7, 3, 8, 7, 11, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 8, 4, 7, 8, 10, false, pRandom, STONE_SELECTOR);
         this.generateAirBox(pLevel, pBox, 3, 1, 3, 8, 2, 11);
         this.generateAirBox(pLevel, pBox, 4, 3, 6, 7, 3, 9);
         this.generateAirBox(pLevel, pBox, 2, 4, 2, 9, 5, 12);
         this.generateAirBox(pLevel, pBox, 4, 6, 5, 7, 6, 9);
         this.generateAirBox(pLevel, pBox, 5, 7, 6, 6, 7, 8);
         this.generateAirBox(pLevel, pBox, 5, 1, 2, 6, 2, 2);
         this.generateAirBox(pLevel, pBox, 5, 2, 12, 6, 2, 12);
         this.generateAirBox(pLevel, pBox, 5, 5, 1, 6, 5, 1);
         this.generateAirBox(pLevel, pBox, 5, 5, 13, 6, 5, 13);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 1, 5, 5, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 10, 5, 5, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 1, 5, 9, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 10, 5, 9, pBox);

         for(int i = 0; i <= 14; i += 14) {
            this.generateBox(pLevel, pBox, 2, 4, i, 2, 5, i, false, pRandom, STONE_SELECTOR);
            this.generateBox(pLevel, pBox, 4, 4, i, 4, 5, i, false, pRandom, STONE_SELECTOR);
            this.generateBox(pLevel, pBox, 7, 4, i, 7, 5, i, false, pRandom, STONE_SELECTOR);
            this.generateBox(pLevel, pBox, 9, 4, i, 9, 5, i, false, pRandom, STONE_SELECTOR);
         }

         this.generateBox(pLevel, pBox, 5, 6, 0, 6, 6, 0, false, pRandom, STONE_SELECTOR);

         for(int l = 0; l <= 11; l += 11) {
            for(int j = 2; j <= 12; j += 2) {
               this.generateBox(pLevel, pBox, l, 4, j, l, 5, j, false, pRandom, STONE_SELECTOR);
            }

            this.generateBox(pLevel, pBox, l, 6, 5, l, 6, 5, false, pRandom, STONE_SELECTOR);
            this.generateBox(pLevel, pBox, l, 6, 9, l, 6, 9, false, pRandom, STONE_SELECTOR);
         }

         this.generateBox(pLevel, pBox, 2, 7, 2, 2, 9, 2, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 9, 7, 2, 9, 9, 2, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, 7, 12, 2, 9, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 9, 7, 12, 9, 9, 12, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 9, 4, 4, 9, 4, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 7, 9, 4, 7, 9, 4, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 9, 10, 4, 9, 10, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 7, 9, 10, 7, 9, 10, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 5, 9, 7, 6, 9, 7, false, pRandom, STONE_SELECTOR);
         BlockState blockstate3 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate4 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         BlockState blockstate = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         BlockState blockstate1 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         this.placeBlock(pLevel, blockstate1, 5, 9, 6, pBox);
         this.placeBlock(pLevel, blockstate1, 6, 9, 6, pBox);
         this.placeBlock(pLevel, blockstate, 5, 9, 8, pBox);
         this.placeBlock(pLevel, blockstate, 6, 9, 8, pBox);
         this.placeBlock(pLevel, blockstate1, 4, 0, 0, pBox);
         this.placeBlock(pLevel, blockstate1, 5, 0, 0, pBox);
         this.placeBlock(pLevel, blockstate1, 6, 0, 0, pBox);
         this.placeBlock(pLevel, blockstate1, 7, 0, 0, pBox);
         this.placeBlock(pLevel, blockstate1, 4, 1, 8, pBox);
         this.placeBlock(pLevel, blockstate1, 4, 2, 9, pBox);
         this.placeBlock(pLevel, blockstate1, 4, 3, 10, pBox);
         this.placeBlock(pLevel, blockstate1, 7, 1, 8, pBox);
         this.placeBlock(pLevel, blockstate1, 7, 2, 9, pBox);
         this.placeBlock(pLevel, blockstate1, 7, 3, 10, pBox);
         this.generateBox(pLevel, pBox, 4, 1, 9, 4, 1, 9, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 7, 1, 9, 7, 1, 9, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 1, 10, 7, 2, 10, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 5, 4, 5, 6, 4, 5, false, pRandom, STONE_SELECTOR);
         this.placeBlock(pLevel, blockstate3, 4, 4, 5, pBox);
         this.placeBlock(pLevel, blockstate4, 7, 4, 5, pBox);

         for(int k = 0; k < 4; ++k) {
            this.placeBlock(pLevel, blockstate, 5, 0 - k, 6 + k, pBox);
            this.placeBlock(pLevel, blockstate, 6, 0 - k, 6 + k, pBox);
            this.generateAirBox(pLevel, pBox, 5, 0 - k, 7 + k, 6, 0 - k, 9 + k);
         }

         this.generateAirBox(pLevel, pBox, 1, -3, 12, 10, -1, 13);
         this.generateAirBox(pLevel, pBox, 1, -3, 1, 3, -1, 13);
         this.generateAirBox(pLevel, pBox, 1, -3, 1, 9, -1, 5);

         for(int i1 = 1; i1 <= 13; i1 += 2) {
            this.generateBox(pLevel, pBox, 1, -3, i1, 1, -2, i1, false, pRandom, STONE_SELECTOR);
         }

         for(int j1 = 2; j1 <= 12; j1 += 2) {
            this.generateBox(pLevel, pBox, 1, -1, j1, 3, -1, j1, false, pRandom, STONE_SELECTOR);
         }

         this.generateBox(pLevel, pBox, 2, -2, 1, 5, -2, 1, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 7, -2, 1, 9, -2, 1, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 6, -3, 1, 6, -3, 1, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 6, -1, 1, 6, -1, 1, false, pRandom, STONE_SELECTOR);
         this.placeBlock(pLevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.EAST).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 1, -3, 8, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.WEST).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 4, -3, 8, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.EAST, Boolean.valueOf(true)).setValue(TripWireBlock.WEST, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 2, -3, 8, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.EAST, Boolean.valueOf(true)).setValue(TripWireBlock.WEST, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 3, -3, 8, pBox);
         BlockState blockstate5 = Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE);
         this.placeBlock(pLevel, blockstate5, 5, -3, 7, pBox);
         this.placeBlock(pLevel, blockstate5, 5, -3, 6, pBox);
         this.placeBlock(pLevel, blockstate5, 5, -3, 5, pBox);
         this.placeBlock(pLevel, blockstate5, 5, -3, 4, pBox);
         this.placeBlock(pLevel, blockstate5, 5, -3, 3, pBox);
         this.placeBlock(pLevel, blockstate5, 5, -3, 2, pBox);
         this.placeBlock(pLevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 5, -3, 1, pBox);
         this.placeBlock(pLevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 4, -3, 1, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3, -3, 1, pBox);
         if (!this.placedTrap1) {
            this.placedTrap1 = this.createDispenser(pLevel, pBox, pRandom, 3, -2, 1, Direction.NORTH, BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
         }

         this.placeBlock(pLevel, Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, Boolean.valueOf(true)), 3, -2, 2, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.NORTH).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 1, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.SOUTH).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 5, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.NORTH, Boolean.valueOf(true)).setValue(TripWireBlock.SOUTH, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 2, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.NORTH, Boolean.valueOf(true)).setValue(TripWireBlock.SOUTH, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 3, pBox);
         this.placeBlock(pLevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.NORTH, Boolean.valueOf(true)).setValue(TripWireBlock.SOUTH, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 4, pBox);
         this.placeBlock(pLevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 8, -3, 6, pBox);
         this.placeBlock(pLevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE), 9, -3, 6, pBox);
         this.placeBlock(pLevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.UP), 9, -3, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 9, -3, 4, pBox);
         this.placeBlock(pLevel, blockstate5, 9, -2, 4, pBox);
         if (!this.placedTrap2) {
            this.placedTrap2 = this.createDispenser(pLevel, pBox, pRandom, 9, -2, 3, Direction.WEST, BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
         }

         this.placeBlock(pLevel, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, Boolean.valueOf(true)), 8, -1, 3, pBox);
         this.placeBlock(pLevel, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, Boolean.valueOf(true)), 8, -2, 3, pBox);
         if (!this.placedMainChest) {
            this.placedMainChest = this.createChest(pLevel, pBox, pRandom, 8, -3, 3, BuiltInLootTables.JUNGLE_TEMPLE);
         }

         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 9, -3, 2, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 8, -3, 1, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 4, -3, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 5, -2, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 5, -1, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 6, -3, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 7, -2, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 7, -1, 5, pBox);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 8, -3, 5, pBox);
         this.generateBox(pLevel, pBox, 9, -1, 1, 9, -1, 5, false, pRandom, STONE_SELECTOR);
         this.generateAirBox(pLevel, pBox, 8, -3, 8, 10, -1, 10);
         this.placeBlock(pLevel, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 8, -2, 11, pBox);
         this.placeBlock(pLevel, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 9, -2, 11, pBox);
         this.placeBlock(pLevel, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 10, -2, 11, pBox);
         BlockState blockstate2 = Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACING, Direction.NORTH).setValue(LeverBlock.FACE, AttachFace.WALL);
         this.placeBlock(pLevel, blockstate2, 8, -2, 12, pBox);
         this.placeBlock(pLevel, blockstate2, 9, -2, 12, pBox);
         this.placeBlock(pLevel, blockstate2, 10, -2, 12, pBox);
         this.generateBox(pLevel, pBox, 8, -3, 8, 8, -3, 10, false, pRandom, STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 10, -3, 8, 10, -3, 10, false, pRandom, STONE_SELECTOR);
         this.placeBlock(pLevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 10, -2, 9, pBox);
         this.placeBlock(pLevel, blockstate5, 8, -2, 9, pBox);
         this.placeBlock(pLevel, blockstate5, 8, -2, 10, pBox);
         this.placeBlock(pLevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 10, -1, 9, pBox);
         this.placeBlock(pLevel, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP), 9, -2, 8, pBox);
         this.placeBlock(pLevel, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), 10, -2, 8, pBox);
         this.placeBlock(pLevel, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), 10, -1, 8, pBox);
         this.placeBlock(pLevel, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.NORTH), 10, -2, 10, pBox);
         if (!this.placedHiddenChest) {
            this.placedHiddenChest = this.createChest(pLevel, pBox, pRandom, 9, -3, 10, BuiltInLootTables.JUNGLE_TEMPLE);
         }

      }
   }

   static class MossStoneSelector extends StructurePiece.BlockSelector {
      public void next(RandomSource p_227686_, int p_227687_, int p_227688_, int p_227689_, boolean p_227690_) {
         if (p_227686_.nextFloat() < 0.4F) {
            this.next = Blocks.COBBLESTONE.defaultBlockState();
         } else {
            this.next = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
         }

      }
   }
}