package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public abstract class HangingEntity extends Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final Predicate<Entity> HANGING_ENTITY = (p_31734_) -> {
      return p_31734_ instanceof HangingEntity;
   };
   private int checkInterval;
   protected BlockPos pos;
   /** The direction the entity is facing */
   protected Direction direction = Direction.SOUTH;

   protected HangingEntity(EntityType<? extends HangingEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected HangingEntity(EntityType<? extends HangingEntity> pEntityType, Level pLevel, BlockPos pPos) {
      this(pEntityType, pLevel);
      this.pos = pPos;
   }

   protected void defineSynchedData() {
   }

   /**
    * Updates facing and bounding box based on it
    */
   protected void setDirection(Direction pFacingDirection) {
      Validate.notNull(pFacingDirection);
      Validate.isTrue(pFacingDirection.getAxis().isHorizontal());
      this.direction = pFacingDirection;
      this.setYRot((float)(this.direction.get2DDataValue() * 90));
      this.yRotO = this.getYRot();
      this.recalculateBoundingBox();
   }

   /**
    * Updates the entity bounding box based on current facing
    */
   protected void recalculateBoundingBox() {
      if (this.direction != null) {
         double d0 = (double)this.pos.getX() + 0.5D;
         double d1 = (double)this.pos.getY() + 0.5D;
         double d2 = (double)this.pos.getZ() + 0.5D;
         double d3 = 0.46875D;
         double d4 = this.offs(this.getWidth());
         double d5 = this.offs(this.getHeight());
         d0 -= (double)this.direction.getStepX() * 0.46875D;
         d2 -= (double)this.direction.getStepZ() * 0.46875D;
         d1 += d5;
         Direction direction = this.direction.getCounterClockWise();
         d0 += d4 * (double)direction.getStepX();
         d2 += d4 * (double)direction.getStepZ();
         this.setPosRaw(d0, d1, d2);
         double d6 = (double)this.getWidth();
         double d7 = (double)this.getHeight();
         double d8 = (double)this.getWidth();
         if (this.direction.getAxis() == Direction.Axis.Z) {
            d8 = 1.0D;
         } else {
            d6 = 1.0D;
         }

         d6 /= 32.0D;
         d7 /= 32.0D;
         d8 /= 32.0D;
         this.setBoundingBox(new AABB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8));
      }
   }

   private double offs(int pOffset) {
      return pOffset % 32 == 0 ? 0.5D : 0.0D;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level().isClientSide) {
         this.checkBelowWorld();
         if (this.checkInterval++ == 100) {
            this.checkInterval = 0;
            if (!this.isRemoved() && !this.survives()) {
               this.discard();
               this.dropItem((Entity)null);
            }
         }
      }

   }

   /**
    * Checks to make sure the {@code HangingEntity} can be placed there.
    */
   public boolean survives() {
      if (!this.level().noCollision(this)) {
         return false;
      } else {
         int i = Math.max(1, this.getWidth() / 16);
         int j = Math.max(1, this.getHeight() / 16);
         BlockPos blockpos = this.pos.relative(this.direction.getOpposite());
         Direction direction = this.direction.getCounterClockWise();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int k = 0; k < i; ++k) {
            for(int l = 0; l < j; ++l) {
               int i1 = (i - 1) / -2;
               int j1 = (j - 1) / -2;
               blockpos$mutableblockpos.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
               BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
               if (net.minecraft.world.level.block.Block.canSupportCenter(this.level(), blockpos$mutableblockpos, this.direction))
                  continue;
               if (!blockstate.isSolid() && !DiodeBlock.isDiode(blockstate)) {
                  return false;
               }
            }
         }

         return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
      }
   }

   /**
    * Returns {@code true} if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return true;
   }

   /**
    * Called when a player attacks an entity. If this returns true the attack will not happen.
    */
   public boolean skipAttackInteraction(Entity pEntity) {
      if (pEntity instanceof Player player) {
         return !this.level().mayInteract(player, this.pos) ? true : this.hurt(this.damageSources().playerAttack(player), 0.0F);
      } else {
         return false;
      }
   }

   /**
    * Gets the horizontal facing direction of this Entity.
    */
   public Direction getDirection() {
      return this.direction;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         if (!this.isRemoved() && !this.level().isClientSide) {
            this.kill();
            this.markHurt();
            this.dropItem(pSource.getEntity());
         }

         return true;
      }
   }

   public void move(MoverType pType, Vec3 pPos) {
      if (!this.level().isClientSide && !this.isRemoved() && pPos.lengthSqr() > 0.0D) {
         this.kill();
         this.dropItem((Entity)null);
      }

   }

   /**
    * Adds to the current velocity of the entity, and sets {@link #isAirBorne} to true.
    */
   public void push(double pX, double pY, double pZ) {
      if (!this.level().isClientSide && !this.isRemoved() && pX * pX + pY * pY + pZ * pZ > 0.0D) {
         this.kill();
         this.dropItem((Entity)null);
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      BlockPos blockpos = this.getPos();
      pCompound.putInt("TileX", blockpos.getX());
      pCompound.putInt("TileY", blockpos.getY());
      pCompound.putInt("TileZ", blockpos.getZ());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      BlockPos blockpos = new BlockPos(pCompound.getInt("TileX"), pCompound.getInt("TileY"), pCompound.getInt("TileZ"));
      if (!blockpos.closerThan(this.blockPosition(), 16.0D)) {
         LOGGER.error("Hanging entity at invalid position: {}", (Object)blockpos);
      } else {
         this.pos = blockpos;
      }
   }

   public abstract int getWidth();

   public abstract int getHeight();

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public abstract void dropItem(@Nullable Entity pBrokenEntity);

   public abstract void playPlacementSound();

   /**
    * Drops an item at the position of the entity.
    */
   public ItemEntity spawnAtLocation(ItemStack pStack, float pOffsetY) {
      ItemEntity itementity = new ItemEntity(this.level(), this.getX() + (double)((float)this.direction.getStepX() * 0.15F), this.getY() + (double)pOffsetY, this.getZ() + (double)((float)this.direction.getStepZ() * 0.15F), pStack);
      itementity.setDefaultPickUpDelay();
      this.level().addFreshEntity(itementity);
      return itementity;
   }

   protected boolean repositionEntityAfterLoad() {
      return false;
   }

   /**
    * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
    */
   public void setPos(double pX, double pY, double pZ) {
      this.pos = BlockPos.containing(pX, pY, pZ);
      this.recalculateBoundingBox();
      this.hasImpulse = true;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   /**
    * Transforms the entity's current yaw with the given Rotation and returns it. This does not have a side-effect.
    */
   public float rotate(Rotation pTransformRotation) {
      if (this.direction.getAxis() != Direction.Axis.Y) {
         switch (pTransformRotation) {
            case CLOCKWISE_180:
               this.direction = this.direction.getOpposite();
               break;
            case COUNTERCLOCKWISE_90:
               this.direction = this.direction.getCounterClockWise();
               break;
            case CLOCKWISE_90:
               this.direction = this.direction.getClockWise();
         }
      }

      float f = Mth.wrapDegrees(this.getYRot());
      switch (pTransformRotation) {
         case CLOCKWISE_180:
            return f + 180.0F;
         case COUNTERCLOCKWISE_90:
            return f + 90.0F;
         case CLOCKWISE_90:
            return f + 270.0F;
         default:
            return f;
      }
   }

   /**
    * Transforms the entity's current yaw with the given Mirror and returns it. This does not have a side-effect.
    */
   public float mirror(Mirror pTransformMirror) {
      return this.rotate(pTransformMirror.getRotation(this.direction));
   }

   public void thunderHit(ServerLevel pLevel, LightningBolt pLightning) {
   }

   public void refreshDimensions() {
   }
}
