package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmBlock extends Block {
   public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
   public static final int MAX_MOISTURE = 7;

   public FarmBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(0)));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.UP && !pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.scheduleTick(pCurrentPos, this, 1);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.above());
      return !blockstate.isSolid() || blockstate.getBlock() instanceof FenceGateBlock || blockstate.getBlock() instanceof MovingPistonBlock;
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return !this.defaultBlockState().canSurvive(pContext.getLevel(), pContext.getClickedPos()) ? Blocks.DIRT.defaultBlockState() : super.getStateForPlacement(pContext);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pState.canSurvive(pLevel, pPos)) {
         turnToDirt((Entity)null, pState, pLevel, pPos);
      }

   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      int i = pState.getValue(MOISTURE);
      if (!isNearWater(pLevel, pPos) && !pLevel.isRainingAt(pPos.above())) {
         if (i > 0) {
            pLevel.setBlock(pPos, pState.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
         } else if (!shouldMaintainFarmland(pLevel, pPos)) {
            turnToDirt((Entity)null, pState, pLevel, pPos);
         }
      } else if (i < 7) {
         pLevel.setBlock(pPos, pState.setValue(MOISTURE, Integer.valueOf(7)), 2);
      }

   }

   public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
      if (!pLevel.isClientSide && net.minecraftforge.common.ForgeHooks.onFarmlandTrample(pLevel, pPos, Blocks.DIRT.defaultBlockState(), pFallDistance, pEntity)) { // Forge: Move logic to Entity#canTrample
         turnToDirt(pEntity, pState, pLevel, pPos);
      }

      super.fallOn(pLevel, pState, pPos, pEntity, pFallDistance);
   }

   public static void turnToDirt(@Nullable Entity pEntity, BlockState pState, Level pLevel, BlockPos pPos) {
      BlockState blockstate = pushEntitiesUp(pState, Blocks.DIRT.defaultBlockState(), pLevel, pPos);
      pLevel.setBlockAndUpdate(pPos, blockstate);
      pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pEntity, blockstate));
   }

   private static boolean shouldMaintainFarmland(BlockGetter pLevel, BlockPos pPos) {
      BlockState plant = pLevel.getBlockState(pPos.above());
      BlockState state = pLevel.getBlockState(pPos);
      return plant.getBlock() instanceof net.minecraftforge.common.IPlantable && state.canSustainPlant(pLevel, pPos, Direction.UP, (net.minecraftforge.common.IPlantable)plant.getBlock());
   }

   private static boolean isNearWater(LevelReader pLevel, BlockPos pPos) {
      BlockState state = pLevel.getBlockState(pPos);
      for(BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-4, 0, -4), pPos.offset(4, 1, 4))) {
         if (state.canBeHydrated(pLevel, pPos, pLevel.getFluidState(blockpos), blockpos)) {
            return true;
         }
      }

      return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(pLevel, pPos);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(MOISTURE);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}
