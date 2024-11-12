package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseContainerBlockEntity extends BlockEntity implements Container, MenuProvider, Nameable {
   private LockCode lockKey = LockCode.NO_LOCK;
   @Nullable
   private Component name;

   protected BaseContainerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
      super(pType, pPos, pBlockState);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.lockKey = LockCode.fromTag(pTag);
      if (pTag.contains("CustomName", 8)) {
         this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
      }

   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      this.lockKey.addToTag(pTag);
      if (this.name != null) {
         pTag.putString("CustomName", Component.Serializer.toJson(this.name));
      }

   }

   public void setCustomName(Component pName) {
      this.name = pName;
   }

   public Component getName() {
      return this.name != null ? this.name : this.getDefaultName();
   }

   public Component getDisplayName() {
      return this.getName();
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }

   protected abstract Component getDefaultName();

   public boolean canOpen(Player pPlayer) {
      return canUnlock(pPlayer, this.lockKey, this.getDisplayName());
   }

   public static boolean canUnlock(Player pPlayer, LockCode pCode, Component pDisplayName) {
      if (!pPlayer.isSpectator() && !pCode.unlocksWith(pPlayer.getMainHandItem())) {
         pPlayer.displayClientMessage(Component.translatable("container.isLocked", pDisplayName), true);
         pPlayer.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
      return this.canOpen(pPlayer) ? this.createMenu(pContainerId, pPlayerInventory) : null;
   }

   protected abstract AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory);

   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> createUnSidedHandler());
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.wrapper.InvWrapper(this);
   }

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
      itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> createUnSidedHandler());
   }
}
