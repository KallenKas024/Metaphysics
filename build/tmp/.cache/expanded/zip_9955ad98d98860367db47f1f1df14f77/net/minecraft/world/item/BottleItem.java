package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BottleItem extends Item {
   public BottleItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      List<AreaEffectCloud> list = pLevel.getEntitiesOfClass(AreaEffectCloud.class, pPlayer.getBoundingBox().inflate(2.0D), (p_289499_) -> {
         return p_289499_ != null && p_289499_.isAlive() && p_289499_.getOwner() instanceof EnderDragon;
      });
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!list.isEmpty()) {
         AreaEffectCloud areaeffectcloud = list.get(0);
         areaeffectcloud.setRadius(areaeffectcloud.getRadius() - 0.5F);
         pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
         pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, pPlayer.position());
         if (pPlayer instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)pPlayer;
            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(serverplayer, itemstack, areaeffectcloud);
         }

         return InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemstack, pPlayer, new ItemStack(Items.DRAGON_BREATH)), pLevel.isClientSide());
      } else {
         BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
         if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
         } else {
            if (blockhitresult.getType() == HitResult.Type.BLOCK) {
               BlockPos blockpos = blockhitresult.getBlockPos();
               if (!pLevel.mayInteract(pPlayer, blockpos)) {
                  return InteractionResultHolder.pass(itemstack);
               }

               if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
                  pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                  pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, blockpos);
                  return InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemstack, pPlayer, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)), pLevel.isClientSide());
               }
            }

            return InteractionResultHolder.pass(itemstack);
         }
      }
   }

   protected ItemStack turnBottleIntoItem(ItemStack pBottleStack, Player pPlayer, ItemStack pFilledBottleStack) {
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return ItemUtils.createFilledResult(pBottleStack, pPlayer, pFilledBottleStack);
   }
}