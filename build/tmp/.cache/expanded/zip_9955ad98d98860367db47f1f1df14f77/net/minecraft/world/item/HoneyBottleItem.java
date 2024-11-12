package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HoneyBottleItem extends Item {
   private static final int DRINK_DURATION = 40;

   public HoneyBottleItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
      super.finishUsingItem(pStack, pLevel, pEntityLiving);
      if (pEntityLiving instanceof ServerPlayer serverplayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, pStack);
         serverplayer.awardStat(Stats.ITEM_USED.get(this));
      }

      if (!pLevel.isClientSide) {
         pEntityLiving.removeEffect(MobEffects.POISON);
      }

      if (pStack.isEmpty()) {
         return new ItemStack(Items.GLASS_BOTTLE);
      } else {
         if (pEntityLiving instanceof Player && !((Player)pEntityLiving).getAbilities().instabuild) {
            ItemStack itemstack = new ItemStack(Items.GLASS_BOTTLE);
            Player player = (Player)pEntityLiving;
            if (!player.getInventory().add(itemstack)) {
               player.drop(itemstack, false);
            }
         }

         return pStack;
      }
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 40;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.DRINK;
   }

   public SoundEvent getDrinkingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   public SoundEvent getEatingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
   }
}