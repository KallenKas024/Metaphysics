package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartFurnace extends AbstractMinecart {
   private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
   private int fuel;
   public double xPush;
   public double zPush;
   /** The fuel item used to make the minecart move. */
   private static final Ingredient INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

   public MinecartFurnace(EntityType<? extends MinecartFurnace> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public MinecartFurnace(Level pLevel, double pX, double pY, double pZ) {
      super(EntityType.FURNACE_MINECART, pLevel, pX, pY, pZ);
   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.FURNACE;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_FUEL, false);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.fuel > 0) {
            --this.fuel;
         }

         if (this.fuel <= 0) {
            this.xPush = 0.0D;
            this.zPush = 0.0D;
         }

         this.setHasFuel(this.fuel > 0);
      }

      if (this.hasFuel() && this.random.nextInt(4) == 0) {
         this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 0.0D, 0.0D, 0.0D);
      }

   }

   /**
    * Gets the maximum speed for a minecart
    */
   protected double getMaxSpeed() {
      return (this.isInWater() ? 3.0D : 4.0D) / 20.0D;
   }

   protected Item getDropItem() {
      return Items.FURNACE_MINECART;
   }

   protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
      double d0 = 1.0E-4D;
      double d1 = 0.001D;
      super.moveAlongTrack(pPos, pState);
      Vec3 vec3 = this.getDeltaMovement();
      double d2 = vec3.horizontalDistanceSqr();
      double d3 = this.xPush * this.xPush + this.zPush * this.zPush;
      if (d3 > 1.0E-4D && d2 > 0.001D) {
         double d4 = Math.sqrt(d2);
         double d5 = Math.sqrt(d3);
         this.xPush = vec3.x / d4 * d5;
         this.zPush = vec3.z / d4 * d5;
      }

   }

   protected void applyNaturalSlowdown() {
      double d0 = this.xPush * this.xPush + this.zPush * this.zPush;
      if (d0 > 1.0E-7D) {
         d0 = Math.sqrt(d0);
         this.xPush /= d0;
         this.zPush /= d0;
         Vec3 vec3 = this.getDeltaMovement().multiply(0.8D, 0.0D, 0.8D).add(this.xPush, 0.0D, this.zPush);
         if (this.isInWater()) {
            vec3 = vec3.scale(0.1D);
         }

         this.setDeltaMovement(vec3);
      } else {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.98D, 0.0D, 0.98D));
      }

      super.applyNaturalSlowdown();
   }

   public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      InteractionResult ret = super.interact(pPlayer, pHand);
      if (ret.consumesAction()) return ret;
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (INGREDIENT.test(itemstack) && this.fuel + 3600 <= 32000) {
         if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         this.fuel += 3600;
      }

      if (this.fuel > 0) {
         this.xPush = this.getX() - pPlayer.getX();
         this.zPush = this.getZ() - pPlayer.getZ();
      }

      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   @Override
   public float getMaxCartSpeedOnRail() {
      return 0.2f;
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putDouble("PushX", this.xPush);
      pCompound.putDouble("PushZ", this.zPush);
      pCompound.putShort("Fuel", (short)this.fuel);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.xPush = pCompound.getDouble("PushX");
      this.zPush = pCompound.getDouble("PushZ");
      this.fuel = pCompound.getShort("Fuel");
   }

   protected boolean hasFuel() {
      return this.entityData.get(DATA_ID_FUEL);
   }

   protected void setHasFuel(boolean pHasFuel) {
      this.entityData.set(DATA_ID_FUEL, pHasFuel);
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.valueOf(this.hasFuel()));
   }
}
