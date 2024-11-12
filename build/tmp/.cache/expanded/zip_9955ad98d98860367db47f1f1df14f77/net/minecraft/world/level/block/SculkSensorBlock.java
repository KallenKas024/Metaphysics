package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final int ACTIVE_TICKS = 30;
   public static final int COOLDOWN_TICKS = 10;
   public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private static final float[] RESONANCE_PITCH_BEND = Util.make(new float[16], (p_277301_) -> {
      int[] aint = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

      for(int i = 0; i < 16; ++i) {
         p_277301_[i] = NoteBlock.getPitchFromNote(aint[i]);
      }

   });

   public SculkSensorBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (getPhase(pState) != SculkSensorPhase.ACTIVE) {
         if (getPhase(pState) == SculkSensorPhase.COOLDOWN) {
            pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            if (!pState.getValue(WATERLOGGED)) {
               pLevel.playSound((Player)null, pPos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, pLevel.random.nextFloat() * 0.2F + 0.8F);
            }
         }

      } else {
         deactivate(pLevel, pPos, pState);
      }
   }

   public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
      if (!pLevel.isClientSide() && canActivate(pState) && pEntity.getType() != EntityType.WARDEN) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity sculksensorblockentity = (SculkSensorBlockEntity)blockentity;
            if (pLevel instanceof ServerLevel) {
               ServerLevel serverlevel = (ServerLevel)pLevel;
               if (sculksensorblockentity.getVibrationUser().canReceiveVibration(serverlevel, pPos, GameEvent.STEP, GameEvent.Context.of(pState))) {
                  sculksensorblockentity.getListener().forceScheduleVibration(serverlevel, GameEvent.STEP, GameEvent.Context.of(pEntity), pEntity.position());
               }
            }
         }
      }

      super.stepOn(pLevel, pPos, pState, pEntity);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
      if (!pLevel.isClientSide() && !pState.is(pOldState.getBlock())) {
         if (pState.getValue(POWER) > 0 && !pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
            pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(0)), 18);
         }

      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
      if (!pState.is(pNewState.getBlock())) {
         if (getPhase(pState) == SculkSensorPhase.ACTIVE) {
            updateNeighbours(pLevel, pPos, pState);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
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

   private static void updateNeighbours(Level pLevel, BlockPos pPos, BlockState pState) {
      Block block = pState.getBlock();
      pLevel.updateNeighborsAt(pPos, block);
      pLevel.updateNeighborsAt(pPos.below(), block);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SculkSensorBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return !pLevel.isClientSide ? createTickerHelper(pBlockEntityType, BlockEntityType.SCULK_SENSOR, (p_281130_, p_281131_, p_281132_, p_281133_) -> {
         VibrationSystem.Ticker.tick(p_281130_, p_281133_.getVibrationData(), p_281133_.getVibrationUser());
      }) : null;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   /**
    * Returns whether this block is capable of emitting redstone signals.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#isSignalSource}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   /**
    * Returns the signal this block emits in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getSignal}
    * whenever possible. Implementing/overriding is fine.
    */
   public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
      return pState.getValue(POWER);
   }

   /**
    * Returns the direct signal this block emits in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getDirectSignal}
    * whenever possible. Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
      return pDirection == Direction.UP ? pState.getSignal(pLevel, pPos, pDirection) : 0;
   }

   public static SculkSensorPhase getPhase(BlockState pState) {
      return pState.getValue(PHASE);
   }

   public static boolean canActivate(BlockState pState) {
      return getPhase(pState) == SculkSensorPhase.INACTIVE;
   }

   public static void deactivate(Level pLevel, BlockPos pPos, BlockState pState) {
      pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
      pLevel.scheduleTick(pPos, pState.getBlock(), 10);
      updateNeighbours(pLevel, pPos, pState);
   }

   @VisibleForTesting
   public int getActiveTicks() {
      return 30;
   }

   public void activate(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, BlockState pState, int pPower, int pFrequency) {
      pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(pPower)), 3);
      pLevel.scheduleTick(pPos, pState.getBlock(), this.getActiveTicks());
      updateNeighbours(pLevel, pPos, pState);
      tryResonateVibration(pEntity, pLevel, pPos, pFrequency);
      pLevel.gameEvent(pEntity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, pPos);
      if (!pState.getValue(WATERLOGGED)) {
         pLevel.playSound((Player)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, pLevel.random.nextFloat() * 0.2F + 0.8F);
      }

   }

   public static void tryResonateVibration(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, int pFrequency) {
      for(Direction direction : Direction.values()) {
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(BlockTags.VIBRATION_RESONATORS)) {
            pLevel.gameEvent(VibrationSystem.getResonanceEventByFrequency(pFrequency), blockpos, GameEvent.Context.of(pEntity, blockstate));
            float f = RESONANCE_PITCH_BEND[pFrequency];
            pLevel.playSound((Player)null, blockpos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, f);
         }
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (getPhase(pState) == SculkSensorPhase.ACTIVE) {
         Direction direction = Direction.getRandom(pRandom);
         if (direction != Direction.UP && direction != Direction.DOWN) {
            double d0 = (double)pPos.getX() + 0.5D + (direction.getStepX() == 0 ? 0.5D - pRandom.nextDouble() : (double)direction.getStepX() * 0.6D);
            double d1 = (double)pPos.getY() + 0.25D;
            double d2 = (double)pPos.getZ() + 0.5D + (direction.getStepZ() == 0 ? 0.5D - pRandom.nextDouble() : (double)direction.getStepZ() * 0.6D);
            double d3 = (double)pRandom.nextFloat() * 0.04D;
            pLevel.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0D, d3, 0.0D);
         }
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(PHASE, POWER, WATERLOGGED);
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof SculkSensorBlockEntity sculksensorblockentity) {
         return getPhase(pState) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
      } else {
         return 0;
      }
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack, pDropExperience);

   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
      return silkTouchLevel == 0 ? 5 : 0;
   }
}
