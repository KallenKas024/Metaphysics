package net.minecraft.world.entity.decoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity implements VariantHolder<Holder<PaintingVariant>> {
   private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(Painting.class, EntityDataSerializers.PAINTING_VARIANT);
   private static final ResourceKey<PaintingVariant> DEFAULT_VARIANT = PaintingVariants.KEBAB;
   public static final String VARIANT_TAG = "variant";

   private static Holder<PaintingVariant> getDefaultVariant() {
      return BuiltInRegistries.PAINTING_VARIANT.getHolderOrThrow(DEFAULT_VARIANT);
   }

   public Painting(EntityType<? extends Painting> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_PAINTING_VARIANT_ID, getDefaultVariant());
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (DATA_PAINTING_VARIANT_ID.equals(pKey)) {
         this.recalculateBoundingBox();
      }

   }

   public void setVariant(Holder<PaintingVariant> pVariant) {
      this.entityData.set(DATA_PAINTING_VARIANT_ID, pVariant);
   }

   public Holder<PaintingVariant> getVariant() {
      return this.entityData.get(DATA_PAINTING_VARIANT_ID);
   }

   public static Optional<Painting> create(Level pLevel, BlockPos pPos, Direction pDirection) {
      Painting painting = new Painting(pLevel, pPos);
      List<Holder<PaintingVariant>> list = new ArrayList<>();
      BuiltInRegistries.PAINTING_VARIANT.getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(list::add);
      if (list.isEmpty()) {
         return Optional.empty();
      } else {
         painting.setDirection(pDirection);
         list.removeIf((p_289458_) -> {
            painting.setVariant(p_289458_);
            return !painting.survives();
         });
         if (list.isEmpty()) {
            return Optional.empty();
         } else {
            int i = list.stream().mapToInt(Painting::variantArea).max().orElse(0);
            list.removeIf((p_218883_) -> {
               return variantArea(p_218883_) < i;
            });
            Optional<Holder<PaintingVariant>> optional = Util.getRandomSafe(list, painting.random);
            if (optional.isEmpty()) {
               return Optional.empty();
            } else {
               painting.setVariant(optional.get());
               painting.setDirection(pDirection);
               return Optional.of(painting);
            }
         }
      }
   }

   private static int variantArea(Holder<PaintingVariant> p_218899_) {
      return p_218899_.value().getWidth() * p_218899_.value().getHeight();
   }

   private Painting(Level pLevel, BlockPos pPos) {
      super(EntityType.PAINTING, pLevel, pPos);
   }

   public Painting(Level pLevel, BlockPos pPos, Direction pDirection, Holder<PaintingVariant> pVariant) {
      this(pLevel, pPos);
      this.setVariant(pVariant);
      this.setDirection(pDirection);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      storeVariant(pCompound, this.getVariant());
      pCompound.putByte("facing", (byte)this.direction.get2DDataValue());
      super.addAdditionalSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      Holder<PaintingVariant> holder = loadVariant(pCompound).orElseGet(Painting::getDefaultVariant);
      this.setVariant(holder);
      this.direction = Direction.from2DDataValue(pCompound.getByte("facing"));
      super.readAdditionalSaveData(pCompound);
      this.setDirection(this.direction);
   }

   public static void storeVariant(CompoundTag pTag, Holder<PaintingVariant> pVariant) {
      pTag.putString("variant", pVariant.unwrapKey().orElse(DEFAULT_VARIANT).location().toString());
   }

   public static Optional<Holder<PaintingVariant>> loadVariant(CompoundTag pTag) {
      return Optional.ofNullable(ResourceLocation.tryParse(pTag.getString("variant"))).map((p_248378_) -> {
         return ResourceKey.create(Registries.PAINTING_VARIANT, p_248378_);
      }).flatMap(BuiltInRegistries.PAINTING_VARIANT::getHolder);
   }

   public int getWidth() {
      return this.getVariant().value().getWidth();
   }

   public int getHeight() {
      return this.getVariant().value().getHeight();
   }

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public void dropItem(@Nullable Entity pBrokenEntity) {
      if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
         if (pBrokenEntity instanceof Player) {
            Player player = (Player)pBrokenEntity;
            if (player.getAbilities().instabuild) {
               return;
            }
         }

         this.spawnAtLocation(Items.PAINTING);
      }
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
   }

   /**
    * Sets the location and rotation of the entity in the world.
    */
   public void moveTo(double pX, double pY, double pZ, float pYaw, float pPitch) {
      this.setPos(pX, pY, pZ);
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
      this.setPos(pX, pY, pZ);
   }

   public Vec3 trackingPosition() {
      return Vec3.atLowerCornerOf(this.pos);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
   }

   public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
      super.recreateFromPacket(pPacket);
      this.setDirection(Direction.from3DDataValue(pPacket.getData()));
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.PAINTING);
   }
}