package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceBlock extends CrossCollisionBlock {
   private final VoxelShape[] occlusionByIndex;

   public FenceBlock(BlockBehaviour.Properties p_53302_) {
      super(2.0F, 2.0F, 16.0F, 16.0F, 24.0F, p_53302_);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
      this.occlusionByIndex = this.makeShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);
   }

   public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return this.occlusionByIndex[this.getAABBIndex(pState)];
   }

   public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
      return this.getShape(pState, pReader, pPos, pContext);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public boolean connectsTo(BlockState pState, boolean pIsSideSolid, Direction pDirection) {
      Block block = pState.getBlock();
      boolean flag = this.isSameFence(pState);
      boolean flag1 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(pState, pDirection);
      return !isExceptionForConnection(pState) && pIsSideSolid || flag || flag1;
   }

   private boolean isSameFence(BlockState pState) {
      return pState.is(BlockTags.FENCES) && pState.is(BlockTags.WOODEN_FENCES) == this.defaultBlockState().is(BlockTags.WOODEN_FENCES);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         return itemstack.is(Items.LEAD) ? InteractionResult.SUCCESS : InteractionResult.PASS;
      } else {
         return LeadItem.bindPlayerMobs(pPlayer, pLevel, pPos);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockGetter blockgetter = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      BlockPos blockpos1 = blockpos.north();
      BlockPos blockpos2 = blockpos.east();
      BlockPos blockpos3 = blockpos.south();
      BlockPos blockpos4 = blockpos.west();
      BlockState blockstate = blockgetter.getBlockState(blockpos1);
      BlockState blockstate1 = blockgetter.getBlockState(blockpos2);
      BlockState blockstate2 = blockgetter.getBlockState(blockpos3);
      BlockState blockstate3 = blockgetter.getBlockState(blockpos4);
      return super.getStateForPlacement(pContext).setValue(NORTH, Boolean.valueOf(this.connectsTo(blockstate, blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.SOUTH), Direction.SOUTH))).setValue(EAST, Boolean.valueOf(this.connectsTo(blockstate1, blockstate1.isFaceSturdy(blockgetter, blockpos2, Direction.WEST), Direction.WEST))).setValue(SOUTH, Boolean.valueOf(this.connectsTo(blockstate2, blockstate2.isFaceSturdy(blockgetter, blockpos3, Direction.NORTH), Direction.NORTH))).setValue(WEST, Boolean.valueOf(this.connectsTo(blockstate3, blockstate3.isFaceSturdy(blockgetter, blockpos4, Direction.EAST), Direction.EAST))).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return pFacing.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(this.connectsTo(pFacingState, pFacingState.isFaceSturdy(pLevel, pFacingPos, pFacing.getOpposite()), pFacing.getOpposite()))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
   }
}