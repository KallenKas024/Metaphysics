package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class NetherFortressPieces {
   private static final int MAX_DEPTH = 30;
   private static final int LOWEST_Y_POSITION = 10;
   public static final int MAGIC_START_Y = 64;
   static final NetherFortressPieces.PieceWeight[] BRIDGE_PIECE_WEIGHTS = new NetherFortressPieces.PieceWeight[]{new NetherFortressPieces.PieceWeight(NetherFortressPieces.BridgeStraight.class, 30, 0, true), new NetherFortressPieces.PieceWeight(NetherFortressPieces.BridgeCrossing.class, 10, 4), new NetherFortressPieces.PieceWeight(NetherFortressPieces.RoomCrossing.class, 10, 4), new NetherFortressPieces.PieceWeight(NetherFortressPieces.StairsRoom.class, 10, 3), new NetherFortressPieces.PieceWeight(NetherFortressPieces.MonsterThrone.class, 5, 2), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleEntrance.class, 5, 1)};
   static final NetherFortressPieces.PieceWeight[] CASTLE_PIECE_WEIGHTS = new NetherFortressPieces.PieceWeight[]{new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorPiece.class, 25, 0, true), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorCrossingPiece.class, 15, 5), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class, 5, 10), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class, 5, 10), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleCorridorStairsPiece.class, 10, 3, true), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleCorridorTBalconyPiece.class, 7, 2), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleStalkRoom.class, 5, 2)};

   static NetherFortressPieces.NetherBridgePiece findAndCreateBridgePieceFactory(NetherFortressPieces.PieceWeight pWeight, StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
      Class<? extends NetherFortressPieces.NetherBridgePiece> oclass = pWeight.pieceClass;
      NetherFortressPieces.NetherBridgePiece netherfortresspieces$netherbridgepiece = null;
      if (oclass == NetherFortressPieces.BridgeStraight.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.BridgeStraight.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.BridgeCrossing.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.BridgeCrossing.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.RoomCrossing.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.RoomCrossing.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.StairsRoom.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.StairsRoom.createPiece(pPieces, pX, pY, pZ, pGenDepth, pOrientation);
      } else if (oclass == NetherFortressPieces.MonsterThrone.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.MonsterThrone.createPiece(pPieces, pX, pY, pZ, pGenDepth, pOrientation);
      } else if (oclass == NetherFortressPieces.CastleEntrance.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleEntrance.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorPiece.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleSmallCorridorPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleSmallCorridorRightTurnPiece.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleCorridorStairsPiece.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleCorridorStairsPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleCorridorTBalconyPiece.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleCorridorTBalconyPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorCrossingPiece.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleSmallCorridorCrossingPiece.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      } else if (oclass == NetherFortressPieces.CastleStalkRoom.class) {
         netherfortresspieces$netherbridgepiece = NetherFortressPieces.CastleStalkRoom.createPiece(pPieces, pX, pY, pZ, pOrientation, pGenDepth);
      }

      return netherfortresspieces$netherbridgepiece;
   }

   public static class BridgeCrossing extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 19;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 19;

      public BridgeCrossing(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      protected BridgeCrossing(int pX, int pZ, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0, StructurePiece.makeBoundingBox(pX, 64, pZ, pOrientation, 19, 10, 19));
         this.setOrientation(pOrientation);
      }

      protected BridgeCrossing(StructurePieceType pType, CompoundTag pTag) {
         super(pType, pTag);
      }

      public BridgeCrossing(CompoundTag pTag) {
         this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 8, 3, false);
         this.generateChildLeft((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 3, 8, false);
         this.generateChildRight((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 3, 8, false);
      }

      public static NetherFortressPieces.BridgeCrossing createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -8, -3, 0, 19, 10, 19, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.BridgeCrossing(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 7; i <= 11; ++i) {
            for(int j = 0; j <= 2; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, pBox);
            }
         }

         this.generateBox(pLevel, pBox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int k = 0; k <= 2; ++k) {
            for(int l = 7; l <= 11; ++l) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), k, -1, l, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - k, -1, l, pBox);
            }
         }

      }
   }

   public static class BridgeEndFiller extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 8;
      private final int selfSeed;

      public BridgeEndFiller(int pGenDepth, RandomSource pRandom, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, pGenDepth, pBox);
         this.setOrientation(pOrientation);
         this.selfSeed = pRandom.nextInt();
      }

      public BridgeEndFiller(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, pTag);
         this.selfSeed = pTag.getInt("Seed");
      }

      public static NetherFortressPieces.BridgeEndFiller createPiece(StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, -3, 0, 5, 10, 8, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.BridgeEndFiller(pGenDepth, pRandom, boundingbox, pOrientation) : null;
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
         super.addAdditionalSaveData(pContext, pTag);
         pTag.putInt("Seed", this.selfSeed);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         RandomSource randomsource = RandomSource.create((long)this.selfSeed);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 3; j <= 4; ++j) {
               int k = randomsource.nextInt(8);
               this.generateBox(pLevel, pBox, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }
         }

         int l = randomsource.nextInt(8);
         this.generateBox(pLevel, pBox, 0, 5, 0, 0, 5, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         l = randomsource.nextInt(8);
         this.generateBox(pLevel, pBox, 4, 5, 0, 4, 5, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i1 = 0; i1 <= 4; ++i1) {
            int k1 = randomsource.nextInt(5);
            this.generateBox(pLevel, pBox, i1, 2, 0, i1, 2, k1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         }

         for(int j1 = 0; j1 <= 4; ++j1) {
            for(int l1 = 0; l1 <= 1; ++l1) {
               int i2 = randomsource.nextInt(3);
               this.generateBox(pLevel, pBox, j1, l1, 0, j1, l1, i2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }
         }

      }
   }

   public static class BridgeStraight extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 19;

      public BridgeStraight(int pGenDepth, RandomSource pRandom, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public BridgeStraight(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 1, 3, false);
      }

      public static NetherFortressPieces.BridgeStraight createPiece(StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, -3, 0, 5, 10, 19, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.BridgeStraight(pGenDepth, pRandom, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 2; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, pBox);
            }
         }

         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate2 = blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate = blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 0, 1, 1, 0, 4, 1, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 0, 3, 4, 0, 4, 4, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 0, 3, 14, 0, 4, 14, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 0, 1, 17, 0, 4, 17, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 4, 1, 1, 4, 4, 1, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 4, 3, 4, 4, 4, 4, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 4, 3, 14, 4, 4, 14, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 4, 1, 17, 4, 4, 17, blockstate, blockstate, false);
      }
   }

   public static class CastleCorridorStairsPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 10;

      public CastleCorridorStairsPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public CastleCorridorStairsPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 1, 0, true);
      }

      public static NetherFortressPieces.CastleCorridorStairsPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, -7, 0, 5, 14, 10, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleCorridorStairsPiece(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         BlockState blockstate = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

         for(int i = 0; i <= 9; ++i) {
            int j = Math.max(1, 7 - i);
            int k = Math.min(Math.max(j + 5, 14 - i), 13);
            int l = i;
            this.generateBox(pLevel, pBox, 0, 0, i, 4, j, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 1, j + 1, i, 3, k - 1, i, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            if (i <= 6) {
               this.placeBlock(pLevel, blockstate, 1, j + 1, i, pBox);
               this.placeBlock(pLevel, blockstate, 2, j + 1, i, pBox);
               this.placeBlock(pLevel, blockstate, 3, j + 1, i, pBox);
            }

            this.generateBox(pLevel, pBox, 0, k, i, 4, k, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 0, j + 1, i, 0, k - 1, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 4, j + 1, i, 4, k - 1, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            if ((i & 1) == 0) {
               this.generateBox(pLevel, pBox, 0, j + 2, i, 0, j + 3, i, blockstate1, blockstate1, false);
               this.generateBox(pLevel, pBox, 4, j + 2, i, 4, j + 3, i, blockstate1, blockstate1, false);
            }

            for(int i1 = 0; i1 <= 4; ++i1) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i1, -1, l, pBox);
            }
         }

      }
   }

   public static class CastleCorridorTBalconyPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 9;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 9;

      public CastleCorridorTBalconyPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public CastleCorridorTBalconyPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         int i = 1;
         Direction direction = this.getOrientation();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 5;
         }

         this.generateChildLeft((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, i, pRandom.nextInt(8) > 0);
         this.generateChildRight((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, i, pRandom.nextInt(8) > 0);
      }

      public static NetherFortressPieces.CastleCorridorTBalconyPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -3, 0, 0, 9, 7, 9, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleCorridorTBalconyPiece(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 3, 0, 1, 4, 0, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 7, 3, 0, 7, 4, 0, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 3, 8, 7, 3, 8, blockstate1, blockstate1, false);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 0, 3, 8, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 8, 3, 8, pBox);
         this.generateBox(pLevel, pBox, 0, 3, 6, 0, 3, 7, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 8, 3, 6, 8, 3, 7, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 4, 5, 1, 5, 5, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 7, 4, 5, 7, 5, 5, blockstate1, blockstate1, false);

         for(int i = 0; i <= 5; ++i) {
            for(int j = 0; j <= 8; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), j, -1, i, pBox);
            }
         }

      }
   }

   public static class CastleEntrance extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 13;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 13;

      public CastleEntrance(int pGenDepth, RandomSource pRandom, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public CastleEntrance(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 5, 3, true);
      }

      public static NetherFortressPieces.CastleEntrance createPiece(StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -5, -3, 0, 13, 14, 13, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleEntrance(pGenDepth, pRandom, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

         for(int i = 1; i <= 11; i += 2) {
            this.generateBox(pLevel, pBox, i, 10, 0, i, 11, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, i, 10, 12, i, 11, 12, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 0, 10, i, 0, 11, i, blockstate1, blockstate1, false);
            this.generateBox(pLevel, pBox, 12, 10, i, 12, 11, i, blockstate1, blockstate1, false);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, pBox);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, pBox);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, pBox);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, pBox);
            if (i != 11) {
               this.placeBlock(pLevel, blockstate, i + 1, 13, 0, pBox);
               this.placeBlock(pLevel, blockstate, i + 1, 13, 12, pBox);
               this.placeBlock(pLevel, blockstate1, 0, 13, i + 1, pBox);
               this.placeBlock(pLevel, blockstate1, 12, 13, i + 1, pBox);
            }
         }

         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 0, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 12, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 12, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 0, pBox);

         for(int k = 3; k <= 9; k += 2) {
            this.generateBox(pLevel, pBox, 1, 7, k, 1, 8, k, blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), false);
            this.generateBox(pLevel, pBox, 11, 7, k, 11, 8, k, blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), false);
         }

         this.generateBox(pLevel, pBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int l = 4; l <= 8; ++l) {
            for(int j = 0; j <= 2; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, j, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, 12 - j, pBox);
            }
         }

         for(int i1 = 0; i1 <= 2; ++i1) {
            for(int j1 = 4; j1 <= 8; ++j1) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i1, -1, j1, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i1, -1, j1, pBox);
            }
         }

         this.generateBox(pLevel, pBox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, pBox);
         this.placeBlock(pLevel, Blocks.LAVA.defaultBlockState(), 6, 5, 6, pBox);
         BlockPos blockpos = this.getWorldPos(6, 5, 6);
         if (pBox.isInside(blockpos)) {
            pLevel.scheduleTick(blockpos, Fluids.LAVA, 0);
         }

      }
   }

   public static class CastleSmallCorridorCrossingPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;

      public CastleSmallCorridorCrossingPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public CastleSmallCorridorCrossingPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 1, 0, true);
         this.generateChildLeft((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
         this.generateChildRight((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorCrossingPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorCrossingPiece(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   public static class CastleSmallCorridorLeftTurnPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;
      private boolean isNeedingChest;

      public CastleSmallCorridorLeftTurnPiece(int pGenDepth, RandomSource pRandom, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, pGenDepth, pBox);
         this.setOrientation(pOrientation);
         this.isNeedingChest = pRandom.nextInt(3) == 0;
      }

      public CastleSmallCorridorLeftTurnPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, pTag);
         this.isNeedingChest = pTag.getBoolean("Chest");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
         super.addAdditionalSaveData(pContext, pTag);
         pTag.putBoolean("Chest", this.isNeedingChest);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildLeft((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorLeftTurnPiece createPiece(StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorLeftTurnPiece(pGenDepth, pRandom, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 3, 1, 4, 4, 1, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 4, 3, 3, 4, 4, 3, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 3, 4, 1, 4, 4, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 3, 3, 4, 3, 4, 4, blockstate, blockstate, false);
         if (this.isNeedingChest && pBox.isInside(this.getWorldPos(3, 2, 3))) {
            this.isNeedingChest = false;
            this.createChest(pLevel, pBox, pRandom, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
         }

         this.generateBox(pLevel, pBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   public static class CastleSmallCorridorPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;

      public CastleSmallCorridorPiece(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public CastleSmallCorridorPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 1, 0, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorPiece createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorPiece(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 3, 1, 0, 4, 1, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 0, 3, 3, 0, 4, 3, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 4, 3, 1, 4, 4, 1, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 4, 3, 3, 4, 4, 3, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   public static class CastleSmallCorridorRightTurnPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;
      private boolean isNeedingChest;

      public CastleSmallCorridorRightTurnPiece(int pGenDepth, RandomSource pRandom, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, pGenDepth, pBox);
         this.setOrientation(pOrientation);
         this.isNeedingChest = pRandom.nextInt(3) == 0;
      }

      public CastleSmallCorridorRightTurnPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, pTag);
         this.isNeedingChest = pTag.getBoolean("Chest");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
         super.addAdditionalSaveData(pContext, pTag);
         pTag.putBoolean("Chest", this.isNeedingChest);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildRight((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, 1, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorRightTurnPiece createPiece(StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -1, 0, 0, 5, 7, 5, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorRightTurnPiece(pGenDepth, pRandom, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 3, 1, 0, 4, 1, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 0, 3, 3, 0, 4, 3, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 3, 4, 1, 4, 4, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 3, 3, 4, 3, 4, 4, blockstate, blockstate, false);
         if (this.isNeedingChest && pBox.isInside(this.getWorldPos(1, 2, 3))) {
            this.isNeedingChest = false;
            this.createChest(pLevel, pBox, pRandom, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
         }

         this.generateBox(pLevel, pBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   public static class CastleStalkRoom extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 13;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 13;

      public CastleStalkRoom(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public CastleStalkRoom(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 5, 3, true);
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 5, 11, true);
      }

      public static NetherFortressPieces.CastleStalkRoom createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -5, -3, 0, 13, 14, 13, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleStalkRoom(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate2 = blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true));
         BlockState blockstate3 = blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true));

         for(int i = 1; i <= 11; i += 2) {
            this.generateBox(pLevel, pBox, i, 10, 0, i, 11, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, i, 10, 12, i, 11, 12, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 0, 10, i, 0, 11, i, blockstate1, blockstate1, false);
            this.generateBox(pLevel, pBox, 12, 10, i, 12, 11, i, blockstate1, blockstate1, false);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, pBox);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, pBox);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, pBox);
            this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, pBox);
            if (i != 11) {
               this.placeBlock(pLevel, blockstate, i + 1, 13, 0, pBox);
               this.placeBlock(pLevel, blockstate, i + 1, 13, 12, pBox);
               this.placeBlock(pLevel, blockstate1, 0, 13, i + 1, pBox);
               this.placeBlock(pLevel, blockstate1, 12, 13, i + 1, pBox);
            }
         }

         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 0, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 12, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 12, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 0, pBox);

         for(int j1 = 3; j1 <= 9; j1 += 2) {
            this.generateBox(pLevel, pBox, 1, 7, j1, 1, 8, j1, blockstate2, blockstate2, false);
            this.generateBox(pLevel, pBox, 11, 7, j1, 11, 8, j1, blockstate3, blockstate3, false);
         }

         BlockState blockstate4 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);

         for(int j = 0; j <= 6; ++j) {
            int k = j + 4;

            for(int l = 5; l <= 7; ++l) {
               this.placeBlock(pLevel, blockstate4, l, 5 + j, k, pBox);
            }

            if (k >= 5 && k <= 8) {
               this.generateBox(pLevel, pBox, 5, 5, k, 7, j + 4, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            } else if (k >= 9 && k <= 10) {
               this.generateBox(pLevel, pBox, 5, 8, k, 7, j + 4, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }

            if (j >= 1) {
               this.generateBox(pLevel, pBox, 5, 6 + j, k, 7, 9 + j, k, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }
         }

         for(int k1 = 5; k1 <= 7; ++k1) {
            this.placeBlock(pLevel, blockstate4, k1, 12, 11, pBox);
         }

         this.generateBox(pLevel, pBox, 5, 6, 7, 5, 7, 7, blockstate3, blockstate3, false);
         this.generateBox(pLevel, pBox, 7, 6, 7, 7, 7, 7, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate5 = blockstate4.setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate6 = blockstate4.setValue(StairBlock.FACING, Direction.WEST);
         this.placeBlock(pLevel, blockstate6, 4, 5, 2, pBox);
         this.placeBlock(pLevel, blockstate6, 4, 5, 3, pBox);
         this.placeBlock(pLevel, blockstate6, 4, 5, 9, pBox);
         this.placeBlock(pLevel, blockstate6, 4, 5, 10, pBox);
         this.placeBlock(pLevel, blockstate5, 8, 5, 2, pBox);
         this.placeBlock(pLevel, blockstate5, 8, 5, 3, pBox);
         this.placeBlock(pLevel, blockstate5, 8, 5, 9, pBox);
         this.placeBlock(pLevel, blockstate5, 8, 5, 10, pBox);
         this.generateBox(pLevel, pBox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int l1 = 4; l1 <= 8; ++l1) {
            for(int i1 = 0; i1 <= 2; ++i1) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l1, -1, i1, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l1, -1, 12 - i1, pBox);
            }
         }

         for(int i2 = 0; i2 <= 2; ++i2) {
            for(int j2 = 4; j2 <= 8; ++j2) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i2, -1, j2, pBox);
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i2, -1, j2, pBox);
            }
         }

      }
   }

   public static class MonsterThrone extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 8;
      private static final int DEPTH = 9;
      private boolean hasPlacedSpawner;

      public MonsterThrone(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public MonsterThrone(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, pTag);
         this.hasPlacedSpawner = pTag.getBoolean("Mob");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
         super.addAdditionalSaveData(pContext, pTag);
         pTag.putBoolean("Mob", this.hasPlacedSpawner);
      }

      public static NetherFortressPieces.MonsterThrone createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, int pGenDepth, Direction pOrientation) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -2, 0, 0, 7, 8, 9, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.MonsterThrone(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 1, 6, 3, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 5, 6, 3, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 0, 6, 3, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 6, 6, 3, pBox);
         this.generateBox(pLevel, pBox, 0, 6, 4, 0, 6, 7, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 6, 6, 4, 6, 6, 7, blockstate1, blockstate1, false);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 0, 6, 8, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 6, 6, 8, pBox);
         this.generateBox(pLevel, pBox, 1, 6, 8, 5, 6, 8, blockstate, blockstate, false);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 1, 7, 8, pBox);
         this.generateBox(pLevel, pBox, 2, 7, 8, 4, 7, 8, blockstate, blockstate, false);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 5, 7, 8, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 2, 8, 8, pBox);
         this.placeBlock(pLevel, blockstate, 3, 8, 8, pBox);
         this.placeBlock(pLevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 4, 8, 8, pBox);
         if (!this.hasPlacedSpawner) {
            BlockPos blockpos = this.getWorldPos(3, 5, 5);
            if (pBox.isInside(blockpos)) {
               this.hasPlacedSpawner = true;
               pLevel.setBlock(blockpos, Blocks.SPAWNER.defaultBlockState(), 2);
               BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
               if (blockentity instanceof SpawnerBlockEntity) {
                  SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
                  spawnerblockentity.setEntityId(EntityType.BLAZE, pRandom);
               }
            }
         }

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   abstract static class NetherBridgePiece extends StructurePiece {
      protected NetherBridgePiece(StructurePieceType pType, int pGenDepth, BoundingBox pBoundingBox) {
         super(pType, pGenDepth, pBoundingBox);
      }

      public NetherBridgePiece(StructurePieceType pType, CompoundTag pTag) {
         super(pType, pTag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      }

      private int updatePieceWeight(List<NetherFortressPieces.PieceWeight> pWeights) {
         boolean flag = false;
         int i = 0;

         for(NetherFortressPieces.PieceWeight netherfortresspieces$pieceweight : pWeights) {
            if (netherfortresspieces$pieceweight.maxPlaceCount > 0 && netherfortresspieces$pieceweight.placeCount < netherfortresspieces$pieceweight.maxPlaceCount) {
               flag = true;
            }

            i += netherfortresspieces$pieceweight.weight;
         }

         return flag ? i : -1;
      }

      private NetherFortressPieces.NetherBridgePiece generatePiece(NetherFortressPieces.StartPiece pStartPiece, List<NetherFortressPieces.PieceWeight> pWeights, StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         int i = this.updatePieceWeight(pWeights);
         boolean flag = i > 0 && pGenDepth <= 30;
         int j = 0;

         while(j < 5 && flag) {
            ++j;
            int k = pRandom.nextInt(i);

            for(NetherFortressPieces.PieceWeight netherfortresspieces$pieceweight : pWeights) {
               k -= netherfortresspieces$pieceweight.weight;
               if (k < 0) {
                  if (!netherfortresspieces$pieceweight.doPlace(pGenDepth) || netherfortresspieces$pieceweight == pStartPiece.previousPiece && !netherfortresspieces$pieceweight.allowInRow) {
                     break;
                  }

                  NetherFortressPieces.NetherBridgePiece netherfortresspieces$netherbridgepiece = NetherFortressPieces.findAndCreateBridgePieceFactory(netherfortresspieces$pieceweight, pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
                  if (netherfortresspieces$netherbridgepiece != null) {
                     ++netherfortresspieces$pieceweight.placeCount;
                     pStartPiece.previousPiece = netherfortresspieces$pieceweight;
                     if (!netherfortresspieces$pieceweight.isValid()) {
                        pWeights.remove(netherfortresspieces$pieceweight);
                     }

                     return netherfortresspieces$netherbridgepiece;
                  }
               }
            }
         }

         return NetherFortressPieces.BridgeEndFiller.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
      }

      private StructurePiece generateAndAddPiece(NetherFortressPieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, RandomSource pRandom, int pX, int pY, int pZ, @Nullable Direction pOrientation, int pGenDepth, boolean pCastlePiece) {
         if (Math.abs(pX - pStartPiece.getBoundingBox().minX()) <= 112 && Math.abs(pZ - pStartPiece.getBoundingBox().minZ()) <= 112) {
            List<NetherFortressPieces.PieceWeight> list = pStartPiece.availableBridgePieces;
            if (pCastlePiece) {
               list = pStartPiece.availableCastlePieces;
            }

            StructurePiece structurepiece = this.generatePiece(pStartPiece, list, pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth + 1);
            if (structurepiece != null) {
               pPieces.addPiece(structurepiece);
               pStartPiece.pendingChildren.add(structurepiece);
            }

            return structurepiece;
         } else {
            return NetherFortressPieces.BridgeEndFiller.createPiece(pPieces, pRandom, pX, pY, pZ, pOrientation, pGenDepth);
         }
      }

      @Nullable
      protected StructurePiece generateChildForward(NetherFortressPieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, RandomSource pRandom, int pOffsetX, int pOffsetY, boolean pCastlePiece) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() - 1, direction, this.getGenDepth(), pCastlePiece);
               case SOUTH:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.maxZ() + 1, direction, this.getGenDepth(), pCastlePiece);
               case WEST:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, direction, this.getGenDepth(), pCastlePiece);
               case EAST:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, direction, this.getGenDepth(), pCastlePiece);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateChildLeft(NetherFortressPieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, RandomSource pRandom, int pOffsetY, int pOffsetX, boolean pCastlePiece) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.WEST, this.getGenDepth(), pCastlePiece);
               case SOUTH:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() - 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.WEST, this.getGenDepth(), pCastlePiece);
               case WEST:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), pCastlePiece);
               case EAST:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), pCastlePiece);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateChildRight(NetherFortressPieces.StartPiece pStartPiece, StructurePieceAccessor pPieces, RandomSource pRandom, int pOffsetY, int pOffsetX, boolean pCastlePiece) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.EAST, this.getGenDepth(), pCastlePiece);
               case SOUTH:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.maxX() + 1, this.boundingBox.minY() + pOffsetY, this.boundingBox.minZ() + pOffsetX, Direction.EAST, this.getGenDepth(), pCastlePiece);
               case WEST:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), pCastlePiece);
               case EAST:
                  return this.generateAndAddPiece(pStartPiece, pPieces, pRandom, this.boundingBox.minX() + pOffsetX, this.boundingBox.minY() + pOffsetY, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), pCastlePiece);
            }
         }

         return null;
      }

      protected static boolean isOkBox(BoundingBox pBox) {
         return pBox != null && pBox.minY() > 10;
      }
   }

   static class PieceWeight {
      public final Class<? extends NetherFortressPieces.NetherBridgePiece> pieceClass;
      public final int weight;
      public int placeCount;
      public final int maxPlaceCount;
      public final boolean allowInRow;

      public PieceWeight(Class<? extends NetherFortressPieces.NetherBridgePiece> pPieceClass, int pWeight, int pMaxPlaceCount, boolean pAllowInRow) {
         this.pieceClass = pPieceClass;
         this.weight = pWeight;
         this.maxPlaceCount = pMaxPlaceCount;
         this.allowInRow = pAllowInRow;
      }

      public PieceWeight(Class<? extends NetherFortressPieces.NetherBridgePiece> pPieceClass, int pWeight, int pMaxPlaceCount) {
         this(pPieceClass, pWeight, pMaxPlaceCount, false);
      }

      public boolean doPlace(int pGenDepth) {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }

      public boolean isValid() {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }
   }

   public static class RoomCrossing extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 9;
      private static final int DEPTH = 7;

      public RoomCrossing(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public RoomCrossing(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildForward((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 2, 0, false);
         this.generateChildLeft((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, 2, false);
         this.generateChildRight((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 0, 2, false);
      }

      public static NetherFortressPieces.RoomCrossing createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, Direction pOrientation, int pGenDepth) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -2, 0, 0, 7, 9, 7, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.RoomCrossing(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 0, 4, 5, 0, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 6, 4, 5, 6, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 5, 2, 0, 5, 4, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 5, 2, 6, 5, 4, blockstate1, blockstate1, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   public static class StairsRoom extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 7;

      public StairsRoom(int pGenDepth, BoundingBox pBox, Direction pOrientation) {
         super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      public StairsRoom(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, pTag);
      }

      public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
         this.generateChildRight((NetherFortressPieces.StartPiece)pPiece, pPieces, pRandom, 6, 2, false);
      }

      public static NetherFortressPieces.StairsRoom createPiece(StructurePieceAccessor pPieces, int pX, int pY, int pZ, int pGenDepth, Direction pOrientation) {
         BoundingBox boundingbox = BoundingBox.orientBox(pX, pY, pZ, -2, 0, 0, 7, 11, 7, pOrientation);
         return isOkBox(boundingbox) && pPieces.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.StairsRoom(pGenDepth, boundingbox, pOrientation) : null;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(pLevel, pBox, 0, 3, 2, 0, 5, 4, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 6, 3, 2, 6, 5, 2, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 6, 3, 4, 6, 5, 4, blockstate1, blockstate1, false);
         this.placeBlock(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, pBox);
         this.generateBox(pLevel, pBox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 5, 0, 4, 5, 0, blockstate, blockstate, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillColumnDown(pLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, pBox);
            }
         }

      }
   }

   public static class StartPiece extends NetherFortressPieces.BridgeCrossing {
      public NetherFortressPieces.PieceWeight previousPiece;
      public List<NetherFortressPieces.PieceWeight> availableBridgePieces;
      public List<NetherFortressPieces.PieceWeight> availableCastlePieces;
      public final List<StructurePiece> pendingChildren = Lists.newArrayList();

      public StartPiece(RandomSource pRandom, int pX, int pZ) {
         super(pX, pZ, getRandomHorizontalDirection(pRandom));
         this.availableBridgePieces = Lists.newArrayList();

         for(NetherFortressPieces.PieceWeight netherfortresspieces$pieceweight : NetherFortressPieces.BRIDGE_PIECE_WEIGHTS) {
            netherfortresspieces$pieceweight.placeCount = 0;
            this.availableBridgePieces.add(netherfortresspieces$pieceweight);
         }

         this.availableCastlePieces = Lists.newArrayList();

         for(NetherFortressPieces.PieceWeight netherfortresspieces$pieceweight1 : NetherFortressPieces.CASTLE_PIECE_WEIGHTS) {
            netherfortresspieces$pieceweight1.placeCount = 0;
            this.availableCastlePieces.add(netherfortresspieces$pieceweight1);
         }

      }

      public StartPiece(CompoundTag pTag) {
         super(StructurePieceType.NETHER_FORTRESS_START, pTag);
      }
   }
}