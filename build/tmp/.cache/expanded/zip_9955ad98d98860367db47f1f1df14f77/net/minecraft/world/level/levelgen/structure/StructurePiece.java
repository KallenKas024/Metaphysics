package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public abstract class StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected BoundingBox boundingBox;
   @Nullable
   private Direction orientation;
   private Mirror mirror;
   private Rotation rotation;
   protected int genDepth;
   private final StructurePieceType type;
   private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();

   protected StructurePiece(StructurePieceType pType, int pGenDepth, BoundingBox pBoundingBox) {
      this.type = pType;
      this.genDepth = pGenDepth;
      this.boundingBox = pBoundingBox;
   }

   public StructurePiece(StructurePieceType pType, CompoundTag pTag) {
      this(pType, pTag.getInt("GD"), BoundingBox.CODEC.parse(NbtOps.INSTANCE, pTag.get("BB")).resultOrPartial(LOGGER::error).orElseThrow(() -> {
         return new IllegalArgumentException("Invalid boundingbox");
      }));
      int i = pTag.getInt("O");
      this.setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
   }

   protected static BoundingBox makeBoundingBox(int pX, int pY, int pZ, Direction pDirection, int pOffsetX, int pOffsetY, int pOffsetZ) {
      return pDirection.getAxis() == Direction.Axis.Z ? new BoundingBox(pX, pY, pZ, pX + pOffsetX - 1, pY + pOffsetY - 1, pZ + pOffsetZ - 1) : new BoundingBox(pX, pY, pZ, pX + pOffsetZ - 1, pY + pOffsetY - 1, pZ + pOffsetX - 1);
   }

   protected static Direction getRandomHorizontalDirection(RandomSource pRandom) {
      return Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
   }

   public final CompoundTag createTag(StructurePieceSerializationContext pContext) {
      if (BuiltInRegistries.STRUCTURE_PIECE.getKey(this.getType()) == null) { // FORGE: Friendlier error then the Null String error below.
         throw new RuntimeException("StructurePiece \"" + this.getClass().getName() + "\": \"" + this.getType() + "\" unregistered, serializing impossible.");
      }
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("id", BuiltInRegistries.STRUCTURE_PIECE.getKey(this.getType()).toString());
      BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(LOGGER::error).ifPresent((p_163579_) -> {
         compoundtag.put("BB", p_163579_);
      });
      Direction direction = this.getOrientation();
      compoundtag.putInt("O", direction == null ? -1 : direction.get2DDataValue());
      compoundtag.putInt("GD", this.genDepth);
      this.addAdditionalSaveData(pContext, compoundtag);
      return compoundtag;
   }

   protected abstract void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag);

   public void addChildren(StructurePiece pPiece, StructurePieceAccessor pPieces, RandomSource pRandom) {
   }

   public abstract void postProcess(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, RandomSource pRandom, BoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos);

   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getGenDepth() {
      return this.genDepth;
   }

   public void setGenDepth(int pGenDepth) {
      this.genDepth = pGenDepth;
   }

   public boolean isCloseToChunk(ChunkPos pChunkPos, int pDistance) {
      int i = pChunkPos.getMinBlockX();
      int j = pChunkPos.getMinBlockZ();
      return this.boundingBox.intersects(i - pDistance, j - pDistance, i + 15 + pDistance, j + 15 + pDistance);
   }

   public BlockPos getLocatorPosition() {
      return new BlockPos(this.boundingBox.getCenter());
   }

   protected BlockPos.MutableBlockPos getWorldPos(int pX, int pY, int pZ) {
      return new BlockPos.MutableBlockPos(this.getWorldX(pX, pZ), this.getWorldY(pY), this.getWorldZ(pX, pZ));
   }

   protected int getWorldX(int pX, int pZ) {
      Direction direction = this.getOrientation();
      if (direction == null) {
         return pX;
      } else {
         switch (direction) {
            case NORTH:
            case SOUTH:
               return this.boundingBox.minX() + pX;
            case WEST:
               return this.boundingBox.maxX() - pZ;
            case EAST:
               return this.boundingBox.minX() + pZ;
            default:
               return pX;
         }
      }
   }

   protected int getWorldY(int pY) {
      return this.getOrientation() == null ? pY : pY + this.boundingBox.minY();
   }

   protected int getWorldZ(int pX, int pZ) {
      Direction direction = this.getOrientation();
      if (direction == null) {
         return pZ;
      } else {
         switch (direction) {
            case NORTH:
               return this.boundingBox.maxZ() - pZ;
            case SOUTH:
               return this.boundingBox.minZ() + pZ;
            case WEST:
            case EAST:
               return this.boundingBox.minZ() + pX;
            default:
               return pZ;
         }
      }
   }

   protected void placeBlock(WorldGenLevel pLevel, BlockState pBlockstate, int pX, int pY, int pZ, BoundingBox pBoundingbox) {
      BlockPos blockpos = this.getWorldPos(pX, pY, pZ);
      if (pBoundingbox.isInside(blockpos)) {
         if (this.canBeReplaced(pLevel, pX, pY, pZ, pBoundingbox)) {
            if (this.mirror != Mirror.NONE) {
               pBlockstate = pBlockstate.mirror(this.mirror);
            }

            if (this.rotation != Rotation.NONE) {
               pBlockstate = pBlockstate.rotate(this.rotation);
            }

            pLevel.setBlock(blockpos, pBlockstate, 2);
            FluidState fluidstate = pLevel.getFluidState(blockpos);
            if (!fluidstate.isEmpty()) {
               pLevel.scheduleTick(blockpos, fluidstate.getType(), 0);
            }

            if (SHAPE_CHECK_BLOCKS.contains(pBlockstate.getBlock())) {
               pLevel.getChunk(blockpos).markPosForPostprocessing(blockpos);
            }

         }
      }
   }

   protected boolean canBeReplaced(LevelReader pLevel, int pX, int pY, int pZ, BoundingBox pBox) {
      return true;
   }

   protected BlockState getBlock(BlockGetter pLevel, int pX, int pY, int pZ, BoundingBox pBox) {
      BlockPos blockpos = this.getWorldPos(pX, pY, pZ);
      return !pBox.isInside(blockpos) ? Blocks.AIR.defaultBlockState() : pLevel.getBlockState(blockpos);
   }

   protected boolean isInterior(LevelReader pLevel, int pX, int pY, int pZ, BoundingBox pBox) {
      BlockPos blockpos = this.getWorldPos(pX, pY + 1, pZ);
      if (!pBox.isInside(blockpos)) {
         return false;
      } else {
         return blockpos.getY() < pLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockpos.getX(), blockpos.getZ());
      }
   }

   protected void generateAirBox(WorldGenLevel pLevel, BoundingBox pBox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      for(int i = pMinY; i <= pMaxY; ++i) {
         for(int j = pMinX; j <= pMaxX; ++j) {
            for(int k = pMinZ; k <= pMaxZ; ++k) {
               this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), j, i, k, pBox);
            }
         }
      }

   }

   /**
    * Fill the given area with the selected blocks
    */
   protected void generateBox(WorldGenLevel pLevel, BoundingBox pBox, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax, BlockState pBoundaryBlockState, BlockState pInsideBlockState, boolean pExistingOnly) {
      for(int i = pYMin; i <= pYMax; ++i) {
         for(int j = pXMin; j <= pXMax; ++j) {
            for(int k = pZMin; k <= pZMax; ++k) {
               if (!pExistingOnly || !this.getBlock(pLevel, j, i, k, pBox).isAir()) {
                  if (i != pYMin && i != pYMax && j != pXMin && j != pXMax && k != pZMin && k != pZMax) {
                     this.placeBlock(pLevel, pInsideBlockState, j, i, k, pBox);
                  } else {
                     this.placeBlock(pLevel, pBoundaryBlockState, j, i, k, pBox);
                  }
               }
            }
         }
      }

   }

   protected void generateBox(WorldGenLevel pLevel, BoundingBox pBoundingBox, BoundingBox pBox, BlockState pBoundaryBlockState, BlockState pInsideBlockState, boolean pExistingOnly) {
      this.generateBox(pLevel, pBoundingBox, pBox.minX(), pBox.minY(), pBox.minZ(), pBox.maxX(), pBox.maxY(), pBox.maxZ(), pBoundaryBlockState, pInsideBlockState, pExistingOnly);
   }

   protected void generateBox(WorldGenLevel pLevel, BoundingBox pBox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ, boolean pAlwaysReplace, RandomSource pRandom, StructurePiece.BlockSelector pBlockSelector) {
      for(int i = pMinY; i <= pMaxY; ++i) {
         for(int j = pMinX; j <= pMaxX; ++j) {
            for(int k = pMinZ; k <= pMaxZ; ++k) {
               if (!pAlwaysReplace || !this.getBlock(pLevel, j, i, k, pBox).isAir()) {
                  pBlockSelector.next(pRandom, j, i, k, i == pMinY || i == pMaxY || j == pMinX || j == pMaxX || k == pMinZ || k == pMaxZ);
                  this.placeBlock(pLevel, pBlockSelector.getNext(), j, i, k, pBox);
               }
            }
         }
      }

   }

   protected void generateBox(WorldGenLevel pLevel, BoundingBox pBoundingBox, BoundingBox pBox, boolean pAlwaysReplace, RandomSource pRandom, StructurePiece.BlockSelector pBlockSelector) {
      this.generateBox(pLevel, pBoundingBox, pBox.minX(), pBox.minY(), pBox.minZ(), pBox.maxX(), pBox.maxY(), pBox.maxZ(), pAlwaysReplace, pRandom, pBlockSelector);
   }

   protected void generateMaybeBox(WorldGenLevel pLevel, BoundingBox pBox, RandomSource pRandom, float pChance, int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2, BlockState pEdgeState, BlockState pState, boolean pRequireNonAir, boolean pRequireSkylight) {
      for(int i = pY1; i <= pY2; ++i) {
         for(int j = pX1; j <= pX2; ++j) {
            for(int k = pZ1; k <= pZ2; ++k) {
               if (!(pRandom.nextFloat() > pChance) && (!pRequireNonAir || !this.getBlock(pLevel, j, i, k, pBox).isAir()) && (!pRequireSkylight || this.isInterior(pLevel, j, i, k, pBox))) {
                  if (i != pY1 && i != pY2 && j != pX1 && j != pX2 && k != pZ1 && k != pZ2) {
                     this.placeBlock(pLevel, pState, j, i, k, pBox);
                  } else {
                     this.placeBlock(pLevel, pEdgeState, j, i, k, pBox);
                  }
               }
            }
         }
      }

   }

   protected void maybeGenerateBlock(WorldGenLevel pLevel, BoundingBox pBox, RandomSource pRandom, float pChance, int pX, int pY, int pZ, BlockState pState) {
      if (pRandom.nextFloat() < pChance) {
         this.placeBlock(pLevel, pState, pX, pY, pZ, pBox);
      }

   }

   protected void generateUpperHalfSphere(WorldGenLevel pLevel, BoundingBox pBox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ, BlockState pState, boolean pExcludeAir) {
      float f = (float)(pMaxX - pMinX + 1);
      float f1 = (float)(pMaxY - pMinY + 1);
      float f2 = (float)(pMaxZ - pMinZ + 1);
      float f3 = (float)pMinX + f / 2.0F;
      float f4 = (float)pMinZ + f2 / 2.0F;

      for(int i = pMinY; i <= pMaxY; ++i) {
         float f5 = (float)(i - pMinY) / f1;

         for(int j = pMinX; j <= pMaxX; ++j) {
            float f6 = ((float)j - f3) / (f * 0.5F);

            for(int k = pMinZ; k <= pMaxZ; ++k) {
               float f7 = ((float)k - f4) / (f2 * 0.5F);
               if (!pExcludeAir || !this.getBlock(pLevel, j, i, k, pBox).isAir()) {
                  float f8 = f6 * f6 + f5 * f5 + f7 * f7;
                  if (f8 <= 1.05F) {
                     this.placeBlock(pLevel, pState, j, i, k, pBox);
                  }
               }
            }
         }
      }

   }

   protected void fillColumnDown(WorldGenLevel pLevel, BlockState pState, int pX, int pY, int pZ, BoundingBox pBox) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = this.getWorldPos(pX, pY, pZ);
      if (pBox.isInside(blockpos$mutableblockpos)) {
         while(this.isReplaceableByStructures(pLevel.getBlockState(blockpos$mutableblockpos)) && blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight() + 1) {
            pLevel.setBlock(blockpos$mutableblockpos, pState, 2);
            blockpos$mutableblockpos.move(Direction.DOWN);
         }

      }
   }

   protected boolean isReplaceableByStructures(BlockState pState) {
      return pState.isAir() || pState.liquid() || pState.is(Blocks.GLOW_LICHEN) || pState.is(Blocks.SEAGRASS) || pState.is(Blocks.TALL_SEAGRASS);
   }

   protected boolean createChest(WorldGenLevel pLevel, BoundingBox pBox, RandomSource pRandom, int pX, int pY, int pZ, ResourceLocation pLootTable) {
      return this.createChest(pLevel, pBox, pRandom, this.getWorldPos(pX, pY, pZ), pLootTable, (BlockState)null);
   }

   public static BlockState reorient(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = null;

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction1);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(Blocks.CHEST)) {
            return pState;
         }

         if (blockstate.isSolidRender(pLevel, blockpos)) {
            if (direction != null) {
               direction = null;
               break;
            }

            direction = direction1;
         }
      }

      if (direction != null) {
         return pState.setValue(HorizontalDirectionalBlock.FACING, direction.getOpposite());
      } else {
         Direction direction2 = pState.getValue(HorizontalDirectionalBlock.FACING);
         BlockPos blockpos1 = pPos.relative(direction2);
         if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1)) {
            direction2 = direction2.getOpposite();
            blockpos1 = pPos.relative(direction2);
         }

         if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1)) {
            direction2 = direction2.getClockWise();
            blockpos1 = pPos.relative(direction2);
         }

         if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1)) {
            direction2 = direction2.getOpposite();
            blockpos1 = pPos.relative(direction2);
         }

         return pState.setValue(HorizontalDirectionalBlock.FACING, direction2);
      }
   }

   protected boolean createChest(ServerLevelAccessor pLevel, BoundingBox pBox, RandomSource pRandom, BlockPos pPos, ResourceLocation pLootTable, @Nullable BlockState pState) {
      if (pBox.isInside(pPos) && !pLevel.getBlockState(pPos).is(Blocks.CHEST)) {
         if (pState == null) {
            pState = reorient(pLevel, pPos, Blocks.CHEST.defaultBlockState());
         }

         pLevel.setBlock(pPos, pState, 2);
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockentity).setLootTable(pLootTable, pRandom.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean createDispenser(WorldGenLevel pLevel, BoundingBox pBox, RandomSource pRandom, int pX, int pY, int pZ, Direction pFacing, ResourceLocation pLootTable) {
      BlockPos blockpos = this.getWorldPos(pX, pY, pZ);
      if (pBox.isInside(blockpos) && !pLevel.getBlockState(blockpos).is(Blocks.DISPENSER)) {
         this.placeBlock(pLevel, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, pFacing), pX, pY, pZ, pBox);
         BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
         if (blockentity instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)blockentity).setLootTable(pLootTable, pRandom.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   public void move(int pX, int pY, int pZ) {
      this.boundingBox.move(pX, pY, pZ);
   }

   public static BoundingBox createBoundingBox(Stream<StructurePiece> pPieces) {
      return BoundingBox.encapsulatingBoxes(pPieces.map(StructurePiece::getBoundingBox)::iterator).orElseThrow(() -> {
         return new IllegalStateException("Unable to calculate boundingbox without pieces");
      });
   }

   @Nullable
   public static StructurePiece findCollisionPiece(List<StructurePiece> pPieces, BoundingBox pBoundingBox) {
      for(StructurePiece structurepiece : pPieces) {
         if (structurepiece.getBoundingBox().intersects(pBoundingBox)) {
            return structurepiece;
         }
      }

      return null;
   }

   @Nullable
   public Direction getOrientation() {
      return this.orientation;
   }

   public void setOrientation(@Nullable Direction pOrientation) {
      this.orientation = pOrientation;
      if (pOrientation == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch (pOrientation) {
            case SOUTH:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.NONE;
               break;
            case WEST:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            case EAST:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            default:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.NONE;
         }
      }

   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public StructurePieceType getType() {
      return this.type;
   }

   public abstract static class BlockSelector {
      protected BlockState next = Blocks.AIR.defaultBlockState();

      public abstract void next(RandomSource pRandom, int pX, int pY, int pZ, boolean pWall);

      public BlockState getNext() {
         return this.next;
      }
   }
}
