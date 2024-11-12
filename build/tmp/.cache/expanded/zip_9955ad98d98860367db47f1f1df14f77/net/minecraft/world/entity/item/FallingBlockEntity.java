package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class FallingBlockEntity extends Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private BlockState blockState = Blocks.SAND.defaultBlockState();
   public int time;
   public boolean dropItem = true;
   private boolean cancelDrop;
   private boolean hurtEntities;
   private int fallDamageMax = 40;
   private float fallDamagePerDistance;
   @Nullable
   public CompoundTag blockData;
   protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

   public FallingBlockEntity(EntityType<? extends FallingBlockEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   private FallingBlockEntity(Level pLevel, double pX, double pY, double pZ, BlockState pState) {
      this(EntityType.FALLING_BLOCK, pLevel);
      this.blockState = pState;
      this.blocksBuilding = true;
      this.setPos(pX, pY, pZ);
      this.setDeltaMovement(Vec3.ZERO);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
      this.setStartPos(this.blockPosition());
   }

   public static FallingBlockEntity fall(Level pLevel, BlockPos pPos, BlockState pBlockState) {
      FallingBlockEntity fallingblockentity = new FallingBlockEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, pBlockState.hasProperty(BlockStateProperties.WATERLOGGED) ? pBlockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : pBlockState);
      pLevel.setBlock(pPos, pBlockState.getFluidState().createLegacyBlock(), 3);
      pLevel.addFreshEntity(fallingblockentity);
      return fallingblockentity;
   }

   /**
    * Returns {@code true} if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }

   public void setStartPos(BlockPos pStartPos) {
      this.entityData.set(DATA_START_POS, pStartPos);
   }

   public BlockPos getStartPos() {
      return this.entityData.get(DATA_START_POS);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_START_POS, BlockPos.ZERO);
   }

   /**
    * Returns {@code true} if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return !this.isRemoved();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (this.blockState.isAir()) {
         this.discard();
      } else {
         Block block = this.blockState.getBlock();
         ++this.time;
         if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
         }

         this.move(MoverType.SELF, this.getDeltaMovement());
         if (!this.level().isClientSide) {
            BlockPos blockpos = this.blockPosition();
            boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock;
            boolean flag1 = flag && this.blockState.canBeHydrated(this.level(), blockpos, this.level().getFluidState(blockpos), blockpos);
            double d0 = this.getDeltaMovement().lengthSqr();
            if (flag && d0 > 1.0D) {
               BlockHitResult blockhitresult = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
               if (blockhitresult.getType() != HitResult.Type.MISS && this.blockState.canBeHydrated(this.level(), blockpos, this.level().getFluidState(blockhitresult.getBlockPos()), blockhitresult.getBlockPos())) {
                  blockpos = blockhitresult.getBlockPos();
                  flag1 = true;
               }
            }

            if (!this.onGround() && !flag1) {
               if (!this.level().isClientSide && (this.time > 100 && (blockpos.getY() <= this.level().getMinBuildHeight() || blockpos.getY() > this.level().getMaxBuildHeight()) || this.time > 600)) {
                  if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                     this.spawnAtLocation(block);
                  }

                  this.discard();
               }
            } else {
               BlockState blockstate = this.level().getBlockState(blockpos);
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
               if (!blockstate.is(Blocks.MOVING_PISTON)) {
                  if (!this.cancelDrop) {
                     boolean flag2 = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level(), blockpos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                     boolean flag3 = FallingBlock.isFree(this.level().getBlockState(blockpos.below())) && (!flag || !flag1);
                     boolean flag4 = this.blockState.canSurvive(this.level(), blockpos) && !flag3;
                     if (flag2 && flag4) {
                        if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(blockpos).getType() == Fluids.WATER) {
                           this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
                        }

                        if (this.level().setBlock(blockpos, this.blockState, 3)) {
                           ((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.level().getBlockState(blockpos)));
                           this.discard();
                           if (block instanceof Fallable) {
                              ((Fallable)block).onLand(this.level(), blockpos, this.blockState, blockstate, this);
                           }

                           if (this.blockData != null && this.blockState.hasBlockEntity()) {
                              BlockEntity blockentity = this.level().getBlockEntity(blockpos);
                              if (blockentity != null) {
                                 CompoundTag compoundtag = blockentity.saveWithoutMetadata();

                                 for(String s : this.blockData.getAllKeys()) {
                                    compoundtag.put(s, this.blockData.get(s).copy());
                                 }

                                 try {
                                    blockentity.load(compoundtag);
                                 } catch (Exception exception) {
                                    LOGGER.error("Failed to load block entity from falling block", (Throwable)exception);
                                 }

                                 blockentity.setChanged();
                              }
                           }
                        } else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                           this.discard();
                           this.callOnBrokenAfterFall(block, blockpos);
                           this.spawnAtLocation(block);
                        }
                     } else {
                        this.discard();
                        if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                           this.callOnBrokenAfterFall(block, blockpos);
                           this.spawnAtLocation(block);
                        }
                     }
                  } else {
                     this.discard();
                     this.callOnBrokenAfterFall(block, blockpos);
                  }
               }
            }
         }

         this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
      }
   }

   public void callOnBrokenAfterFall(Block pBlock, BlockPos pPos) {
      if (pBlock instanceof Fallable) {
         ((Fallable)pBlock).onBrokenAfterFall(this.level(), pPos, this);
      }

   }

   public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
      if (!this.hurtEntities) {
         return false;
      } else {
         int i = Mth.ceil(pFallDistance - 1.0F);
         if (i < 0) {
            return false;
         } else {
            Predicate<Entity> predicate = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
            Block $$8 = this.blockState.getBlock();
            DamageSource damagesource1;
            if ($$8 instanceof Fallable) {
               Fallable fallable = (Fallable)$$8;
               damagesource1 = fallable.getFallDamageSource(this);
            } else {
               damagesource1 = this.damageSources().fallingBlock(this);
            }

            DamageSource damagesource = damagesource1;
            float f = (float)Math.min(Mth.floor((float)i * this.fallDamagePerDistance), this.fallDamageMax);
            this.level().getEntities(this, this.getBoundingBox(), predicate).forEach((p_149649_) -> {
               p_149649_.hurt(damagesource, f);
            });
            boolean flag = this.blockState.is(BlockTags.ANVIL);
            if (flag && f > 0.0F && this.random.nextFloat() < 0.05F + (float)i * 0.05F) {
               BlockState blockstate = AnvilBlock.damage(this.blockState);
               if (blockstate == null) {
                  this.cancelDrop = true;
               } else {
                  this.blockState = blockstate;
               }
            }

            return false;
         }
      }
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.put("BlockState", NbtUtils.writeBlockState(this.blockState));
      pCompound.putInt("Time", this.time);
      pCompound.putBoolean("DropItem", this.dropItem);
      pCompound.putBoolean("HurtEntities", this.hurtEntities);
      pCompound.putFloat("FallHurtAmount", this.fallDamagePerDistance);
      pCompound.putInt("FallHurtMax", this.fallDamageMax);
      if (this.blockData != null) {
         pCompound.put("TileEntityData", this.blockData);
      }

      pCompound.putBoolean("CancelDrop", this.cancelDrop);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      this.blockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), pCompound.getCompound("BlockState"));
      this.time = pCompound.getInt("Time");
      if (pCompound.contains("HurtEntities", 99)) {
         this.hurtEntities = pCompound.getBoolean("HurtEntities");
         this.fallDamagePerDistance = pCompound.getFloat("FallHurtAmount");
         this.fallDamageMax = pCompound.getInt("FallHurtMax");
      } else if (this.blockState.is(BlockTags.ANVIL)) {
         this.hurtEntities = true;
      }

      if (pCompound.contains("DropItem", 99)) {
         this.dropItem = pCompound.getBoolean("DropItem");
      }

      if (pCompound.contains("TileEntityData", 10)) {
         this.blockData = pCompound.getCompound("TileEntityData");
      }

      this.cancelDrop = pCompound.getBoolean("CancelDrop");
      if (this.blockState.isAir()) {
         this.blockState = Blocks.SAND.defaultBlockState();
      }

   }

   public void setHurtsEntities(float pFallDamagePerDistance, int pFallDamageMax) {
      this.hurtEntities = true;
      this.fallDamagePerDistance = pFallDamagePerDistance;
      this.fallDamageMax = pFallDamageMax;
   }

   public void disableDrop() {
      this.cancelDrop = true;
   }

   /**
    * Return whether this entity should be rendered as on fire.
    */
   public boolean displayFireAnimation() {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory pCategory) {
      super.fillCrashReportCategory(pCategory);
      pCategory.setDetail("Immitating BlockState", this.blockState.toString());
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   protected Component getTypeName() {
      return Component.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
   }

   /**
    * Checks if players can use this entity to access operator (permission level 2) commands either directly or
    * indirectly, such as give or setblock. A similar method exists for entities at {@link
    * net.minecraft.world.entity.Entity#onlyOpCanSetNbt()}.<p>For example, {@link
    * net.minecraft.world.entity.vehicle.MinecartCommandBlock#onlyOpCanSetNbt() command block minecarts} and {@link
    * net.minecraft.world.entity.vehicle.MinecartSpawner#onlyOpCanSetNbt() mob spawner minecarts} (spawning command
    * block minecarts or drops) are considered accessible.</p>@return true if this entity offers ways for unauthorized
    * players to use restricted commands
    */
   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
   }

   public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
      super.recreateFromPacket(pPacket);
      this.blockState = Block.stateById(pPacket.getData());
      this.blocksBuilding = true;
      double d0 = pPacket.getX();
      double d1 = pPacket.getY();
      double d2 = pPacket.getZ();
      this.setPos(d0, d1, d2);
      this.setStartPos(this.blockPosition());
   }
}
