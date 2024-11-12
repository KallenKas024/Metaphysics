package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntity extends BlockEntity implements Clearable, MenuProvider {
   public static final int DATA_PAGE = 0;
   public static final int NUM_DATA = 1;
   public static final int SLOT_BOOK = 0;
   public static final int NUM_SLOTS = 1;
   private final Container bookAccess = new Container() {
      /**
       * Returns the number of slots in the inventory.
       */
      public int getContainerSize() {
         return 1;
      }

      public boolean isEmpty() {
         return LecternBlockEntity.this.book.isEmpty();
      }

      /**
       * Returns the stack in the given slot.
       */
      public ItemStack getItem(int p_59580_) {
         return p_59580_ == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
      }

      /**
       * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
       */
      public ItemStack removeItem(int p_59582_, int p_59583_) {
         if (p_59582_ == 0) {
            ItemStack itemstack = LecternBlockEntity.this.book.split(p_59583_);
            if (LecternBlockEntity.this.book.isEmpty()) {
               LecternBlockEntity.this.onBookItemRemove();
            }

            return itemstack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      /**
       * Removes a stack from the given slot and returns it.
       */
      public ItemStack removeItemNoUpdate(int p_59590_) {
         if (p_59590_ == 0) {
            ItemStack itemstack = LecternBlockEntity.this.book;
            LecternBlockEntity.this.book = ItemStack.EMPTY;
            LecternBlockEntity.this.onBookItemRemove();
            return itemstack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      /**
       * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
       */
      public void setItem(int p_59585_, ItemStack p_59586_) {
      }

      /**
       * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }

      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         LecternBlockEntity.this.setChanged();
      }

      /**
       * Don't rename this method to canInteractWith due to conflicts with Container
       */
      public boolean stillValid(Player p_59588_) {
         return Container.stillValidBlockEntity(LecternBlockEntity.this, p_59588_) && LecternBlockEntity.this.hasBook();
      }

      /**
       * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given
       * slot. For guis use Slot.isItemValid
       */
      public boolean canPlaceItem(int p_59592_, ItemStack p_59593_) {
         return false;
      }

      public void clearContent() {
      }
   };
   private final ContainerData dataAccess = new ContainerData() {
      public int get(int p_59600_) {
         return p_59600_ == 0 ? LecternBlockEntity.this.page : 0;
      }

      public void set(int p_59602_, int p_59603_) {
         if (p_59602_ == 0) {
            LecternBlockEntity.this.setPage(p_59603_);
         }

      }

      public int getCount() {
         return 1;
      }
   };
   ItemStack book = ItemStack.EMPTY;
   int page;
   private int pageCount;

   public LecternBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.LECTERN, pPos, pBlockState);
   }

   public ItemStack getBook() {
      return this.book;
   }

   /**
    * @return whether the ItemStack in this lectern is a book or written book
    */
   public boolean hasBook() {
      return this.book.is(Items.WRITABLE_BOOK) || this.book.is(Items.WRITTEN_BOOK);
   }

   /**
    * Sets the ItemStack in this lectern. Note that this does not update the block state, use {@link
    * net.minecraft.world.level.block.LecternBlock#tryPlaceBook} for that.
    */
   public void setBook(ItemStack pStack) {
      this.setBook(pStack, (Player)null);
   }

   void onBookItemRemove() {
      this.page = 0;
      this.pageCount = 0;
      LecternBlock.resetBookState((Entity)null, this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
   }

   /**
    * Sets the ItemStack in this lectern. Note that this does not update the block state, use {@link
    * net.minecraft.world.level.block.LecternBlock#tryPlaceBook} for that.
    * @param pPlayer the player used for resolving the components within the book
    */
   public void setBook(ItemStack pStack, @Nullable Player pPlayer) {
      this.book = this.resolveBook(pStack, pPlayer);
      this.page = 0;
      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.setChanged();
   }

   void setPage(int pPage) {
      int i = Mth.clamp(pPage, 0, this.pageCount - 1);
      if (i != this.page) {
         this.page = i;
         this.setChanged();
         LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public int getPage() {
      return this.page;
   }

   public int getRedstoneSignal() {
      float f = this.pageCount > 1 ? (float)this.getPage() / ((float)this.pageCount - 1.0F) : 1.0F;
      return Mth.floor(f * 14.0F) + (this.hasBook() ? 1 : 0);
   }

   /**
    * Resolves the contents of the passed ItemStack, if it is a book
    */
   private ItemStack resolveBook(ItemStack pStack, @Nullable Player pPlayer) {
      if (this.level instanceof ServerLevel && pStack.is(Items.WRITTEN_BOOK)) {
         WrittenBookItem.resolveBookComponents(pStack, this.createCommandSourceStack(pPlayer), pPlayer);
      }

      return pStack;
   }

   /**
    * Creates a CommandSourceStack for resolving the contents of a book. If the player is null, a CommandSourceStack
    * with the generic name {@code "Lectern"} is used.
    */
   private CommandSourceStack createCommandSourceStack(@Nullable Player pPlayer) {
      String s;
      Component component;
      if (pPlayer == null) {
         s = "Lectern";
         component = Component.literal("Lectern");
      } else {
         s = pPlayer.getName().getString();
         component = pPlayer.getDisplayName();
      }

      Vec3 vec3 = Vec3.atCenterOf(this.worldPosition);
      return new CommandSourceStack(CommandSource.NULL, vec3, Vec2.ZERO, (ServerLevel)this.level, 2, s, component, this.level.getServer(), pPlayer);
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("Book", 10)) {
         this.book = this.resolveBook(ItemStack.of(pTag.getCompound("Book")), (Player)null);
      } else {
         this.book = ItemStack.EMPTY;
      }

      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.page = Mth.clamp(pTag.getInt("Page"), 0, this.pageCount - 1);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.getBook().isEmpty()) {
         pTag.put("Book", this.getBook().save(new CompoundTag()));
         pTag.putInt("Page", this.page);
      }

   }

   public void clearContent() {
      this.setBook(ItemStack.EMPTY);
   }

   public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
      return new LecternMenu(pContainerId, this.bookAccess, this.dataAccess);
   }

   public Component getDisplayName() {
      return Component.translatable("container.lectern");
   }
}