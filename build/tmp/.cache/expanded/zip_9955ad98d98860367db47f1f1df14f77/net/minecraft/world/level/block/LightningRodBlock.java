package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightningRodBlock extends RodBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int ACTIVATION_TICKS = 8;
   public static final int RANGE = 128;
   private static final int SPARK_CYCLE = 200;

   public LightningRodBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      boolean flag = fluidstate.getType() == Fluids.WATER;
      return this.defaultBlockState().setValue(FACING, pContext.getClickedFace()).setValue(WATERLOGGED, Boolean.valueOf(flag));
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
      return pState.getValue(POWERED) ? 15 : 0;
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
      return pState.getValue(POWERED) && pState.getValue(FACING) == pDirection ? 15 : 0;
   }

   public void onLightningStrike(BlockState pState, Level pLevel, BlockPos pPos) {
      pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 3);
      this.updateNeighbours(pState, pLevel, pPos);
      pLevel.scheduleTick(pPos, this, 8);
      pLevel.levelEvent(3002, pPos, pState.getValue(FACING).getAxis().ordinal());
   }

   private void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
      pLevel.updateNeighborsAt(pPos.relative(pState.getValue(FACING).getOpposite()), this);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)), 3);
      this.updateNeighbours(pState, pLevel, pPos);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.isThundering() && (long)pLevel.random.nextInt(200) <= pLevel.getGameTime() % 200L && pPos.getY() == pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, pPos.getX(), pPos.getZ()) - 1) {
         ParticleUtils.spawnParticlesAlongAxis(pState.getValue(FACING).getAxis(), pLevel, pPos, 0.125D, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2));
      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
      if (!pState.is(pNewState.getBlock())) {
         if (pState.getValue(POWERED)) {
            this.updateNeighbours(pState, pLevel, pPos);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
      }
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
      if (!pState.is(pOldState.getBlock())) {
         if (pState.getValue(POWERED) && !pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)), 18);
         }

      }
   }

   public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
      if (pLevel.isThundering() && pProjectile instanceof ThrownTrident && ((ThrownTrident)pProjectile).isChanneling()) {
         BlockPos blockpos = pHit.getBlockPos();
         if (pLevel.canSeeSky(blockpos)) {
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(pLevel);
            if (lightningbolt != null) {
               lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos.above()));
               Entity entity = pProjectile.getOwner();
               lightningbolt.setCause(entity instanceof ServerPlayer ? (ServerPlayer)entity : null);
               pLevel.addFreshEntity(lightningbolt);
            }

            pLevel.playSound((Player)null, blockpos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, POWERED, WATERLOGGED);
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
}