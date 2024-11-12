package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class Inventory implements Container, Nameable {
   public static final int POP_TIME_DURATION = 5;
   public static final int INVENTORY_SIZE = 36;
   private static final int SELECTION_SIZE = 9;
   public static final int SLOT_OFFHAND = 40;
   public static final int NOT_FOUND_INDEX = -1;
   public static final int[] ALL_ARMOR_SLOTS = new int[]{0, 1, 2, 3};
   public static final int[] HELMET_SLOT_ONLY = new int[]{3};
   public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
   public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
   public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
   private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(this.items, this.armor, this.offhand);
   public int selected;
   public final Player player;
   private int timesChanged;

   public Inventory(Player pPlayer) {
      this.player = pPlayer;
   }

   /**
    * Returns the item stack currently held by the player.
    */
   public ItemStack getSelected() {
      return isHotbarSlot(this.selected) ? this.items.get(this.selected) : ItemStack.EMPTY;
   }

   /**
    * Get the size of the player hotbar inventory
    */
   public static int getSelectionSize() {
      return 9;
   }

   private boolean hasRemainingSpaceForItem(ItemStack pDestination, ItemStack pOrigin) {
      return !pDestination.isEmpty() && ItemStack.isSameItemSameTags(pDestination, pOrigin) && pDestination.isStackable() && pDestination.getCount() < pDestination.getMaxStackSize() && pDestination.getCount() < this.getMaxStackSize();
   }

   /**
    * Returns the first item stack that is empty.
    */
   public int getFreeSlot() {
      for(int i = 0; i < this.items.size(); ++i) {
         if (this.items.get(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public void setPickedItem(ItemStack pStack) {
      int i = this.findSlotMatchingItem(pStack);
      if (isHotbarSlot(i)) {
         this.selected = i;
      } else {
         if (i == -1) {
            this.selected = this.getSuitableHotbarSlot();
            if (!this.items.get(this.selected).isEmpty()) {
               int j = this.getFreeSlot();
               if (j != -1) {
                  this.items.set(j, this.items.get(this.selected));
               }
            }

            this.items.set(this.selected, pStack);
         } else {
            this.pickSlot(i);
         }

      }
   }

   public void pickSlot(int pIndex) {
      this.selected = this.getSuitableHotbarSlot();
      ItemStack itemstack = this.items.get(this.selected);
      this.items.set(this.selected, this.items.get(pIndex));
      this.items.set(pIndex, itemstack);
   }

   public static boolean isHotbarSlot(int pIndex) {
      return pIndex >= 0 && pIndex < 9;
   }

   /**
    * Finds the stack or an equivalent one in the main inventory
    */
   public int findSlotMatchingItem(ItemStack pStack) {
      for(int i = 0; i < this.items.size(); ++i) {
         if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(pStack, this.items.get(i))) {
            return i;
         }
      }

      return -1;
   }

   public int findSlotMatchingUnusedItem(ItemStack pStack) {
      for(int i = 0; i < this.items.size(); ++i) {
         ItemStack itemstack = this.items.get(i);
         if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(pStack, this.items.get(i)) && !this.items.get(i).isDamaged() && !itemstack.isEnchanted() && !itemstack.hasCustomHoverName()) {
            return i;
         }
      }

      return -1;
   }

   public int getSuitableHotbarSlot() {
      for(int i = 0; i < 9; ++i) {
         int j = (this.selected + i) % 9;
         if (this.items.get(j).isEmpty()) {
            return j;
         }
      }

      for(int k = 0; k < 9; ++k) {
         int l = (this.selected + k) % 9;
         if (!this.items.get(l).isNotReplaceableByPickAction(this.player, l)) {
            return l;
         }
      }

      return this.selected;
   }

   /**
    * Change the selected item in the hotbar after a mouse scroll. Select the slot to the left if {@code direction} is
    * positive, or to the right if negative.
    */
   public void swapPaint(double pDirection) {
      int i = (int)Math.signum(pDirection);

      for(this.selected -= i; this.selected < 0; this.selected += 9) {
      }

      while(this.selected >= 9) {
         this.selected -= 9;
      }

   }

   public int clearOrCountMatchingItems(Predicate<ItemStack> pStackPredicate, int pMaxCount, Container pInventory) {
      int i = 0;
      boolean flag = pMaxCount == 0;
      i += ContainerHelper.clearOrCountMatchingItems(this, pStackPredicate, pMaxCount - i, flag);
      i += ContainerHelper.clearOrCountMatchingItems(pInventory, pStackPredicate, pMaxCount - i, flag);
      ItemStack itemstack = this.player.containerMenu.getCarried();
      i += ContainerHelper.clearOrCountMatchingItems(itemstack, pStackPredicate, pMaxCount - i, flag);
      if (itemstack.isEmpty()) {
         this.player.containerMenu.setCarried(ItemStack.EMPTY);
      }

      return i;
   }

   /**
    * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of left
    * over items.
    */
   private int addResource(ItemStack pStack) {
      int i = this.getSlotWithRemainingSpace(pStack);
      if (i == -1) {
         i = this.getFreeSlot();
      }

      return i == -1 ? pStack.getCount() : this.addResource(i, pStack);
   }

   private int addResource(int pSlot, ItemStack pStack) {
      Item item = pStack.getItem();
      int i = pStack.getCount();
      ItemStack itemstack = this.getItem(pSlot);
      if (itemstack.isEmpty()) {
         itemstack = pStack.copy(); // Forge: Replace Item clone above to preserve item capabilities when picking the item up.
         itemstack.setCount(0);
         if (pStack.hasTag()) {
            itemstack.setTag(pStack.getTag().copy());
         }

         this.setItem(pSlot, itemstack);
      }

      int j = i;
      if (i > itemstack.getMaxStackSize() - itemstack.getCount()) {
         j = itemstack.getMaxStackSize() - itemstack.getCount();
      }

      if (j > this.getMaxStackSize() - itemstack.getCount()) {
         j = this.getMaxStackSize() - itemstack.getCount();
      }

      if (j == 0) {
         return i;
      } else {
         i -= j;
         itemstack.grow(j);
         itemstack.setPopTime(5);
         return i;
      }
   }

   /**
    * Stores a stack in the player's inventory. It first tries to place it in the selected slot in the player's hotbar,
    * then the offhand slot, then any available/empty slot in the player's inventory.
    */
   public int getSlotWithRemainingSpace(ItemStack pStack) {
      if (this.hasRemainingSpaceForItem(this.getItem(this.selected), pStack)) {
         return this.selected;
      } else if (this.hasRemainingSpaceForItem(this.getItem(40), pStack)) {
         return 40;
      } else {
         for(int i = 0; i < this.items.size(); ++i) {
            if (this.hasRemainingSpaceForItem(this.items.get(i), pStack)) {
               return i;
            }
         }

         return -1;
      }
   }

   /**
    * Ticks every item in inventory. Used for animations. Is called on client and server.
    */
   public void tick() {
      int idx = 0;
      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         for(int i = 0; i < nonnulllist.size(); ++i) {
            if (!nonnulllist.get(i).isEmpty()) {
               nonnulllist.get(i).onInventoryTick(this.player.level(), this.player, idx, this.selected);
            }
            idx++;
         }
      }

   }

   /**
    * Adds the stack to the first empty slot in the player's inventory. Returns {@code false} if it's not possible to
    * place the entire stack in the inventory.
    */
   public boolean add(ItemStack pStack) {
      return this.add(-1, pStack);
   }

   /**
    * Adds the stack to the specified slot in the player's inventory. Returns {@code false} if it's not possible to
    * place the entire stack in the inventory.
    */
   public boolean add(int pSlot, ItemStack pStack) {
      if (pStack.isEmpty()) {
         return false;
      } else {
         try {
            if (pStack.isDamaged()) {
               if (pSlot == -1) {
                  pSlot = this.getFreeSlot();
               }

               if (pSlot >= 0) {
                  this.items.set(pSlot, pStack.copyAndClear());
                  this.items.get(pSlot).setPopTime(5);
                  return true;
               } else if (this.player.getAbilities().instabuild) {
                  pStack.setCount(0);
                  return true;
               } else {
                  return false;
               }
            } else {
               int i;
               do {
                  i = pStack.getCount();
                  if (pSlot == -1) {
                     pStack.setCount(this.addResource(pStack));
                  } else {
                     pStack.setCount(this.addResource(pSlot, pStack));
                  }
               } while(!pStack.isEmpty() && pStack.getCount() < i);

               if (pStack.getCount() == i && this.player.getAbilities().instabuild) {
                  pStack.setCount(0);
                  return true;
               } else {
                  return pStack.getCount() < i;
               }
            }
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
            crashreportcategory.setDetail("Registry Name", () -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(pStack.getItem())));
            crashreportcategory.setDetail("Item Class", () -> pStack.getItem().getClass().getName());
            crashreportcategory.setDetail("Item ID", Item.getId(pStack.getItem()));
            crashreportcategory.setDetail("Item data", pStack.getDamageValue());
            crashreportcategory.setDetail("Item name", () -> {
               return pStack.getHoverName().getString();
            });
            throw new ReportedException(crashreport);
         }
      }
   }

   public void placeItemBackInInventory(ItemStack pStack) {
      this.placeItemBackInInventory(pStack, true);
   }

   public void placeItemBackInInventory(ItemStack pStack, boolean pSendPacket) {
      while(true) {
         if (!pStack.isEmpty()) {
            int i = this.getSlotWithRemainingSpace(pStack);
            if (i == -1) {
               i = this.getFreeSlot();
            }

            if (i != -1) {
               int j = pStack.getMaxStackSize() - this.getItem(i).getCount();
               if (this.add(i, pStack.split(j)) && pSendPacket && this.player instanceof ServerPlayer) {
                  ((ServerPlayer)this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, i, this.getItem(i)));
               }
               continue;
            }

            this.player.drop(pStack, false);
         }

         return;
      }
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      List<ItemStack> list = null;

      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         if (pIndex < nonnulllist.size()) {
            list = nonnulllist;
            break;
         }

         pIndex -= nonnulllist.size();
      }

      return list != null && !list.get(pIndex).isEmpty() ? ContainerHelper.removeItem(list, pIndex, pCount) : ItemStack.EMPTY;
   }

   public void removeItem(ItemStack pStack) {
      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         for(int i = 0; i < nonnulllist.size(); ++i) {
            if (nonnulllist.get(i) == pStack) {
               nonnulllist.set(i, ItemStack.EMPTY);
               break;
            }
         }
      }

   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      NonNullList<ItemStack> nonnulllist = null;

      for(NonNullList<ItemStack> nonnulllist1 : this.compartments) {
         if (pIndex < nonnulllist1.size()) {
            nonnulllist = nonnulllist1;
            break;
         }

         pIndex -= nonnulllist1.size();
      }

      if (nonnulllist != null && !nonnulllist.get(pIndex).isEmpty()) {
         ItemStack itemstack = nonnulllist.get(pIndex);
         nonnulllist.set(pIndex, ItemStack.EMPTY);
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      NonNullList<ItemStack> nonnulllist = null;

      for(NonNullList<ItemStack> nonnulllist1 : this.compartments) {
         if (pIndex < nonnulllist1.size()) {
            nonnulllist = nonnulllist1;
            break;
         }

         pIndex -= nonnulllist1.size();
      }

      if (nonnulllist != null) {
         nonnulllist.set(pIndex, pStack);
      }

   }

   public float getDestroySpeed(BlockState pState) {
      return this.items.get(this.selected).getDestroySpeed(pState);
   }

   /**
    * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
    * for crafting).
    */
   public ListTag save(ListTag pListTag) {
      for(int i = 0; i < this.items.size(); ++i) {
         if (!this.items.get(i).isEmpty()) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putByte("Slot", (byte)i);
            this.items.get(i).save(compoundtag);
            pListTag.add(compoundtag);
         }
      }

      for(int j = 0; j < this.armor.size(); ++j) {
         if (!this.armor.get(j).isEmpty()) {
            CompoundTag compoundtag1 = new CompoundTag();
            compoundtag1.putByte("Slot", (byte)(j + 100));
            this.armor.get(j).save(compoundtag1);
            pListTag.add(compoundtag1);
         }
      }

      for(int k = 0; k < this.offhand.size(); ++k) {
         if (!this.offhand.get(k).isEmpty()) {
            CompoundTag compoundtag2 = new CompoundTag();
            compoundtag2.putByte("Slot", (byte)(k + 150));
            this.offhand.get(k).save(compoundtag2);
            pListTag.add(compoundtag2);
         }
      }

      return pListTag;
   }

   /**
    * Reads from the given tag list and fills the slots in the inventory with the correct items.
    */
   public void load(ListTag pListTag) {
      this.items.clear();
      this.armor.clear();
      this.offhand.clear();

      for(int i = 0; i < pListTag.size(); ++i) {
         CompoundTag compoundtag = pListTag.getCompound(i);
         int j = compoundtag.getByte("Slot") & 255;
         ItemStack itemstack = ItemStack.of(compoundtag);
         if (!itemstack.isEmpty()) {
            if (j >= 0 && j < this.items.size()) {
               this.items.set(j, itemstack);
            } else if (j >= 100 && j < this.armor.size() + 100) {
               this.armor.set(j - 100, itemstack);
            } else if (j >= 150 && j < this.offhand.size() + 150) {
               this.offhand.set(j - 150, itemstack);
            }
         }
      }

   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.items.size() + this.armor.size() + this.offhand.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      for(ItemStack itemstack1 : this.armor) {
         if (!itemstack1.isEmpty()) {
            return false;
         }
      }

      for(ItemStack itemstack2 : this.offhand) {
         if (!itemstack2.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      List<ItemStack> list = null;

      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         if (pIndex < nonnulllist.size()) {
            list = nonnulllist;
            break;
         }

         pIndex -= nonnulllist.size();
      }

      return list == null ? ItemStack.EMPTY : list.get(pIndex);
   }

   public Component getName() {
      return Component.translatable("container.inventory");
   }

   /**
    * @return a player armor item (as an {@code ItemStack}) contained in specified armor slot
    */
   public ItemStack getArmor(int pSlot) {
      return this.armor.get(pSlot);
   }

   public void hurtArmor(DamageSource pSource, float pDamage, int[] pArmorPieces) {
      if (!(pDamage <= 0.0F)) {
         pDamage /= 4.0F;
         if (pDamage < 1.0F) {
            pDamage = 1.0F;
         }

         for(int i : pArmorPieces) {
            ItemStack itemstack = this.armor.get(i);
            if ((!pSource.is(DamageTypeTags.IS_FIRE) || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
               itemstack.hurtAndBreak((int)pDamage, this.player, (p_35997_) -> {
                  p_35997_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i));
               });
            }
         }

      }
   }

   /**
    * Drop all armor and main inventory items.
    */
   public void dropAll() {
      for(List<ItemStack> list : this.compartments) {
         for(int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            if (!itemstack.isEmpty()) {
               this.player.drop(itemstack, true, false);
               list.set(i, ItemStack.EMPTY);
            }
         }
      }

   }

   /**
    * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
      ++this.timesChanged;
   }

   public int getTimesChanged() {
      return this.timesChanged;
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      if (this.player.isRemoved()) {
         return false;
      } else {
         return !(pPlayer.distanceToSqr(this.player) > 64.0D);
      }
   }

   /**
    * Returns {@code true} if the specified {@link net.minecraft.world.item.ItemStack} exists in the inventory.
    */
   public boolean contains(ItemStack pStack) {
      for(List<ItemStack> list : this.compartments) {
         for(ItemStack itemstack : list) {
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemstack, pStack)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean contains(TagKey<Item> pTag) {
      for(List<ItemStack> list : this.compartments) {
         for(ItemStack itemstack : list) {
            if (!itemstack.isEmpty() && itemstack.is(pTag)) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Copy the ItemStack contents from another InventoryPlayer instance
    */
   public void replaceWith(Inventory pPlayerInventory) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         this.setItem(i, pPlayerInventory.getItem(i));
      }

      this.selected = pPlayerInventory.selected;
   }

   public void clearContent() {
      for(List<ItemStack> list : this.compartments) {
         list.clear();
      }

   }

   public void fillStackedContents(StackedContents pStackedContent) {
      for(ItemStack itemstack : this.items) {
         pStackedContent.accountSimpleStack(itemstack);
      }

   }

   /**
    * 
    * @param pRemoveStack Whether to remove the entire stack of items. If {@code false}, removes a single item.
    */
   public ItemStack removeFromSelected(boolean pRemoveStack) {
      ItemStack itemstack = this.getSelected();
      return itemstack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, pRemoveStack ? itemstack.getCount() : 1);
   }
}
