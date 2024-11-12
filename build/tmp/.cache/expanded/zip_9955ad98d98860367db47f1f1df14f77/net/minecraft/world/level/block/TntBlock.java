package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
   public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

   public TntBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
   }

   public void onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable net.minecraft.core.Direction face, @Nullable LivingEntity igniter) {
      explode(world, pos, igniter);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         if (pLevel.hasNeighborSignal(pPos)) {
            onCaughtFire(pState, pLevel, pPos, null, null);
            pLevel.removeBlock(pPos, false);
         }

      }
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (pLevel.hasNeighborSignal(pPos)) {
         onCaughtFire(pState, pLevel, pPos, null, null);
         pLevel.removeBlock(pPos, false);
      }

   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide() && !pPlayer.isCreative() && pState.getValue(UNSTABLE)) {
         onCaughtFire(pState, pLevel, pPos, null, null);
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   /**
    * Called when this Block is destroyed by an Explosion
    */
   public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
      if (!pLevel.isClientSide) {
         PrimedTnt primedtnt = new PrimedTnt(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, pExplosion.getIndirectSourceEntity());
         int i = primedtnt.getFuse();
         primedtnt.setFuse((short)(pLevel.random.nextInt(i / 4) + i / 8));
         pLevel.addFreshEntity(primedtnt);
      }
   }

   @Deprecated //Forge: Prefer using IForgeBlock#catchFire
   public static void explode(Level pLevel, BlockPos pPos) {
      explode(pLevel, pPos, (LivingEntity)null);
   }

   @Deprecated //Forge: Prefer using IForgeBlock#catchFire
   private static void explode(Level pLevel, BlockPos pPos, @Nullable LivingEntity pEntity) {
      if (!pLevel.isClientSide) {
         PrimedTnt primedtnt = new PrimedTnt(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, pEntity);
         pLevel.addFreshEntity(primedtnt);
         pLevel.playSound((Player)null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         pLevel.gameEvent(pEntity, GameEvent.PRIME_FUSE, pPos);
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!itemstack.is(Items.FLINT_AND_STEEL) && !itemstack.is(Items.FIRE_CHARGE)) {
         return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
      } else {
         onCaughtFire(pState, pLevel, pPos, pHit.getDirection(), pPlayer);
         pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
         Item item = itemstack.getItem();
         if (!pPlayer.isCreative()) {
            if (itemstack.is(Items.FLINT_AND_STEEL)) {
               itemstack.hurtAndBreak(1, pPlayer, (p_57425_) -> {
                  p_57425_.broadcastBreakEvent(pHand);
               });
            } else {
               itemstack.shrink(1);
            }
         }

         pPlayer.awardStat(Stats.ITEM_USED.get(item));
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      }
   }

   public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
      if (!pLevel.isClientSide) {
         BlockPos blockpos = pHit.getBlockPos();
         Entity entity = pProjectile.getOwner();
         if (pProjectile.isOnFire() && pProjectile.mayInteract(pLevel, blockpos)) {
            onCaughtFire(pState, pLevel, blockpos, null, entity instanceof LivingEntity ? (LivingEntity)entity : null);
            pLevel.removeBlock(blockpos, false);
         }
      }

   }

   /**
    * @return whether this block should drop its drops when destroyed by the given explosion
    */
   public boolean dropFromExplosion(Explosion pExplosion) {
      return false;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(UNSTABLE);
   }
}
