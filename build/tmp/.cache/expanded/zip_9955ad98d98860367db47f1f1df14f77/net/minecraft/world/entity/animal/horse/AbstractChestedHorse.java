package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractChestedHorse extends AbstractHorse {
   private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);
   public static final int INV_CHEST_COUNT = 15;

   protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.canGallop = false;
   }

   protected void randomizeAttributes(RandomSource pRandom) {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)generateMaxHealth(pRandom::nextInt));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_CHEST, false);
   }

   public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
      return createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.175F).add(Attributes.JUMP_STRENGTH, 0.5D);
   }

   public boolean hasChest() {
      return this.entityData.get(DATA_ID_CHEST);
   }

   public void setChest(boolean pChested) {
      this.entityData.set(DATA_ID_CHEST, pChested);
   }

   protected int getInventorySize() {
      return this.hasChest() ? 17 : super.getInventorySize();
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return super.getPassengersRidingOffset() - 0.25D;
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.hasChest()) {
         if (!this.level().isClientSide) {
            this.spawnAtLocation(Blocks.CHEST);
         }

         this.setChest(false);
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("ChestedHorse", this.hasChest());
      if (this.hasChest()) {
         ListTag listtag = new ListTag();

         for(int i = 2; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
               CompoundTag compoundtag = new CompoundTag();
               compoundtag.putByte("Slot", (byte)i);
               itemstack.save(compoundtag);
               listtag.add(compoundtag);
            }
         }

         pCompound.put("Items", listtag);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setChest(pCompound.getBoolean("ChestedHorse"));
      this.createInventory();
      if (this.hasChest()) {
         ListTag listtag = pCompound.getList("Items", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 2 && j < this.inventory.getContainerSize()) {
               this.inventory.setItem(j, ItemStack.of(compoundtag));
            }
         }
      }

      this.updateContainerEquipment();
   }

   public SlotAccess getSlot(int pSlot) {
      return pSlot == 499 ? new SlotAccess() {
         public ItemStack get() {
            return AbstractChestedHorse.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
         }

         public boolean set(ItemStack p_149485_) {
            if (p_149485_.isEmpty()) {
               if (AbstractChestedHorse.this.hasChest()) {
                  AbstractChestedHorse.this.setChest(false);
                  AbstractChestedHorse.this.createInventory();
               }

               return true;
            } else if (p_149485_.is(Items.CHEST)) {
               if (!AbstractChestedHorse.this.hasChest()) {
                  AbstractChestedHorse.this.setChest(true);
                  AbstractChestedHorse.this.createInventory();
               }

               return true;
            } else {
               return false;
            }
         }
      } : super.getSlot(pSlot);
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      boolean flag = !this.isBaby() && this.isTamed() && pPlayer.isSecondaryUseActive();
      if (!this.isVehicle() && !flag) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (!itemstack.isEmpty()) {
            if (this.isFood(itemstack)) {
               return this.fedFood(pPlayer, itemstack);
            }

            if (!this.isTamed()) {
               this.makeMad();
               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (!this.hasChest() && itemstack.is(Items.CHEST)) {
               this.equipChest(pPlayer, itemstack);
               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
         }

         return super.mobInteract(pPlayer, pHand);
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   private void equipChest(Player pPlayer, ItemStack pChestStack) {
      this.setChest(true);
      this.playChestEquipsSound();
      if (!pPlayer.getAbilities().instabuild) {
         pChestStack.shrink(1);
      }

      this.createInventory();
   }

   protected void playChestEquipsSound() {
      this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   public int getInventoryColumns() {
      return 5;
   }
}