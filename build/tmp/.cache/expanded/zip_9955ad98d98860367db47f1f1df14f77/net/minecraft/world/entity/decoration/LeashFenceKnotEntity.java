package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LeashFenceKnotEntity extends HangingEntity {
   public static final double OFFSET_Y = 0.375D;

   public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public LeashFenceKnotEntity(Level pLevel, BlockPos pPos) {
      super(EntityType.LEASH_KNOT, pLevel, pPos);
      this.setPos((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());
   }

   /**
    * Updates the entity bounding box based on current facing
    */
   protected void recalculateBoundingBox() {
      this.setPosRaw((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.375D, (double)this.pos.getZ() + 0.5D);
      double d0 = (double)this.getType().getWidth() / 2.0D;
      double d1 = (double)this.getType().getHeight();
      this.setBoundingBox(new AABB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0, this.getY() + d1, this.getZ() + d0));
   }

   /**
    * Updates facing and bounding box based on it
    */
   public void setDirection(Direction pFacingDirection) {
   }

   public int getWidth() {
      return 9;
   }

   public int getHeight() {
      return 9;
   }

   protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 0.0625F;
   }

   /**
    * Checks if the entity is in range to render.
    */
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      return pDistance < 1024.0D;
   }

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public void dropItem(@Nullable Entity pBrokenEntity) {
      this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
   }

   public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      if (this.level().isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         boolean flag = false;
         double d0 = 7.0D;
         List<Mob> list = this.level().getEntitiesOfClass(Mob.class, new AABB(this.getX() - 7.0D, this.getY() - 7.0D, this.getZ() - 7.0D, this.getX() + 7.0D, this.getY() + 7.0D, this.getZ() + 7.0D));

         for(Mob mob : list) {
            if (mob.getLeashHolder() == pPlayer) {
               mob.setLeashedTo(this, true);
               flag = true;
            }
         }

         boolean flag1 = false;
         if (!flag) {
            this.discard();
            if (pPlayer.getAbilities().instabuild) {
               for(Mob mob1 : list) {
                  if (mob1.isLeashed() && mob1.getLeashHolder() == this) {
                     mob1.dropLeash(true, false);
                     flag1 = true;
                  }
               }
            }
         }

         if (flag || flag1) {
            this.gameEvent(GameEvent.BLOCK_ATTACH, pPlayer);
         }

         return InteractionResult.CONSUME;
      }
   }

   /**
    * Checks to make sure the {@code HangingEntity} can be placed there.
    */
   public boolean survives() {
      return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
   }

   public static LeashFenceKnotEntity getOrCreateKnot(Level pLevel, BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();

      for(LeashFenceKnotEntity leashfenceknotentity : pLevel.getEntitiesOfClass(LeashFenceKnotEntity.class, new AABB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D))) {
         if (leashfenceknotentity.getPos().equals(pPos)) {
            return leashfenceknotentity;
         }
      }

      LeashFenceKnotEntity leashfenceknotentity1 = new LeashFenceKnotEntity(pLevel, pPos);
      pLevel.addFreshEntity(leashfenceknotentity1);
      return leashfenceknotentity1;
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, 0, this.getPos());
   }

   public Vec3 getRopeHoldPosition(float pPartialTicks) {
      return this.getPosition(pPartialTicks).add(0.0D, 0.2D, 0.0D);
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.LEAD);
   }
}