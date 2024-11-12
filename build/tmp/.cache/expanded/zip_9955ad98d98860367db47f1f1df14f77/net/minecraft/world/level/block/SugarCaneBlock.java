package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarCaneBlock extends Block implements net.minecraftforge.common.IPlantable {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

   public SugarCaneBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.isEmptyBlock(pPos.above())) {
         int i;
         for(i = 1; pLevel.getBlockState(pPos.below(i)).is(this); ++i) {
         }

         if (i < 3) {
            int j = pState.getValue(AGE);
            if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, true)) {
            if (j == 15) {
               pLevel.setBlockAndUpdate(pPos.above(), this.defaultBlockState());
               net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos.above(), this.defaultBlockState());
               pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(0)), 4);
            } else {
               pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(j + 1)), 4);
            }
            }
         }
      }

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
      BlockState soil = pLevel.getBlockState(pPos.below());
      if (soil.canSustainPlant(pLevel, pPos.below(), Direction.UP, this)) return true;
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      if (blockstate.is(this)) {
         return true;
      } else {
         if (blockstate.is(BlockTags.DIRT) || blockstate.is(BlockTags.SAND)) {
            BlockPos blockpos = pPos.below();

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               BlockState blockstate1 = pLevel.getBlockState(blockpos.relative(direction));
               FluidState fluidstate = pLevel.getFluidState(blockpos.relative(direction));
               if (pState.canBeHydrated(pLevel, pPos, fluidstate, blockpos.relative(direction)) || blockstate1.is(Blocks.FROSTED_ICE)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   @Override
   public net.minecraftforge.common.PlantType getPlantType(BlockGetter world, BlockPos pos) {
       return net.minecraftforge.common.PlantType.BEACH;
   }

   @Override
   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      return defaultBlockState();
   }
}
