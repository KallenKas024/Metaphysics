package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item {
   private final EntityType<T> canInteractWith;
   private final int consumeItemDamage;

   public FoodOnAStickItem(Item.Properties pProperties, EntityType<T> pCanInteractWith, int pConsumeItemDamage) {
      super(pProperties);
      this.canInteractWith = pCanInteractWith;
      this.consumeItemDamage = pConsumeItemDamage;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (pLevel.isClientSide) {
         return InteractionResultHolder.pass(itemstack);
      } else {
         Entity entity = pPlayer.getControlledVehicle();
         if (pPlayer.isPassenger() && entity instanceof ItemSteerable) {
            ItemSteerable itemsteerable = (ItemSteerable)entity;
            if (entity.getType() == this.canInteractWith && itemsteerable.boost()) {
               itemstack.hurtAndBreak(this.consumeItemDamage, pPlayer, (p_41312_) -> {
                  p_41312_.broadcastBreakEvent(pHand);
               });
               if (itemstack.isEmpty()) {
                  ItemStack itemstack1 = new ItemStack(Items.FISHING_ROD);
                  itemstack1.setTag(itemstack.getTag());
                  return InteractionResultHolder.success(itemstack1);
               }

               return InteractionResultHolder.success(itemstack);
            }
         }

         pPlayer.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResultHolder.pass(itemstack);
      }
   }
}