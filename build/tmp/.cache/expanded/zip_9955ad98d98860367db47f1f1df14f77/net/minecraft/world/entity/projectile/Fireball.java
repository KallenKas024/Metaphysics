package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public abstract class Fireball extends AbstractHurtingProjectile implements ItemSupplier {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Fireball.class, EntityDataSerializers.ITEM_STACK);

   public Fireball(EntityType<? extends Fireball> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public Fireball(EntityType<? extends Fireball> pEntityType, double pX, double pY, double pZ, double pOffsetX, double pOffsetY, double pOffsetZ, Level pLevel) {
      super(pEntityType, pX, pY, pZ, pOffsetX, pOffsetY, pOffsetZ, pLevel);
   }

   public Fireball(EntityType<? extends Fireball> pEntityType, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, Level pLevel) {
      super(pEntityType, pShooter, pOffsetX, pOffsetY, pOffsetZ, pLevel);
   }

   public void setItem(ItemStack pStack) {
      if (!pStack.is(Items.FIRE_CHARGE) || pStack.hasTag()) {
         this.getEntityData().set(DATA_ITEM_STACK, pStack.copyWithCount(1));
      }

   }

   protected ItemStack getItemRaw() {
      return this.getEntityData().get(DATA_ITEM_STACK);
   }

   public ItemStack getItem() {
      ItemStack itemstack = this.getItemRaw();
      return itemstack.isEmpty() ? new ItemStack(Items.FIRE_CHARGE) : itemstack;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      ItemStack itemstack = this.getItemRaw();
      if (!itemstack.isEmpty()) {
         pCompound.put("Item", itemstack.save(new CompoundTag()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      ItemStack itemstack = ItemStack.of(pCompound.getCompound("Item"));
      this.setItem(itemstack);
   }
}