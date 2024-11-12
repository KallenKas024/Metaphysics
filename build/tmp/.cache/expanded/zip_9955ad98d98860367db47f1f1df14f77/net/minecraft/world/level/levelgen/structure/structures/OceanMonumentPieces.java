package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OceanMonumentPieces {
   private OceanMonumentPieces() {
   }

   static class FitDoubleXRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228592_) {
         return p_228592_.hasOpening[Direction.EAST.get3DDataValue()] && !p_228592_.connections[Direction.EAST.get3DDataValue()].claimed;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228594_, OceanMonumentPieces.RoomDefinition p_228595_, RandomSource p_228596_) {
         p_228595_.claimed = true;
         p_228595_.connections[Direction.EAST.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleXRoom(p_228594_, p_228595_);
      }
   }

   static class FitDoubleXYRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228599_) {
         if (p_228599_.hasOpening[Direction.EAST.get3DDataValue()] && !p_228599_.connections[Direction.EAST.get3DDataValue()].claimed && p_228599_.hasOpening[Direction.UP.get3DDataValue()] && !p_228599_.connections[Direction.UP.get3DDataValue()].claimed) {
            OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = p_228599_.connections[Direction.EAST.get3DDataValue()];
            return oceanmonumentpieces$roomdefinition.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228601_, OceanMonumentPieces.RoomDefinition p_228602_, RandomSource p_228603_) {
         p_228602_.claimed = true;
         p_228602_.connections[Direction.EAST.get3DDataValue()].claimed = true;
         p_228602_.connections[Direction.UP.get3DDataValue()].claimed = true;
         p_228602_.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleXYRoom(p_228601_, p_228602_);
      }
   }

   static class FitDoubleYRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228606_) {
         return p_228606_.hasOpening[Direction.UP.get3DDataValue()] && !p_228606_.connections[Direction.UP.get3DDataValue()].claimed;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228608_, OceanMonumentPieces.RoomDefinition p_228609_, RandomSource p_228610_) {
         p_228609_.claimed = true;
         p_228609_.connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleYRoom(p_228608_, p_228609_);
      }
   }

   static class FitDoubleYZRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228613_) {
         if (p_228613_.hasOpening[Direction.NORTH.get3DDataValue()] && !p_228613_.connections[Direction.NORTH.get3DDataValue()].claimed && p_228613_.hasOpening[Direction.UP.get3DDataValue()] && !p_228613_.connections[Direction.UP.get3DDataValue()].claimed) {
            OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = p_228613_.connections[Direction.NORTH.get3DDataValue()];
            return oceanmonumentpieces$roomdefinition.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228615_, OceanMonumentPieces.RoomDefinition p_228616_, RandomSource p_228617_) {
         p_228616_.claimed = true;
         p_228616_.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         p_228616_.connections[Direction.UP.get3DDataValue()].claimed = true;
         p_228616_.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleYZRoom(p_228615_, p_228616_);
      }
   }

   static class FitDoubleZRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228620_) {
         return p_228620_.hasOpening[Direction.NORTH.get3DDataValue()] && !p_228620_.connections[Direction.NORTH.get3DDataValue()].claimed;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228622_, OceanMonumentPieces.RoomDefinition p_228623_, RandomSource p_228624_) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = p_228623_;
         if (!p_228623_.hasOpening[Direction.NORTH.get3DDataValue()] || p_228623_.connections[Direction.NORTH.get3DDataValue()].claimed) {
            oceanmonumentpieces$roomdefinition = p_228623_.connections[Direction.SOUTH.get3DDataValue()];
         }

         oceanmonumentpieces$roomdefinition.claimed = true;
         oceanmonumentpieces$roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleZRoom(p_228622_, oceanmonumentpieces$roomdefinition);
      }
   }

   static class FitSimpleRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228627_) {
         return true;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228629_, OceanMonumentPieces.RoomDefinition p_228630_, RandomSource p_228631_) {
         p_228630_.claimed = true;
         return new OceanMonumentPieces.OceanMonumentSimpleRoom(p_228629_, p_228630_, p_228631_);
      }
   }

   static class FitSimpleTopRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition p_228634_) {
         return !p_228634_.hasOpening[Direction.WEST.get3DDataValue()] && !p_228634_.hasOpening[Direction.EAST.get3DDataValue()] && !p_228634_.hasOpening[Direction.NORTH.get3DDataValue()] && !p_228634_.hasOpening[Direction.SOUTH.get3DDataValue()] && !p_228634_.hasOpening[Direction.UP.get3DDataValue()];
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction p_228636_, OceanMonumentPieces.RoomDefinition p_228637_, RandomSource p_228638_) {
         p_228637_.claimed = true;
         return new OceanMonumentPieces.OceanMonumentSimpleTopRoom(p_228636_, p_228637_);
      }
   }

   public static class MonumentBuilding extends OceanMonumentPieces.OceanMonumentPiece {
      private static final int WIDTH = 58;
      private static final int HEIGHT = 22;
      private static final int DEPTH = 58;
      public static final int BIOME_RANGE_CHECK = 29;
      private static final int TOP_POSITION = 61;
      private OceanMonumentPieces.RoomDefinition sourceRoom;
      private OceanMonumentPieces.RoomDefinition coreRoom;
      private final List<OceanMonumentPieces.OceanMonumentPiece> childPieces = Lists.newArrayList();

      public MonumentBuilding(RandomSource pRandom, int pX, int pZ, Direction pOrientation) {
         super(StructurePieceType.OCEAN_MONUMENT_BUILDING, pOrientation, 0, makeBoundingBox(pX, 39, pZ, pOrientation, 58, 23, 58));
         this.setOrientation(pOrientation);
         List<OceanMonumentPieces.RoomDefinition> list = this.generateRoomGraph(pRandom);
         this.sourceRoom.claimed = true;
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentEntryRoom(pOrientation, this.sourceRoom));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentCoreRoom(pOrientation, this.coreRoom));
         List<OceanMonumentPieces.MonumentRoomFitter> list1 = Lists.newArrayList();
         list1.add(new OceanMonumentPieces.FitDoubleXYRoom());
         list1.add(new OceanMonumentPieces.FitDoubleYZRoom());
         list1.add(new OceanMonumentPieces.FitDoubleZRoom());
         list1.add(new OceanMonumentPieces.FitDoubleXRoom());
         list1.add(new OceanMonumentPieces.FitDoubleYRoom());
         list1.add(new OceanMonumentPieces.FitSimpleTopRoom());
         list1.add(new OceanMonumentPieces.FitSimpleRoom());

         for(OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition : list) {
            if (!oceanmonumentpieces$roomdefinition.claimed && !oceanmonumentpieces$roomdefinition.isSpecial()) {
               for(OceanMonumentPieces.MonumentRoomFitter oceanmonumentpieces$monumentroomfitter : list1) {
                  if (oceanmonumentpieces$monumentroomfitter.fits(oceanmonumentpieces$roomdefinition)) {
                     this.childPieces.add(oceanmonumentpieces$monumentroomfitter.create(pOrientation, oceanmonumentpieces$roomdefinition, pRandom));
                     break;
                  }
               }
            }
         }

         BlockPos blockpos = this.getWorldPos(9, 0, 22);

         for(OceanMonumentPieces.OceanMonumentPiece oceanmonumentpieces$oceanmonumentpiece : this.childPieces) {
            oceanmonumentpieces$oceanmonumentpiece.getBoundingBox().move(blockpos);
         }

         BoundingBox boundingbox = BoundingBox.fromCorners(this.getWorldPos(1, 1, 1), this.getWorldPos(23, 8, 21));
         BoundingBox boundingbox1 = BoundingBox.fromCorners(this.getWorldPos(34, 1, 1), this.getWorldPos(56, 8, 21));
         BoundingBox boundingbox2 = BoundingBox.fromCorners(this.getWorldPos(22, 13, 22), this.getWorldPos(35, 17, 35));
         int i = pRandom.nextInt();
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(pOrientation, boundingbox, i++));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(pOrientation, boundingbox1, i++));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentPenthouse(pOrientation, boundingbox2));
      }

      public MonumentBuilding(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_BUILDING, pTag);
      }

      private List<OceanMonumentPieces.RoomDefinition> generateRoomGraph(RandomSource pRandom) {
         OceanMonumentPieces.RoomDefinition[] aoceanmonumentpieces$roomdefinition = new OceanMonumentPieces.RoomDefinition[75];

         for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 4; ++j) {
               int k = 0;
               int l = getRoomIndex(i, 0, j);
               aoceanmonumentpieces$roomdefinition[l] = new OceanMonumentPieces.RoomDefinition(l);
            }
         }

         for(int i2 = 0; i2 < 5; ++i2) {
            for(int l2 = 0; l2 < 4; ++l2) {
               int k3 = 1;
               int j4 = getRoomIndex(i2, 1, l2);
               aoceanmonumentpieces$roomdefinition[j4] = new OceanMonumentPieces.RoomDefinition(j4);
            }
         }

         for(int j2 = 1; j2 < 4; ++j2) {
            for(int i3 = 0; i3 < 2; ++i3) {
               int l3 = 2;
               int k4 = getRoomIndex(j2, 2, i3);
               aoceanmonumentpieces$roomdefinition[k4] = new OceanMonumentPieces.RoomDefinition(k4);
            }
         }

         this.sourceRoom = aoceanmonumentpieces$roomdefinition[GRIDROOM_SOURCE_INDEX];

         for(int k2 = 0; k2 < 5; ++k2) {
            for(int j3 = 0; j3 < 5; ++j3) {
               for(int i4 = 0; i4 < 3; ++i4) {
                  int l4 = getRoomIndex(k2, i4, j3);
                  if (aoceanmonumentpieces$roomdefinition[l4] != null) {
                     for(Direction direction : Direction.values()) {
                        int i1 = k2 + direction.getStepX();
                        int j1 = i4 + direction.getStepY();
                        int k1 = j3 + direction.getStepZ();
                        if (i1 >= 0 && i1 < 5 && k1 >= 0 && k1 < 5 && j1 >= 0 && j1 < 3) {
                           int l1 = getRoomIndex(i1, j1, k1);
                           if (aoceanmonumentpieces$roomdefinition[l1] != null) {
                              if (k1 == j3) {
                                 aoceanmonumentpieces$roomdefinition[l4].setConnection(direction, aoceanmonumentpieces$roomdefinition[l1]);
                              } else {
                                 aoceanmonumentpieces$roomdefinition[l4].setConnection(direction.getOpposite(), aoceanmonumentpieces$roomdefinition[l1]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = new OceanMonumentPieces.RoomDefinition(1003);
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition1 = new OceanMonumentPieces.RoomDefinition(1001);
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition2 = new OceanMonumentPieces.RoomDefinition(1002);
         aoceanmonumentpieces$roomdefinition[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, oceanmonumentpieces$roomdefinition);
         aoceanmonumentpieces$roomdefinition[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, oceanmonumentpieces$roomdefinition1);
         aoceanmonumentpieces$roomdefinition[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, oceanmonumentpieces$roomdefinition2);
         oceanmonumentpieces$roomdefinition.claimed = true;
         oceanmonumentpieces$roomdefinition1.claimed = true;
         oceanmonumentpieces$roomdefinition2.claimed = true;
         this.sourceRoom.isSource = true;
         this.coreRoom = aoceanmonumentpieces$roomdefinition[getRoomIndex(pRandom.nextInt(4), 0, 2)];
         this.coreRoom.claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         ObjectArrayList<OceanMonumentPieces.RoomDefinition> objectarraylist = new ObjectArrayList<>();

         for(OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition4 : aoceanmonumentpieces$roomdefinition) {
            if (oceanmonumentpieces$roomdefinition4 != null) {
               oceanmonumentpieces$roomdefinition4.updateOpenings();
               objectarraylist.add(oceanmonumentpieces$roomdefinition4);
            }
         }

         oceanmonumentpieces$roomdefinition.updateOpenings();
         Util.shuffle(objectarraylist, pRandom);
         int i5 = 1;

         for(OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition3 : objectarraylist) {
            int j5 = 0;
            int k5 = 0;

            while(j5 < 2 && k5 < 5) {
               ++k5;
               int l5 = pRandom.nextInt(6);
               if (oceanmonumentpieces$roomdefinition3.hasOpening[l5]) {
                  int i6 = Direction.from3DDataValue(l5).getOpposite().get3DDataValue();
                  oceanmonumentpieces$roomdefinition3.hasOpening[l5] = false;
                  oceanmonumentpieces$roomdefinition3.connections[l5].hasOpening[i6] = false;
                  if (oceanmonumentpieces$roomdefinition3.findSource(i5++) && oceanmonumentpieces$roomdefinition3.connections[l5].findSource(i5++)) {
                     ++j5;
                  } else {
                     oceanmonumentpieces$roomdefinition3.hasOpening[l5] = true;
                     oceanmonumentpieces$roomdefinition3.connections[l5].hasOpening[i6] = true;
                  }
               }
            }
         }

         objectarraylist.add(oceanmonumentpieces$roomdefinition);
         objectarraylist.add(oceanmonumentpieces$roomdefinition1);
         objectarraylist.add(oceanmonumentpieces$roomdefinition2);
         return objectarraylist;
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         int i = Math.max(pLevel.getSeaLevel(), 64) - this.boundingBox.minY();
         this.generateWaterBox(pLevel, pBox, 0, 0, 0, 58, i, 58);
         this.generateWing(false, 0, pLevel, pRandom, pBox);
         this.generateWing(true, 33, pLevel, pRandom, pBox);
         this.generateEntranceArchs(pLevel, pRandom, pBox);
         this.generateEntranceWall(pLevel, pRandom, pBox);
         this.generateRoofPiece(pLevel, pRandom, pBox);
         this.generateLowerWall(pLevel, pRandom, pBox);
         this.generateMiddleWall(pLevel, pRandom, pBox);
         this.generateUpperWall(pLevel, pRandom, pBox);

         for(int j = 0; j < 7; ++j) {
            int k = 0;

            while(k < 7) {
               if (k == 0 && j == 3) {
                  k = 6;
               }

               int l = j * 9;
               int i1 = k * 9;

               for(int j1 = 0; j1 < 4; ++j1) {
                  for(int k1 = 0; k1 < 4; ++k1) {
                     this.placeBlock(pLevel, BASE_LIGHT, l + j1, 0, i1 + k1, pBox);
                     this.fillColumnDown(pLevel, BASE_LIGHT, l + j1, -1, i1 + k1, pBox);
                  }
               }

               if (j != 0 && j != 6) {
                  k += 6;
               } else {
                  ++k;
               }
            }
         }

         for(int l1 = 0; l1 < 5; ++l1) {
            this.generateWaterBox(pLevel, pBox, -1 - l1, 0 + l1 * 2, -1 - l1, -1 - l1, 23, 58 + l1);
            this.generateWaterBox(pLevel, pBox, 58 + l1, 0 + l1 * 2, -1 - l1, 58 + l1, 23, 58 + l1);
            this.generateWaterBox(pLevel, pBox, 0 - l1, 0 + l1 * 2, -1 - l1, 57 + l1, 23, -1 - l1);
            this.generateWaterBox(pLevel, pBox, 0 - l1, 0 + l1 * 2, 58 + l1, 57 + l1, 23, 58 + l1);
         }

         for(OceanMonumentPieces.OceanMonumentPiece oceanmonumentpieces$oceanmonumentpiece : this.childPieces) {
            if (oceanmonumentpieces$oceanmonumentpiece.getBoundingBox().intersects(pBox)) {
               oceanmonumentpieces$oceanmonumentpiece.postProcess(pLevel, pStructureManager, pGenerator, pRandom, pBox, pChunkPos, pPos);
            }
         }

      }

      private void generateWing(boolean pWing, int pX, WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         int i = 24;
         if (this.chunkIntersects(pBox, pX, 0, pX + 23, 20)) {
            this.generateBox(pLevel, pBox, pX + 0, 0, 0, pX + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, pX + 0, 1, 0, pX + 24, 10, 20);

            for(int j = 0; j < 4; ++j) {
               this.generateBox(pLevel, pBox, pX + j, j + 1, j, pX + j, j + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, pX + j + 7, j + 5, j + 7, pX + j + 7, j + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, pX + 17 - j, j + 5, j + 7, pX + 17 - j, j + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, pX + 24 - j, j + 1, j, pX + 24 - j, j + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, pX + j + 1, j + 1, j, pX + 23 - j, j + 1, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, pX + j + 8, j + 5, j + 7, pX + 16 - j, j + 5, j + 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(pLevel, pBox, pX + 4, 4, 4, pX + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 7, 4, 4, pX + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 18, 4, 4, pX + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 11, 8, 11, pX + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(pLevel, DOT_DECO_DATA, pX + 12, 9, 12, pBox);
            this.placeBlock(pLevel, DOT_DECO_DATA, pX + 12, 9, 15, pBox);
            this.placeBlock(pLevel, DOT_DECO_DATA, pX + 12, 9, 18, pBox);
            int j1 = pX + (pWing ? 19 : 5);
            int k = pX + (pWing ? 5 : 19);

            for(int l = 20; l >= 5; l -= 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, j1, 5, l, pBox);
            }

            for(int k1 = 19; k1 >= 7; k1 -= 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, k, 5, k1, pBox);
            }

            for(int l1 = 0; l1 < 4; ++l1) {
               int i1 = pWing ? pX + 24 - (17 - l1 * 3) : pX + 17 - l1 * 3;
               this.placeBlock(pLevel, DOT_DECO_DATA, i1, 5, 5, pBox);
            }

            this.placeBlock(pLevel, DOT_DECO_DATA, k, 5, 5, pBox);
            this.generateBox(pLevel, pBox, pX + 11, 1, 12, pX + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 12, 1, 11, pX + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
         }

      }

      private void generateEntranceArchs(WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (this.chunkIntersects(pBox, 22, 5, 35, 17)) {
            this.generateWaterBox(pLevel, pBox, 25, 0, 0, 32, 8, 20);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(pLevel, pBox, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock(pLevel, BASE_LIGHT, 25, 5, 5 + i * 4, pBox);
               this.placeBlock(pLevel, BASE_LIGHT, 26, 6, 5 + i * 4, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, 26, 5, 5 + i * 4, pBox);
               this.generateBox(pLevel, pBox, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock(pLevel, BASE_LIGHT, 32, 5, 5 + i * 4, pBox);
               this.placeBlock(pLevel, BASE_LIGHT, 31, 6, 5 + i * 4, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, 31, 5, 5 + i * 4, pBox);
               this.generateBox(pLevel, pBox, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, BASE_GRAY, BASE_GRAY, false);
            }
         }

      }

      private void generateEntranceWall(WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (this.chunkIntersects(pBox, 15, 20, 42, 21)) {
            this.generateBox(pLevel, pBox, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 26, 1, 21, 31, 3, 21);
            this.generateBox(pLevel, pBox, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(pLevel, BASE_LIGHT, 27, 3, 21, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 30, 3, 21, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 26, 2, 21, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 31, 2, 21, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 25, 1, 21, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 32, 1, 21, pBox);

            for(int i = 0; i < 7; ++i) {
               this.placeBlock(pLevel, BASE_BLACK, 28 - i, 6 + i, 21, pBox);
               this.placeBlock(pLevel, BASE_BLACK, 29 + i, 6 + i, 21, pBox);
            }

            for(int j = 0; j < 4; ++j) {
               this.placeBlock(pLevel, BASE_BLACK, 28 - j, 9 + j, 21, pBox);
               this.placeBlock(pLevel, BASE_BLACK, 29 + j, 9 + j, 21, pBox);
            }

            this.placeBlock(pLevel, BASE_BLACK, 28, 12, 21, pBox);
            this.placeBlock(pLevel, BASE_BLACK, 29, 12, 21, pBox);

            for(int k = 0; k < 3; ++k) {
               this.placeBlock(pLevel, BASE_BLACK, 22 - k * 2, 8, 21, pBox);
               this.placeBlock(pLevel, BASE_BLACK, 22 - k * 2, 9, 21, pBox);
               this.placeBlock(pLevel, BASE_BLACK, 35 + k * 2, 8, 21, pBox);
               this.placeBlock(pLevel, BASE_BLACK, 35 + k * 2, 9, 21, pBox);
            }

            this.generateWaterBox(pLevel, pBox, 15, 13, 21, 42, 15, 21);
            this.generateWaterBox(pLevel, pBox, 15, 1, 21, 15, 6, 21);
            this.generateWaterBox(pLevel, pBox, 16, 1, 21, 16, 5, 21);
            this.generateWaterBox(pLevel, pBox, 17, 1, 21, 20, 4, 21);
            this.generateWaterBox(pLevel, pBox, 21, 1, 21, 21, 3, 21);
            this.generateWaterBox(pLevel, pBox, 22, 1, 21, 22, 2, 21);
            this.generateWaterBox(pLevel, pBox, 23, 1, 21, 24, 1, 21);
            this.generateWaterBox(pLevel, pBox, 42, 1, 21, 42, 6, 21);
            this.generateWaterBox(pLevel, pBox, 41, 1, 21, 41, 5, 21);
            this.generateWaterBox(pLevel, pBox, 37, 1, 21, 40, 4, 21);
            this.generateWaterBox(pLevel, pBox, 36, 1, 21, 36, 3, 21);
            this.generateWaterBox(pLevel, pBox, 33, 1, 21, 34, 1, 21);
            this.generateWaterBox(pLevel, pBox, 35, 1, 21, 35, 2, 21);
         }

      }

      private void generateRoofPiece(WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (this.chunkIntersects(pBox, 21, 21, 36, 36)) {
            this.generateBox(pLevel, pBox, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 21, 1, 22, 36, 23, 36);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(pLevel, pBox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(pLevel, pBox, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(pLevel, BASE_LIGHT, 26, 20, 26, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 27, 21, 27, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 27, 20, 27, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 26, 20, 31, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 27, 21, 30, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 27, 20, 30, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 31, 20, 31, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 30, 21, 30, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 30, 20, 30, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 31, 20, 26, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 30, 21, 27, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 30, 20, 27, pBox);
            this.generateBox(pLevel, pBox, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
         }

      }

      private void generateLowerWall(WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (this.chunkIntersects(pBox, 0, 21, 6, 58)) {
            this.generateBox(pLevel, pBox, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 0, 1, 21, 6, 7, 57);
            this.generateBox(pLevel, pBox, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(pLevel, pBox, i, i + 1, 21, i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j = 23; j < 53; j += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, 5, 5, j, pBox);
            }

            this.placeBlock(pLevel, DOT_DECO_DATA, 5, 5, 52, pBox);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(pLevel, pBox, k, k + 1, 21, k, k + 1, 57 - k, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(pLevel, pBox, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
         }

         if (this.chunkIntersects(pBox, 51, 21, 58, 58)) {
            this.generateBox(pLevel, pBox, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 51, 1, 21, 57, 7, 57);
            this.generateBox(pLevel, pBox, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);

            for(int l = 0; l < 4; ++l) {
               this.generateBox(pLevel, pBox, 57 - l, l + 1, 21, 57 - l, l + 1, 57 - l, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int i1 = 23; i1 < 53; i1 += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, 52, 5, i1, pBox);
            }

            this.placeBlock(pLevel, DOT_DECO_DATA, 52, 5, 52, pBox);
            this.generateBox(pLevel, pBox, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
         }

         if (this.chunkIntersects(pBox, 0, 51, 57, 57)) {
            this.generateBox(pLevel, pBox, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 7, 1, 51, 50, 10, 57);

            for(int j1 = 0; j1 < 4; ++j1) {
               this.generateBox(pLevel, pBox, j1 + 1, j1 + 1, 57 - j1, 56 - j1, j1 + 1, 57 - j1, BASE_LIGHT, BASE_LIGHT, false);
            }
         }

      }

      private void generateMiddleWall(WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (this.chunkIntersects(pBox, 7, 21, 13, 50)) {
            this.generateBox(pLevel, pBox, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 7, 1, 21, 13, 10, 50);
            this.generateBox(pLevel, pBox, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(pLevel, pBox, i + 7, i + 5, 21, i + 7, i + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j = 21; j <= 45; j += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, 12, 9, j, pBox);
            }
         }

         if (this.chunkIntersects(pBox, 44, 21, 50, 54)) {
            this.generateBox(pLevel, pBox, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 44, 1, 21, 50, 10, 50);
            this.generateBox(pLevel, pBox, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(pLevel, pBox, 50 - k, k + 5, 21, 50 - k, k + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int l = 21; l <= 45; l += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, 45, 9, l, pBox);
            }
         }

         if (this.chunkIntersects(pBox, 8, 44, 49, 54)) {
            this.generateBox(pLevel, pBox, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 14, 1, 44, 43, 10, 50);

            for(int i1 = 12; i1 <= 45; i1 += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, i1, 9, 45, pBox);
               this.placeBlock(pLevel, DOT_DECO_DATA, i1, 9, 52, pBox);
               if (i1 == 12 || i1 == 18 || i1 == 24 || i1 == 33 || i1 == 39 || i1 == 45) {
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 9, 47, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 9, 50, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 10, 45, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 10, 46, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 10, 51, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 10, 52, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 11, 47, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 11, 50, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 12, 48, pBox);
                  this.placeBlock(pLevel, DOT_DECO_DATA, i1, 12, 49, pBox);
               }
            }

            for(int j1 = 0; j1 < 3; ++j1) {
               this.generateBox(pLevel, pBox, 8 + j1, 5 + j1, 54, 49 - j1, 5 + j1, 54, BASE_GRAY, BASE_GRAY, false);
            }

            this.generateBox(pLevel, pBox, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
         }

      }

      private void generateUpperWall(WorldGenLevel pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (this.chunkIntersects(pBox, 14, 21, 20, 43)) {
            this.generateBox(pLevel, pBox, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 14, 1, 22, 20, 14, 43);
            this.generateBox(pLevel, pBox, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(pLevel, pBox, i + 14, i + 9, 21, i + 14, i + 9, 43 - i, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j = 23; j <= 39; j += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, 19, 13, j, pBox);
            }
         }

         if (this.chunkIntersects(pBox, 37, 21, 43, 43)) {
            this.generateBox(pLevel, pBox, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 37, 1, 22, 43, 14, 43);
            this.generateBox(pLevel, pBox, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(pLevel, pBox, 43 - k, k + 9, 21, 43 - k, k + 9, 43 - k, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int l = 23; l <= 39; l += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, 38, 13, l, pBox);
            }
         }

         if (this.chunkIntersects(pBox, 15, 37, 42, 43)) {
            this.generateBox(pLevel, pBox, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(pLevel, pBox, 21, 1, 37, 36, 14, 43);
            this.generateBox(pLevel, pBox, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);

            for(int i1 = 0; i1 < 4; ++i1) {
               this.generateBox(pLevel, pBox, 15 + i1, i1 + 9, 43 - i1, 42 - i1, i1 + 9, 43 - i1, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j1 = 21; j1 <= 36; j1 += 3) {
               this.placeBlock(pLevel, DOT_DECO_DATA, j1, 13, 38, pBox);
            }
         }

      }
   }

   interface MonumentRoomFitter {
      boolean fits(OceanMonumentPieces.RoomDefinition pRoom);

      OceanMonumentPieces.OceanMonumentPiece create(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom, RandomSource pRandom);
   }

   public static class OceanMonumentCoreRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentCoreRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, pDirection, pRoom, 2, 2, 2);
      }

      public OceanMonumentCoreRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBoxOnFillOnly(pLevel, pBox, 1, 8, 0, 14, 8, 14, BASE_GRAY);
         int i = 7;
         BlockState blockstate = BASE_LIGHT;
         this.generateBox(pLevel, pBox, 0, 7, 0, 0, 7, 15, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 15, 7, 0, 15, 7, 15, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 1, 7, 0, 15, 7, 0, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 1, 7, 15, 14, 7, 15, blockstate, blockstate, false);

         for(int k = 1; k <= 6; ++k) {
            blockstate = BASE_LIGHT;
            if (k == 2 || k == 6) {
               blockstate = BASE_GRAY;
            }

            for(int j = 0; j <= 15; j += 15) {
               this.generateBox(pLevel, pBox, j, k, 0, j, k, 1, blockstate, blockstate, false);
               this.generateBox(pLevel, pBox, j, k, 6, j, k, 9, blockstate, blockstate, false);
               this.generateBox(pLevel, pBox, j, k, 14, j, k, 15, blockstate, blockstate, false);
            }

            this.generateBox(pLevel, pBox, 1, k, 0, 1, k, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 6, k, 0, 9, k, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 14, k, 0, 14, k, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 1, k, 15, 14, k, 15, blockstate, blockstate, false);
         }

         this.generateBox(pLevel, pBox, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);

         for(int l = 3; l <= 6; l += 3) {
            for(int i1 = 6; i1 <= 9; i1 += 3) {
               this.placeBlock(pLevel, LAMP_BLOCK, i1, l, 6, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, i1, l, 9, pBox);
            }
         }

         this.generateBox(pLevel, pBox, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
      }
   }

   public static class OceanMonumentDoubleXRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleXRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, pDirection, pRoom, 2, 1, 1);
      }

      public OceanMonumentDoubleXRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition1 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 8, 0, oceanmonumentpieces$roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(pLevel, pBox, 0, 0, oceanmonumentpieces$roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces$roomdefinition1.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 4, 1, 7, 4, 6, BASE_GRAY);
         }

         if (oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 8, 4, 1, 14, 4, 6, BASE_GRAY);
         }

         this.generateBox(pLevel, pBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(pLevel, LAMP_BLOCK, 6, 2, 3, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 9, 2, 3, pBox);
         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 7, 4, 2, 7);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 11, 1, 0, 12, 2, 0);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 11, 1, 7, 12, 2, 7);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 15, 1, 3, 15, 2, 4);
         }

      }
   }

   public static class OceanMonumentDoubleXYRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleXYRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, pDirection, pRoom, 2, 2, 1);
      }

      public OceanMonumentDoubleXYRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition1 = this.roomDefinition;
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition2 = oceanmonumentpieces$roomdefinition1.connections[Direction.UP.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition3 = oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 8, 0, oceanmonumentpieces$roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(pLevel, pBox, 0, 0, oceanmonumentpieces$roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces$roomdefinition2.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 8, 1, 7, 8, 6, BASE_GRAY);
         }

         if (oceanmonumentpieces$roomdefinition3.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 8, 8, 1, 14, 8, 6, BASE_GRAY);
         }

         for(int i = 1; i <= 7; ++i) {
            BlockState blockstate = BASE_LIGHT;
            if (i == 2 || i == 6) {
               blockstate = BASE_GRAY;
            }

            this.generateBox(pLevel, pBox, 0, i, 0, 0, i, 7, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 15, i, 0, 15, i, 7, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 1, i, 0, 15, i, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 1, i, 7, 14, i, 7, blockstate, blockstate, false);
         }

         this.generateBox(pLevel, pBox, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(pLevel, BASE_LIGHT, 6, 6, 2, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 9, 6, 2, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 6, 6, 5, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 9, 6, 5, pBox);
         this.generateBox(pLevel, pBox, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(pLevel, LAMP_BLOCK, 5, 4, 2, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 5, 4, 5, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 10, 4, 2, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 10, 4, 5, pBox);
         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 7, 4, 2, 7);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 11, 1, 0, 12, 2, 0);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 11, 1, 7, 12, 2, 7);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 15, 1, 3, 15, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 5, 0, 4, 6, 0);
         }

         if (oceanmonumentpieces$roomdefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 5, 7, 4, 6, 7);
         }

         if (oceanmonumentpieces$roomdefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 5, 3, 0, 6, 4);
         }

         if (oceanmonumentpieces$roomdefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 11, 5, 0, 12, 6, 0);
         }

         if (oceanmonumentpieces$roomdefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 11, 5, 7, 12, 6, 7);
         }

         if (oceanmonumentpieces$roomdefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 15, 5, 3, 15, 6, 4);
         }

      }
   }

   public static class OceanMonumentDoubleYRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleYRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, pDirection, pRoom, 1, 2, 1);
      }

      public OceanMonumentDoubleYRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
         if (oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 8, 1, 6, 8, 6, BASE_GRAY);
         }

         this.generateBox(pLevel, pBox, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition1 = this.roomDefinition;

         for(int i = 1; i <= 5; i += 4) {
            int j = 0;
            if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 2, i, j, 2, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 5, i, j, 5, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 3, i + 2, j, 4, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, 0, i, j, 7, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 0, i + 1, j, 7, i + 1, j, BASE_GRAY, BASE_GRAY, false);
            }

            j = 7;
            if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 2, i, j, 2, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 5, i, j, 5, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 3, i + 2, j, 4, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, 0, i, j, 7, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 0, i + 1, j, 7, i + 1, j, BASE_GRAY, BASE_GRAY, false);
            }

            int k = 0;
            if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, k, i, 2, k, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, k, i, 5, k, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, k, i + 2, 3, k, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, k, i, 0, k, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, k, i + 1, 0, k, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
            }

            k = 7;
            if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, k, i, 2, k, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, k, i, 5, k, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, k, i + 2, 3, k, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, k, i, 0, k, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, k, i + 1, 0, k, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
            }

            oceanmonumentpieces$roomdefinition1 = oceanmonumentpieces$roomdefinition;
         }

      }
   }

   public static class OceanMonumentDoubleYZRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleYZRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, pDirection, pRoom, 1, 2, 2);
      }

      public OceanMonumentDoubleYZRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition1 = this.roomDefinition;
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition2 = oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition3 = oceanmonumentpieces$roomdefinition1.connections[Direction.UP.get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 0, 8, oceanmonumentpieces$roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(pLevel, pBox, 0, 0, oceanmonumentpieces$roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces$roomdefinition3.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 8, 1, 6, 8, 7, BASE_GRAY);
         }

         if (oceanmonumentpieces$roomdefinition2.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 8, 8, 6, 8, 14, BASE_GRAY);
         }

         for(int i = 1; i <= 7; ++i) {
            BlockState blockstate = BASE_LIGHT;
            if (i == 2 || i == 6) {
               blockstate = BASE_GRAY;
            }

            this.generateBox(pLevel, pBox, 0, i, 0, 0, i, 15, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 7, i, 0, 7, i, 15, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 1, i, 0, 6, i, 0, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 1, i, 15, 6, i, 15, blockstate, blockstate, false);
         }

         for(int j = 1; j <= 7; ++j) {
            BlockState blockstate1 = BASE_BLACK;
            if (j == 2 || j == 6) {
               blockstate1 = LAMP_BLOCK;
            }

            this.generateBox(pLevel, pBox, 3, j, 7, 4, j, 8, blockstate1, blockstate1, false);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 7, 1, 3, 7, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 15, 4, 2, 15);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 11, 0, 2, 12);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 7, 1, 11, 7, 2, 12);
         }

         if (oceanmonumentpieces$roomdefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 5, 0, 4, 6, 0);
         }

         if (oceanmonumentpieces$roomdefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 7, 5, 3, 7, 6, 4);
            this.generateBox(pLevel, pBox, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
         }

         if (oceanmonumentpieces$roomdefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 5, 3, 0, 6, 4);
            this.generateBox(pLevel, pBox, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
         }

         if (oceanmonumentpieces$roomdefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 5, 15, 4, 6, 15);
         }

         if (oceanmonumentpieces$roomdefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 5, 11, 0, 6, 12);
            this.generateBox(pLevel, pBox, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
         }

         if (oceanmonumentpieces$roomdefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 7, 5, 11, 7, 6, 12);
            this.generateBox(pLevel, pBox, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
         }

      }
   }

   public static class OceanMonumentDoubleZRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleZRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, pDirection, pRoom, 1, 1, 2);
      }

      public OceanMonumentDoubleZRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces$roomdefinition1 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 0, 8, oceanmonumentpieces$roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(pLevel, pBox, 0, 0, oceanmonumentpieces$roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces$roomdefinition1.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 4, 1, 6, 4, 7, BASE_GRAY);
         }

         if (oceanmonumentpieces$roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 4, 8, 6, 4, 14, BASE_GRAY);
         }

         this.generateBox(pLevel, pBox, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(pLevel, LAMP_BLOCK, 2, 2, 5, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 5, 2, 5, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 2, 2, 10, pBox);
         this.placeBlock(pLevel, LAMP_BLOCK, 5, 2, 10, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 2, 3, 5, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 5, 3, 5, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 2, 3, 10, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 5, 3, 10, pBox);
         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 7, 1, 3, 7, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 15, 4, 2, 15);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 11, 0, 2, 12);
         }

         if (oceanmonumentpieces$roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 7, 1, 11, 7, 2, 12);
         }

      }
   }

   public static class OceanMonumentEntryRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentEntryRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, pDirection, pRoom, 1, 1, 1);
      }

      public OceanMonumentEntryRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 7, 4, 2, 7);
         }

         if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 0, 1, 3, 1, 2, 4);
         }

         if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 6, 1, 3, 7, 2, 4);
         }

      }
   }

   public static class OceanMonumentPenthouse extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentPenthouse(Direction pDirection, BoundingBox pBox) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, pDirection, 1, pBox);
      }

      public OceanMonumentPenthouse(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(pLevel, pBox, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);

         for(int i = 2; i <= 11; i += 3) {
            this.placeBlock(pLevel, LAMP_BLOCK, 0, 0, i, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 13, 0, i, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, i, 0, 0, pBox);
         }

         this.generateBox(pLevel, pBox, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(pLevel, BASE_LIGHT, 5, 0, 8, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 8, 0, 8, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 10, 0, 10, pBox);
         this.placeBlock(pLevel, BASE_LIGHT, 3, 0, 10, pBox);
         this.generateBox(pLevel, pBox, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
         int l = 3;

         for(int j = 0; j < 2; ++j) {
            for(int k = 2; k <= 8; k += 3) {
               this.generateBox(pLevel, pBox, l, 0, k, l, 2, k, BASE_LIGHT, BASE_LIGHT, false);
            }

            l = 10;
         }

         this.generateBox(pLevel, pBox, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
         this.generateWaterBox(pLevel, pBox, 6, -1, 3, 7, -1, 4);
         this.spawnElder(pLevel, pBox, 6, 1, 6);
      }
   }

   protected abstract static class OceanMonumentPiece extends StructurePiece {
      protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
      protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
      protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
      protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
      protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
      protected static final boolean DO_FILL = true;
      protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
      protected static final Set<Block> FILL_KEEP = ImmutableSet.<Block>builder().add(Blocks.ICE).add(Blocks.PACKED_ICE).add(Blocks.BLUE_ICE).add(FILL_BLOCK.getBlock()).build();
      protected static final int GRIDROOM_WIDTH = 8;
      protected static final int GRIDROOM_DEPTH = 8;
      protected static final int GRIDROOM_HEIGHT = 4;
      protected static final int GRID_WIDTH = 5;
      protected static final int GRID_DEPTH = 5;
      protected static final int GRID_HEIGHT = 3;
      protected static final int GRID_FLOOR_COUNT = 25;
      protected static final int GRID_SIZE = 75;
      protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
      protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
      protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
      protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
      protected static final int LEFTWING_INDEX = 1001;
      protected static final int RIGHTWING_INDEX = 1002;
      protected static final int PENTHOUSE_INDEX = 1003;
      protected OceanMonumentPieces.RoomDefinition roomDefinition;

      protected static int getRoomIndex(int pX, int pY, int pZ) {
         return pY * 25 + pZ * 5 + pX;
      }

      public OceanMonumentPiece(StructurePieceType pType, Direction pOrientation, int pGenDepth, BoundingBox pBox) {
         super(pType, pGenDepth, pBox);
         this.setOrientation(pOrientation);
      }

      protected OceanMonumentPiece(StructurePieceType pType, int pGenDepth, Direction pOrientation, OceanMonumentPieces.RoomDefinition pRoomDefinition, int pX, int pY, int pZ) {
         super(pType, pGenDepth, makeBoundingBox(pOrientation, pRoomDefinition, pX, pY, pZ));
         this.setOrientation(pOrientation);
         this.roomDefinition = pRoomDefinition;
      }

      private static BoundingBox makeBoundingBox(Direction pDirection, OceanMonumentPieces.RoomDefinition pDefinition, int pX, int pY, int pZ) {
         int i = pDefinition.index;
         int j = i % 5;
         int k = i / 5 % 5;
         int l = i / 25;
         BoundingBox boundingbox = makeBoundingBox(0, 0, 0, pDirection, pX * 8, pY * 4, pZ * 8);
         switch (pDirection) {
            case NORTH:
               boundingbox.move(j * 8, l * 4, -(k + pZ) * 8 + 1);
               break;
            case SOUTH:
               boundingbox.move(j * 8, l * 4, k * 8);
               break;
            case WEST:
               boundingbox.move(-(k + pZ) * 8 + 1, l * 4, j * 8);
               break;
            case EAST:
            default:
               boundingbox.move(k * 8, l * 4, j * 8);
         }

         return boundingbox;
      }

      public OceanMonumentPiece(StructurePieceType pType, CompoundTag pTag) {
         super(pType, pTag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
      }

      protected void generateWaterBox(WorldGenLevel pLevel, BoundingBox pBoundingBox, int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2) {
         for(int i = pY1; i <= pY2; ++i) {
            for(int j = pX1; j <= pX2; ++j) {
               for(int k = pZ1; k <= pZ2; ++k) {
                  BlockState blockstate = this.getBlock(pLevel, j, i, k, pBoundingBox);
                  if (!FILL_KEEP.contains(blockstate.getBlock())) {
                     if (this.getWorldY(i) >= pLevel.getSeaLevel() && blockstate != FILL_BLOCK) {
                        this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), j, i, k, pBoundingBox);
                     } else {
                        this.placeBlock(pLevel, FILL_BLOCK, j, i, k, pBoundingBox);
                     }
                  }
               }
            }
         }

      }

      protected void generateDefaultFloor(WorldGenLevel pLevel, BoundingBox pBox, int pX, int pZ, boolean pHasOpeningDownwards) {
         if (pHasOpeningDownwards) {
            this.generateBox(pLevel, pBox, pX + 0, 0, pZ + 0, pX + 2, 0, pZ + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 5, 0, pZ + 0, pX + 8 - 1, 0, pZ + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 3, 0, pZ + 0, pX + 4, 0, pZ + 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 3, 0, pZ + 5, pX + 4, 0, pZ + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, pX + 3, 0, pZ + 2, pX + 4, 0, pZ + 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, pX + 3, 0, pZ + 5, pX + 4, 0, pZ + 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, pX + 2, 0, pZ + 3, pX + 2, 0, pZ + 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, pX + 5, 0, pZ + 3, pX + 5, 0, pZ + 4, BASE_LIGHT, BASE_LIGHT, false);
         } else {
            this.generateBox(pLevel, pBox, pX + 0, 0, pZ + 0, pX + 8 - 1, 0, pZ + 8 - 1, BASE_GRAY, BASE_GRAY, false);
         }

      }

      protected void generateBoxOnFillOnly(WorldGenLevel pLevel, BoundingBox pBox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ, BlockState pState) {
         for(int i = pMinY; i <= pMaxY; ++i) {
            for(int j = pMinX; j <= pMaxX; ++j) {
               for(int k = pMinZ; k <= pMaxZ; ++k) {
                  if (this.getBlock(pLevel, j, i, k, pBox) == FILL_BLOCK) {
                     this.placeBlock(pLevel, pState, j, i, k, pBox);
                  }
               }
            }
         }

      }

      protected boolean chunkIntersects(BoundingBox pBox, int pMinX, int pMinZ, int pMaxX, int pMaxZ) {
         int i = this.getWorldX(pMinX, pMinZ);
         int j = this.getWorldZ(pMinX, pMinZ);
         int k = this.getWorldX(pMaxX, pMaxZ);
         int l = this.getWorldZ(pMaxX, pMaxZ);
         return pBox.intersects(Math.min(i, k), Math.min(j, l), Math.max(i, k), Math.max(j, l));
      }

      protected void spawnElder(WorldGenLevel pLevel, BoundingBox pBox, int pX, int pY, int pZ) {
         BlockPos blockpos = this.getWorldPos(pX, pY, pZ);
         if (pBox.isInside(blockpos)) {
            ElderGuardian elderguardian = EntityType.ELDER_GUARDIAN.create(pLevel.getLevel());
            if (elderguardian != null) {
               elderguardian.heal(elderguardian.getMaxHealth());
               elderguardian.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
               elderguardian.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(elderguardian.blockPosition()), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
               pLevel.addFreshEntityWithPassengers(elderguardian);
            }
         }

      }
   }

   public static class OceanMonumentSimpleRoom extends OceanMonumentPieces.OceanMonumentPiece {
      private int mainDesign;

      public OceanMonumentSimpleRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom, RandomSource pRandom) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, pDirection, pRoom, 1, 1, 1);
         this.mainDesign = pRandom.nextInt(3);
      }

      public OceanMonumentSimpleRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
         }

         boolean flag = this.mainDesign != 0 && pRandom.nextBoolean() && !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()] && !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && this.roomDefinition.countOpenings() > 1;
         if (this.mainDesign == 0) {
            this.generateBox(pLevel, pBox, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(pLevel, LAMP_BLOCK, 1, 2, 1, pBox);
            this.generateBox(pLevel, pBox, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(pLevel, LAMP_BLOCK, 6, 2, 1, pBox);
            this.generateBox(pLevel, pBox, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(pLevel, LAMP_BLOCK, 1, 2, 6, pBox);
            this.generateBox(pLevel, pBox, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(pLevel, LAMP_BLOCK, 6, 2, 6, pBox);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(pLevel, pBox, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            }
         } else if (this.mainDesign == 1) {
            this.generateBox(pLevel, pBox, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(pLevel, LAMP_BLOCK, 2, 2, 2, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 2, 2, 5, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 5, 2, 5, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 5, 2, 2, pBox);
            this.generateBox(pLevel, pBox, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(pLevel, BASE_GRAY, 1, 2, 0, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 0, 2, 1, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 1, 2, 7, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 0, 2, 6, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 6, 2, 7, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 7, 2, 6, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 6, 2, 0, pBox);
            this.placeBlock(pLevel, BASE_GRAY, 7, 2, 1, pBox);
            if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox(pLevel, pBox, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(pLevel, pBox, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
            }
         } else if (this.mainDesign == 2) {
            this.generateBox(pLevel, pBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateWaterBox(pLevel, pBox, 3, 1, 0, 4, 2, 0);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateWaterBox(pLevel, pBox, 3, 1, 7, 4, 2, 7);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateWaterBox(pLevel, pBox, 0, 1, 3, 0, 2, 4);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateWaterBox(pLevel, pBox, 7, 1, 3, 7, 2, 4);
            }
         }

         if (flag) {
            this.generateBox(pLevel, pBox, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(pLevel, pBox, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         }

      }
   }

   public static class OceanMonumentSimpleTopRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentSimpleTopRoom(Direction pDirection, OceanMonumentPieces.RoomDefinition pRoom) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, pDirection, pRoom, 1, 1, 1);
      }

      public OceanMonumentSimpleTopRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(pLevel, pBox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(pLevel, pBox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
         }

         for(int i = 1; i <= 6; ++i) {
            for(int j = 1; j <= 6; ++j) {
               if (pRandom.nextInt(3) != 0) {
                  int k = 2 + (pRandom.nextInt(4) == 0 ? 0 : 1);
                  BlockState blockstate = Blocks.WET_SPONGE.defaultBlockState();
                  this.generateBox(pLevel, pBox, i, k, j, i, 3, j, blockstate, blockstate, false);
               }
            }
         }

         this.generateBox(pLevel, pBox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(pLevel, pBox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(pLevel, pBox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
         if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(pLevel, pBox, 3, 1, 0, 4, 2, 0);
         }

      }
   }

   public static class OceanMonumentWingRoom extends OceanMonumentPieces.OceanMonumentPiece {
      private int mainDesign;

      public OceanMonumentWingRoom(Direction pDirection, BoundingBox pBox, int pFlag) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, pDirection, 1, pBox);
         this.mainDesign = pFlag & 1;
      }

      public OceanMonumentWingRoom(CompoundTag pTag) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, pTag);
      }

      public void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         if (this.mainDesign == 0) {
            for(int i = 0; i < 4; ++i) {
               this.generateBox(pLevel, pBox, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(pLevel, pBox, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);

            for(int i1 = 18; i1 >= 7; i1 -= 3) {
               this.placeBlock(pLevel, LAMP_BLOCK, 6, 3, i1, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, 16, 3, i1, pBox);
            }

            this.placeBlock(pLevel, LAMP_BLOCK, 10, 0, 10, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 12, 0, 10, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 10, 0, 12, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 12, 0, 12, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 8, 3, 6, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 14, 3, 6, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 4, 2, 4, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 4, 1, 4, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 4, 0, 4, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 18, 2, 4, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 18, 1, 4, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 18, 0, 4, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 4, 2, 18, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 4, 1, 18, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 4, 0, 18, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 18, 2, 18, pBox);
            this.placeBlock(pLevel, LAMP_BLOCK, 18, 1, 18, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 18, 0, 18, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 9, 7, 20, pBox);
            this.placeBlock(pLevel, BASE_LIGHT, 13, 7, 20, pBox);
            this.generateBox(pLevel, pBox, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.spawnElder(pLevel, pBox, 11, 2, 16);
         } else if (this.mainDesign == 1) {
            this.generateBox(pLevel, pBox, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(pLevel, pBox, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
            int j1 = 9;
            int j = 20;
            int k = 5;

            for(int l = 0; l < 2; ++l) {
               this.placeBlock(pLevel, BASE_LIGHT, j1, 6, 20, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, j1, 5, 20, pBox);
               this.placeBlock(pLevel, BASE_LIGHT, j1, 4, 20, pBox);
               j1 = 13;
            }

            this.generateBox(pLevel, pBox, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            j1 = 10;

            for(int k1 = 0; k1 < 2; ++k1) {
               this.generateBox(pLevel, pBox, j1, 0, 10, j1, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, j1, 0, 12, j1, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock(pLevel, LAMP_BLOCK, j1, 0, 10, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, j1, 0, 12, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, j1, 4, 10, pBox);
               this.placeBlock(pLevel, LAMP_BLOCK, j1, 4, 12, pBox);
               j1 = 12;
            }

            j1 = 8;

            for(int l1 = 0; l1 < 2; ++l1) {
               this.generateBox(pLevel, pBox, j1, 0, 7, j1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(pLevel, pBox, j1, 0, 14, j1, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
               j1 = 14;
            }

            this.generateBox(pLevel, pBox, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(pLevel, pBox, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
            this.spawnElder(pLevel, pBox, 11, 5, 13);
         }

      }
   }

   static class RoomDefinition {
      final int index;
      final OceanMonumentPieces.RoomDefinition[] connections = new OceanMonumentPieces.RoomDefinition[6];
      final boolean[] hasOpening = new boolean[6];
      boolean claimed;
      boolean isSource;
      private int scanIndex;

      public RoomDefinition(int pIndex) {
         this.index = pIndex;
      }

      public void setConnection(Direction pDirection, OceanMonumentPieces.RoomDefinition pConnectingRoom) {
         this.connections[pDirection.get3DDataValue()] = pConnectingRoom;
         pConnectingRoom.connections[pDirection.getOpposite().get3DDataValue()] = this;
      }

      public void updateOpenings() {
         for(int i = 0; i < 6; ++i) {
            this.hasOpening[i] = this.connections[i] != null;
         }

      }

      public boolean findSource(int pIndex) {
         if (this.isSource) {
            return true;
         } else {
            this.scanIndex = pIndex;

            for(int i = 0; i < 6; ++i) {
               if (this.connections[i] != null && this.hasOpening[i] && this.connections[i].scanIndex != pIndex && this.connections[i].findSource(pIndex)) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean isSpecial() {
         return this.index >= 75;
      }

      public int countOpenings() {
         int i = 0;

         for(int j = 0; j < 6; ++j) {
            if (this.hasOpening[j]) {
               ++i;
            }
         }

         return i;
      }
   }
}