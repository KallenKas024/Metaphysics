package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;

public class EggItem extends Item {
   public EggItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
      if (!pLevel.isClientSide) {
         ThrownEgg thrownegg = new ThrownEgg(pLevel, pPlayer);
         thrownegg.setItem(itemstack);
         thrownegg.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.5F, 1.0F);
         pLevel.addFreshEntity(thrownegg);
      }

      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      if (!pPlayer.getAbilities().instabuild) {
         itemstack.shrink(1);
      }

      return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
   }
}