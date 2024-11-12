package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class BowItem extends ProjectileWeaponItem implements Vanishable {
   public static final int MAX_DRAW_DURATION = 20;
   public static final int DEFAULT_RANGE = 15;

   public BowItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
      if (pEntityLiving instanceof Player player) {
         boolean flag = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pStack) > 0;
         ItemStack itemstack = player.getProjectile(pStack);

         int i = this.getUseDuration(pStack) - pTimeLeft;
         i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(pStack, pLevel, player, i, !itemstack.isEmpty() || flag);
         if (i < 0) return;

         if (!itemstack.isEmpty() || flag) {
            if (itemstack.isEmpty()) {
               itemstack = new ItemStack(Items.ARROW);
            }

            float f = getPowerForTime(i);
            if (!((double)f < 0.1D)) {
               boolean flag1 = player.getAbilities().instabuild || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, pStack, player));
               if (!pLevel.isClientSide) {
                  ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                  AbstractArrow abstractarrow = arrowitem.createArrow(pLevel, itemstack, player);
                  abstractarrow = customArrow(abstractarrow);
                  abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 1.0F);
                  if (f == 1.0F) {
                     abstractarrow.setCritArrow(true);
                  }

                  int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, pStack);
                  if (j > 0) {
                     abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double)j * 0.5D + 0.5D);
                  }

                  int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, pStack);
                  if (k > 0) {
                     abstractarrow.setKnockback(k);
                  }

                  if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, pStack) > 0) {
                     abstractarrow.setSecondsOnFire(100);
                  }

                  pStack.hurtAndBreak(1, player, (p_289501_) -> {
                     p_289501_.broadcastBreakEvent(player.getUsedItemHand());
                  });
                  if (flag1 || player.getAbilities().instabuild && (itemstack.is(Items.SPECTRAL_ARROW) || itemstack.is(Items.TIPPED_ARROW))) {
                     abstractarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                  }

                  pLevel.addFreshEntity(abstractarrow);
               }

               pLevel.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               if (!flag1 && !player.getAbilities().instabuild) {
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     player.getInventory().removeItem(itemstack);
                  }
               }

               player.awardStat(Stats.ITEM_USED.get(this));
            }
         }
      }
   }

   /**
    * Gets the velocity of the arrow entity from the bow's charge
    */
   public static float getPowerForTime(int pCharge) {
      float f = (float)pCharge / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }

      return f;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 72000;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.BOW;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      boolean flag = !pPlayer.getProjectile(itemstack).isEmpty();

      InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, pLevel, pPlayer, pHand, flag);
      if (ret != null) return ret;

      if (!pPlayer.getAbilities().instabuild && !flag) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         pPlayer.startUsingItem(pHand);
         return InteractionResultHolder.consume(itemstack);
      }
   }

   /**
    * Get the predicate to match ammunition when searching the player's inventory, not their main/offhand
    */
   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }

   public AbstractArrow customArrow(AbstractArrow arrow) {
      return arrow;
   }

   public int getDefaultProjectileRange() {
      return 15;
   }
}
