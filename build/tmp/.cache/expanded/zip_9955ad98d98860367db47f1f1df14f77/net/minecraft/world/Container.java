package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container extends Clearable {
   int LARGE_MAX_STACK_SIZE = 64;
   int DEFAULT_DISTANCE_LIMIT = 8;

   /**
    * Returns the number of slots in the inventory.
    */
   int getContainerSize();

   boolean isEmpty();

   /**
    * Returns the stack in the given slot.
    */
   ItemStack getItem(int pSlot);

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   ItemStack removeItem(int pSlot, int pAmount);

   /**
    * Removes a stack from the given slot and returns it.
    */
   ItemStack removeItemNoUpdate(int pSlot);

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   void setItem(int pSlot, ItemStack pStack);

   /**
    * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
    */
   default int getMaxStackSize() {
      return 64;
   }

   /**
    * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   void setChanged();

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   boolean stillValid(Player pPlayer);

   default void startOpen(Player pPlayer) {
   }

   default void stopOpen(Player pPlayer) {
   }

   /**
    * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
    * For guis use Slot.isItemValid
    */
   default boolean canPlaceItem(int pIndex, ItemStack pStack) {
      return true;
   }

   /**
    * {@return {@code true} if the given stack can be extracted into the target inventory}
    * @param pTarget the container into which the item should be extracted
    * @param pIndex the slot from which to extract the item
    * @param pStack the item to extract
    */
   default boolean canTakeItem(Container pTarget, int pIndex, ItemStack pStack) {
      return true;
   }

   /**
    * Returns the total amount of the specified item in this inventory. This method does not check for nbt.
    */
   default int countItem(Item pItem) {
      int i = 0;

      for(int j = 0; j < this.getContainerSize(); ++j) {
         ItemStack itemstack = this.getItem(j);
         if (itemstack.getItem().equals(pItem)) {
            i += itemstack.getCount();
         }
      }

      return i;
   }

   /**
    * Returns {@code true} if any item from the passed set exists in this inventory.
    */
   default boolean hasAnyOf(Set<Item> pSet) {
      return this.hasAnyMatching((p_216873_) -> {
         return !p_216873_.isEmpty() && pSet.contains(p_216873_.getItem());
      });
   }

   default boolean hasAnyMatching(Predicate<ItemStack> pPredicate) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (pPredicate.test(itemstack)) {
            return true;
         }
      }

      return false;
   }

   static boolean stillValidBlockEntity(BlockEntity pBlockEntity, Player pPlayer) {
      return stillValidBlockEntity(pBlockEntity, pPlayer, 8);
   }

   static boolean stillValidBlockEntity(BlockEntity pBlockEntity, Player pPlayer, int pMaxDistance) {
      Level level = pBlockEntity.getLevel();
      BlockPos blockpos = pBlockEntity.getBlockPos();
      if (level == null) {
         return false;
      } else if (level.getBlockEntity(blockpos) != pBlockEntity) {
         return false;
      } else {
         return pPlayer.distanceToSqr((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) <= (double)(pMaxDistance * pMaxDistance);
      }
   }
}