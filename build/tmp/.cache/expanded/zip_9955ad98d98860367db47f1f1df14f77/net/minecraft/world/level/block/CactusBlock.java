package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusBlock extends Block implements net.minecraftforge.common.IPlantable {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   public static final int MAX_AGE = 15;
   protected static final int AABB_OFFSET = 1;
   protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
   protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public CactusBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pLevel.isAreaLoaded(pPos, 1)) return; // Forge: prevent growing cactus from loading unloaded chunks with block update
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockPos blockpos = pPos.above();
      if (pLevel.isEmptyBlock(blockpos)) {
         int i;
         for(i = 1; pLevel.getBlockState(pPos.below(i)).is(this); ++i) {
         }

         if (i < 3) {
            int j = pState.getValue(AGE);
            if(net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, blockpos, pState, true)) {
            if (j == 15) {
               pLevel.setBlockAndUpdate(blockpos, this.defaultBlockState());
               BlockState blockstate = pState.setValue(AGE, 0);
               pLevel.setBlock(pPos, blockstate, 4);
               pLevel.neighborChanged(blockstate, blockpos, this, pPos, false);
            } else {
               pLevel.setBlock(pPos, pState.setValue(AGE, j + 1), 4);
            }
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
            }
         }
      }
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return OUTLINE_SHAPE;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (!pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.scheduleTick(pCurrentPos, this, 1);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
         if (blockstate.isSolid() || pLevel.getFluidState(pPos.relative(direction)).is(FluidTags.LAVA)) {
            return false;
         }
      }

      BlockState blockstate1 = pLevel.getBlockState(pPos.below());
      return blockstate1.canSustainPlant(pLevel, pPos, Direction.UP, this) && !pLevel.getBlockState(pPos.above()).liquid();
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      pEntity.hurt(pLevel.damageSources().cactus(), 1.0F);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   @Override
   public net.minecraftforge.common.PlantType getPlantType(BlockGetter world, BlockPos pos) {
      return net.minecraftforge.common.PlantType.DESERT;
   }

   @Override
   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      return defaultBlockState();
   }
}
