package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity implements TraceableEntity {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
   private static final int LIFETIME = 6000;
   private static final int INFINITE_PICKUP_DELAY = 32767;
   private static final int INFINITE_LIFETIME = -32768;
   private int age;
   private int pickupDelay;
   private int health = 5;
   @Nullable
   private UUID thrower;
   @Nullable
   private UUID target;
   public final float bobOffs;
   /**
    * The maximum age of this EntityItem.  The item is expired once this is reached.
    */
   public int lifespan = ItemEntity.LIFETIME;

   public ItemEntity(EntityType<? extends ItemEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
      this.setYRot(this.random.nextFloat() * 360.0F);
   }

   public ItemEntity(Level pLevel, double pPosX, double pPosY, double pPosZ, ItemStack pItemStack) {
      this(pLevel, pPosX, pPosY, pPosZ, pItemStack, pLevel.random.nextDouble() * 0.2D - 0.1D, 0.2D, pLevel.random.nextDouble() * 0.2D - 0.1D);
   }

   public ItemEntity(Level pLevel, double pPosX, double pPosY, double pPosZ, ItemStack pItemStack, double pDeltaX, double pDeltaY, double pDeltaZ) {
      this(EntityType.ITEM, pLevel);
      this.setPos(pPosX, pPosY, pPosZ);
      this.setDeltaMovement(pDeltaX, pDeltaY, pDeltaZ);
      this.setItem(pItemStack);
      this.lifespan = (pItemStack.getItem() == null ? ItemEntity.LIFETIME : pItemStack.getEntityLifespan(pLevel));
   }

   private ItemEntity(ItemEntity pOther) {
      super(pOther.getType(), pOther.level());
      this.setItem(pOther.getItem().copy());
      this.copyPosition(pOther);
      this.age = pOther.age;
      this.bobOffs = pOther.bobOffs;
      this.lifespan = pOther.lifespan;
   }

   public boolean dampensVibrations() {
      return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
   }

   /**
    * Returns null or the entityliving it was ignited by
    */
   @Nullable
   public Entity getOwner() {
      if (this.thrower != null) {
         Level level = this.level();
         if (level instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)level;
            return serverlevel.getEntity(this.thrower);
         }
      }

      return null;
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (getItem().onEntityItemUpdate(this)) return;
      if (this.getItem().isEmpty()) {
         this.discard();
      } else {
         super.tick();
         if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            --this.pickupDelay;
         }

         this.xo = this.getX();
         this.yo = this.getY();
         this.zo = this.getZ();
         Vec3 vec3 = this.getDeltaMovement();
         float f = this.getEyeHeight() - 0.11111111F;
         net.minecraftforge.fluids.FluidType fluidType = this.getMaxHeightFluidType();
         if (!fluidType.isAir() && !fluidType.isVanilla() && this.getFluidTypeHeight(fluidType) > (double)f) fluidType.setItemMovement(this);
         else
         if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.setUnderwaterMovement();
         } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.setUnderLavaMovement();
         } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
         }

         if (this.level().isClientSide) {
            this.noPhysics = false;
         } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7D));
            if (this.noPhysics) {
               this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
            }
         }

         if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > (double)1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f1 = 0.98F;
            if (this.onGround()) {
               BlockPos groundPos = getBlockPosBelowThatAffectsMyMovement();
               f1 = this.level().getBlockState(groundPos).getFriction(level(), groundPos, this) * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply((double)f1, 0.98D, (double)f1));
            if (this.onGround()) {
               Vec3 vec31 = this.getDeltaMovement();
               if (vec31.y < 0.0D) {
                  this.setDeltaMovement(vec31.multiply(1.0D, -0.5D, 1.0D));
               }
            }
         }

         boolean flag = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
         int i = flag ? 2 : 40;
         if (this.tickCount % i == 0 && !this.level().isClientSide && this.isMergable()) {
            this.mergeWithNeighbours();
         }

         if (this.age != -32768) {
            ++this.age;
         }

         this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
         if (!this.level().isClientSide) {
            double d0 = this.getDeltaMovement().subtract(vec3).lengthSqr();
            if (d0 > 0.01D) {
               this.hasImpulse = true;
            }
         }

         ItemStack item = this.getItem();
         if (!this.level().isClientSide && this.age >= lifespan) {
             int hook = net.minecraftforge.event.ForgeEventFactory.onItemExpire(this, item);
             if (hook < 0) {
                this.discard();
             } else {
                this.lifespan += hook;
             }
         }
         if (item.isEmpty() && !this.isRemoved()) {
            this.discard();
         }

      }
   }

   protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return this.getOnPos(0.999999F);
   }

   private void setUnderwaterMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * (double)0.99F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.99F);
   }

   private void setUnderLavaMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
   }

   /**
    * Looks for other itemstacks nearby and tries to stack them together
    */
   private void mergeWithNeighbours() {
      if (this.isMergable()) {
         for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5D, 0.0D, 0.5D), (p_186268_) -> {
            return p_186268_ != this && p_186268_.isMergable();
         })) {
            if (itementity.isMergable()) {
               this.tryToMerge(itementity);
               if (this.isRemoved()) {
                  break;
               }
            }
         }

      }
   }

   private boolean isMergable() {
      ItemStack itemstack = this.getItem();
      return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemstack.getCount() < itemstack.getMaxStackSize();
   }

   private void tryToMerge(ItemEntity pItemEntity) {
      ItemStack itemstack = this.getItem();
      ItemStack itemstack1 = pItemEntity.getItem();
      if (Objects.equals(this.target, pItemEntity.target) && areMergable(itemstack, itemstack1)) {
         if (itemstack1.getCount() < itemstack.getCount()) {
            merge(this, itemstack, pItemEntity, itemstack1);
         } else {
            merge(pItemEntity, itemstack1, this, itemstack);
         }

      }
   }

   public static boolean areMergable(ItemStack pDestinationStack, ItemStack pOriginStack) {
      if (!pOriginStack.is(pDestinationStack.getItem())) {
         return false;
      } else if (pOriginStack.getCount() + pDestinationStack.getCount() > pOriginStack.getMaxStackSize()) {
         return false;
      } else if (pOriginStack.hasTag() ^ pDestinationStack.hasTag()) {
         return false;
      } else if (!pDestinationStack.areCapsCompatible(pOriginStack)) {
         return false;
      } else {
         return !pOriginStack.hasTag() || pOriginStack.getTag().equals(pDestinationStack.getTag());
      }
   }

   public static ItemStack merge(ItemStack pDestinationStack, ItemStack pOriginStack, int pAmount) {
      int i = Math.min(Math.min(pDestinationStack.getMaxStackSize(), pAmount) - pDestinationStack.getCount(), pOriginStack.getCount());
      ItemStack itemstack = pDestinationStack.copyWithCount(pDestinationStack.getCount() + i);
      pOriginStack.shrink(i);
      return itemstack;
   }

   private static void merge(ItemEntity pDestinationEntity, ItemStack pDestinationStack, ItemStack pOriginStack) {
      ItemStack itemstack = merge(pDestinationStack, pOriginStack, 64);
      pDestinationEntity.setItem(itemstack);
   }

   private static void merge(ItemEntity pDestinationEntity, ItemStack pDestinationStack, ItemEntity pOriginEntity, ItemStack pOriginStack) {
      merge(pDestinationEntity, pDestinationStack, pOriginStack);
      pDestinationEntity.pickupDelay = Math.max(pDestinationEntity.pickupDelay, pOriginEntity.pickupDelay);
      pDestinationEntity.age = Math.min(pDestinationEntity.age, pOriginEntity.age);
      if (pOriginStack.isEmpty()) {
         pOriginEntity.discard();
      }

   }

   public boolean fireImmune() {
      return this.getItem().getItem().isFireResistant() || super.fireImmune();
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (!this.getItem().isEmpty() && this.getItem().is(Items.NETHER_STAR) && pSource.is(DamageTypeTags.IS_EXPLOSION)) {
         return false;
      } else if (!this.getItem().getItem().canBeHurtBy(pSource)) {
         return false;
      } else if (this.level().isClientSide) {
         return true;
      } else {
         this.markHurt();
         this.health = (int)((float)this.health - pAmount);
         this.gameEvent(GameEvent.ENTITY_DAMAGE, pSource.getEntity());
         if (this.health <= 0) {
            this.getItem().onDestroyed(this, pSource);
            this.discard();
         }

         return true;
      }
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putShort("Health", (short)this.health);
      pCompound.putShort("Age", (short)this.age);
      pCompound.putShort("PickupDelay", (short)this.pickupDelay);
      pCompound.putInt("Lifespan", this.lifespan);
      if (this.thrower != null) {
         pCompound.putUUID("Thrower", this.thrower);
      }

      if (this.target != null) {
         pCompound.putUUID("Owner", this.target);
      }

      if (!this.getItem().isEmpty()) {
         pCompound.put("Item", this.getItem().save(new CompoundTag()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      this.health = pCompound.getShort("Health");
      this.age = pCompound.getShort("Age");
      if (pCompound.contains("PickupDelay")) {
         this.pickupDelay = pCompound.getShort("PickupDelay");
      }
      if (pCompound.contains("Lifespan")) {
         this.lifespan = pCompound.getInt("Lifespan");
      }

      if (pCompound.hasUUID("Owner")) {
         this.target = pCompound.getUUID("Owner");
      }

      if (pCompound.hasUUID("Thrower")) {
         this.thrower = pCompound.getUUID("Thrower");
      }

      CompoundTag compoundtag = pCompound.getCompound("Item");
      this.setItem(ItemStack.of(compoundtag));
      if (this.getItem().isEmpty()) {
         this.discard();
      }

   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(Player pEntity) {
      if (!this.level().isClientSide) {
         if (this.pickupDelay > 0) return;
         ItemStack itemstack = this.getItem();
         Item item = itemstack.getItem();
         int i = itemstack.getCount();
         int hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(this, pEntity);
         if (hook < 0) return;
         ItemStack copy = itemstack.copy();
         if (this.pickupDelay == 0 && (this.target == null || this.target.equals(pEntity.getUUID())) && (hook == 1 || i <= 0 || pEntity.getInventory().add(itemstack))) {
            i = copy.getCount() - itemstack.getCount();
            copy.setCount(i);
            net.minecraftforge.event.ForgeEventFactory.firePlayerItemPickupEvent(pEntity, this, copy);
            pEntity.take(this, i);
            if (itemstack.isEmpty()) {
               this.discard();
               itemstack.setCount(i);
            }

            pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            pEntity.onItemPickup(this);
         }

      }
   }

   public Component getName() {
      Component component = this.getCustomName();
      return (Component)(component != null ? component : Component.translatable(this.getItem().getDescriptionId()));
   }

   /**
    * Returns {@code true} if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }

   @Nullable
   public Entity changeDimension(ServerLevel pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      Entity entity = super.changeDimension(pServer, teleporter);
      if (!this.level().isClientSide && entity instanceof ItemEntity) {
         ((ItemEntity)entity).mergeWithNeighbours();
      }

      return entity;
   }

   /**
    * Gets the item that this entity represents.
    */
   public ItemStack getItem() {
      return this.getEntityData().get(DATA_ITEM);
   }

   /**
    * Sets the item that this entity represents.
    */
   public void setItem(ItemStack pStack) {
      this.getEntityData().set(DATA_ITEM, pStack);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_ITEM.equals(pKey)) {
         this.getItem().setEntityRepresentation(this);
      }

   }

   public void setTarget(@Nullable UUID pTarget) {
      this.target = pTarget;
   }

   public void setThrower(@Nullable UUID pThrower) {
      this.thrower = pThrower;
   }

   public int getAge() {
      return this.age;
   }

   public void setDefaultPickUpDelay() {
      this.pickupDelay = 10;
   }

   public void setNoPickUpDelay() {
      this.pickupDelay = 0;
   }

   public void setNeverPickUp() {
      this.pickupDelay = 32767;
   }

   public void setPickUpDelay(int pPickupDelay) {
      this.pickupDelay = pPickupDelay;
   }

   public boolean hasPickUpDelay() {
      return this.pickupDelay > 0;
   }

   public void setUnlimitedLifetime() {
      this.age = -32768;
   }

   public void setExtendedLifetime() {
      this.age = -6000;
   }

   public void makeFakeItem() {
      this.setNeverPickUp();
      this.age = getItem().getEntityLifespan(this.level()) - 1;
   }

   public float getSpin(float pPartialTicks) {
      return ((float)this.getAge() + pPartialTicks) / 20.0F + this.bobOffs;
   }

   public ItemEntity copy() {
      return new ItemEntity(this);
   }

   public SoundSource getSoundSource() {
      return SoundSource.AMBIENT;
   }

   public float getVisualRotationYInDegrees() {
      return 180.0F - this.getSpin(0.5F) / ((float)Math.PI * 2F) * 360.0F;
   }
}
