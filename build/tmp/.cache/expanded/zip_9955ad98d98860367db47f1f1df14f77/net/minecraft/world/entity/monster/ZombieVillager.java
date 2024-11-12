package net.minecraft.world.entity.monster;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class ZombieVillager extends Zombie implements VillagerDataHolder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.VILLAGER_DATA);
   private static final int VILLAGER_CONVERSION_WAIT_MIN = 3600;
   private static final int VILLAGER_CONVERSION_WAIT_MAX = 6000;
   private static final int MAX_SPECIAL_BLOCKS_COUNT = 14;
   private static final int SPECIAL_BLOCK_RADIUS = 4;
   private int villagerConversionTime;
   @Nullable
   private UUID conversionStarter;
   @Nullable
   private Tag gossips;
   @Nullable
   private CompoundTag tradeOffers;
   private int villagerXp;

   public ZombieVillager(EntityType<? extends ZombieVillager> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      BuiltInRegistries.VILLAGER_PROFESSION.getRandom(this.random).ifPresent((p_255550_) -> {
         this.setVillagerData(this.getVillagerData().setProfession(p_255550_.value()));
      });
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_CONVERTING_ID, false);
      this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData()).resultOrPartial(LOGGER::error).ifPresent((p_204072_) -> {
         pCompound.put("VillagerData", p_204072_);
      });
      if (this.tradeOffers != null) {
         pCompound.put("Offers", this.tradeOffers);
      }

      if (this.gossips != null) {
         pCompound.put("Gossips", this.gossips);
      }

      pCompound.putInt("ConversionTime", this.isConverting() ? this.villagerConversionTime : -1);
      if (this.conversionStarter != null) {
         pCompound.putUUID("ConversionPlayer", this.conversionStarter);
      }

      pCompound.putInt("Xp", this.villagerXp);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("VillagerData", 10)) {
         DataResult<VillagerData> dataresult = VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pCompound.get("VillagerData")));
         dataresult.resultOrPartial(LOGGER::error).ifPresent(this::setVillagerData);
      }

      if (pCompound.contains("Offers", 10)) {
         this.tradeOffers = pCompound.getCompound("Offers");
      }

      if (pCompound.contains("Gossips", 9)) {
         this.gossips = pCompound.getList("Gossips", 10);
      }

      if (pCompound.contains("ConversionTime", 99) && pCompound.getInt("ConversionTime") > -1) {
         this.startConverting(pCompound.hasUUID("ConversionPlayer") ? pCompound.getUUID("ConversionPlayer") : null, pCompound.getInt("ConversionTime"));
      }

      if (pCompound.contains("Xp", 3)) {
         this.villagerXp = pCompound.getInt("Xp");
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level().isClientSide && this.isAlive() && this.isConverting()) {
         int i = this.getConversionProgress();
         this.villagerConversionTime -= i;
         if (this.villagerConversionTime <= 0 && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.VILLAGER, (timer) -> this.villagerConversionTime = timer)) {
            this.finishConversion((ServerLevel)this.level());
         }
      }

      super.tick();
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.is(Items.GOLDEN_APPLE)) {
         if (this.hasEffect(MobEffects.WEAKNESS)) {
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            if (!this.level().isClientSide) {
               this.startConverting(pPlayer.getUUID(), this.random.nextInt(2401) + 3600);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.CONSUME;
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   protected boolean convertsInWater() {
      return false;
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return !this.isConverting() && this.villagerXp == 0;
   }

   /**
    * Returns whether this zombie is in the process of converting to a villager
    */
   public boolean isConverting() {
      return this.getEntityData().get(DATA_CONVERTING_ID);
   }

   /**
    * Starts conversion of this zombie villager to a villager
    */
   private void startConverting(@Nullable UUID pConversionStarter, int pVillagerConversionTime) {
      this.conversionStarter = pConversionStarter;
      this.villagerConversionTime = pVillagerConversionTime;
      this.getEntityData().set(DATA_CONVERTING_ID, true);
      this.removeEffect(MobEffects.WEAKNESS);
      this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, pVillagerConversionTime, Math.min(this.level().getDifficulty().getId() - 1, 0)));
      this.level().broadcastEntityEvent(this, (byte)16);
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 16) {
         if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
         }

      } else {
         super.handleEntityEvent(pId);
      }
   }

   private void finishConversion(ServerLevel pServerLevel) {
      Villager villager = this.convertTo(EntityType.VILLAGER, false);

      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
         ItemStack itemstack = this.getItemBySlot(equipmentslot);
         if (!itemstack.isEmpty()) {
            if (EnchantmentHelper.hasBindingCurse(itemstack)) {
               villager.getSlot(equipmentslot.getIndex() + 300).set(itemstack);
            } else {
               double d0 = (double)this.getEquipmentDropChance(equipmentslot);
               if (d0 > 1.0D) {
                  this.spawnAtLocation(itemstack);
               }
            }
         }
      }

      villager.setVillagerData(this.getVillagerData());
      if (this.gossips != null) {
         villager.setGossips(this.gossips);
      }

      if (this.tradeOffers != null) {
         villager.setOffers(new MerchantOffers(this.tradeOffers));
      }

      villager.setVillagerXp(this.villagerXp);
      villager.finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
      villager.refreshBrain(pServerLevel);
      if (this.conversionStarter != null) {
         Player player = pServerLevel.getPlayerByUUID(this.conversionStarter);
         if (player instanceof ServerPlayer) {
            CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayer)player, this, villager);
            pServerLevel.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, player, villager);
         }
      }

      villager.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
      if (!this.isSilent()) {
         pServerLevel.levelEvent((Player)null, 1027, this.blockPosition(), 0);
      }
      net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, villager);
   }

   private int getConversionProgress() {
      int i = 1;
      if (this.random.nextFloat() < 0.01F) {
         int j = 0;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; ++k) {
            for(int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; ++l) {
               for(int i1 = (int)this.getZ() - 4; i1 < (int)this.getZ() + 4 && j < 14; ++i1) {
                  BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos.set(k, l, i1));
                  if (blockstate.is(Blocks.IRON_BARS) || blockstate.getBlock() instanceof BedBlock) {
                     if (this.random.nextFloat() < 0.3F) {
                        ++i;
                     }

                     ++j;
                  }
               }
            }
         }
      }

      return i;
   }

   /**
    * Gets the pitch of living sounds in living entities.
    */
   public float getVoicePitch() {
      return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.0F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F;
   }

   public SoundEvent getAmbientSound() {
      return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
   }

   public SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ZOMBIE_VILLAGER_HURT;
   }

   public SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_VILLAGER_DEATH;
   }

   public SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_VILLAGER_STEP;
   }

   protected ItemStack getSkull() {
      return ItemStack.EMPTY;
   }

   public void setTradeOffers(CompoundTag pTradeOffers) {
      this.tradeOffers = pTradeOffers;
   }

   public void setGossips(Tag pGossips) {
      this.gossips = pGossips;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(pLevel.getBiome(this.blockPosition()))));
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public void setVillagerData(VillagerData p_34376_) {
      VillagerData villagerdata = this.getVillagerData();
      if (villagerdata.getProfession() != p_34376_.getProfession()) {
         this.tradeOffers = null;
      }

      this.entityData.set(DATA_VILLAGER_DATA, p_34376_);
   }

   public VillagerData getVillagerData() {
      return this.entityData.get(DATA_VILLAGER_DATA);
   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public void setVillagerXp(int pVillagerXp) {
      this.villagerXp = pVillagerXp;
   }
}
