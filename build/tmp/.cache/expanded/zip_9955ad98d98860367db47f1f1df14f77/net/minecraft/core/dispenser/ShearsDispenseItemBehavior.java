package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
   /**
    * Dispense the specified stack, play the dispense sound, and spawn particles.
    */
   protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
      ServerLevel serverlevel = pSource.getLevel();
      if (!serverlevel.isClientSide()) {
         BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
         this.setSuccess(tryShearBeehive(serverlevel, blockpos) || tryShearLivingEntity(serverlevel, blockpos));
         if (this.isSuccess() && pStack.hurt(1, serverlevel.getRandom(), (ServerPlayer)null)) {
            pStack.setCount(0);
         }
      }

      return pStack;
   }

   private static boolean tryShearBeehive(ServerLevel pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (blockstate.is(BlockTags.BEEHIVES, (p_202454_) -> {
         return p_202454_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_202454_.getBlock() instanceof BeehiveBlock;
      })) {
         int i = blockstate.getValue(BeehiveBlock.HONEY_LEVEL);
         if (i >= 5) {
            pLevel.playSound((Player)null, pPos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            BeehiveBlock.dropHoneycomb(pLevel, pPos);
            ((BeehiveBlock)blockstate.getBlock()).releaseBeesAndResetHoneyLevel(pLevel, blockstate, pPos, (Player)null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
            pLevel.gameEvent((Entity)null, GameEvent.SHEAR, pPos);
            return true;
         }
      }

      return false;
   }

   private static boolean tryShearLivingEntity(ServerLevel pLevel, BlockPos pPos) {
      for(LivingEntity livingentity : pLevel.getEntitiesOfClass(LivingEntity.class, new AABB(pPos), EntitySelector.NO_SPECTATORS)) {
         if (livingentity instanceof Shearable shearable) {
            if (shearable.readyForShearing()) {
               shearable.shear(SoundSource.BLOCKS);
               pLevel.gameEvent((Entity)null, GameEvent.SHEAR, pPos);
               return true;
            }
         }
      }

      return false;
   }
}