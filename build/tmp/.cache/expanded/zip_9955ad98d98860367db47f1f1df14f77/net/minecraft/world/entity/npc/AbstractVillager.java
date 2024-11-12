package net.minecraft.world.entity.npc;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractVillager extends AgeableMob implements InventoryCarrier, Npc, Merchant {
   private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
   public static final int VILLAGER_SLOT_OFFSET = 300;
   private static final int VILLAGER_INVENTORY_SIZE = 8;
   @Nullable
   private Player tradingPlayer;
   @Nullable
   protected MerchantOffers offers;
   private final SimpleContainer inventory = new SimpleContainer(8);

   public AbstractVillager(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableMob.AgeableMobGroupData(false);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public int getUnhappyCounter() {
      return this.entityData.get(DATA_UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int pUnhappyCounter) {
      this.entityData.set(DATA_UNHAPPY_COUNTER, pUnhappyCounter);
   }

   public int getVillagerXp() {
      return 0;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return this.isBaby() ? 0.81F : 1.62F;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
   }

   public void setTradingPlayer(@Nullable Player pPlayer) {
      this.tradingPlayer = pPlayer;
   }

   @Nullable
   public Player getTradingPlayer() {
      return this.tradingPlayer;
   }

   public boolean isTrading() {
      return this.tradingPlayer != null;
   }

   public MerchantOffers getOffers() {
      if (this.offers == null) {
         this.offers = new MerchantOffers();
         this.updateTrades();
      }

      return this.offers;
   }

   public void overrideOffers(@Nullable MerchantOffers pOffers) {
   }

   public void overrideXp(int pXp) {
   }

   public void notifyTrade(MerchantOffer pOffer) {
      pOffer.increaseUses();
      this.ambientSoundTime = -this.getAmbientSoundInterval();
      this.rewardTradeXp(pOffer);
      if (this.tradingPlayer instanceof ServerPlayer) {
         CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, pOffer.getResult());
      }

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.TradeWithVillagerEvent(this.tradingPlayer, pOffer, this));
   }

   protected abstract void rewardTradeXp(MerchantOffer pOffer);

   public boolean showProgressBar() {
      return true;
   }

   /**
    * Notifies the merchant of a possible merchant recipe being fulfilled or not. Usually, this is just a sound byte
    * being played depending on whether the suggested {@link net.minecraft.world.item.ItemStack} is not empty.
    */
   public void notifyTradeUpdated(ItemStack pStack) {
      if (!this.level().isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
         this.ambientSoundTime = -this.getAmbientSoundInterval();
         this.playSound(this.getTradeUpdatedSound(!pStack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }

   protected SoundEvent getTradeUpdatedSound(boolean pIsYesSound) {
      return pIsYesSound ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
   }

   public void playCelebrateSound() {
      this.playSound(SoundEvents.VILLAGER_CELEBRATE, this.getSoundVolume(), this.getVoicePitch());
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      MerchantOffers merchantoffers = this.getOffers();
      if (!merchantoffers.isEmpty()) {
         pCompound.put("Offers", merchantoffers.createTag());
      }

      this.writeInventoryToTag(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Offers", 10)) {
         this.offers = new MerchantOffers(pCompound.getCompound("Offers"));
      }

      this.readInventoryFromTag(pCompound);
   }

   @Nullable
   public Entity changeDimension(ServerLevel pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      this.stopTrading();
      return super.changeDimension(pServer, teleporter);
   }

   protected void stopTrading() {
      this.setTradingPlayer((Player)null);
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      super.die(pCause);
      this.stopTrading();
   }

   protected void addParticlesAroundSelf(ParticleOptions pParticleOption) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(pParticleOption, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   public boolean canBeLeashed(Player pPlayer) {
      return false;
   }

   public SimpleContainer getInventory() {
      return this.inventory;
   }

   public SlotAccess getSlot(int pSlot) {
      int i = pSlot - 300;
      return i >= 0 && i < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, i) : super.getSlot(pSlot);
   }

   protected abstract void updateTrades();

   /**
    * Adds limited numbers of trades to the given {@link net.minecraft.world.item.trading.MerchantOffers}.
    */
   protected void addOffersFromItemListings(MerchantOffers pGivenMerchantOffers, VillagerTrades.ItemListing[] pNewTrades, int pMaxNumbers) {
      Set<Integer> set = Sets.newHashSet();
      if (pNewTrades.length > pMaxNumbers) {
         while(set.size() < pMaxNumbers) {
            set.add(this.random.nextInt(pNewTrades.length));
         }
      } else {
         for(int i = 0; i < pNewTrades.length; ++i) {
            set.add(i);
         }
      }

      for(Integer integer : set) {
         VillagerTrades.ItemListing villagertrades$itemlisting = pNewTrades[integer];
         MerchantOffer merchantoffer = villagertrades$itemlisting.getOffer(this, this.random);
         if (merchantoffer != null) {
            pGivenMerchantOffers.add(merchantoffer);
         }
      }

   }

   public Vec3 getRopeHoldPosition(float pPartialTicks) {
      float f = Mth.lerp(pPartialTicks, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180F);
      Vec3 vec3 = new Vec3(0.0D, this.getBoundingBox().getYsize() - 1.0D, 0.2D);
      return this.getPosition(pPartialTicks).add(vec3.yRot(-f));
   }

   public boolean isClientSide() {
      return this.level().isClientSide;
   }
}
