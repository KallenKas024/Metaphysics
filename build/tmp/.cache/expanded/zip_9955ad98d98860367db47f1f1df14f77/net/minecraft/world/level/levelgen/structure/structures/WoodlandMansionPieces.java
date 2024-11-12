package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class WoodlandMansionPieces {
   public static void generateMansion(StructureTemplateManager pStructureTemplateManager, BlockPos pPos, Rotation pRotation, List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, RandomSource pRandom) {
      WoodlandMansionPieces.MansionGrid woodlandmansionpieces$mansiongrid = new WoodlandMansionPieces.MansionGrid(pRandom);
      WoodlandMansionPieces.MansionPiecePlacer woodlandmansionpieces$mansionpieceplacer = new WoodlandMansionPieces.MansionPiecePlacer(pStructureTemplateManager, pRandom);
      woodlandmansionpieces$mansionpieceplacer.createMansion(pPos, pRotation, pPieces, woodlandmansionpieces$mansiongrid);
   }

   public static void main(String[] pArgs) {
      RandomSource randomsource = RandomSource.create();
      long i = randomsource.nextLong();
      System.out.println("Seed: " + i);
      randomsource.setSeed(i);
      WoodlandMansionPieces.MansionGrid woodlandmansionpieces$mansiongrid = new WoodlandMansionPieces.MansionGrid(randomsource);
      woodlandmansionpieces$mansiongrid.print();
   }

   static class FirstFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
      public String get1x1(RandomSource p_229995_) {
         return "1x1_a" + (p_229995_.nextInt(5) + 1);
      }

      public String get1x1Secret(RandomSource p_230000_) {
         return "1x1_as" + (p_230000_.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(RandomSource p_229997_, boolean p_229998_) {
         return "1x2_a" + (p_229997_.nextInt(9) + 1);
      }

      public String get1x2FrontEntrance(RandomSource p_230002_, boolean p_230003_) {
         return "1x2_b" + (p_230002_.nextInt(5) + 1);
      }

      public String get1x2Secret(RandomSource p_230005_) {
         return "1x2_s" + (p_230005_.nextInt(2) + 1);
      }

      public String get2x2(RandomSource p_230007_) {
         return "2x2_a" + (p_230007_.nextInt(4) + 1);
      }

      public String get2x2Secret(RandomSource p_230009_) {
         return "2x2_s1";
      }
   }

   abstract static class FloorRoomCollection {
      public abstract String get1x1(RandomSource pRandom);

      public abstract String get1x1Secret(RandomSource pRandom);

      public abstract String get1x2SideEntrance(RandomSource pRandom, boolean pIsStairs);

      public abstract String get1x2FrontEntrance(RandomSource pRandom, boolean pIsStairs);

      public abstract String get1x2Secret(RandomSource pRandom);

      public abstract String get2x2(RandomSource pRandom);

      public abstract String get2x2Secret(RandomSource pRandom);
   }

   static class MansionGrid {
      private static final int DEFAULT_SIZE = 11;
      private static final int CLEAR = 0;
      private static final int CORRIDOR = 1;
      private static final int ROOM = 2;
      private static final int START_ROOM = 3;
      private static final int TEST_ROOM = 4;
      private static final int BLOCKED = 5;
      private static final int ROOM_1x1 = 65536;
      private static final int ROOM_1x2 = 131072;
      private static final int ROOM_2x2 = 262144;
      private static final int ROOM_ORIGIN_FLAG = 1048576;
      private static final int ROOM_DOOR_FLAG = 2097152;
      private static final int ROOM_STAIRS_FLAG = 4194304;
      private static final int ROOM_CORRIDOR_FLAG = 8388608;
      private static final int ROOM_TYPE_MASK = 983040;
      private static final int ROOM_ID_MASK = 65535;
      private final RandomSource random;
      final WoodlandMansionPieces.SimpleGrid baseGrid;
      final WoodlandMansionPieces.SimpleGrid thirdFloorGrid;
      final WoodlandMansionPieces.SimpleGrid[] floorRooms;
      final int entranceX;
      final int entranceY;

      public MansionGrid(RandomSource pRandom) {
         this.random = pRandom;
         int i = 11;
         this.entranceX = 7;
         this.entranceY = 4;
         this.baseGrid = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
         this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
         this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
         this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
         this.baseGrid.set(0, 0, 11, 1, 5);
         this.baseGrid.set(0, 9, 11, 11, 5);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

         while(this.cleanEdges(this.baseGrid)) {
         }

         this.floorRooms = new WoodlandMansionPieces.SimpleGrid[3];
         this.floorRooms[0] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[1] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[2] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.identifyRooms(this.baseGrid, this.floorRooms[0]);
         this.identifyRooms(this.baseGrid, this.floorRooms[1]);
         this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.thirdFloorGrid = new WoodlandMansionPieces.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
         this.setupThirdFloor();
         this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
      }

      public static boolean isHouse(WoodlandMansionPieces.SimpleGrid pLayout, int pX, int pY) {
         int i = pLayout.get(pX, pY);
         return i == 1 || i == 2 || i == 3 || i == 4;
      }

      public boolean isRoomId(WoodlandMansionPieces.SimpleGrid pLayout, int pX, int pY, int pFloor, int pRoomId) {
         return (this.floorRooms[pFloor].get(pX, pY) & '\uffff') == pRoomId;
      }

      @Nullable
      public Direction get1x2RoomDirection(WoodlandMansionPieces.SimpleGrid pLayout, int pX, int pY, int pFloor, int pRoomId) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.isRoomId(pLayout, pX + direction.getStepX(), pY + direction.getStepZ(), pFloor, pRoomId)) {
               return direction;
            }
         }

         return null;
      }

      private void recursiveCorridor(WoodlandMansionPieces.SimpleGrid pLayout, int pX, int pY, Direction pDirection, int pLength) {
         if (pLength > 0) {
            pLayout.set(pX, pY, 1);
            pLayout.setif(pX + pDirection.getStepX(), pY + pDirection.getStepZ(), 0, 1);

            for(int i = 0; i < 8; ++i) {
               Direction direction = Direction.from2DDataValue(this.random.nextInt(4));
               if (direction != pDirection.getOpposite() && (direction != Direction.EAST || !this.random.nextBoolean())) {
                  int j = pX + pDirection.getStepX();
                  int k = pY + pDirection.getStepZ();
                  if (pLayout.get(j + direction.getStepX(), k + direction.getStepZ()) == 0 && pLayout.get(j + direction.getStepX() * 2, k + direction.getStepZ() * 2) == 0) {
                     this.recursiveCorridor(pLayout, pX + pDirection.getStepX() + direction.getStepX(), pY + pDirection.getStepZ() + direction.getStepZ(), direction, pLength - 1);
                     break;
                  }
               }
            }

            Direction direction1 = pDirection.getClockWise();
            Direction direction2 = pDirection.getCounterClockWise();
            pLayout.setif(pX + direction1.getStepX(), pY + direction1.getStepZ(), 0, 2);
            pLayout.setif(pX + direction2.getStepX(), pY + direction2.getStepZ(), 0, 2);
            pLayout.setif(pX + pDirection.getStepX() + direction1.getStepX(), pY + pDirection.getStepZ() + direction1.getStepZ(), 0, 2);
            pLayout.setif(pX + pDirection.getStepX() + direction2.getStepX(), pY + pDirection.getStepZ() + direction2.getStepZ(), 0, 2);
            pLayout.setif(pX + pDirection.getStepX() * 2, pY + pDirection.getStepZ() * 2, 0, 2);
            pLayout.setif(pX + direction1.getStepX() * 2, pY + direction1.getStepZ() * 2, 0, 2);
            pLayout.setif(pX + direction2.getStepX() * 2, pY + direction2.getStepZ() * 2, 0, 2);
         }
      }

      private boolean cleanEdges(WoodlandMansionPieces.SimpleGrid pGrid) {
         boolean flag = false;

         for(int i = 0; i < pGrid.height; ++i) {
            for(int j = 0; j < pGrid.width; ++j) {
               if (pGrid.get(j, i) == 0) {
                  int k = 0;
                  k += isHouse(pGrid, j + 1, i) ? 1 : 0;
                  k += isHouse(pGrid, j - 1, i) ? 1 : 0;
                  k += isHouse(pGrid, j, i + 1) ? 1 : 0;
                  k += isHouse(pGrid, j, i - 1) ? 1 : 0;
                  if (k >= 3) {
                     pGrid.set(j, i, 2);
                     flag = true;
                  } else if (k == 2) {
                     int l = 0;
                     l += isHouse(pGrid, j + 1, i + 1) ? 1 : 0;
                     l += isHouse(pGrid, j - 1, i + 1) ? 1 : 0;
                     l += isHouse(pGrid, j + 1, i - 1) ? 1 : 0;
                     l += isHouse(pGrid, j - 1, i - 1) ? 1 : 0;
                     if (l <= 1) {
                        pGrid.set(j, i, 2);
                        flag = true;
                     }
                  }
               }
            }
         }

         return flag;
      }

      private void setupThirdFloor() {
         List<Tuple<Integer, Integer>> list = Lists.newArrayList();
         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = this.floorRooms[1];

         for(int i = 0; i < this.thirdFloorGrid.height; ++i) {
            for(int j = 0; j < this.thirdFloorGrid.width; ++j) {
               int k = woodlandmansionpieces$simplegrid.get(j, i);
               int l = k & 983040;
               if (l == 131072 && (k & 2097152) == 2097152) {
                  list.add(new Tuple<>(j, i));
               }
            }
         }

         if (list.isEmpty()) {
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
         } else {
            Tuple<Integer, Integer> tuple = list.get(this.random.nextInt(list.size()));
            int l1 = woodlandmansionpieces$simplegrid.get(tuple.getA(), tuple.getB());
            woodlandmansionpieces$simplegrid.set(tuple.getA(), tuple.getB(), l1 | 4194304);
            Direction direction1 = this.get1x2RoomDirection(this.baseGrid, tuple.getA(), tuple.getB(), 1, l1 & '\uffff');
            int i2 = tuple.getA() + direction1.getStepX();
            int i1 = tuple.getB() + direction1.getStepZ();

            for(int j1 = 0; j1 < this.thirdFloorGrid.height; ++j1) {
               for(int k1 = 0; k1 < this.thirdFloorGrid.width; ++k1) {
                  if (!isHouse(this.baseGrid, k1, j1)) {
                     this.thirdFloorGrid.set(k1, j1, 5);
                  } else if (k1 == tuple.getA() && j1 == tuple.getB()) {
                     this.thirdFloorGrid.set(k1, j1, 3);
                  } else if (k1 == i2 && j1 == i1) {
                     this.thirdFloorGrid.set(k1, j1, 3);
                     this.floorRooms[2].set(k1, j1, 8388608);
                  }
               }
            }

            List<Direction> list1 = Lists.newArrayList();

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               if (this.thirdFloorGrid.get(i2 + direction.getStepX(), i1 + direction.getStepZ()) == 0) {
                  list1.add(direction);
               }
            }

            if (list1.isEmpty()) {
               this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
               woodlandmansionpieces$simplegrid.set(tuple.getA(), tuple.getB(), l1);
            } else {
               Direction direction2 = list1.get(this.random.nextInt(list1.size()));
               this.recursiveCorridor(this.thirdFloorGrid, i2 + direction2.getStepX(), i1 + direction2.getStepZ(), direction2, 4);

               while(this.cleanEdges(this.thirdFloorGrid)) {
               }

            }
         }
      }

      private void identifyRooms(WoodlandMansionPieces.SimpleGrid pGrid, WoodlandMansionPieces.SimpleGrid pFloorRooms) {
         ObjectArrayList<Tuple<Integer, Integer>> objectarraylist = new ObjectArrayList<>();

         for(int i = 0; i < pGrid.height; ++i) {
            for(int j = 0; j < pGrid.width; ++j) {
               if (pGrid.get(j, i) == 2) {
                  objectarraylist.add(new Tuple<>(j, i));
               }
            }
         }

         Util.shuffle(objectarraylist, this.random);
         int k3 = 10;

         for(Tuple<Integer, Integer> tuple : objectarraylist) {
            int k = tuple.getA();
            int l = tuple.getB();
            if (pFloorRooms.get(k, l) == 0) {
               int i1 = k;
               int j1 = k;
               int k1 = l;
               int l1 = l;
               int i2 = 65536;
               if (pFloorRooms.get(k + 1, l) == 0 && pFloorRooms.get(k, l + 1) == 0 && pFloorRooms.get(k + 1, l + 1) == 0 && pGrid.get(k + 1, l) == 2 && pGrid.get(k, l + 1) == 2 && pGrid.get(k + 1, l + 1) == 2) {
                  j1 = k + 1;
                  l1 = l + 1;
                  i2 = 262144;
               } else if (pFloorRooms.get(k - 1, l) == 0 && pFloorRooms.get(k, l + 1) == 0 && pFloorRooms.get(k - 1, l + 1) == 0 && pGrid.get(k - 1, l) == 2 && pGrid.get(k, l + 1) == 2 && pGrid.get(k - 1, l + 1) == 2) {
                  i1 = k - 1;
                  l1 = l + 1;
                  i2 = 262144;
               } else if (pFloorRooms.get(k - 1, l) == 0 && pFloorRooms.get(k, l - 1) == 0 && pFloorRooms.get(k - 1, l - 1) == 0 && pGrid.get(k - 1, l) == 2 && pGrid.get(k, l - 1) == 2 && pGrid.get(k - 1, l - 1) == 2) {
                  i1 = k - 1;
                  k1 = l - 1;
                  i2 = 262144;
               } else if (pFloorRooms.get(k + 1, l) == 0 && pGrid.get(k + 1, l) == 2) {
                  j1 = k + 1;
                  i2 = 131072;
               } else if (pFloorRooms.get(k, l + 1) == 0 && pGrid.get(k, l + 1) == 2) {
                  l1 = l + 1;
                  i2 = 131072;
               } else if (pFloorRooms.get(k - 1, l) == 0 && pGrid.get(k - 1, l) == 2) {
                  i1 = k - 1;
                  i2 = 131072;
               } else if (pFloorRooms.get(k, l - 1) == 0 && pGrid.get(k, l - 1) == 2) {
                  k1 = l - 1;
                  i2 = 131072;
               }

               int j2 = this.random.nextBoolean() ? i1 : j1;
               int k2 = this.random.nextBoolean() ? k1 : l1;
               int l2 = 2097152;
               if (!pGrid.edgesTo(j2, k2, 1)) {
                  j2 = j2 == i1 ? j1 : i1;
                  k2 = k2 == k1 ? l1 : k1;
                  if (!pGrid.edgesTo(j2, k2, 1)) {
                     k2 = k2 == k1 ? l1 : k1;
                     if (!pGrid.edgesTo(j2, k2, 1)) {
                        j2 = j2 == i1 ? j1 : i1;
                        k2 = k2 == k1 ? l1 : k1;
                        if (!pGrid.edgesTo(j2, k2, 1)) {
                           l2 = 0;
                           j2 = i1;
                           k2 = k1;
                        }
                     }
                  }
               }

               for(int i3 = k1; i3 <= l1; ++i3) {
                  for(int j3 = i1; j3 <= j1; ++j3) {
                     if (j3 == j2 && i3 == k2) {
                        pFloorRooms.set(j3, i3, 1048576 | l2 | i2 | k3);
                     } else {
                        pFloorRooms.set(j3, i3, i2 | k3);
                     }
                  }
               }

               ++k3;
            }
         }

      }

      public void print() {
         for(int i = 0; i < 2; ++i) {
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = i == 0 ? this.baseGrid : this.thirdFloorGrid;

            for(int j = 0; j < woodlandmansionpieces$simplegrid.height; ++j) {
               for(int k = 0; k < woodlandmansionpieces$simplegrid.width; ++k) {
                  int l = woodlandmansionpieces$simplegrid.get(k, j);
                  if (l == 1) {
                     System.out.print("+");
                  } else if (l == 4) {
                     System.out.print("x");
                  } else if (l == 2) {
                     System.out.print("X");
                  } else if (l == 3) {
                     System.out.print("O");
                  } else if (l == 5) {
                     System.out.print("#");
                  } else {
                     System.out.print(" ");
                  }
               }

               System.out.println("");
            }

            System.out.println("");
         }

      }
   }

   static class MansionPiecePlacer {
      private final StructureTemplateManager structureTemplateManager;
      private final RandomSource random;
      private int startX;
      private int startY;

      public MansionPiecePlacer(StructureTemplateManager pStructureTemplateManager, RandomSource pRandom) {
         this.structureTemplateManager = pStructureTemplateManager;
         this.random = pRandom;
      }

      public void createMansion(BlockPos pPos, Rotation pRotation, List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, WoodlandMansionPieces.MansionGrid pGrid) {
         WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata = new WoodlandMansionPieces.PlacementData();
         woodlandmansionpieces$placementdata.position = pPos;
         woodlandmansionpieces$placementdata.rotation = pRotation;
         woodlandmansionpieces$placementdata.wallType = "wall_flat";
         WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata1 = new WoodlandMansionPieces.PlacementData();
         this.entrance(pPieces, woodlandmansionpieces$placementdata);
         woodlandmansionpieces$placementdata1.position = woodlandmansionpieces$placementdata.position.above(8);
         woodlandmansionpieces$placementdata1.rotation = woodlandmansionpieces$placementdata.rotation;
         woodlandmansionpieces$placementdata1.wallType = "wall_window";
         if (!pPieces.isEmpty()) {
         }

         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = pGrid.baseGrid;
         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid1 = pGrid.thirdFloorGrid;
         this.startX = pGrid.entranceX + 1;
         this.startY = pGrid.entranceY + 1;
         int i = pGrid.entranceX + 1;
         int j = pGrid.entranceY;
         this.traverseOuterWalls(pPieces, woodlandmansionpieces$placementdata, woodlandmansionpieces$simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         this.traverseOuterWalls(pPieces, woodlandmansionpieces$placementdata1, woodlandmansionpieces$simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata2 = new WoodlandMansionPieces.PlacementData();
         woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata.position.above(19);
         woodlandmansionpieces$placementdata2.rotation = woodlandmansionpieces$placementdata.rotation;
         woodlandmansionpieces$placementdata2.wallType = "wall_window";
         boolean flag = false;

         for(int k = 0; k < woodlandmansionpieces$simplegrid1.height && !flag; ++k) {
            for(int l = woodlandmansionpieces$simplegrid1.width - 1; l >= 0 && !flag; --l) {
               if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces$simplegrid1, l, k)) {
                  woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata2.position.relative(pRotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata2.position.relative(pRotation.rotate(Direction.EAST), (l - this.startX) * 8);
                  this.traverseWallPiece(pPieces, woodlandmansionpieces$placementdata2);
                  this.traverseOuterWalls(pPieces, woodlandmansionpieces$placementdata2, woodlandmansionpieces$simplegrid1, Direction.SOUTH, l, k, l, k);
                  flag = true;
               }
            }
         }

         this.createRoof(pPieces, pPos.above(16), pRotation, woodlandmansionpieces$simplegrid, woodlandmansionpieces$simplegrid1);
         this.createRoof(pPieces, pPos.above(27), pRotation, woodlandmansionpieces$simplegrid1, (WoodlandMansionPieces.SimpleGrid)null);
         if (!pPieces.isEmpty()) {
         }

         WoodlandMansionPieces.FloorRoomCollection[] awoodlandmansionpieces$floorroomcollection = new WoodlandMansionPieces.FloorRoomCollection[]{new WoodlandMansionPieces.FirstFloorRoomCollection(), new WoodlandMansionPieces.SecondFloorRoomCollection(), new WoodlandMansionPieces.ThirdFloorRoomCollection()};

         for(int l2 = 0; l2 < 3; ++l2) {
            BlockPos blockpos = pPos.above(8 * l2 + (l2 == 2 ? 3 : 0));
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid2 = pGrid.floorRooms[l2];
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid3 = l2 == 2 ? woodlandmansionpieces$simplegrid1 : woodlandmansionpieces$simplegrid;
            String s = l2 == 0 ? "carpet_south_1" : "carpet_south_2";
            String s1 = l2 == 0 ? "carpet_west_1" : "carpet_west_2";

            for(int i1 = 0; i1 < woodlandmansionpieces$simplegrid3.height; ++i1) {
               for(int j1 = 0; j1 < woodlandmansionpieces$simplegrid3.width; ++j1) {
                  if (woodlandmansionpieces$simplegrid3.get(j1, i1) == 1) {
                     BlockPos blockpos1 = blockpos.relative(pRotation.rotate(Direction.SOUTH), 8 + (i1 - this.startY) * 8);
                     blockpos1 = blockpos1.relative(pRotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
                     pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "corridor_floor", blockpos1, pRotation));
                     if (woodlandmansionpieces$simplegrid3.get(j1, i1 - 1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1, i1 - 1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "carpet_north", blockpos1.relative(pRotation.rotate(Direction.EAST), 1).above(), pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(j1 + 1, i1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1 + 1, i1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "carpet_east", blockpos1.relative(pRotation.rotate(Direction.SOUTH), 1).relative(pRotation.rotate(Direction.EAST), 5).above(), pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(j1, i1 + 1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1, i1 + 1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, s, blockpos1.relative(pRotation.rotate(Direction.SOUTH), 5).relative(pRotation.rotate(Direction.WEST), 1), pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(j1 - 1, i1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1 - 1, i1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, s1, blockpos1.relative(pRotation.rotate(Direction.WEST), 1).relative(pRotation.rotate(Direction.NORTH), 1), pRotation));
                     }
                  }
               }
            }

            String s2 = l2 == 0 ? "indoors_wall_1" : "indoors_wall_2";
            String s3 = l2 == 0 ? "indoors_door_1" : "indoors_door_2";
            List<Direction> list = Lists.newArrayList();

            for(int k1 = 0; k1 < woodlandmansionpieces$simplegrid3.height; ++k1) {
               for(int l1 = 0; l1 < woodlandmansionpieces$simplegrid3.width; ++l1) {
                  boolean flag1 = l2 == 2 && woodlandmansionpieces$simplegrid3.get(l1, k1) == 3;
                  if (woodlandmansionpieces$simplegrid3.get(l1, k1) == 2 || flag1) {
                     int i2 = woodlandmansionpieces$simplegrid2.get(l1, k1);
                     int j2 = i2 & 983040;
                     int k2 = i2 & '\uffff';
                     flag1 = flag1 && (i2 & 8388608) == 8388608;
                     list.clear();
                     if ((i2 & 2097152) == 2097152) {
                        for(Direction direction : Direction.Plane.HORIZONTAL) {
                           if (woodlandmansionpieces$simplegrid3.get(l1 + direction.getStepX(), k1 + direction.getStepZ()) == 1) {
                              list.add(direction);
                           }
                        }
                     }

                     Direction direction1 = null;
                     if (!list.isEmpty()) {
                        direction1 = list.get(this.random.nextInt(list.size()));
                     } else if ((i2 & 1048576) == 1048576) {
                        direction1 = Direction.UP;
                     }

                     BlockPos blockpos3 = blockpos.relative(pRotation.rotate(Direction.SOUTH), 8 + (k1 - this.startY) * 8);
                     blockpos3 = blockpos3.relative(pRotation.rotate(Direction.EAST), -1 + (l1 - this.startX) * 8);
                     if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces$simplegrid3, l1 - 1, k1) && !pGrid.isRoomId(woodlandmansionpieces$simplegrid3, l1 - 1, k1, l2, k2)) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.WEST ? s3 : s2, blockpos3, pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(l1 + 1, k1) == 1 && !flag1) {
                        BlockPos blockpos2 = blockpos3.relative(pRotation.rotate(Direction.EAST), 8);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.EAST ? s3 : s2, blockpos2, pRotation));
                     }

                     if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces$simplegrid3, l1, k1 + 1) && !pGrid.isRoomId(woodlandmansionpieces$simplegrid3, l1, k1 + 1, l2, k2)) {
                        BlockPos blockpos4 = blockpos3.relative(pRotation.rotate(Direction.SOUTH), 7);
                        blockpos4 = blockpos4.relative(pRotation.rotate(Direction.EAST), 7);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.SOUTH ? s3 : s2, blockpos4, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(l1, k1 - 1) == 1 && !flag1) {
                        BlockPos blockpos5 = blockpos3.relative(pRotation.rotate(Direction.NORTH), 1);
                        blockpos5 = blockpos5.relative(pRotation.rotate(Direction.EAST), 7);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.NORTH ? s3 : s2, blockpos5, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (j2 == 65536) {
                        this.addRoom1x1(pPieces, blockpos3, pRotation, direction1, awoodlandmansionpieces$floorroomcollection[l2]);
                     } else if (j2 == 131072 && direction1 != null) {
                        Direction direction3 = pGrid.get1x2RoomDirection(woodlandmansionpieces$simplegrid3, l1, k1, l2, k2);
                        boolean flag2 = (i2 & 4194304) == 4194304;
                        this.addRoom1x2(pPieces, blockpos3, pRotation, direction3, direction1, awoodlandmansionpieces$floorroomcollection[l2], flag2);
                     } else if (j2 == 262144 && direction1 != null && direction1 != Direction.UP) {
                        Direction direction2 = direction1.getClockWise();
                        if (!pGrid.isRoomId(woodlandmansionpieces$simplegrid3, l1 + direction2.getStepX(), k1 + direction2.getStepZ(), l2, k2)) {
                           direction2 = direction2.getOpposite();
                        }

                        this.addRoom2x2(pPieces, blockpos3, pRotation, direction2, direction1, awoodlandmansionpieces$floorroomcollection[l2]);
                     } else if (j2 == 262144 && direction1 == Direction.UP) {
                        this.addRoom2x2Secret(pPieces, blockpos3, pRotation, awoodlandmansionpieces$floorroomcollection[l2]);
                     }
                  }
               }
            }
         }

      }

      private void traverseOuterWalls(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, WoodlandMansionPieces.PlacementData pData, WoodlandMansionPieces.SimpleGrid pLayout, Direction pDirection, int pStartX, int pStartY, int pEntranceX, int pEntranceY) {
         int i = pStartX;
         int j = pStartY;
         Direction direction = pDirection;

         do {
            if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i + pDirection.getStepX(), j + pDirection.getStepZ())) {
               this.traverseTurn(pPieces, pData);
               pDirection = pDirection.getClockWise();
               if (i != pEntranceX || j != pEntranceY || direction != pDirection) {
                  this.traverseWallPiece(pPieces, pData);
               }
            } else if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i + pDirection.getStepX(), j + pDirection.getStepZ()) && WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i + pDirection.getStepX() + pDirection.getCounterClockWise().getStepX(), j + pDirection.getStepZ() + pDirection.getCounterClockWise().getStepZ())) {
               this.traverseInnerTurn(pPieces, pData);
               i += pDirection.getStepX();
               j += pDirection.getStepZ();
               pDirection = pDirection.getCounterClockWise();
            } else {
               i += pDirection.getStepX();
               j += pDirection.getStepZ();
               if (i != pEntranceX || j != pEntranceY || direction != pDirection) {
                  this.traverseWallPiece(pPieces, pData);
               }
            }
         } while(i != pEntranceX || j != pEntranceY || direction != pDirection);

      }

      private void createRoof(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, BlockPos pPos, Rotation pRotation, WoodlandMansionPieces.SimpleGrid pLayout, @Nullable WoodlandMansionPieces.SimpleGrid pNextFloorLayout) {
         for(int i = 0; i < pLayout.height; ++i) {
            for(int j = 0; j < pLayout.width; ++j) {
               BlockPos $$27 = pPos.relative(pRotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
               $$27 = $$27.relative(pRotation.rotate(Direction.EAST), (j - this.startX) * 8);
               boolean flag = pNextFloorLayout != null && WoodlandMansionPieces.MansionGrid.isHouse(pNextFloorLayout, j, i);
               if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j, i) && !flag) {
                  pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof", $$27.above(3), pRotation));
                  if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j + 1, i)) {
                     BlockPos blockpos1 = $$27.relative(pRotation.rotate(Direction.EAST), 6);
                     pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos1, pRotation));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j - 1, i)) {
                     BlockPos blockpos5 = $$27.relative(pRotation.rotate(Direction.EAST), 0);
                     blockpos5 = blockpos5.relative(pRotation.rotate(Direction.SOUTH), 7);
                     pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos5, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j, i - 1)) {
                     BlockPos blockpos6 = $$27.relative(pRotation.rotate(Direction.WEST), 1);
                     pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos6, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j, i + 1)) {
                     BlockPos blockpos7 = $$27.relative(pRotation.rotate(Direction.EAST), 6);
                     blockpos7 = blockpos7.relative(pRotation.rotate(Direction.SOUTH), 6);
                     pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos7, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if (pNextFloorLayout != null) {
            for(int k = 0; k < pLayout.height; ++k) {
               for(int i1 = 0; i1 < pLayout.width; ++i1) {
                  BlockPos blockpos3 = pPos.relative(pRotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  blockpos3 = blockpos3.relative(pRotation.rotate(Direction.EAST), (i1 - this.startX) * 8);
                  boolean flag1 = WoodlandMansionPieces.MansionGrid.isHouse(pNextFloorLayout, i1, k);
                  if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k) && flag1) {
                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1 + 1, k)) {
                        BlockPos blockpos8 = blockpos3.relative(pRotation.rotate(Direction.EAST), 7);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos8, pRotation));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1 - 1, k)) {
                        BlockPos blockpos9 = blockpos3.relative(pRotation.rotate(Direction.WEST), 1);
                        blockpos9 = blockpos9.relative(pRotation.rotate(Direction.SOUTH), 6);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos9, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k - 1)) {
                        BlockPos blockpos10 = blockpos3.relative(pRotation.rotate(Direction.WEST), 0);
                        blockpos10 = blockpos10.relative(pRotation.rotate(Direction.NORTH), 1);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos10, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k + 1)) {
                        BlockPos blockpos11 = blockpos3.relative(pRotation.rotate(Direction.EAST), 6);
                        blockpos11 = blockpos11.relative(pRotation.rotate(Direction.SOUTH), 7);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos11, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1 + 1, k)) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k - 1)) {
                           BlockPos blockpos12 = blockpos3.relative(pRotation.rotate(Direction.EAST), 7);
                           blockpos12 = blockpos12.relative(pRotation.rotate(Direction.NORTH), 2);
                           pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos12, pRotation));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k + 1)) {
                           BlockPos blockpos13 = blockpos3.relative(pRotation.rotate(Direction.EAST), 8);
                           blockpos13 = blockpos13.relative(pRotation.rotate(Direction.SOUTH), 7);
                           pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos13, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1 - 1, k)) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k - 1)) {
                           BlockPos blockpos14 = blockpos3.relative(pRotation.rotate(Direction.WEST), 2);
                           blockpos14 = blockpos14.relative(pRotation.rotate(Direction.NORTH), 1);
                           pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos14, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, i1, k + 1)) {
                           BlockPos blockpos15 = blockpos3.relative(pRotation.rotate(Direction.WEST), 1);
                           blockpos15 = blockpos15.relative(pRotation.rotate(Direction.SOUTH), 8);
                           pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos15, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                        }
                     }
                  }
               }
            }
         }

         for(int l = 0; l < pLayout.height; ++l) {
            for(int j1 = 0; j1 < pLayout.width; ++j1) {
               BlockPos blockpos4 = pPos.relative(pRotation.rotate(Direction.SOUTH), 8 + (l - this.startY) * 8);
               blockpos4 = blockpos4.relative(pRotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
               boolean flag2 = pNextFloorLayout != null && WoodlandMansionPieces.MansionGrid.isHouse(pNextFloorLayout, j1, l);
               if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1, l) && !flag2) {
                  if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1 + 1, l)) {
                     BlockPos blockpos16 = blockpos4.relative(pRotation.rotate(Direction.EAST), 6);
                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1, l + 1)) {
                        BlockPos blockpos2 = blockpos16.relative(pRotation.rotate(Direction.SOUTH), 6);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos2, pRotation));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1 + 1, l + 1)) {
                        BlockPos blockpos18 = blockpos16.relative(pRotation.rotate(Direction.SOUTH), 5);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos18, pRotation));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1, l - 1)) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos16, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1 + 1, l - 1)) {
                        BlockPos blockpos19 = blockpos4.relative(pRotation.rotate(Direction.EAST), 9);
                        blockpos19 = blockpos19.relative(pRotation.rotate(Direction.NORTH), 2);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos19, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1 - 1, l)) {
                     BlockPos blockpos17 = blockpos4.relative(pRotation.rotate(Direction.EAST), 0);
                     blockpos17 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 0);
                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1, l + 1)) {
                        BlockPos blockpos20 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 6);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos20, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1 - 1, l + 1)) {
                        BlockPos blockpos21 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 8);
                        blockpos21 = blockpos21.relative(pRotation.rotate(Direction.WEST), 3);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos21, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1, l - 1)) {
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos17, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(pLayout, j1 - 1, l - 1)) {
                        BlockPos blockpos22 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 1);
                        pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos22, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                     }
                  }
               }
            }
         }

      }

      private void entrance(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, WoodlandMansionPieces.PlacementData pData) {
         Direction direction = pData.rotation.rotate(Direction.WEST);
         pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "entrance", pData.position.relative(direction, 9), pData.rotation));
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), 16);
      }

      private void traverseWallPiece(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, WoodlandMansionPieces.PlacementData pData) {
         pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pData.wallType, pData.position.relative(pData.rotation.rotate(Direction.EAST), 7), pData.rotation));
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), 8);
      }

      private void traverseTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, WoodlandMansionPieces.PlacementData pData) {
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), -1);
         pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "wall_corner", pData.position, pData.rotation));
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), -7);
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.WEST), -6);
         pData.rotation = pData.rotation.getRotated(Rotation.CLOCKWISE_90);
      }

      private void traverseInnerTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, WoodlandMansionPieces.PlacementData pData) {
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), 6);
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.EAST), 8);
         pData.rotation = pData.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
      }

      private void addRoom1x1(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, BlockPos pPos, Rotation pRotation, Direction pDirection, WoodlandMansionPieces.FloorRoomCollection pFloorRooms) {
         Rotation rotation = Rotation.NONE;
         String s = pFloorRooms.get1x1(this.random);
         if (pDirection != Direction.EAST) {
            if (pDirection == Direction.NORTH) {
               rotation = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (pDirection == Direction.WEST) {
               rotation = rotation.getRotated(Rotation.CLOCKWISE_180);
            } else if (pDirection == Direction.SOUTH) {
               rotation = rotation.getRotated(Rotation.CLOCKWISE_90);
            } else {
               s = pFloorRooms.get1x1Secret(this.random);
            }
         }

         BlockPos blockpos = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation, 7, 7);
         rotation = rotation.getRotated(pRotation);
         blockpos = blockpos.rotate(pRotation);
         BlockPos blockpos1 = pPos.offset(blockpos.getX(), 0, blockpos.getZ());
         pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, s, blockpos1, rotation));
      }

      private void addRoom1x2(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, BlockPos pPos, Rotation pRotation, Direction pRoomDirection, Direction p_230126_, WoodlandMansionPieces.FloorRoomCollection pFloorRooms, boolean pIsStairs) {
         if (p_230126_ == Direction.EAST && pRoomDirection == Direction.SOUTH) {
            BlockPos blockpos13 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos13, pRotation));
         } else if (p_230126_ == Direction.EAST && pRoomDirection == Direction.NORTH) {
            BlockPos blockpos12 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos12 = blockpos12.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos12, pRotation, Mirror.LEFT_RIGHT));
         } else if (p_230126_ == Direction.WEST && pRoomDirection == Direction.NORTH) {
            BlockPos blockpos11 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            blockpos11 = blockpos11.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos11, pRotation.getRotated(Rotation.CLOCKWISE_180)));
         } else if (p_230126_ == Direction.WEST && pRoomDirection == Direction.SOUTH) {
            BlockPos blockpos10 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos10, pRotation, Mirror.FRONT_BACK));
         } else if (p_230126_ == Direction.SOUTH && pRoomDirection == Direction.EAST) {
            BlockPos blockpos9 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos9, pRotation.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
         } else if (p_230126_ == Direction.SOUTH && pRoomDirection == Direction.WEST) {
            BlockPos blockpos8 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos8, pRotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (p_230126_ == Direction.NORTH && pRoomDirection == Direction.WEST) {
            BlockPos blockpos7 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            blockpos7 = blockpos7.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos7, pRotation.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
         } else if (p_230126_ == Direction.NORTH && pRoomDirection == Direction.EAST) {
            BlockPos blockpos6 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos6 = blockpos6.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2SideEntrance(this.random, pIsStairs), blockpos6, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
         } else if (p_230126_ == Direction.SOUTH && pRoomDirection == Direction.NORTH) {
            BlockPos blockpos5 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos5 = blockpos5.relative(pRotation.rotate(Direction.NORTH), 8);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2FrontEntrance(this.random, pIsStairs), blockpos5, pRotation));
         } else if (p_230126_ == Direction.NORTH && pRoomDirection == Direction.SOUTH) {
            BlockPos blockpos4 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            blockpos4 = blockpos4.relative(pRotation.rotate(Direction.SOUTH), 14);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2FrontEntrance(this.random, pIsStairs), blockpos4, pRotation.getRotated(Rotation.CLOCKWISE_180)));
         } else if (p_230126_ == Direction.WEST && pRoomDirection == Direction.EAST) {
            BlockPos blockpos3 = pPos.relative(pRotation.rotate(Direction.EAST), 15);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2FrontEntrance(this.random, pIsStairs), blockpos3, pRotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (p_230126_ == Direction.EAST && pRoomDirection == Direction.WEST) {
            BlockPos blockpos2 = pPos.relative(pRotation.rotate(Direction.WEST), 7);
            blockpos2 = blockpos2.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2FrontEntrance(this.random, pIsStairs), blockpos2, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
         } else if (p_230126_ == Direction.UP && pRoomDirection == Direction.EAST) {
            BlockPos blockpos1 = pPos.relative(pRotation.rotate(Direction.EAST), 15);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2Secret(this.random), blockpos1, pRotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (p_230126_ == Direction.UP && pRoomDirection == Direction.SOUTH) {
            BlockPos blockpos = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos = blockpos.relative(pRotation.rotate(Direction.NORTH), 0);
            pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get1x2Secret(this.random), blockpos, pRotation));
         }

      }

      private void addRoom2x2(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, BlockPos pPos, Rotation pRotation, Direction p_230118_, Direction p_230119_, WoodlandMansionPieces.FloorRoomCollection pFloorRooms) {
         int i = 0;
         int j = 0;
         Rotation rotation = pRotation;
         Mirror mirror = Mirror.NONE;
         if (p_230119_ == Direction.EAST && p_230118_ == Direction.SOUTH) {
            i = -7;
         } else if (p_230119_ == Direction.EAST && p_230118_ == Direction.NORTH) {
            i = -7;
            j = 6;
            mirror = Mirror.LEFT_RIGHT;
         } else if (p_230119_ == Direction.NORTH && p_230118_ == Direction.EAST) {
            i = 1;
            j = 14;
            rotation = pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
         } else if (p_230119_ == Direction.NORTH && p_230118_ == Direction.WEST) {
            i = 7;
            j = 14;
            rotation = pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (p_230119_ == Direction.SOUTH && p_230118_ == Direction.WEST) {
            i = 7;
            j = -8;
            rotation = pRotation.getRotated(Rotation.CLOCKWISE_90);
         } else if (p_230119_ == Direction.SOUTH && p_230118_ == Direction.EAST) {
            i = 1;
            j = -8;
            rotation = pRotation.getRotated(Rotation.CLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (p_230119_ == Direction.WEST && p_230118_ == Direction.NORTH) {
            i = 15;
            j = 6;
            rotation = pRotation.getRotated(Rotation.CLOCKWISE_180);
         } else if (p_230119_ == Direction.WEST && p_230118_ == Direction.SOUTH) {
            i = 15;
            mirror = Mirror.FRONT_BACK;
         }

         BlockPos blockpos = pPos.relative(pRotation.rotate(Direction.EAST), i);
         blockpos = blockpos.relative(pRotation.rotate(Direction.SOUTH), j);
         pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get2x2(this.random), blockpos, rotation, mirror));
      }

      private void addRoom2x2Secret(List<WoodlandMansionPieces.WoodlandMansionPiece> pPieces, BlockPos pPos, Rotation pRotation, WoodlandMansionPieces.FloorRoomCollection pFloorRooms) {
         BlockPos blockpos = pPos.relative(pRotation.rotate(Direction.EAST), 1);
         pPieces.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, pFloorRooms.get2x2Secret(this.random), blockpos, pRotation, Mirror.NONE));
      }
   }

   static class PlacementData {
      public Rotation rotation;
      public BlockPos position;
      public String wallType;
   }

   static class SecondFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
      public String get1x1(RandomSource p_230144_) {
         return "1x1_b" + (p_230144_.nextInt(4) + 1);
      }

      public String get1x1Secret(RandomSource p_230149_) {
         return "1x1_as" + (p_230149_.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(RandomSource p_230146_, boolean p_230147_) {
         return p_230147_ ? "1x2_c_stairs" : "1x2_c" + (p_230146_.nextInt(4) + 1);
      }

      public String get1x2FrontEntrance(RandomSource p_230151_, boolean p_230152_) {
         return p_230152_ ? "1x2_d_stairs" : "1x2_d" + (p_230151_.nextInt(5) + 1);
      }

      public String get1x2Secret(RandomSource p_230154_) {
         return "1x2_se" + (p_230154_.nextInt(1) + 1);
      }

      public String get2x2(RandomSource p_230156_) {
         return "2x2_b" + (p_230156_.nextInt(5) + 1);
      }

      public String get2x2Secret(RandomSource p_230158_) {
         return "2x2_s1";
      }
   }

   static class SimpleGrid {
      private final int[][] grid;
      final int width;
      final int height;
      private final int valueIfOutside;

      public SimpleGrid(int pWidth, int pHeight, int pValueIfOutside) {
         this.width = pWidth;
         this.height = pHeight;
         this.valueIfOutside = pValueIfOutside;
         this.grid = new int[pWidth][pHeight];
      }

      public void set(int pX, int pY, int pValue) {
         if (pX >= 0 && pX < this.width && pY >= 0 && pY < this.height) {
            this.grid[pX][pY] = pValue;
         }

      }

      public void set(int pMinX, int pMinY, int pMaxX, int pMaxY, int pValue) {
         for(int i = pMinY; i <= pMaxY; ++i) {
            for(int j = pMinX; j <= pMaxX; ++j) {
               this.set(j, i, pValue);
            }
         }

      }

      public int get(int pX, int pY) {
         return pX >= 0 && pX < this.width && pY >= 0 && pY < this.height ? this.grid[pX][pY] : this.valueIfOutside;
      }

      public void setif(int pX, int pY, int pOldValue, int pNewValue) {
         if (this.get(pX, pY) == pOldValue) {
            this.set(pX, pY, pNewValue);
         }

      }

      public boolean edgesTo(int pX, int pY, int pExpectedValue) {
         return this.get(pX - 1, pY) == pExpectedValue || this.get(pX + 1, pY) == pExpectedValue || this.get(pX, pY + 1) == pExpectedValue || this.get(pX, pY - 1) == pExpectedValue;
      }
   }

   static class ThirdFloorRoomCollection extends WoodlandMansionPieces.SecondFloorRoomCollection {
   }

   public static class WoodlandMansionPiece extends TemplateStructurePiece {
      public WoodlandMansionPiece(StructureTemplateManager pStructureTemplateManager, String pTemplateName, BlockPos pTemplatePosition, Rotation pRotation) {
         this(pStructureTemplateManager, pTemplateName, pTemplatePosition, pRotation, Mirror.NONE);
      }

      public WoodlandMansionPiece(StructureTemplateManager pStructureTemplateManager, String pTemplateName, BlockPos pTemplatePosition, Rotation pRotation, Mirror pMirror) {
         super(StructurePieceType.WOODLAND_MANSION_PIECE, 0, pStructureTemplateManager, makeLocation(pTemplateName), pTemplateName, makeSettings(pMirror, pRotation), pTemplatePosition);
      }

      public WoodlandMansionPiece(StructureTemplateManager pStructureTemplateManager, CompoundTag pTag) {
         super(StructurePieceType.WOODLAND_MANSION_PIECE, pTag, pStructureTemplateManager, (p_230220_) -> {
            return makeSettings(Mirror.valueOf(pTag.getString("Mi")), Rotation.valueOf(pTag.getString("Rot")));
         });
      }

      protected ResourceLocation makeTemplateLocation() {
         return makeLocation(this.templateName);
      }

      private static ResourceLocation makeLocation(String pName) {
         return new ResourceLocation("woodland_mansion/" + pName);
      }

      private static StructurePlaceSettings makeSettings(Mirror pMirror, Rotation pRotation) {
         return (new StructurePlaceSettings()).setIgnoreEntities(true).setRotation(pRotation).setMirror(pMirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
         super.addAdditionalSaveData(pContext, pTag);
         pTag.putString("Rot", this.placeSettings.getRotation().name());
         pTag.putString("Mi", this.placeSettings.getMirror().name());
      }

      protected void handleDataMarker(String pName, BlockPos pPos, ServerLevelAccessor pLevel, RandomSource pRandom, BoundingBox pBox) {
         if (pName.startsWith("Chest")) {
            Rotation rotation = this.placeSettings.getRotation();
            BlockState blockstate = Blocks.CHEST.defaultBlockState();
            if ("ChestWest".equals(pName)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.WEST));
            } else if ("ChestEast".equals(pName)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.EAST));
            } else if ("ChestSouth".equals(pName)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.SOUTH));
            } else if ("ChestNorth".equals(pName)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.NORTH));
            }

            this.createChest(pLevel, pBox, pRandom, pPos, BuiltInLootTables.WOODLAND_MANSION, blockstate);
         } else {
            List<Mob> list = new ArrayList<>();
            switch (pName) {
               case "Mage":
                  list.add(EntityType.EVOKER.create(pLevel.getLevel()));
                  break;
               case "Warrior":
                  list.add(EntityType.VINDICATOR.create(pLevel.getLevel()));
                  break;
               case "Group of Allays":
                  int i = pLevel.getRandom().nextInt(3) + 1;

                  for(int j = 0; j < i; ++j) {
                     list.add(EntityType.ALLAY.create(pLevel.getLevel()));
                  }
                  break;
               default:
                  return;
            }

            for(Mob mob : list) {
               if (mob != null) {
                  mob.setPersistenceRequired();
                  mob.moveTo(pPos, 0.0F, 0.0F);
                  mob.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
                  pLevel.addFreshEntityWithPassengers(mob);
                  pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }

      }
   }
}