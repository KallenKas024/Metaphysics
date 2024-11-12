package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EnderEyeItem extends Item {
   public EnderEyeItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(Blocks.END_PORTAL_FRAME) && !blockstate.getValue(EndPortalFrameBlock.HAS_EYE)) {
         if (level.isClientSide) {
            return InteractionResult.SUCCESS;
         } else {
            BlockState blockstate1 = blockstate.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(true));
            Block.pushEntitiesUp(blockstate, blockstate1, level, blockpos);
            level.setBlock(blockpos, blockstate1, 2);
            level.updateNeighbourForOutputSignal(blockpos, Blocks.END_PORTAL_FRAME);
            pContext.getItemInHand().shrink(1);
            level.levelEvent(1503, blockpos, 0);
            BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = EndPortalFrameBlock.getOrCreatePortalShape().find(level, blockpos);
            if (blockpattern$blockpatternmatch != null) {
               BlockPos blockpos1 = blockpattern$blockpatternmatch.getFrontTopLeft().offset(-3, 0, -3);

               for(int i = 0; i < 3; ++i) {
                  for(int j = 0; j < 3; ++j) {
                     level.setBlock(blockpos1.offset(i, 0, j), Blocks.END_PORTAL.defaultBlockState(), 2);
                  }
               }

               level.globalLevelEvent(1038, blockpos1.offset(1, 0, 1), 0);
            }

            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.NONE);
      if (blockhitresult.getType() == HitResult.Type.BLOCK && pLevel.getBlockState(blockhitresult.getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
         return InteractionResultHolder.pass(itemstack);
      } else {
         pPlayer.startUsingItem(pHand);
         if (pLevel instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)pLevel;
            BlockPos blockpos = serverlevel.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, pPlayer.blockPosition(), 100, false);
            if (blockpos != null) {
               EyeOfEnder eyeofender = new EyeOfEnder(pLevel, pPlayer.getX(), pPlayer.getY(0.5D), pPlayer.getZ());
               eyeofender.setItem(itemstack);
               eyeofender.signalTo(blockpos);
               pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, eyeofender.position(), GameEvent.Context.of(pPlayer));
               pLevel.addFreshEntity(eyeofender);
               if (pPlayer instanceof ServerPlayer) {
                  CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer)pPlayer, blockpos);
               }

               pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
               pLevel.levelEvent((Player)null, 1003, pPlayer.blockPosition(), 0);
               if (!pPlayer.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }

               pPlayer.awardStat(Stats.ITEM_USED.get(this));
               pPlayer.swing(pHand, true);
               return InteractionResultHolder.success(itemstack);
            }
         }

         return InteractionResultHolder.consume(itemstack);
      }
   }
}