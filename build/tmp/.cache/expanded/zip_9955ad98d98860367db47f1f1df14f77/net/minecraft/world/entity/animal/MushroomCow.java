package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.commons.lang3.tuple.Pair;

public class MushroomCow extends Cow implements Shearable, net.minecraftforge.common.IForgeShearable {
   private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.STRING);
   private static final int MUTATE_CHANCE = 1024;
   @Nullable
   private MobEffect effect;
   private int effectDuration;
   /** Stores the UUID of the most recent lightning bolt to strike */
   @Nullable
   private UUID lastLightningBoltUUID;

   public MushroomCow(EntityType<? extends MushroomCow> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return pLevel.getBlockState(pPos.below()).is(Blocks.MYCELIUM) ? 10.0F : pLevel.getPathfindingCostFromLightLevels(pPos);
   }

   public static boolean checkMushroomSpawnRules(EntityType<MushroomCow> pMushroomCow, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandomSource) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   public void thunderHit(ServerLevel pLevel, LightningBolt pLightning) {
      UUID uuid = pLightning.getUUID();
      if (!uuid.equals(this.lastLightningBoltUUID)) {
         this.setVariant(this.getVariant() == MushroomCow.MushroomType.RED ? MushroomCow.MushroomType.BROWN : MushroomCow.MushroomType.RED);
         this.lastLightningBoltUUID = uuid;
         this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0F, 1.0F);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TYPE, MushroomCow.MushroomType.RED.type);
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.is(Items.BOWL) && !this.isBaby()) {
         boolean flag = false;
         ItemStack itemstack1;
         if (this.effect != null) {
            flag = true;
            itemstack1 = new ItemStack(Items.SUSPICIOUS_STEW);
            SuspiciousStewItem.saveMobEffect(itemstack1, this.effect, this.effectDuration);
            this.effect = null;
            this.effectDuration = 0;
         } else {
            itemstack1 = new ItemStack(Items.MUSHROOM_STEW);
         }

         ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, pPlayer, itemstack1, false);
         pPlayer.setItemInHand(pHand, itemstack2);
         SoundEvent soundevent;
         if (flag) {
            soundevent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
         } else {
            soundevent = SoundEvents.MOOSHROOM_MILK;
         }

         this.playSound(soundevent, 1.0F, 1.0F);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else if (false && itemstack.getItem() == Items.SHEARS && this.readyForShearing()) { //Forge: Moved to onSheared
         this.shear(SoundSource.PLAYERS);
         this.gameEvent(GameEvent.SHEAR, pPlayer);
         if (!this.level().isClientSide) {
            itemstack.hurtAndBreak(1, pPlayer, (p_28927_) -> {
               p_28927_.broadcastBreakEvent(pHand);
            });
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else if (this.getVariant() == MushroomCow.MushroomType.BROWN && itemstack.is(ItemTags.SMALL_FLOWERS)) {
         if (this.effect != null) {
            for(int i = 0; i < 2; ++i) {
               this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0D, this.getY(0.5D), this.getZ() + this.random.nextDouble() / 2.0D, 0.0D, this.random.nextDouble() / 5.0D, 0.0D);
            }
         } else {
            Optional<Pair<MobEffect, Integer>> optional = this.getEffectFromItemStack(itemstack);
            if (!optional.isPresent()) {
               return InteractionResult.PASS;
            }

            Pair<MobEffect, Integer> pair = optional.get();
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            for(int j = 0; j < 4; ++j) {
               this.level().addParticle(ParticleTypes.EFFECT, this.getX() + this.random.nextDouble() / 2.0D, this.getY(0.5D), this.getZ() + this.random.nextDouble() / 2.0D, 0.0D, this.random.nextDouble() / 5.0D, 0.0D);
            }

            this.effect = pair.getLeft();
            this.effectDuration = pair.getRight();
            this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0F, 1.0F);
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   @Override
   public java.util.List<ItemStack> onSheared(@org.jetbrains.annotations.Nullable Player player, @org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos, int fortune) {
      this.gameEvent(GameEvent.SHEAR, player);
      return shearInternal(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
   }

   public void shear(SoundSource pCategory) {
      shearInternal(pCategory).forEach(s -> this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(1.0D), this.getZ(), s)));
   }

   private java.util.List<ItemStack> shearInternal(SoundSource pCategory) {
      this.level().playSound((Player)null, this, SoundEvents.MOOSHROOM_SHEAR, pCategory, 1.0F, 1.0F);
      if (!this.level().isClientSide()) {
         Cow cow = EntityType.COW.create(this.level());
         if (cow != null) {
            ((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            this.discard();
            cow.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            cow.setHealth(this.getHealth());
            cow.yBodyRot = this.yBodyRot;
            if (this.hasCustomName()) {
               cow.setCustomName(this.getCustomName());
               cow.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
               cow.setPersistenceRequired();
            }

            cow.setInvulnerable(this.isInvulnerable());
            this.level().addFreshEntity(cow);

            java.util.List<ItemStack> items = new java.util.ArrayList<>();
            for(int i = 0; i < 5; ++i) {
               items.add(new ItemStack(this.getVariant().blockState.getBlock()));
            }
            return items;
         }
      }
      return java.util.Collections.emptyList();

   }

   public boolean readyForShearing() {
      return this.isAlive() && !this.isBaby();
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putString("Type", this.getVariant().getSerializedName());
      if (this.effect != null) {
         pCompound.putInt("EffectId", MobEffect.getId(this.effect));
         net.minecraftforge.common.ForgeHooks.saveMobEffect(pCompound, "forge:effect_id", this.effect);
         pCompound.putInt("EffectDuration", this.effectDuration);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setVariant(MushroomCow.MushroomType.byType(pCompound.getString("Type")));
      if (pCompound.contains("EffectId", 99)) {
         this.effect = MobEffect.byId(pCompound.getInt("EffectId"));
         this.effect = net.minecraftforge.common.ForgeHooks.loadMobEffect(pCompound, "forge:effect_id", this.effect);
      }

      if (pCompound.contains("EffectDuration", 99)) {
         this.effectDuration = pCompound.getInt("EffectDuration");
      }

   }

   private Optional<Pair<MobEffect, Integer>> getEffectFromItemStack(ItemStack pStack) {
      SuspiciousEffectHolder suspiciouseffectholder = SuspiciousEffectHolder.tryGet(pStack.getItem());
      return suspiciouseffectholder != null ? Optional.of(Pair.of(suspiciouseffectholder.getSuspiciousEffect(), suspiciouseffectholder.getEffectDuration())) : Optional.empty();
   }

   public void setVariant(MushroomCow.MushroomType pVariant) {
      this.entityData.set(DATA_TYPE, pVariant.type);
   }

   public MushroomCow.MushroomType getVariant() {
      return MushroomCow.MushroomType.byType(this.entityData.get(DATA_TYPE));
   }

   @Nullable
   public MushroomCow getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      MushroomCow mushroomcow = EntityType.MOOSHROOM.create(pLevel);
      if (mushroomcow != null) {
         mushroomcow.setVariant(this.getOffspringType((MushroomCow)pOtherParent));
      }

      return mushroomcow;
   }

   private MushroomCow.MushroomType getOffspringType(MushroomCow pMate) {
      MushroomCow.MushroomType mushroomcow$mushroomtype = this.getVariant();
      MushroomCow.MushroomType mushroomcow$mushroomtype1 = pMate.getVariant();
      MushroomCow.MushroomType mushroomcow$mushroomtype2;
      if (mushroomcow$mushroomtype == mushroomcow$mushroomtype1 && this.random.nextInt(1024) == 0) {
         mushroomcow$mushroomtype2 = mushroomcow$mushroomtype == MushroomCow.MushroomType.BROWN ? MushroomCow.MushroomType.RED : MushroomCow.MushroomType.BROWN;
      } else {
         mushroomcow$mushroomtype2 = this.random.nextBoolean() ? mushroomcow$mushroomtype : mushroomcow$mushroomtype1;
      }

      return mushroomcow$mushroomtype2;
   }

   @Override
   public boolean isShearable(@org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos) {
      return readyForShearing();
   }

   public static enum MushroomType implements StringRepresentable {
      RED("red", Blocks.RED_MUSHROOM.defaultBlockState()),
      BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState());

      public static final StringRepresentable.EnumCodec<MushroomCow.MushroomType> CODEC = StringRepresentable.fromEnum(MushroomCow.MushroomType::values);
      final String type;
      final BlockState blockState;

      private MushroomType(String pType, BlockState pBlockState) {
         this.type = pType;
         this.blockState = pBlockState;
      }

      /**
       * A block state that is rendered on the back of the mooshroom.
       */
      public BlockState getBlockState() {
         return this.blockState;
      }

      public String getSerializedName() {
         return this.type;
      }

      static MushroomCow.MushroomType byType(String pName) {
         return CODEC.byName(pName, RED);
      }
   }
}
