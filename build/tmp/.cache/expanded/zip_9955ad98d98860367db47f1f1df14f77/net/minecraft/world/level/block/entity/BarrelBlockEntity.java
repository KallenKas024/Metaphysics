package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
   private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void onOpen(Level p_155062_, BlockPos p_155063_, BlockState p_155064_) {
         BarrelBlockEntity.this.playSound(p_155064_, SoundEvents.BARREL_OPEN);
         BarrelBlockEntity.this.updateBlockState(p_155064_, true);
      }

      protected void onClose(Level p_155072_, BlockPos p_155073_, BlockState p_155074_) {
         BarrelBlockEntity.this.playSound(p_155074_, SoundEvents.BARREL_CLOSE);
         BarrelBlockEntity.this.updateBlockState(p_155074_, false);
      }

      protected void openerCountChanged(Level p_155066_, BlockPos p_155067_, BlockState p_155068_, int p_155069_, int p_155070_) {
      }

      protected boolean isOwnContainer(Player p_155060_) {
         if (p_155060_.containerMenu instanceof ChestMenu) {
            Container container = ((ChestMenu)p_155060_.containerMenu).getContainer();
            return container == BarrelBlockEntity.this;
         } else {
            return false;
         }
      }
   };

   public BarrelBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.BARREL, pPos, pBlockState);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.trySaveLootTable(pTag)) {
         ContainerHelper.saveAllItems(pTag, this.items);
      }

   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(pTag)) {
         ContainerHelper.loadAllItems(pTag, this.items);
      }

   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return 27;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> pItems) {
      this.items = pItems;
   }

   protected Component getDefaultName() {
      return Component.translatable("container.barrel");
   }

   protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
      return ChestMenu.threeRows(pId, pPlayer, this);
   }

   public void startOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public void stopOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   void updateBlockState(BlockState pState, boolean pOpen) {
      this.level.setBlock(this.getBlockPos(), pState.setValue(BarrelBlock.OPEN, Boolean.valueOf(pOpen)), 3);
   }

   void playSound(BlockState pState, SoundEvent pSound) {
      Vec3i vec3i = pState.getValue(BarrelBlock.FACING).getNormal();
      double d0 = (double)this.worldPosition.getX() + 0.5D + (double)vec3i.getX() / 2.0D;
      double d1 = (double)this.worldPosition.getY() + 0.5D + (double)vec3i.getY() / 2.0D;
      double d2 = (double)this.worldPosition.getZ() + 0.5D + (double)vec3i.getZ() / 2.0D;
      this.level.playSound((Player)null, d0, d1, d2, pSound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
   }
}