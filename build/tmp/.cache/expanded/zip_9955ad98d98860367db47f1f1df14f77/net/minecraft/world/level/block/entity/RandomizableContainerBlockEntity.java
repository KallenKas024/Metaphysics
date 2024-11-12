package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity {
   public static final String LOOT_TABLE_TAG = "LootTable";
   public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
   @Nullable
   protected ResourceLocation lootTable;
   protected long lootTableSeed;

   protected RandomizableContainerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
      super(pType, pPos, pBlockState);
   }

   public static void setLootTable(BlockGetter pLevel, RandomSource pRandom, BlockPos pPos, ResourceLocation pLootTable) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof RandomizableContainerBlockEntity) {
         ((RandomizableContainerBlockEntity)blockentity).setLootTable(pLootTable, pRandom.nextLong());
      }

   }

   protected boolean tryLoadLootTable(CompoundTag pTag) {
      if (pTag.contains("LootTable", 8)) {
         this.lootTable = new ResourceLocation(pTag.getString("LootTable"));
         this.lootTableSeed = pTag.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   protected boolean trySaveLootTable(CompoundTag pTag) {
      if (this.lootTable == null) {
         return false;
      } else {
         pTag.putString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            pTag.putLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      }
   }

   public void unpackLootTable(@Nullable Player pPlayer) {
      if (this.lootTable != null && this.level.getServer() != null) {
         LootTable loottable = this.level.getServer().getLootData().getLootTable(this.lootTable);
         if (pPlayer instanceof ServerPlayer) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)pPlayer, this.lootTable);
         }

         this.lootTable = null;
         LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition));
         if (pPlayer != null) {
            lootparams$builder.withLuck(pPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, pPlayer);
         }

         loottable.fill(this, lootparams$builder.create(LootContextParamSets.CHEST), this.lootTableSeed);
      }

   }

   public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed) {
      this.lootTable = pLootTable;
      this.lootTableSeed = pLootTableSeed;
   }

   public boolean isEmpty() {
      this.unpackLootTable((Player)null);
      return this.getItems().stream().allMatch(ItemStack::isEmpty);
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      this.unpackLootTable((Player)null);
      return this.getItems().get(pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      this.unpackLootTable((Player)null);
      ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), pIndex, pCount);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      this.unpackLootTable((Player)null);
      return ContainerHelper.takeItem(this.getItems(), pIndex);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.unpackLootTable((Player)null);
      this.getItems().set(pIndex, pStack);
      if (pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      return Container.stillValidBlockEntity(this, pPlayer);
   }

   public void clearContent() {
      this.getItems().clear();
   }

   protected abstract NonNullList<ItemStack> getItems();

   protected abstract void setItems(NonNullList<ItemStack> pItemStacks);

   public boolean canOpen(Player pPlayer) {
      return super.canOpen(pPlayer) && (this.lootTable == null || !pPlayer.isSpectator());
   }

   @Nullable
   public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
      if (this.canOpen(pPlayer)) {
         this.unpackLootTable(pPlayerInventory.player);
         return this.createMenu(pContainerId, pPlayerInventory);
      } else {
         return null;
      }
   }
}