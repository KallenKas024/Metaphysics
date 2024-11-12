package net.minecraft.world.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BeaconMenu extends AbstractContainerMenu {
   private static final int PAYMENT_SLOT = 0;
   private static final int SLOT_COUNT = 1;
   private static final int DATA_COUNT = 3;
   private static final int INV_SLOT_START = 1;
   private static final int INV_SLOT_END = 28;
   private static final int USE_ROW_SLOT_START = 28;
   private static final int USE_ROW_SLOT_END = 37;
   private final Container beacon = new SimpleContainer(1) {
      /**
       * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given
       * slot. For guis use Slot.isItemValid
       */
      public boolean canPlaceItem(int p_39066_, ItemStack p_39067_) {
         return p_39067_.is(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      /**
       * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }
   };
   private final BeaconMenu.PaymentSlot paymentSlot;
   private final ContainerLevelAccess access;
   private final ContainerData beaconData;

   public BeaconMenu(int pContainerId, Container pContainer) {
      this(pContainerId, pContainer, new SimpleContainerData(3), ContainerLevelAccess.NULL);
   }

   public BeaconMenu(int pContainerId, Container pContainer, ContainerData pBeaconData, ContainerLevelAccess pAccess) {
      super(MenuType.BEACON, pContainerId);
      checkContainerDataCount(pBeaconData, 3);
      this.beaconData = pBeaconData;
      this.access = pAccess;
      this.paymentSlot = new BeaconMenu.PaymentSlot(this.beacon, 0, 136, 110);
      this.addSlot(this.paymentSlot);
      this.addDataSlots(pBeaconData);
      int i = 36;
      int j = 137;

      for(int k = 0; k < 3; ++k) {
         for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(pContainer, l + k * 9 + 9, 36 + l * 18, 137 + k * 18));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(pContainer, i1, 36 + i1 * 18, 195));
      }

   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      if (!pPlayer.level().isClientSide) {
         ItemStack itemstack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
         if (!itemstack.isEmpty()) {
            pPlayer.drop(itemstack, false);
         }

      }
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.BEACON);
   }

   public void setData(int pId, int pData) {
      super.setData(pId, pData);
      this.broadcastChanges();
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (this.moveItemStackTo(itemstack1, 0, 1, false)) { //Forge Fix Shift Clicking in beacons with stacks larger then 1.
            return ItemStack.EMPTY;
         } else if (pIndex >= 1 && pIndex < 28) {
            if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 28 && pIndex < 37) {
            if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
      }

      return itemstack;
   }

   public int getLevels() {
      return this.beaconData.get(0);
   }

   @Nullable
   public MobEffect getPrimaryEffect() {
      return MobEffect.byId(this.beaconData.get(1));
   }

   @Nullable
   public MobEffect getSecondaryEffect() {
      return MobEffect.byId(this.beaconData.get(2));
   }

   public void updateEffects(Optional<MobEffect> pPrimaryEffect, Optional<MobEffect> pSecondaryEffect) {
      if (this.paymentSlot.hasItem()) {
         this.beaconData.set(1, pPrimaryEffect.map(MobEffect::getId).orElse(-1));
         this.beaconData.set(2, pSecondaryEffect.map(MobEffect::getId).orElse(-1));
         this.paymentSlot.remove(1);
         this.access.execute(Level::blockEntityChanged);
      }

   }

   public boolean hasPayment() {
      return !this.beacon.getItem(0).isEmpty();
   }

   class PaymentSlot extends Slot {
      public PaymentSlot(Container pContainer, int pContainerIndex, int pXPosition, int pYPosition) {
         super(pContainer, pContainerIndex, pXPosition, pYPosition);
      }

      /**
       * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
       */
      public boolean mayPlace(ItemStack pStack) {
         return pStack.is(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      /**
       * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
       * case of armor slots)
       */
      public int getMaxStackSize() {
         return 1;
      }
   }
}
