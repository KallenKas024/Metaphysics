package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BambooStalkBlock extends Block implements BonemealableBlock, net.minecraftforge.common.IPlantable {
   protected static final float SMALL_LEAVES_AABB_OFFSET = 3.0F;
   protected static final float LARGE_LEAVES_AABB_OFFSET = 5.0F;
   protected static final float COLLISION_AABB_OFFSET = 1.5F;
   protected static final VoxelShape SMALL_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
   protected static final VoxelShape LARGE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   protected static final VoxelShape COLLISION_SHAPE = Block.box(6.5D, 0.0D, 6.5D, 9.5D, 16.0D, 9.5D);
   public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
   public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
   public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
   public static final int MAX_HEIGHT = 16;
   public static final int STAGE_GROWING = 0;
   public static final int STAGE_DONE_GROWING = 1;
   public static final int AGE_THIN_BAMBOO = 0;
   public static final int AGE_THICK_BAMBOO = 1;

   public BambooStalkBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(LEAVES, BambooLeaves.NONE).setValue(STAGE, Integer.valueOf(0)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE, LEAVES, STAGE);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      VoxelShape voxelshape = pState.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      return voxelshape.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      return COLLISION_SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean isCollisionShapeFullBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return false;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      if (!fluidstate.isEmpty()) {
         return null;
      } else {
         BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().below());
         if (blockstate.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockstate.is(Blocks.BAMBOO_SAPLING)) {
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(0));
            } else if (blockstate.is(Blocks.BAMBOO)) {
               int i = blockstate.getValue(AGE) > 0 ? 1 : 0;
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(i));
            } else {
               BlockState blockstate1 = pContext.getLevel().getBlockState(pContext.getClickedPos().above());
               return blockstate1.is(Blocks.BAMBOO) ? this.defaultBlockState().setValue(AGE, blockstate1.getValue(AGE)) : Blocks.BAMBOO_SAPLING.defaultBlockState();
            }
         } else {
            return null;
         }
      }
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   /**
    * @return whether this block needs random ticking.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(STAGE) == 0;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(STAGE) == 0) {
         if (pLevel.isEmptyBlock(pPos.above()) && pLevel.getRawBrightness(pPos.above(), 0) >= 9) {
            int i = this.getHeightBelowUpToMax(pLevel, pPos) + 1;
            if (i < 16 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt(3) == 0)) {
               this.growBamboo(pState, pLevel, pPos, pRandom, i);
               net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
            }
         }

      }
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.scheduleTick(pPos, this, 1);
      }

      if (pDirection == Direction.UP && pNeighborState.is(Blocks.BAMBOO) && pNeighborState.getValue(AGE) > pState.getValue(AGE)) {
         pLevel.setBlock(pPos, pState.cycle(AGE), 2);
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      int i = this.getHeightAboveUpToMax(pLevel, pPos);
      int j = this.getHeightBelowUpToMax(pLevel, pPos);
      return i + j + 1 < 16 && pLevel.getBlockState(pPos.above(i)).getValue(STAGE) != 1;
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      int i = this.getHeightAboveUpToMax(pLevel, pPos);
      int j = this.getHeightBelowUpToMax(pLevel, pPos);
      int k = i + j + 1;
      int l = 1 + pRandom.nextInt(2);

      for(int i1 = 0; i1 < l; ++i1) {
         BlockPos blockpos = pPos.above(i);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (k >= 16 || blockstate.getValue(STAGE) == 1 || !pLevel.isEmptyBlock(blockpos.above())) {
            return;
         }

         this.growBamboo(blockstate, pLevel, blockpos, pRandom, k);
         ++i;
         ++k;
      }

   }

   /**
    * Get the hardness of this Block relative to the ability of the given player
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getDestroyProgress} whenever possible.
    * Implementing/overriding is fine.
    */
   public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
      return pPlayer.getMainHandItem().canPerformAction(net.minecraftforge.common.ToolActions.SWORD_DIG) ? 1.0F : super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
   }

   protected void growBamboo(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom, int pAge) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      BlockPos blockpos = pPos.below(2);
      BlockState blockstate1 = pLevel.getBlockState(blockpos);
      BambooLeaves bambooleaves = BambooLeaves.NONE;
      if (pAge >= 1) {
         if (blockstate.is(Blocks.BAMBOO) && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
            if (blockstate.is(Blocks.BAMBOO) && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
               bambooleaves = BambooLeaves.LARGE;
               if (blockstate1.is(Blocks.BAMBOO)) {
                  pLevel.setBlock(pPos.below(), blockstate.setValue(LEAVES, BambooLeaves.SMALL), 3);
                  pLevel.setBlock(blockpos, blockstate1.setValue(LEAVES, BambooLeaves.NONE), 3);
               }
            }
         } else {
            bambooleaves = BambooLeaves.SMALL;
         }
      }

      int i = pState.getValue(AGE) != 1 && !blockstate1.is(Blocks.BAMBOO) ? 0 : 1;
      int j = (pAge < 11 || !(pRandom.nextFloat() < 0.25F)) && pAge != 15 ? 0 : 1;
      pLevel.setBlock(pPos.above(), this.defaultBlockState().setValue(AGE, Integer.valueOf(i)).setValue(LEAVES, bambooleaves).setValue(STAGE, Integer.valueOf(j)), 3);
   }

   protected int getHeightAboveUpToMax(BlockGetter pLevel, BlockPos pPos) {
      int i;
      for(i = 0; i < 16 && pLevel.getBlockState(pPos.above(i + 1)).is(Blocks.BAMBOO); ++i) {
      }

      return i;
   }

   protected int getHeightBelowUpToMax(BlockGetter pLevel, BlockPos pPos) {
      int i;
      for(i = 0; i < 16 && pLevel.getBlockState(pPos.below(i + 1)).is(Blocks.BAMBOO); ++i) {
      }

      return i;
   }

   @Override
   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() != this) return defaultBlockState();
      return state;
   }
}
