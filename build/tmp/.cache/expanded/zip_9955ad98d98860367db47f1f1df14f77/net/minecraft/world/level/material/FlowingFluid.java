package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid {
   public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
   private static final int CACHE_SIZE = 200;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) {
         protected void rehash(int p_76102_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });
   private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder) {
      pBuilder.add(FALLING);
   }

   public Vec3 getFlow(BlockGetter pBlockReader, BlockPos pPos, FluidState pFluidState) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         blockpos$mutableblockpos.setWithOffset(pPos, direction);
         FluidState fluidstate = pBlockReader.getFluidState(blockpos$mutableblockpos);
         if (this.affectsFlow(fluidstate)) {
            float f = fluidstate.getOwnHeight();
            float f1 = 0.0F;
            if (f == 0.0F) {
               if (!pBlockReader.getBlockState(blockpos$mutableblockpos).blocksMotion()) {
                  BlockPos blockpos = blockpos$mutableblockpos.below();
                  FluidState fluidstate1 = pBlockReader.getFluidState(blockpos);
                  if (this.affectsFlow(fluidstate1)) {
                     f = fluidstate1.getOwnHeight();
                     if (f > 0.0F) {
                        f1 = pFluidState.getOwnHeight() - (f - 0.8888889F);
                     }
                  }
               }
            } else if (f > 0.0F) {
               f1 = pFluidState.getOwnHeight() - f;
            }

            if (f1 != 0.0F) {
               d0 += (double)((float)direction.getStepX() * f1);
               d1 += (double)((float)direction.getStepZ() * f1);
            }
         }
      }

      Vec3 vec3 = new Vec3(d0, 0.0D, d1);
      if (pFluidState.getValue(FALLING)) {
         for(Direction direction1 : Direction.Plane.HORIZONTAL) {
            blockpos$mutableblockpos.setWithOffset(pPos, direction1);
            if (this.isSolidFace(pBlockReader, blockpos$mutableblockpos, direction1) || this.isSolidFace(pBlockReader, blockpos$mutableblockpos.above(), direction1)) {
               vec3 = vec3.normalize().add(0.0D, -6.0D, 0.0D);
               break;
            }
         }
      }

      return vec3.normalize();
   }

   private boolean affectsFlow(FluidState pState) {
      return pState.isEmpty() || pState.getType().isSame(this);
   }

   protected boolean isSolidFace(BlockGetter pLevel, BlockPos pNeighborPos, Direction pSide) {
      BlockState blockstate = pLevel.getBlockState(pNeighborPos);
      FluidState fluidstate = pLevel.getFluidState(pNeighborPos);
      if (fluidstate.getType().isSame(this)) {
         return false;
      } else if (pSide == Direction.UP) {
         return true;
      } else {
         return blockstate.getBlock() instanceof IceBlock ? false : blockstate.isFaceSturdy(pLevel, pNeighborPos, pSide);
      }
   }

   protected void spread(Level pLevel, BlockPos pPos, FluidState pState) {
      if (!pState.isEmpty()) {
         BlockState blockstate = pLevel.getBlockState(pPos);
         BlockPos blockpos = pPos.below();
         BlockState blockstate1 = pLevel.getBlockState(blockpos);
         FluidState fluidstate = this.getNewLiquid(pLevel, blockpos, blockstate1);
         if (this.canSpreadTo(pLevel, pPos, blockstate, Direction.DOWN, blockpos, blockstate1, pLevel.getFluidState(blockpos), fluidstate.getType())) {
            this.spreadTo(pLevel, blockpos, blockstate1, Direction.DOWN, fluidstate);
            if (this.sourceNeighborCount(pLevel, pPos) >= 3) {
               this.spreadToSides(pLevel, pPos, pState, blockstate);
            }
         } else if (pState.isSource() || !this.isWaterHole(pLevel, fluidstate.getType(), pPos, blockstate, blockpos, blockstate1)) {
            this.spreadToSides(pLevel, pPos, pState, blockstate);
         }

      }
   }

   private void spreadToSides(Level pLevel, BlockPos pPos, FluidState pFluidState, BlockState pBlockState) {
      int i = pFluidState.getAmount() - this.getDropOff(pLevel);
      if (pFluidState.getValue(FALLING)) {
         i = 7;
      }

      if (i > 0) {
         Map<Direction, FluidState> map = this.getSpread(pLevel, pPos, pBlockState);

         for(Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            FluidState fluidstate = entry.getValue();
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (this.canSpreadTo(pLevel, pPos, pBlockState, direction, blockpos, blockstate, pLevel.getFluidState(blockpos), fluidstate.getType())) {
               this.spreadTo(pLevel, blockpos, blockstate, direction, fluidstate);
            }
         }

      }
   }

   protected FluidState getNewLiquid(Level pLevel, BlockPos pPos, BlockState pBlockState) {
      int i = 0;
      int j = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         FluidState fluidstate = blockstate.getFluidState();
         if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, pLevel, pPos, pBlockState, blockpos, blockstate)) {
            if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(pLevel, blockpos, blockstate, fluidstate.canConvertToSource(pLevel, blockpos))) {
               ++j;
            }

            i = Math.max(i, fluidstate.getAmount());
         }
      }

      if (j >= 2) {
         BlockState blockstate1 = pLevel.getBlockState(pPos.below());
         FluidState fluidstate1 = blockstate1.getFluidState();
         if (blockstate1.isSolid() || this.isSourceBlockOfThisType(fluidstate1)) {
            return this.getSource(false);
         }
      }

      BlockPos blockpos1 = pPos.above();
      BlockState blockstate2 = pLevel.getBlockState(blockpos1);
      FluidState fluidstate2 = blockstate2.getFluidState();
      if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, pLevel, pPos, pBlockState, blockpos1, blockstate2)) {
         return this.getFlowing(8, true);
      } else {
         int k = i - this.getDropOff(pLevel);
         return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
      }
   }

   private boolean canPassThroughWall(Direction pDirection, BlockGetter pLevel, BlockPos pPos, BlockState pState, BlockPos pSpreadPos, BlockState pSpreadState) {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap;
      if (!pState.getBlock().hasDynamicShape() && !pSpreadState.getBlock().hasDynamicShape()) {
         object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
      } else {
         object2bytelinkedopenhashmap = null;
      }

      Block.BlockStatePairKey block$blockstatepairkey;
      if (object2bytelinkedopenhashmap != null) {
         block$blockstatepairkey = new Block.BlockStatePairKey(pState, pSpreadState, pDirection);
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
         if (b0 != 127) {
            return b0 != 0;
         }
      } else {
         block$blockstatepairkey = null;
      }

      VoxelShape voxelshape1 = pState.getCollisionShape(pLevel, pPos);
      VoxelShape voxelshape = pSpreadState.getCollisionShape(pLevel, pSpreadPos);
      boolean flag = !Shapes.mergedFaceOccludes(voxelshape1, voxelshape, pDirection);
      if (object2bytelinkedopenhashmap != null) {
         if (object2bytelinkedopenhashmap.size() == 200) {
            object2bytelinkedopenhashmap.removeLastByte();
         }

         object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte)(flag ? 1 : 0));
      }

      return flag;
   }

   public abstract Fluid getFlowing();

   public FluidState getFlowing(int pLevel, boolean pFalling) {
      return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(pLevel)).setValue(FALLING, Boolean.valueOf(pFalling));
   }

   public abstract Fluid getSource();

   public FluidState getSource(boolean pFalling) {
      return this.getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(pFalling));
   }

   @Override
   public boolean canConvertToSource(FluidState state, Level level, BlockPos pos) {
      return this.canConvertToSource(level);
   }

   /**
    * @deprecated Forge: Use {@link #canConvertToSource(FluidState, Level, BlockPos)} instead.
    */
   @Deprecated
   protected abstract boolean canConvertToSource(Level pLevel);

   protected void spreadTo(LevelAccessor pLevel, BlockPos pPos, BlockState pBlockState, Direction pDirection, FluidState pFluidState) {
      if (pBlockState.getBlock() instanceof LiquidBlockContainer) {
         ((LiquidBlockContainer)pBlockState.getBlock()).placeLiquid(pLevel, pPos, pBlockState, pFluidState);
      } else {
         if (!pBlockState.isAir()) {
            this.beforeDestroyingBlock(pLevel, pPos, pBlockState);
         }

         pLevel.setBlock(pPos, pFluidState.createLegacyBlock(), 3);
      }

   }

   protected abstract void beforeDestroyingBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState);

   private static short getCacheKey(BlockPos pSourcePos, BlockPos pSpreadPos) {
      int i = pSpreadPos.getX() - pSourcePos.getX();
      int j = pSpreadPos.getZ() - pSourcePos.getZ();
      return (short)((i + 128 & 255) << 8 | j + 128 & 255);
   }

   protected int getSlopeDistance(LevelReader pLevel, BlockPos pSpreadPos, int pDistance, Direction pDirection, BlockState pCurrentSpreadState, BlockPos pSourcePos, Short2ObjectMap<Pair<BlockState, FluidState>> pStateCache, Short2BooleanMap pWaterHoleCache) {
      int i = 1000;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (direction != pDirection) {
            BlockPos blockpos = pSpreadPos.relative(direction);
            short short1 = getCacheKey(pSourcePos, blockpos);
            Pair<BlockState, FluidState> pair = pStateCache.computeIfAbsent(short1, (p_284932_) -> {
               BlockState blockstate1 = pLevel.getBlockState(blockpos);
               return Pair.of(blockstate1, blockstate1.getFluidState());
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            if (this.canPassThrough(pLevel, this.getFlowing(), pSpreadPos, pCurrentSpreadState, direction, blockpos, blockstate, fluidstate)) {
               boolean flag = pWaterHoleCache.computeIfAbsent(short1, (p_192912_) -> {
                  BlockPos blockpos1 = blockpos.below();
                  BlockState blockstate1 = pLevel.getBlockState(blockpos1);
                  return this.isWaterHole(pLevel, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
               });
               if (flag) {
                  return pDistance;
               }

               if (pDistance < this.getSlopeFindDistance(pLevel)) {
                  int j = this.getSlopeDistance(pLevel, blockpos, pDistance + 1, direction.getOpposite(), blockstate, pSourcePos, pStateCache, pWaterHoleCache);
                  if (j < i) {
                     i = j;
                  }
               }
            }
         }
      }

      return i;
   }

   private boolean isWaterHole(BlockGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pState, BlockPos pSpreadPos, BlockState pSpreadState) {
      if (!this.canPassThroughWall(Direction.DOWN, pLevel, pPos, pState, pSpreadPos, pSpreadState)) {
         return false;
      } else {
         return pSpreadState.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(pLevel, pSpreadPos, pSpreadState, pFluid);
      }
   }

   private boolean canPassThrough(BlockGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pState, Direction pDirection, BlockPos pSpreadPos, BlockState pSpreadState, FluidState pFluidState) {
      return !this.isSourceBlockOfThisType(pFluidState) && this.canPassThroughWall(pDirection, pLevel, pPos, pState, pSpreadPos, pSpreadState) && this.canHoldFluid(pLevel, pSpreadPos, pSpreadState, pFluid);
   }

   private boolean isSourceBlockOfThisType(FluidState pState) {
      return pState.getType().isSame(this) && pState.isSource();
   }

   protected abstract int getSlopeFindDistance(LevelReader pLevel);

   /**
    * Returns the number of immediately adjacent source blocks of the same fluid that lie on the horizontal plane.
    */
   private int sourceNeighborCount(LevelReader pLevel, BlockPos pPos) {
      int i = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         FluidState fluidstate = pLevel.getFluidState(blockpos);
         if (this.isSourceBlockOfThisType(fluidstate)) {
            ++i;
         }
      }

      return i;
   }

   protected Map<Direction, FluidState> getSpread(Level pLevel, BlockPos pPos, BlockState pState) {
      int i = 1000;
      Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
      Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
      Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         short short1 = getCacheKey(pPos, blockpos);
         Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, (p_284929_) -> {
            BlockState blockstate1 = pLevel.getBlockState(blockpos);
            return Pair.of(blockstate1, blockstate1.getFluidState());
         });
         BlockState blockstate = pair.getFirst();
         FluidState fluidstate = pair.getSecond();
         FluidState fluidstate1 = this.getNewLiquid(pLevel, blockpos, blockstate);
         if (this.canPassThrough(pLevel, fluidstate1.getType(), pPos, pState, direction, blockpos, blockstate, fluidstate)) {
            BlockPos blockpos1 = blockpos.below();
            boolean flag = short2booleanmap.computeIfAbsent(short1, (p_255612_) -> {
               BlockState blockstate1 = pLevel.getBlockState(blockpos1);
               return this.isWaterHole(pLevel, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
            });
            int j;
            if (flag) {
               j = 0;
            } else {
               j = this.getSlopeDistance(pLevel, blockpos, 1, direction.getOpposite(), blockstate, pPos, short2objectmap, short2booleanmap);
            }

            if (j < i) {
               map.clear();
            }

            if (j <= i) {
               map.put(direction, fluidstate1);
               i = j;
            }
         }
      }

      return map;
   }

   private boolean canHoldFluid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
      Block block = pState.getBlock();
      if (block instanceof LiquidBlockContainer) {
         return ((LiquidBlockContainer)block).canPlaceLiquid(pLevel, pPos, pState, pFluid);
      } else if (!(block instanceof DoorBlock) && !pState.is(BlockTags.SIGNS) && !pState.is(Blocks.LADDER) && !pState.is(Blocks.SUGAR_CANE) && !pState.is(Blocks.BUBBLE_COLUMN)) {
         if (!pState.is(Blocks.NETHER_PORTAL) && !pState.is(Blocks.END_PORTAL) && !pState.is(Blocks.END_GATEWAY) && !pState.is(Blocks.STRUCTURE_VOID)) {
            return !pState.blocksMotion();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean canSpreadTo(BlockGetter pLevel, BlockPos pFromPos, BlockState pFromBlockState, Direction pDirection, BlockPos pToPos, BlockState pToBlockState, FluidState pToFluidState, Fluid pFluid) {
      return pToFluidState.canBeReplacedWith(pLevel, pToPos, pFluid, pDirection) && this.canPassThroughWall(pDirection, pLevel, pFromPos, pFromBlockState, pToPos, pToBlockState) && this.canHoldFluid(pLevel, pToPos, pToBlockState, pFluid);
   }

   protected abstract int getDropOff(LevelReader pLevel);

   protected int getSpreadDelay(Level pLevel, BlockPos pPos, FluidState pCurrentState, FluidState pNewState) {
      return this.getTickDelay(pLevel);
   }

   public void tick(Level pLevel, BlockPos pPos, FluidState pState) {
      if (!pState.isSource()) {
         FluidState fluidstate = this.getNewLiquid(pLevel, pPos, pLevel.getBlockState(pPos));
         int i = this.getSpreadDelay(pLevel, pPos, pState, fluidstate);
         if (fluidstate.isEmpty()) {
            pState = fluidstate;
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
         } else if (!fluidstate.equals(pState)) {
            pState = fluidstate;
            BlockState blockstate = fluidstate.createLegacyBlock();
            pLevel.setBlock(pPos, blockstate, 2);
            pLevel.scheduleTick(pPos, fluidstate.getType(), i);
            pLevel.updateNeighborsAt(pPos, blockstate.getBlock());
         }
      }

      this.spread(pLevel, pPos, pState);
   }

   protected static int getLegacyLevel(FluidState pState) {
      return pState.isSource() ? 0 : 8 - Math.min(pState.getAmount(), 8) + (pState.getValue(FALLING) ? 8 : 0);
   }

   private static boolean hasSameAbove(FluidState pFluidState, BlockGetter pLevel, BlockPos pPos) {
      return pFluidState.getType().isSame(pLevel.getFluidState(pPos.above()).getType());
   }

   public float getHeight(FluidState pState, BlockGetter pLevel, BlockPos pPos) {
      return hasSameAbove(pState, pLevel, pPos) ? 1.0F : pState.getOwnHeight();
   }

   public float getOwnHeight(FluidState pState) {
      return (float)pState.getAmount() / 9.0F;
   }

   public abstract int getAmount(FluidState pState);

   public VoxelShape getShape(FluidState pState, BlockGetter pLevel, BlockPos pPos) {
      return pState.getAmount() == 9 && hasSameAbove(pState, pLevel, pPos) ? Shapes.block() : this.shapes.computeIfAbsent(pState, (p_76073_) -> {
         return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)p_76073_.getHeight(pLevel, pPos), 1.0D);
      });
   }
}
