package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CandleBlock extends AbstractCandleBlock implements SimpleWaterloggedBlock {
   public static final int MIN_CANDLES = 1;
   public static final int MAX_CANDLES = 4;
   public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;
   public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final ToIntFunction<BlockState> LIGHT_EMISSION = (p_152848_) -> {
      return p_152848_.getValue(LIT) ? 3 * p_152848_.getValue(CANDLES) : 0;
   };
   private static final Int2ObjectMap<List<Vec3>> PARTICLE_OFFSETS = Util.make(() -> {
      Int2ObjectMap<List<Vec3>> int2objectmap = new Int2ObjectOpenHashMap<>();
      int2objectmap.defaultReturnValue(ImmutableList.of());
      int2objectmap.put(1, ImmutableList.of(new Vec3(0.5D, 0.5D, 0.5D)));
      int2objectmap.put(2, ImmutableList.of(new Vec3(0.375D, 0.44D, 0.5D), new Vec3(0.625D, 0.5D, 0.44D)));
      int2objectmap.put(3, ImmutableList.of(new Vec3(0.5D, 0.313D, 0.625D), new Vec3(0.375D, 0.44D, 0.5D), new Vec3(0.56D, 0.5D, 0.44D)));
      int2objectmap.put(4, ImmutableList.of(new Vec3(0.44D, 0.313D, 0.56D), new Vec3(0.625D, 0.44D, 0.56D), new Vec3(0.375D, 0.44D, 0.375D), new Vec3(0.56D, 0.5D, 0.375D)));
      return Int2ObjectMaps.unmodifiable(int2objectmap);
   });
   private static final VoxelShape ONE_AABB = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D);
   private static final VoxelShape TWO_AABB = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 6.0D, 9.0D);
   private static final VoxelShape THREE_AABB = Block.box(5.0D, 0.0D, 6.0D, 10.0D, 6.0D, 11.0D);
   private static final VoxelShape FOUR_AABB = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 10.0D);

   public CandleBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(CANDLES, Integer.valueOf(1)).setValue(LIT, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pPlayer.getAbilities().mayBuild && pPlayer.getItemInHand(pHand).isEmpty() && pState.getValue(LIT)) {
         extinguish(pPlayer, pState, pLevel, pPos);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      return !pUseContext.isSecondaryUseActive() && pUseContext.getItemInHand().getItem() == this.asItem() && pState.getValue(CANDLES) < 4 ? true : super.canBeReplaced(pState, pUseContext);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
      if (blockstate.is(this)) {
         return blockstate.cycle(CANDLES);
      } else {
         FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
         boolean flag = fluidstate.getType() == Fluids.WATER;
         return super.getStateForPlacement(pContext).setValue(WATERLOGGED, Boolean.valueOf(flag));
      }
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch (pState.getValue(CANDLES)) {
         case 1:
         default:
            return ONE_AABB;
         case 2:
            return TWO_AABB;
         case 3:
            return THREE_AABB;
         case 4:
            return FOUR_AABB;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(CANDLES, LIT, WATERLOGGED);
   }

   public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
      if (!pState.getValue(WATERLOGGED) && pFluidState.getType() == Fluids.WATER) {
         BlockState blockstate = pState.setValue(WATERLOGGED, Boolean.valueOf(true));
         if (pState.getValue(LIT)) {
            extinguish((Player)null, blockstate, pLevel, pPos);
         } else {
            pLevel.setBlock(pPos, blockstate, 3);
         }

         pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
         return true;
      } else {
         return false;
      }
   }

   public static boolean canLight(BlockState pState) {
      return pState.is(BlockTags.CANDLES, (p_152810_) -> {
         return p_152810_.hasProperty(LIT) && p_152810_.hasProperty(WATERLOGGED);
      }) && !pState.getValue(LIT) && !pState.getValue(WATERLOGGED);
   }

   protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
      return PARTICLE_OFFSETS.get(pState.getValue(CANDLES).intValue());
   }

   protected boolean canBeLit(BlockState pState) {
      return !pState.getValue(WATERLOGGED) && super.canBeLit(pState);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return Block.canSupportCenter(pLevel, pPos.below(), Direction.UP);
   }
}