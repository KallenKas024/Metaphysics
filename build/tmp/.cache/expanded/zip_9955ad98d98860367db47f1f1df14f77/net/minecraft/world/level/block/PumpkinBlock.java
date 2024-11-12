package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class PumpkinBlock extends StemGrownBlock {
   public PumpkinBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SHEARS_CARVE)) {
         if (!pLevel.isClientSide) {
            Direction direction = pHit.getDirection();
            Direction direction1 = direction.getAxis() == Direction.Axis.Y ? pPlayer.getDirection().getOpposite() : direction;
            pLevel.playSound((Player)null, pPos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.setBlock(pPos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction1), 11);
            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D + (double)direction1.getStepX() * 0.65D, (double)pPos.getY() + 0.1D, (double)pPos.getZ() + 0.5D + (double)direction1.getStepZ() * 0.65D, new ItemStack(Items.PUMPKIN_SEEDS, 4));
            itementity.setDeltaMovement(0.05D * (double)direction1.getStepX() + pLevel.random.nextDouble() * 0.02D, 0.05D, 0.05D * (double)direction1.getStepZ() + pLevel.random.nextDouble() * 0.02D);
            pLevel.addFreshEntity(itementity);
            itemstack.hurtAndBreak(1, pPlayer, (p_55287_) -> {
               p_55287_.broadcastBreakEvent(pHand);
            });
            pLevel.gameEvent(pPlayer, GameEvent.SHEAR, pPos);
            pPlayer.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
         }

         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
      }
   }

   public StemBlock getStem() {
      return (StemBlock)Blocks.PUMPKIN_STEM;
   }

   public AttachedStemBlock getAttachedStem() {
      return (AttachedStemBlock)Blocks.ATTACHED_PUMPKIN_STEM;
   }
}
