package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RespawnAnchorBlock extends Block {
   public static final int MIN_CHARGES = 0;
   public static final int MAX_CHARGES = 4;
   public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
   private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
   private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = (new ImmutableList.Builder<Vec3i>()).addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator()).add(new Vec3i(0, 1, 0)).build();

   public RespawnAnchorBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, Integer.valueOf(0)));
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (pHand == InteractionHand.MAIN_HAND && !isRespawnFuel(itemstack) && isRespawnFuel(pPlayer.getItemInHand(InteractionHand.OFF_HAND))) {
         return InteractionResult.PASS;
      } else if (isRespawnFuel(itemstack) && canBeCharged(pState)) {
         charge(pPlayer, pLevel, pPos, pState);
         if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else if (pState.getValue(CHARGE) == 0) {
         return InteractionResult.PASS;
      } else if (!canSetSpawn(pLevel)) {
         if (!pLevel.isClientSide) {
            this.explode(pState, pLevel, pPos);
         }

         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         if (!pLevel.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)pPlayer;
            if (serverplayer.getRespawnDimension() != pLevel.dimension() || !pPos.equals(serverplayer.getRespawnPosition())) {
               serverplayer.setRespawnPosition(pLevel.dimension(), pPos, 0.0F, false, true);
               pLevel.playSound((Player)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.CONSUME;
      }
   }

   private static boolean isRespawnFuel(ItemStack pStack) {
      return pStack.is(Items.GLOWSTONE);
   }

   private static boolean canBeCharged(BlockState pState) {
      return pState.getValue(CHARGE) < 4;
   }

   private static boolean isWaterThatWouldFlow(BlockPos pPos, Level pLevel) {
      FluidState fluidstate = pLevel.getFluidState(pPos);
      if (!fluidstate.is(FluidTags.WATER)) {
         return false;
      } else if (fluidstate.isSource()) {
         return true;
      } else {
         float f = (float)fluidstate.getAmount();
         if (f < 2.0F) {
            return false;
         } else {
            FluidState fluidstate1 = pLevel.getFluidState(pPos.below());
            return !fluidstate1.is(FluidTags.WATER);
         }
      }
   }

   private void explode(BlockState pState, Level pLevel, final BlockPos pPos2) {
      pLevel.removeBlock(pPos2, false);
      boolean flag = Direction.Plane.HORIZONTAL.stream().map(pPos2::relative).anyMatch((p_55854_) -> {
         return isWaterThatWouldFlow(p_55854_, pLevel);
      });
      final boolean flag1 = flag || pLevel.getFluidState(pPos2.above()).is(FluidTags.WATER);
      ExplosionDamageCalculator explosiondamagecalculator = new ExplosionDamageCalculator() {
         public Optional<Float> getBlockExplosionResistance(Explosion p_55904_, BlockGetter p_55905_, BlockPos p_55906_, BlockState p_55907_, FluidState p_55908_) {
            return p_55906_.equals(pPos2) && flag1 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(p_55904_, p_55905_, p_55906_, p_55907_, p_55908_);
         }
      };
      Vec3 vec3 = pPos2.getCenter();
      pLevel.explode((Entity)null, pLevel.damageSources().badRespawnPointExplosion(vec3), explosiondamagecalculator, vec3, 5.0F, true, Level.ExplosionInteraction.BLOCK);
   }

   public static boolean canSetSpawn(Level pLevel) {
      return pLevel.dimensionType().respawnAnchorWorks();
   }

   public static void charge(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, BlockState pState) {
      BlockState blockstate = pState.setValue(CHARGE, Integer.valueOf(pState.getValue(CHARGE) + 1));
      pLevel.setBlock(pPos, blockstate, 3);
      pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pEntity, blockstate));
      pLevel.playSound((Player)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(CHARGE) != 0) {
         if (pRandom.nextInt(100) == 0) {
            pLevel.playSound((Player)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         double d0 = (double)pPos.getX() + 0.5D + (0.5D - pRandom.nextDouble());
         double d1 = (double)pPos.getY() + 1.0D;
         double d2 = (double)pPos.getZ() + 0.5D + (0.5D - pRandom.nextDouble());
         double d3 = (double)pRandom.nextFloat() * 0.04D;
         pLevel.addParticle(ParticleTypes.REVERSE_PORTAL, d0, d1, d2, 0.0D, d3, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(CHARGE);
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   public static int getScaledChargeLevel(BlockState pState, int pScale) {
      return Mth.floor((float)(pState.getValue(CHARGE) - 0) / 4.0F * (float)pScale);
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return getScaledChargeLevel(pBlockState, 15);
   }

   public static Optional<Vec3> findStandUpPosition(EntityType<?> pEntityType, CollisionGetter pLevel, BlockPos pPos) {
      Optional<Vec3> optional = findStandUpPosition(pEntityType, pLevel, pPos, true);
      return optional.isPresent() ? optional : findStandUpPosition(pEntityType, pLevel, pPos, false);
   }

   private static Optional<Vec3> findStandUpPosition(EntityType<?> pEntityType, CollisionGetter pLevel, BlockPos pPos, boolean pSimulate) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Vec3i vec3i : RESPAWN_OFFSETS) {
         blockpos$mutableblockpos.set(pPos).move(vec3i);
         Vec3 vec3 = DismountHelper.findSafeDismountLocation(pEntityType, pLevel, blockpos$mutableblockpos, pSimulate);
         if (vec3 != null) {
            return Optional.of(vec3);
         }
      }

      return Optional.empty();
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}