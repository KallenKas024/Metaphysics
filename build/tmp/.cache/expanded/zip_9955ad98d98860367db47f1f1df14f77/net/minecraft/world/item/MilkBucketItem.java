package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MilkBucketItem extends Item {
   private static final int DRINK_DURATION = 32;

   public MilkBucketItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
      if (!pLevel.isClientSide) pEntityLiving.curePotionEffects(pStack); // FORGE - move up so stack.shrink does not turn stack into air
      if (pEntityLiving instanceof ServerPlayer serverplayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, pStack);
         serverplayer.awardStat(Stats.ITEM_USED.get(this));
      }

      if (pEntityLiving instanceof Player && !((Player)pEntityLiving).getAbilities().instabuild) {
         pStack.shrink(1);
      }

      return pStack.isEmpty() ? new ItemStack(Items.BUCKET) : pStack;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 32;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.DRINK;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
   }

   @Override
   public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag nbt) {
      return new net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper(stack);
   }
}
