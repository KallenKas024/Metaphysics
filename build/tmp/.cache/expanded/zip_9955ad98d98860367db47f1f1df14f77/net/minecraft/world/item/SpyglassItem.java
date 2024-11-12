package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpyglassItem extends Item {
   public static final int USE_DURATION = 1200;
   public static final float ZOOM_FOV_MODIFIER = 0.1F;

   public SpyglassItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 1200;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.SPYGLASS;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      pPlayer.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
      this.stopUsing(pLivingEntity);
      return pStack;
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
      this.stopUsing(pLivingEntity);
   }

   private void stopUsing(LivingEntity pUser) {
      pUser.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
   }
}