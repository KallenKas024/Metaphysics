package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class LeadItem extends Item {
   public LeadItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(BlockTags.FENCES)) {
         Player player = pContext.getPlayer();
         if (!level.isClientSide && player != null) {
            bindPlayerMobs(player, level, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static InteractionResult bindPlayerMobs(Player pPlayer, Level pLevel, BlockPos pPos) {
      LeashFenceKnotEntity leashfenceknotentity = null;
      boolean flag = false;
      double d0 = 7.0D;
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();

      for(Mob mob : pLevel.getEntitiesOfClass(Mob.class, new AABB((double)i - 7.0D, (double)j - 7.0D, (double)k - 7.0D, (double)i + 7.0D, (double)j + 7.0D, (double)k + 7.0D))) {
         if (mob.getLeashHolder() == pPlayer) {
            if (leashfenceknotentity == null) {
               leashfenceknotentity = LeashFenceKnotEntity.getOrCreateKnot(pLevel, pPos);
               leashfenceknotentity.playPlacementSound();
            }

            mob.setLeashedTo(leashfenceknotentity, true);
            flag = true;
         }
      }

      if (flag) {
         pLevel.gameEvent(GameEvent.BLOCK_ATTACH, pPos, GameEvent.Context.of(pPlayer));
      }

      return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
   }
}