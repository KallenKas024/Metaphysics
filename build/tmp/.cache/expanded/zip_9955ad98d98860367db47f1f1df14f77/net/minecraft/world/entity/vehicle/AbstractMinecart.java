package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecart extends Entity implements net.minecraftforge.common.extensions.IForgeAbstractMinecart {
   private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
   private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1));
   protected static final float WATER_SLOWDOWN_FACTOR = 0.95F;
   private boolean flipped;
   private boolean onRails;
   private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), (p_38135_) -> {
      Vec3i vec3i = Direction.WEST.getNormal();
      Vec3i vec3i1 = Direction.EAST.getNormal();
      Vec3i vec3i2 = Direction.NORTH.getNormal();
      Vec3i vec3i3 = Direction.SOUTH.getNormal();
      Vec3i vec3i4 = vec3i.below();
      Vec3i vec3i5 = vec3i1.below();
      Vec3i vec3i6 = vec3i2.below();
      Vec3i vec3i7 = vec3i3.below();
      p_38135_.put(RailShape.NORTH_SOUTH, Pair.of(vec3i2, vec3i3));
      p_38135_.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i1));
      p_38135_.put(RailShape.ASCENDING_EAST, Pair.of(vec3i4, vec3i1));
      p_38135_.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i5));
      p_38135_.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i2, vec3i7));
      p_38135_.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i6, vec3i3));
      p_38135_.put(RailShape.SOUTH_EAST, Pair.of(vec3i3, vec3i1));
      p_38135_.put(RailShape.SOUTH_WEST, Pair.of(vec3i3, vec3i));
      p_38135_.put(RailShape.NORTH_WEST, Pair.of(vec3i2, vec3i));
      p_38135_.put(RailShape.NORTH_EAST, Pair.of(vec3i2, vec3i1));
   });
   private static net.minecraftforge.common.IMinecartCollisionHandler COLLISIONS = null;
   private int lSteps;
   private double lx;
   private double ly;
   private double lz;
   private double lyr;
   private double lxr;
   private double lxd;
   private double lyd;
   private double lzd;
   private boolean canBePushed = true;

   protected AbstractMinecart(EntityType<?> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.blocksBuilding = true;
   }

   protected AbstractMinecart(EntityType<?> pEntityType, Level pLevel, double pX, double pY, double pZ) {
      this(pEntityType, pLevel);
      this.setPos(pX, pY, pZ);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
   }

   public net.minecraftforge.common.IMinecartCollisionHandler getCollisionHandler() {
      return COLLISIONS;
   }

   public static void registerCollisionHandler(@Nullable net.minecraftforge.common.IMinecartCollisionHandler handler) {
      COLLISIONS = handler;
   }

   public static AbstractMinecart createMinecart(Level pLevel, double pX, double pY, double pZ, AbstractMinecart.Type pType) {
      if (pType == AbstractMinecart.Type.CHEST) {
         return new MinecartChest(pLevel, pX, pY, pZ);
      } else if (pType == AbstractMinecart.Type.FURNACE) {
         return new MinecartFurnace(pLevel, pX, pY, pZ);
      } else if (pType == AbstractMinecart.Type.TNT) {
         return new MinecartTNT(pLevel, pX, pY, pZ);
      } else if (pType == AbstractMinecart.Type.SPAWNER) {
         return new MinecartSpawner(pLevel, pX, pY, pZ);
      } else if (pType == AbstractMinecart.Type.HOPPER) {
         return new MinecartHopper(pLevel, pX, pY, pZ);
      } else {
         return (AbstractMinecart)(pType == AbstractMinecart.Type.COMMAND_BLOCK ? new MinecartCommandBlock(pLevel, pX, pY, pZ) : new Minecart(pLevel, pX, pY, pZ));
      }
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ID_HURT, 0);
      this.entityData.define(DATA_ID_HURTDIR, 1);
      this.entityData.define(DATA_ID_DAMAGE, 0.0F);
      this.entityData.define(DATA_ID_DISPLAY_BLOCK, Block.getId(Blocks.AIR.defaultBlockState()));
      this.entityData.define(DATA_ID_DISPLAY_OFFSET, 6);
      this.entityData.define(DATA_ID_CUSTOM_DISPLAY, false);
   }

   public boolean canCollideWith(Entity pEntity) {
      return Boat.canVehicleCollide(this, pEntity);
   }

   /**
    * Returns {@code true} if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return canBePushed;
   }

   protected Vec3 getRelativePortalPosition(Direction.Axis pAxis, BlockUtil.FoundRectangle pPortal) {
      return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(pAxis, pPortal));
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return 0.0D;
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
      Direction direction = this.getMotionDirection();
      if (direction.getAxis() == Direction.Axis.Y) {
         return super.getDismountLocationForPassenger(pLivingEntity);
      } else {
         int[][] aint = DismountHelper.offsetsForDirection(direction);
         BlockPos blockpos = this.blockPosition();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         ImmutableList<Pose> immutablelist = pLivingEntity.getDismountPoses();

         for(Pose pose : immutablelist) {
            EntityDimensions entitydimensions = pLivingEntity.getDimensions(pose);
            float f = Math.min(entitydimensions.width, 1.0F) / 2.0F;

            for(int i : POSE_DISMOUNT_HEIGHTS.get(pose)) {
               for(int[] aint1 : aint) {
                  blockpos$mutableblockpos.set(blockpos.getX() + aint1[0], blockpos.getY() + i, blockpos.getZ() + aint1[1]);
                  double d0 = this.level().getBlockFloorHeight(DismountHelper.nonClimbableShape(this.level(), blockpos$mutableblockpos), () -> {
                     return DismountHelper.nonClimbableShape(this.level(), blockpos$mutableblockpos.below());
                  });
                  if (DismountHelper.isBlockFloorValid(d0)) {
                     AABB aabb = new AABB((double)(-f), 0.0D, (double)(-f), (double)f, (double)entitydimensions.height, (double)f);
                     Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos$mutableblockpos, d0);
                     if (DismountHelper.canDismountTo(this.level(), pLivingEntity, aabb.move(vec3))) {
                        pLivingEntity.setPose(pose);
                        return vec3;
                     }
                  }
               }
            }
         }

         double d1 = this.getBoundingBox().maxY;
         blockpos$mutableblockpos.set((double)blockpos.getX(), d1, (double)blockpos.getZ());

         for(Pose pose1 : immutablelist) {
            double d2 = (double)pLivingEntity.getDimensions(pose1).height;
            int j = Mth.ceil(d1 - (double)blockpos$mutableblockpos.getY() + d2);
            double d3 = DismountHelper.findCeilingFrom(blockpos$mutableblockpos, j, (p_289495_) -> {
               return this.level().getBlockState(p_289495_).getCollisionShape(this.level(), p_289495_);
            });
            if (d1 + d2 <= d3) {
               pLivingEntity.setPose(pose1);
               break;
            }
         }

         return super.getDismountLocationForPassenger(pLivingEntity);
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!this.level().isClientSide && !this.isRemoved()) {
         if (this.isInvulnerableTo(pSource)) {
            return false;
         } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + pAmount * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, pSource.getEntity());
            boolean flag = pSource.getEntity() instanceof Player && ((Player)pSource.getEntity()).getAbilities().instabuild;
            if (flag || this.getDamage() > 40.0F) {
               this.ejectPassengers();
               if (flag && !this.hasCustomName()) {
                  this.discard();
               } else {
                  this.destroy(pSource);
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   protected float getBlockSpeedFactor() {
      BlockState blockstate = this.level().getBlockState(this.blockPosition());
      return blockstate.is(BlockTags.RAILS) ? 1.0F : super.getBlockSpeedFactor();
   }

   public void destroy(DamageSource pDamageSource) {
      this.kill();
      if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         ItemStack itemstack = new ItemStack(this.getDropItem());
         if (this.hasCustomName()) {
            itemstack.setHoverName(this.getCustomName());
         }

         this.spawnAtLocation(itemstack);
      }

   }

   protected abstract Item getDropItem();

   public void animateHurt(float pYaw) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
   }

   /**
    * Returns {@code true} if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return !this.isRemoved();
   }

   private static Pair<Vec3i, Vec3i> exits(RailShape pShape) {
      return EXITS.get(pShape);
   }

   /**
    * Gets the horizontal facing direction of this Entity, adjusted to take specially-treated entity types into account.
    */
   public Direction getMotionDirection() {
      return this.flipped ? this.getDirection().getOpposite().getClockWise() : this.getDirection().getClockWise();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (this.getHurtTime() > 0) {
         this.setHurtTime(this.getHurtTime() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      this.checkBelowWorld();
      this.handleNetherPortal();
      if (this.level().isClientSide) {
         if (this.lSteps > 0) {
            double d5 = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
            double d6 = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
            double d7 = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
            double d2 = Mth.wrapDegrees(this.lyr - (double)this.getYRot());
            this.setYRot(this.getYRot() + (float)d2 / (float)this.lSteps);
            this.setXRot(this.getXRot() + (float)(this.lxr - (double)this.getXRot()) / (float)this.lSteps);
            --this.lSteps;
            this.setPos(d5, d6, d7);
            this.setRot(this.getYRot(), this.getXRot());
         } else {
            this.reapplyPosition();
            this.setRot(this.getYRot(), this.getXRot());
         }

      } else {
         if (!this.isNoGravity()) {
            double d0 = this.isInWater() ? -0.005D : -0.04D;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d0, 0.0D));
         }

         int k = Mth.floor(this.getX());
         int i = Mth.floor(this.getY());
         int j = Mth.floor(this.getZ());
         if (this.level().getBlockState(new BlockPos(k, i - 1, j)).is(BlockTags.RAILS)) {
            --i;
         }

         BlockPos blockpos = new BlockPos(k, i, j);
         BlockState blockstate = this.level().getBlockState(blockpos);
         this.onRails = BaseRailBlock.isRail(blockstate);
         if (canUseRail() && this.onRails) {
            this.moveAlongTrack(blockpos, blockstate);
            if (blockstate.getBlock() instanceof PoweredRailBlock && ((PoweredRailBlock) blockstate.getBlock()).isActivatorRail()) {
               this.activateMinecart(k, i, j, blockstate.getValue(PoweredRailBlock.POWERED));
            }
         } else {
            this.comeOffTrack();
         }

         this.checkInsideBlocks();
         this.setXRot(0.0F);
         double d1 = this.xo - this.getX();
         double d3 = this.zo - this.getZ();
         if (d1 * d1 + d3 * d3 > 0.001D) {
            this.setYRot((float)(Mth.atan2(d3, d1) * 180.0D / Math.PI));
            if (this.flipped) {
               this.setYRot(this.getYRot() + 180.0F);
            }
         }

         double d4 = (double)Mth.wrapDegrees(this.getYRot() - this.yRotO);
         if (d4 < -170.0D || d4 >= 170.0D) {
            this.setYRot(this.getYRot() + 180.0F);
            this.flipped = !this.flipped;
         }

         this.setRot(this.getYRot(), this.getXRot());
         AABB box;
         if (getCollisionHandler() != null) box = getCollisionHandler().getMinecartCollisionBox(this);
         else                               box = this.getBoundingBox().inflate(0.2F, 0.0D, 0.2F);
         if (canBeRidden() && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
            List<Entity> list = this.level().getEntities(this, box, EntitySelector.pushableBy(this));
            if (!list.isEmpty()) {
               for(int l = 0; l < list.size(); ++l) {
                  Entity entity1 = list.get(l);
                  if (!(entity1 instanceof Player) && !(entity1 instanceof IronGolem) && !(entity1 instanceof AbstractMinecart) && !this.isVehicle() && !entity1.isPassenger()) {
                     entity1.startRiding(this);
                  } else {
                     entity1.push(this);
                  }
               }
            }
         } else {
            for(Entity entity : this.level().getEntities(this, box)) {
               if (!this.hasPassenger(entity) && entity.isPushable() && entity instanceof AbstractMinecart) {
                  entity.push(this);
               }
            }
         }

         this.updateInWaterStateAndDoFluidPushing();
         if (this.isInLava()) {
            this.lavaHurt();
            this.fallDistance *= 0.5F;
         }

         this.firstTick = false;
      }
   }

   /**
    * Gets the maximum speed for a minecart
    */
   protected double getMaxSpeed() {
      return (this.isInWater() ? 4.0D : 8.0D) / 20.0D;
   }

   /**
    * Called every tick the minecart is on an activator rail.
    */
   public void activateMinecart(int pX, int pY, int pZ, boolean pPowered) {
   }

   /**
    * Moves a minecart that is not attached to a rail
    */
   protected void comeOffTrack() {
      double d0 = this.onGround() ? this.getMaxSpeed() : getMaxSpeedAirLateral();
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(Mth.clamp(vec3.x, -d0, d0), vec3.y, Mth.clamp(vec3.z, -d0, d0));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
      }

      if (getMaxSpeedAirVertical() > 0 && getDeltaMovement().y > getMaxSpeedAirVertical()) {
          if(Math.abs(getDeltaMovement().x) < 0.3f && Math.abs(getDeltaMovement().z) < 0.3f)
              setDeltaMovement(new Vec3(getDeltaMovement().x, 0.15f, getDeltaMovement().z));
          else
              setDeltaMovement(new Vec3(getDeltaMovement().x, getMaxSpeedAirVertical(), getDeltaMovement().z));
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      if (!this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().scale(getDragAir()));
      }

   }

   protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
      this.resetFallDistance();
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      Vec3 vec3 = this.getPos(d0, d1, d2);
      d1 = (double)pPos.getY();
      boolean flag = false;
      boolean flag1 = false;
      BaseRailBlock baserailblock = (BaseRailBlock) pState.getBlock();
      if (baserailblock instanceof PoweredRailBlock && !((PoweredRailBlock) baserailblock).isActivatorRail()) {
         flag = pState.getValue(PoweredRailBlock.POWERED);
         flag1 = !flag;
      }

      double d3 = getSlopeAdjustment();
      if (this.isInWater()) {
         d3 *= 0.2D;
      }

      Vec3 vec31 = this.getDeltaMovement();
      RailShape railshape = ((BaseRailBlock)pState.getBlock()).getRailDirection(pState, this.level(), pPos, this);
      switch (railshape) {
         case ASCENDING_EAST:
            this.setDeltaMovement(vec31.add(-d3, 0.0D, 0.0D));
            ++d1;
            break;
         case ASCENDING_WEST:
            this.setDeltaMovement(vec31.add(d3, 0.0D, 0.0D));
            ++d1;
            break;
         case ASCENDING_NORTH:
            this.setDeltaMovement(vec31.add(0.0D, 0.0D, d3));
            ++d1;
            break;
         case ASCENDING_SOUTH:
            this.setDeltaMovement(vec31.add(0.0D, 0.0D, -d3));
            ++d1;
      }

      vec31 = this.getDeltaMovement();
      Pair<Vec3i, Vec3i> pair = exits(railshape);
      Vec3i vec3i = pair.getFirst();
      Vec3i vec3i1 = pair.getSecond();
      double d4 = (double)(vec3i1.getX() - vec3i.getX());
      double d5 = (double)(vec3i1.getZ() - vec3i.getZ());
      double d6 = Math.sqrt(d4 * d4 + d5 * d5);
      double d7 = vec31.x * d4 + vec31.z * d5;
      if (d7 < 0.0D) {
         d4 = -d4;
         d5 = -d5;
      }

      double d8 = Math.min(2.0D, vec31.horizontalDistance());
      vec31 = new Vec3(d8 * d4 / d6, vec31.y, d8 * d5 / d6);
      this.setDeltaMovement(vec31);
      Entity entity = this.getFirstPassenger();
      if (entity instanceof Player) {
         Vec3 vec32 = entity.getDeltaMovement();
         double d9 = vec32.horizontalDistanceSqr();
         double d11 = this.getDeltaMovement().horizontalDistanceSqr();
         if (d9 > 1.0E-4D && d11 < 0.01D) {
            this.setDeltaMovement(this.getDeltaMovement().add(vec32.x * 0.1D, 0.0D, vec32.z * 0.1D));
            flag1 = false;
         }
      }

      if (flag1 && shouldDoRailFunctions()) {
         double d22 = this.getDeltaMovement().horizontalDistance();
         if (d22 < 0.03D) {
            this.setDeltaMovement(Vec3.ZERO);
         } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
         }
      }

      double d23 = (double)pPos.getX() + 0.5D + (double)vec3i.getX() * 0.5D;
      double d10 = (double)pPos.getZ() + 0.5D + (double)vec3i.getZ() * 0.5D;
      double d12 = (double)pPos.getX() + 0.5D + (double)vec3i1.getX() * 0.5D;
      double d13 = (double)pPos.getZ() + 0.5D + (double)vec3i1.getZ() * 0.5D;
      d4 = d12 - d23;
      d5 = d13 - d10;
      double d14;
      if (d4 == 0.0D) {
         d14 = d2 - (double)pPos.getZ();
      } else if (d5 == 0.0D) {
         d14 = d0 - (double)pPos.getX();
      } else {
         double d15 = d0 - d23;
         double d16 = d2 - d10;
         d14 = (d15 * d4 + d16 * d5) * 2.0D;
      }

      d0 = d23 + d4 * d14;
      d2 = d10 + d5 * d14;
      this.setPos(d0, d1, d2);
      this.moveMinecartOnRail(pPos);
      if (vec3i.getY() != 0 && Mth.floor(this.getX()) - pPos.getX() == vec3i.getX() && Mth.floor(this.getZ()) - pPos.getZ() == vec3i.getZ()) {
         this.setPos(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
      } else if (vec3i1.getY() != 0 && Mth.floor(this.getX()) - pPos.getX() == vec3i1.getX() && Mth.floor(this.getZ()) - pPos.getZ() == vec3i1.getZ()) {
         this.setPos(this.getX(), this.getY() + (double)vec3i1.getY(), this.getZ());
      }

      this.applyNaturalSlowdown();
      Vec3 vec33 = this.getPos(this.getX(), this.getY(), this.getZ());
      if (vec33 != null && vec3 != null) {
         double d17 = (vec3.y - vec33.y) * 0.05D;
         Vec3 vec34 = this.getDeltaMovement();
         double d18 = vec34.horizontalDistance();
         if (d18 > 0.0D) {
            this.setDeltaMovement(vec34.multiply((d18 + d17) / d18, 1.0D, (d18 + d17) / d18));
         }

         this.setPos(this.getX(), vec33.y, this.getZ());
      }

      int j = Mth.floor(this.getX());
      int i = Mth.floor(this.getZ());
      if (j != pPos.getX() || i != pPos.getZ()) {
         Vec3 vec35 = this.getDeltaMovement();
         double d26 = vec35.horizontalDistance();
         this.setDeltaMovement(d26 * (double)(j - pPos.getX()), vec35.y, d26 * (double)(i - pPos.getZ()));
      }

      if (shouldDoRailFunctions())
          baserailblock.onMinecartPass(pState, level(), pPos, this);

      if (flag && shouldDoRailFunctions()) {
         Vec3 vec36 = this.getDeltaMovement();
         double d27 = vec36.horizontalDistance();
         if (d27 > 0.01D) {
            double d19 = 0.06D;
            this.setDeltaMovement(vec36.add(vec36.x / d27 * 0.06D, 0.0D, vec36.z / d27 * 0.06D));
         } else {
            Vec3 vec37 = this.getDeltaMovement();
            double d20 = vec37.x;
            double d21 = vec37.z;
            if (railshape == RailShape.EAST_WEST) {
               if (this.isRedstoneConductor(pPos.west())) {
                  d20 = 0.02D;
               } else if (this.isRedstoneConductor(pPos.east())) {
                  d20 = -0.02D;
               }
            } else {
               if (railshape != RailShape.NORTH_SOUTH) {
                  return;
               }

               if (this.isRedstoneConductor(pPos.north())) {
                  d21 = 0.02D;
               } else if (this.isRedstoneConductor(pPos.south())) {
                  d21 = -0.02D;
               }
            }

            this.setDeltaMovement(d20, vec37.y, d21);
         }
      }

   }

   public boolean isOnRails() {
      return this.onRails;
   }

   private boolean isRedstoneConductor(BlockPos pPos) {
      return this.level().getBlockState(pPos).isRedstoneConductor(this.level(), pPos);
   }

   protected void applyNaturalSlowdown() {
      double d0 = this.isVehicle() ? 0.997D : 0.96D;
      Vec3 vec3 = this.getDeltaMovement();
      vec3 = vec3.multiply(d0, 0.0D, d0);
      if (this.isInWater()) {
         vec3 = vec3.scale((double)0.95F);
      }

      this.setDeltaMovement(vec3);
   }

   @Nullable
   public Vec3 getPosOffs(double pX, double pY, double pZ, double pOffset) {
      int i = Mth.floor(pX);
      int j = Mth.floor(pY);
      int k = Mth.floor(pZ);
      if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
         --j;
      }

      BlockState blockstate = this.level().getBlockState(new BlockPos(i, j, k));
      if (BaseRailBlock.isRail(blockstate)) {
         RailShape railshape = ((BaseRailBlock)blockstate.getBlock()).getRailDirection(blockstate, this.level(), new BlockPos(i, j, k), this);
         pY = (double)j;
         if (railshape.isAscending()) {
            pY = (double)(j + 1);
         }

         Pair<Vec3i, Vec3i> pair = exits(railshape);
         Vec3i vec3i = pair.getFirst();
         Vec3i vec3i1 = pair.getSecond();
         double d0 = (double)(vec3i1.getX() - vec3i.getX());
         double d1 = (double)(vec3i1.getZ() - vec3i.getZ());
         double d2 = Math.sqrt(d0 * d0 + d1 * d1);
         d0 /= d2;
         d1 /= d2;
         pX += d0 * pOffset;
         pZ += d1 * pOffset;
         if (vec3i.getY() != 0 && Mth.floor(pX) - i == vec3i.getX() && Mth.floor(pZ) - k == vec3i.getZ()) {
            pY += (double)vec3i.getY();
         } else if (vec3i1.getY() != 0 && Mth.floor(pX) - i == vec3i1.getX() && Mth.floor(pZ) - k == vec3i1.getZ()) {
            pY += (double)vec3i1.getY();
         }

         return this.getPos(pX, pY, pZ);
      } else {
         return null;
      }
   }

   @Nullable
   public Vec3 getPos(double pX, double pY, double pZ) {
      int i = Mth.floor(pX);
      int j = Mth.floor(pY);
      int k = Mth.floor(pZ);
      if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
         --j;
      }

      BlockState blockstate = this.level().getBlockState(new BlockPos(i, j, k));
      if (BaseRailBlock.isRail(blockstate)) {
         RailShape railshape = ((BaseRailBlock)blockstate.getBlock()).getRailDirection(blockstate, this.level(), new BlockPos(i, j, k), this);
         Pair<Vec3i, Vec3i> pair = exits(railshape);
         Vec3i vec3i = pair.getFirst();
         Vec3i vec3i1 = pair.getSecond();
         double d0 = (double)i + 0.5D + (double)vec3i.getX() * 0.5D;
         double d1 = (double)j + 0.0625D + (double)vec3i.getY() * 0.5D;
         double d2 = (double)k + 0.5D + (double)vec3i.getZ() * 0.5D;
         double d3 = (double)i + 0.5D + (double)vec3i1.getX() * 0.5D;
         double d4 = (double)j + 0.0625D + (double)vec3i1.getY() * 0.5D;
         double d5 = (double)k + 0.5D + (double)vec3i1.getZ() * 0.5D;
         double d6 = d3 - d0;
         double d7 = (d4 - d1) * 2.0D;
         double d8 = d5 - d2;
         double d9;
         if (d6 == 0.0D) {
            d9 = pZ - (double)k;
         } else if (d8 == 0.0D) {
            d9 = pX - (double)i;
         } else {
            double d10 = pX - d0;
            double d11 = pZ - d2;
            d9 = (d10 * d6 + d11 * d8) * 2.0D;
         }

         pX = d0 + d6 * d9;
         pY = d1 + d7 * d9;
         pZ = d2 + d8 * d9;
         if (d7 < 0.0D) {
            ++pY;
         } else if (d7 > 0.0D) {
            pY += 0.5D;
         }

         return new Vec3(pX, pY, pZ);
      } else {
         return null;
      }
   }

   /**
    * Gets the bounding box of this Entity, adjusted to take auxiliary entities into account (e.g. the tile contained by
    * a minecart, such as a command block).
    */
   public AABB getBoundingBoxForCulling() {
      AABB aabb = this.getBoundingBox();
      return this.hasCustomDisplay() ? aabb.inflate((double)Math.abs(this.getDisplayOffset()) / 16.0D) : aabb;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      if (pCompound.getBoolean("CustomDisplayTile")) {
         this.setDisplayBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), pCompound.getCompound("DisplayState")));
         this.setDisplayOffset(pCompound.getInt("DisplayOffset"));
      }

   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      if (this.hasCustomDisplay()) {
         pCompound.putBoolean("CustomDisplayTile", true);
         pCompound.put("DisplayState", NbtUtils.writeBlockState(this.getDisplayBlockState()));
         pCompound.putInt("DisplayOffset", this.getDisplayOffset());
      }

   }

   /**
    * Applies a velocity to the entities, to push them away from each other.
    */
   public void push(Entity pEntity) {
      if (getCollisionHandler() != null) {
         getCollisionHandler().onEntityCollision(this, pEntity);
         return;
      }
      if (!this.level().isClientSide) {
         if (!pEntity.noPhysics && !this.noPhysics) {
            if (!this.hasPassenger(pEntity)) {
               double d0 = pEntity.getX() - this.getX();
               double d1 = pEntity.getZ() - this.getZ();
               double d2 = d0 * d0 + d1 * d1;
               if (d2 >= (double)1.0E-4F) {
                  d2 = Math.sqrt(d2);
                  d0 /= d2;
                  d1 /= d2;
                  double d3 = 1.0D / d2;
                  if (d3 > 1.0D) {
                     d3 = 1.0D;
                  }

                  d0 *= d3;
                  d1 *= d3;
                  d0 *= (double)0.1F;
                  d1 *= (double)0.1F;
                  d0 *= 0.5D;
                  d1 *= 0.5D;
                  if (pEntity instanceof AbstractMinecart) {
                     double d4 = pEntity.getX() - this.getX();
                     double d5 = pEntity.getZ() - this.getZ();
                     Vec3 vec3 = (new Vec3(d4, 0.0D, d5)).normalize();
                     Vec3 vec31 = (new Vec3((double)Mth.cos(this.getYRot() * ((float)Math.PI / 180F)), 0.0D, (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)))).normalize();
                     double d6 = Math.abs(vec3.dot(vec31));
                     if (d6 < (double)0.8F) {
                        return;
                     }

                     Vec3 vec32 = this.getDeltaMovement();
                     Vec3 vec33 = pEntity.getDeltaMovement();
                     if (((AbstractMinecart)pEntity).isPoweredCart() && !this.isPoweredCart()) {
                        this.setDeltaMovement(vec32.multiply(0.2D, 1.0D, 0.2D));
                        this.push(vec33.x - d0, 0.0D, vec33.z - d1);
                        pEntity.setDeltaMovement(vec33.multiply(0.95D, 1.0D, 0.95D));
                     } else if (!((AbstractMinecart)pEntity).isPoweredCart() && this.isPoweredCart()) {
                        pEntity.setDeltaMovement(vec33.multiply(0.2D, 1.0D, 0.2D));
                        pEntity.push(vec32.x + d0, 0.0D, vec32.z + d1);
                        this.setDeltaMovement(vec32.multiply(0.95D, 1.0D, 0.95D));
                     } else {
                        double d7 = (vec33.x + vec32.x) / 2.0D;
                        double d8 = (vec33.z + vec32.z) / 2.0D;
                        this.setDeltaMovement(vec32.multiply(0.2D, 1.0D, 0.2D));
                        this.push(d7 - d0, 0.0D, d8 - d1);
                        pEntity.setDeltaMovement(vec33.multiply(0.2D, 1.0D, 0.2D));
                        pEntity.push(d7 + d0, 0.0D, d8 + d1);
                     }
                  } else {
                     this.push(-d0, 0.0D, -d1);
                     pEntity.push(d0 / 4.0D, 0.0D, d1 / 4.0D);
                  }
               }

            }
         }
      }
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
      this.lx = pX;
      this.ly = pY;
      this.lz = pZ;
      this.lyr = (double)pYaw;
      this.lxr = (double)pPitch;
      this.lSteps = pPosRotationIncrements + 2;
      this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
   }

   /**
    * Updates the entity motion clientside, called by packets from the server
    */
   public void lerpMotion(double pX, double pY, double pZ) {
      this.lxd = pX;
      this.lyd = pY;
      this.lzd = pZ;
      this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
   }

   /**
    * Sets the current amount of damage the minecart has taken. Decreases over time. The cart breaks when this is over
    * 40.
    */
   public void setDamage(float pDamage) {
      this.entityData.set(DATA_ID_DAMAGE, pDamage);
   }

   /**
    * Gets the current amount of damage the minecart has taken. Decreases over time. The cart breaks when this is over
    * 40.
    */
   public float getDamage() {
      return this.entityData.get(DATA_ID_DAMAGE);
   }

   /**
    * Sets the rolling amplitude the cart rolls while being attacked.
    */
   public void setHurtTime(int pHurtTime) {
      this.entityData.set(DATA_ID_HURT, pHurtTime);
   }

   /**
    * Gets the rolling amplitude the cart rolls while being attacked.
    */
   public int getHurtTime() {
      return this.entityData.get(DATA_ID_HURT);
   }

   /**
    * Sets the rolling direction the cart rolls while being attacked. Can be 1 or -1.
    */
   public void setHurtDir(int pHurtDirection) {
      this.entityData.set(DATA_ID_HURTDIR, pHurtDirection);
   }

   /**
    * Gets the rolling direction the cart rolls while being attacked. Can be 1 or -1.
    */
   public int getHurtDir() {
      return this.entityData.get(DATA_ID_HURTDIR);
   }

   public abstract AbstractMinecart.Type getMinecartType();

   public BlockState getDisplayBlockState() {
      return !this.hasCustomDisplay() ? this.getDefaultDisplayBlockState() : Block.stateById(this.getEntityData().get(DATA_ID_DISPLAY_BLOCK));
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.AIR.defaultBlockState();
   }

   public int getDisplayOffset() {
      return !this.hasCustomDisplay() ? this.getDefaultDisplayOffset() : this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
   }

   public int getDefaultDisplayOffset() {
      return 6;
   }

   public void setDisplayBlockState(BlockState pDisplayState) {
      this.getEntityData().set(DATA_ID_DISPLAY_BLOCK, Block.getId(pDisplayState));
      this.setCustomDisplay(true);
   }

   public void setDisplayOffset(int pDisplayOffset) {
      this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, pDisplayOffset);
      this.setCustomDisplay(true);
   }

   public boolean hasCustomDisplay() {
      return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY);
   }

   public void setCustomDisplay(boolean pCustomDisplay) {
      this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY, pCustomDisplay);
   }

   // Forge Start
   private boolean canUseRail = true;
   @Override public boolean canUseRail() { return canUseRail; }
   @Override public void setCanUseRail(boolean value) { this.canUseRail = value; }
   private float currentSpeedCapOnRail = getMaxCartSpeedOnRail();
   @Override public float getCurrentCartSpeedCapOnRail() { return currentSpeedCapOnRail; }
   @Override public void setCurrentCartSpeedCapOnRail(float value) { currentSpeedCapOnRail = Math.min(value, getMaxCartSpeedOnRail()); }
   @org.jetbrains.annotations.Nullable private Float maxSpeedAirLateral = null;
   @Override public float getMaxSpeedAirLateral() { return maxSpeedAirLateral == null ? (float) this.getMaxSpeed() : maxSpeedAirLateral; }
   @Override public void setMaxSpeedAirLateral(float value) { maxSpeedAirLateral = value; }
   private float maxSpeedAirVertical = DEFAULT_MAX_SPEED_AIR_VERTICAL;
   @Override public float getMaxSpeedAirVertical() { return maxSpeedAirVertical; }
   @Override public void setMaxSpeedAirVertical(float value) { maxSpeedAirVertical = value; }
   private double dragAir = DEFAULT_AIR_DRAG;
   @Override public double getDragAir() { return dragAir; }
   @Override public void setDragAir(double value) { dragAir = value; }
   @Override
   public double getMaxSpeedWithRail() { //Non-default because getMaximumSpeed is protected
      if (!canUseRail()) return getMaxSpeed();
      BlockPos pos = this.getCurrentRailPosition();
      BlockState state = this.level().getBlockState(pos);
      if (!state.is(BlockTags.RAILS)) return getMaxSpeed();

      float railMaxSpeed = ((BaseRailBlock)state.getBlock()).getRailMaxSpeed(state, this.level(), pos, this);
      return Math.min(railMaxSpeed, getCurrentCartSpeedCapOnRail());
   }
   @Override
   public void moveMinecartOnRail(BlockPos pos) { //Non-default because getMaximumSpeed is protected
      AbstractMinecart mc = this;
      double d24 = mc.isVehicle() ? 0.75D : 1.0D;
      double d25 = mc.getMaxSpeedWithRail();
      Vec3 vec3d1 = mc.getDeltaMovement();
      mc.move(MoverType.SELF, new Vec3(Mth.clamp(d24 * vec3d1.x, -d25, d25), 0.0D, Mth.clamp(d24 * vec3d1.z, -d25, d25)));
   }
   // Forge end

   public ItemStack getPickResult() {
      Item item;
      switch (this.getMinecartType()) {
         case FURNACE:
            item = Items.FURNACE_MINECART;
            break;
         case CHEST:
            item = Items.CHEST_MINECART;
            break;
         case TNT:
            item = Items.TNT_MINECART;
            break;
         case HOPPER:
            item = Items.HOPPER_MINECART;
            break;
         case COMMAND_BLOCK:
            item = Items.COMMAND_BLOCK_MINECART;
            break;
         default:
            item = Items.MINECART;
      }

      return new ItemStack(item);
   }

   public static enum Type {
      RIDEABLE,
      CHEST,
      FURNACE,
      TNT,
      SPAWNER,
      HOPPER,
      COMMAND_BLOCK;
   }
}
