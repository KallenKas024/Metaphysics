package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface ContainerEntity extends Container, MenuProvider {
   Vec3 position();

   @Nullable
   ResourceLocation getLootTable();

   void setLootTable(@Nullable ResourceLocation pLootTable);

   long getLootTableSeed();

   void setLootTableSeed(long pLootTableSeed);

   NonNullList<ItemStack> getItemStacks();

   void clearItemStacks();

   Level level();

   boolean isRemoved();

   default boolean isEmpty() {
      return this.isChestVehicleEmpty();
   }

   default void addChestVehicleSaveData(CompoundTag pTag) {
      if (this.getLootTable() != null) {
         pTag.putString("LootTable", this.getLootTable().toString());
         if (this.getLootTableSeed() != 0L) {
            pTag.putLong("LootTableSeed", this.getLootTableSeed());
         }
      } else {
         ContainerHelper.saveAllItems(pTag, this.getItemStacks());
      }

   }

   default void readChestVehicleSaveData(CompoundTag pTag) {
      this.clearItemStacks();
      if (pTag.contains("LootTable", 8)) {
         this.setLootTable(new ResourceLocation(pTag.getString("LootTable")));
         this.setLootTableSeed(pTag.getLong("LootTableSeed"));
      } else {
         ContainerHelper.loadAllItems(pTag, this.getItemStacks());
      }

   }

   default void chestVehicleDestroyed(DamageSource pDamageSource, Level pLevel, Entity pEntity) {
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         Containers.dropContents(pLevel, pEntity, this);
         if (!pLevel.isClientSide) {
            Entity entity = pDamageSource.getDirectEntity();
            if (entity != null && entity.getType() == EntityType.PLAYER) {
               PiglinAi.angerNearbyPiglins((Player)entity, true);
            }
         }

      }
   }

   default InteractionResult interactWithContainerVehicle(Player pPlayer) {
      pPlayer.openMenu(this);
      return !pPlayer.level().isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
   }

   default void unpackChestVehicleLootTable(@Nullable Player pPlayer) {
      MinecraftServer minecraftserver = this.level().getServer();
      if (this.getLootTable() != null && minecraftserver != null) {
         LootTable loottable = minecraftserver.getLootData().getLootTable(this.getLootTable());
         if (pPlayer != null) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)pPlayer, this.getLootTable());
         }

         this.setLootTable((ResourceLocation)null);
         LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());
         // Forge: set the chest to killer_entity for loot context.
         if (this instanceof AbstractMinecartContainer entityContainer)
            lootparams$builder.withParameter(LootContextParams.KILLER_ENTITY, entityContainer);
         if (pPlayer != null) {
            lootparams$builder.withLuck(pPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, pPlayer);
         }

         loottable.fill(this, lootparams$builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
      }

   }

   default void clearChestVehicleContent() {
      this.unpackChestVehicleLootTable((Player)null);
      this.getItemStacks().clear();
   }

   default boolean isChestVehicleEmpty() {
      for(ItemStack itemstack : this.getItemStacks()) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   default ItemStack removeChestVehicleItemNoUpdate(int pSlot) {
      this.unpackChestVehicleLootTable((Player)null);
      ItemStack itemstack = this.getItemStacks().get(pSlot);
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.getItemStacks().set(pSlot, ItemStack.EMPTY);
         return itemstack;
      }
   }

   default ItemStack getChestVehicleItem(int pSlot) {
      this.unpackChestVehicleLootTable((Player)null);
      return this.getItemStacks().get(pSlot);
   }

   default ItemStack removeChestVehicleItem(int pSlot, int pAmount) {
      this.unpackChestVehicleLootTable((Player)null);
      return ContainerHelper.removeItem(this.getItemStacks(), pSlot, pAmount);
   }

   default void setChestVehicleItem(int pSlot, ItemStack pStack) {
      this.unpackChestVehicleLootTable((Player)null);
      this.getItemStacks().set(pSlot, pStack);
      if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

   }

   default SlotAccess getChestVehicleSlot(final int pIndex) {
      return pIndex >= 0 && pIndex < this.getContainerSize() ? new SlotAccess() {
         public ItemStack get() {
            return ContainerEntity.this.getChestVehicleItem(pIndex);
         }

         public boolean set(ItemStack p_219964_) {
            ContainerEntity.this.setChestVehicleItem(pIndex, p_219964_);
            return true;
         }
      } : SlotAccess.NULL;
   }

   default boolean isChestVehicleStillValid(Player pPlayer) {
      return !this.isRemoved() && this.position().closerThan(pPlayer.position(), 8.0D);
   }
}
