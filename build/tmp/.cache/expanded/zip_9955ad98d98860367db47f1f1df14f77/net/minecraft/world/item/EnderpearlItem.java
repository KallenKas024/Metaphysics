package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;

public class EnderpearlItem extends Item {
   public EnderpearlItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
      pPlayer.getCooldowns().addCooldown(this, 20);
      if (!pLevel.isClientSide) {
         ThrownEnderpearl thrownenderpearl = new ThrownEnderpearl(pLevel, pPlayer);
         thrownenderpearl.setItem(itemstack);
         thrownenderpearl.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.5F, 1.0F);
         pLevel.addFreshEntity(thrownenderpearl);
      }

      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      if (!pPlayer.getAbilities().instabuild) {
         itemstack.shrink(1);
      }

      return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
   }
}