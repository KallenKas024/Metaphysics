package net.minecraft.world.entity;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
   SlotAccess NULL = new SlotAccess() {
      public ItemStack get() {
         return ItemStack.EMPTY;
      }

      public boolean set(ItemStack p_147314_) {
         return false;
      }
   };

   static SlotAccess forContainer(final Container pInventory, final int pSlot, final Predicate<ItemStack> pStackFilter) {
      return new SlotAccess() {
         public ItemStack get() {
            return pInventory.getItem(pSlot);
         }

         public boolean set(ItemStack p_147324_) {
            if (!pStackFilter.test(p_147324_)) {
               return false;
            } else {
               pInventory.setItem(pSlot, p_147324_);
               return true;
            }
         }
      };
   }

   static SlotAccess forContainer(Container pInventory, int pSlot) {
      return forContainer(pInventory, pSlot, (p_147310_) -> {
         return true;
      });
   }

   static SlotAccess forEquipmentSlot(final LivingEntity pEntity, final EquipmentSlot pSlot, final Predicate<ItemStack> pStackFilter) {
      return new SlotAccess() {
         public ItemStack get() {
            return pEntity.getItemBySlot(pSlot);
         }

         public boolean set(ItemStack p_147334_) {
            if (!pStackFilter.test(p_147334_)) {
               return false;
            } else {
               pEntity.setItemSlot(pSlot, p_147334_);
               return true;
            }
         }
      };
   }

   static SlotAccess forEquipmentSlot(LivingEntity pEntity, EquipmentSlot pSlot) {
      return forEquipmentSlot(pEntity, pSlot, (p_147308_) -> {
         return true;
      });
   }

   ItemStack get();

   boolean set(ItemStack pCarried);
}