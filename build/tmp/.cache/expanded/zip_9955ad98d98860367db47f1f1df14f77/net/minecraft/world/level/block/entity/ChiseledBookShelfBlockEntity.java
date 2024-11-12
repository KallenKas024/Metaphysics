package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements Container {
   public static final int MAX_BOOKS_IN_STORAGE = 6;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
   private int lastInteractedSlot = -1;

   public ChiseledBookShelfBlockEntity(BlockPos pPos, BlockState pState) {
      super(BlockEntityType.CHISELED_BOOKSHELF, pPos, pState);
   }

   private void updateState(int pSlot) {
      if (pSlot >= 0 && pSlot < 6) {
         this.lastInteractedSlot = pSlot;
         BlockState blockstate = this.getBlockState();

         for(int i = 0; i < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++i) {
            boolean flag = !this.getItem(i).isEmpty();
            BooleanProperty booleanproperty = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(i);
            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(flag));
         }

         Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockstate, 3);
      } else {
         LOGGER.error("Expected slot 0-5, got {}", (int)pSlot);
      }
   }

   public void load(CompoundTag pTag) {
      this.items.clear();
      ContainerHelper.loadAllItems(pTag, this.items);
      this.lastInteractedSlot = pTag.getInt("last_interacted_slot");
   }

   protected void saveAdditional(CompoundTag pTag) {
      ContainerHelper.saveAllItems(pTag, this.items, true);
      pTag.putInt("last_interacted_slot", this.lastInteractedSlot);
   }

   public int count() {
      return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
   }

   public void clearContent() {
      this.items.clear();
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return 6;
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pSlot) {
      return this.items.get(pSlot);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pSlot, int pAmount) {
      ItemStack itemstack = Objects.requireNonNullElse(this.items.get(pSlot), ItemStack.EMPTY);
      this.items.set(pSlot, ItemStack.EMPTY);
      if (!itemstack.isEmpty()) {
         this.updateState(pSlot);
      }

      return itemstack;
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pSlot) {
      return this.removeItem(pSlot, 1);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pSlot, ItemStack pStack) {
      if (pStack.is(ItemTags.BOOKSHELF_BOOKS)) {
         this.items.set(pSlot, pStack);
         this.updateState(pSlot);
      }

   }

   /**
    * {@return {@code true} if the given stack can be extracted into the target inventory}
    * @param pTarget the container into which the item should be extracted
    * @param pIndex the slot from which to extract the item
    * @param pStack the item to extract
    */
   public boolean canTakeItem(Container pTarget, int pIndex, ItemStack pStack) {
      return pTarget.hasAnyMatching((p_281577_) -> {
         if (p_281577_.isEmpty()) {
            return true;
         } else {
            return ItemStack.isSameItemSameTags(pStack, p_281577_) && p_281577_.getCount() + pStack.getCount() <= Math.min(p_281577_.getMaxStackSize(), pTarget.getMaxStackSize());
         }
      });
   }

   /**
    * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
    */
   public int getMaxStackSize() {
      return 1;
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      return Container.stillValidBlockEntity(this, pPlayer);
   }

   /**
    * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
    * For guis use Slot.isItemValid
    */
   public boolean canPlaceItem(int pIndex, ItemStack pStack) {
      return pStack.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(pIndex).isEmpty();
   }

   public int getLastInteractedSlot() {
      return this.lastInteractedSlot;
   }

   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::createUnSidedHandler);
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.wrapper.InvWrapper(this);
   }

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
      if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && !this.remove)
         return itemHandler.cast();
      return super.getCapability(cap, side);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      itemHandler.invalidate();
   }

   @Override
   public void reviveCaps() {
      super.reviveCaps();
      itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::createUnSidedHandler);
   }
}
