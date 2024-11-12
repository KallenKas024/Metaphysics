package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ChestBoat extends Boat implements HasCustomInventoryScreen, ContainerEntity {
   private static final int CONTAINER_SIZE = 27;
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;

   public ChestBoat(EntityType<? extends Boat> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ChestBoat(Level pLevel, double pX, double pY, double pZ) {
      this(EntityType.CHEST_BOAT, pLevel);
      this.setPos(pX, pY, pZ);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
   }

   protected float getSinglePassengerXOffset() {
      return 0.15F;
   }

   protected int getMaxPassengers() {
      return 1;
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.addChestVehicleSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.readChestVehicleSaveData(pCompound);
   }

   public void destroy(DamageSource pDamageSource) {
      super.destroy(pDamageSource);
      this.chestVehicleDestroyed(pDamageSource, this.level(), this);
   }

   public void remove(Entity.RemovalReason pReason) {
      if (!this.level().isClientSide && pReason.shouldDestroy()) {
         Containers.dropContents(this.level(), this, this);
      }

      super.remove(pReason);
   }

   public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      if (this.canAddPassenger(pPlayer) && !pPlayer.isSecondaryUseActive()) {
         return super.interact(pPlayer, pHand);
      } else {
         InteractionResult interactionresult = this.interactWithContainerVehicle(pPlayer);
         if (interactionresult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
         }

         return interactionresult;
      }
   }

   public void openCustomInventoryScreen(Player pPlayer) {
      pPlayer.openMenu(this);
      if (!pPlayer.level().isClientSide) {
         this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
         PiglinAi.angerNearbyPiglins(pPlayer, true);
      }

   }

   public Item getDropItem() {
      Item item;
      switch (this.getVariant()) {
         case SPRUCE:
            item = Items.SPRUCE_CHEST_BOAT;
            break;
         case BIRCH:
            item = Items.BIRCH_CHEST_BOAT;
            break;
         case JUNGLE:
            item = Items.JUNGLE_CHEST_BOAT;
            break;
         case ACACIA:
            item = Items.ACACIA_CHEST_BOAT;
            break;
         case CHERRY:
            item = Items.CHERRY_CHEST_BOAT;
            break;
         case DARK_OAK:
            item = Items.DARK_OAK_CHEST_BOAT;
            break;
         case MANGROVE:
            item = Items.MANGROVE_CHEST_BOAT;
            break;
         case BAMBOO:
            item = Items.BAMBOO_CHEST_RAFT;
            break;
         default:
            item = Items.OAK_CHEST_BOAT;
      }

      return item;
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return 27;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pSlot) {
      return this.getChestVehicleItem(pSlot);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pSlot, int pAmount) {
      return this.removeChestVehicleItem(pSlot, pAmount);
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pSlot) {
      return this.removeChestVehicleItemNoUpdate(pSlot);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pSlot, ItemStack pStack) {
      this.setChestVehicleItem(pSlot, pStack);
   }

   public SlotAccess getSlot(int pSlot) {
      return this.getChestVehicleSlot(pSlot);
   }

   /**
    * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      return this.isChestVehicleStillValid(pPlayer);
   }

   @Nullable
   public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
      if (this.lootTable != null && pPlayer.isSpectator()) {
         return null;
      } else {
         this.unpackLootTable(pPlayerInventory.player);
         return ChestMenu.threeRows(pContainerId, pPlayerInventory, this);
      }
   }

   public void unpackLootTable(@Nullable Player pPlayer) {
      this.unpackChestVehicleLootTable(pPlayer);
   }

   @Nullable
   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(@Nullable ResourceLocation pLootTable) {
      this.lootTable = pLootTable;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long pLootTableSeed) {
      this.lootTableSeed = pLootTableSeed;
   }

   public NonNullList<ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public void clearItemStacks() {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   }

   // Forge Start
   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this));

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction facing) {
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && this.isAlive())
         return itemHandler.cast();
      return super.getCapability(capability, facing);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      itemHandler.invalidate();
   }

   @Override
   public void reviveCaps() {
      super.reviveCaps();
      itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this));
   }

   public void stopOpen(Player pPlayer) {
      this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(pPlayer));
   }
}
