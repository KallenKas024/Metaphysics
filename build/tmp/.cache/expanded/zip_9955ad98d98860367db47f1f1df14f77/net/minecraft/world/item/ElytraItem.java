package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ElytraItem extends Item implements Equipable {
   public ElytraItem(Item.Properties pProperties) {
      super(pProperties);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
   }

   public static boolean isFlyEnabled(ItemStack pElytraStack) {
      return pElytraStack.getDamageValue() < pElytraStack.getMaxDamage() - 1;
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return pRepair.is(Items.PHANTOM_MEMBRANE);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return this.swapWithEquipmentSlot(this, pLevel, pPlayer, pHand);
   }

   @Override
   public boolean canElytraFly(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
      return ElytraItem.isFlyEnabled(stack);
   }

   @Override
   public boolean elytraFlightTick(ItemStack stack, net.minecraft.world.entity.LivingEntity entity, int flightTicks) {
      if (!entity.level().isClientSide) {
         int nextFlightTick = flightTicks + 1;
         if (nextFlightTick % 10 == 0) {
            if (nextFlightTick % 20 == 0) {
               stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(net.minecraft.world.entity.EquipmentSlot.CHEST));
            }
            entity.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
         }
      }
      return true;
   }

   public SoundEvent getEquipSound() {
      return SoundEvents.ARMOR_EQUIP_ELYTRA;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.CHEST;
   }
}
