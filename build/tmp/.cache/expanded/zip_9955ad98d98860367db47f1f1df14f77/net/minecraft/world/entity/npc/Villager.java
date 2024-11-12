package net.minecraft.world.entity.npc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.GolemSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Villager extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(Villager.class, EntityDataSerializers.VILLAGER_DATA);
   public static final int BREEDING_FOOD_THRESHOLD = 12;
   /** Mapping between valid food items and their respective efficiency values. */
   public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
   private static final int TRADES_PER_LEVEL = 2;
   /** Defaults items a villager regardless of its profession can pick up. */
   private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
   private static final int MAX_GOSSIP_TOPICS = 10;
   private static final int GOSSIP_COOLDOWN = 1200;
   private static final int GOSSIP_DECAY_INTERVAL = 24000;
   private static final int REPUTATION_CHANGE_PER_EVENT = 25;
   private static final int HOW_FAR_AWAY_TO_TALK_TO_OTHER_VILLAGERS_ABOUT_GOLEMS = 10;
   private static final int HOW_MANY_VILLAGERS_NEED_TO_AGREE_TO_SPAWN_A_GOLEM = 5;
   private static final long TIME_SINCE_SLEEPING_FOR_GOLEM_SPAWNING = 24000L;
   @VisibleForTesting
   public static final float SPEED_MODIFIER = 0.5F;
   private int updateMerchantTimer;
   private boolean increaseProfessionLevelOnUpdate;
   @Nullable
   private Player lastTradedPlayer;
   private boolean chasing;
   private int foodLevel;
   private final GossipContainer gossips = new GossipContainer();
   private long lastGossipTime;
   private long lastGossipDecayTime;
   private int villagerXp;
   private long lastRestockGameTime;
   private int numberOfRestocksToday;
   private long lastRestockCheckDayTime;
   private boolean assignProfessionWhenSpawned;
   private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY);
   private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED);
   public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<Villager, Holder<PoiType>>> POI_MEMORIES = ImmutableMap.of(MemoryModuleType.HOME, (p_219625_, p_219626_) -> {
      return p_219626_.is(PoiTypes.HOME);
   }, MemoryModuleType.JOB_SITE, (p_219622_, p_219623_) -> {
      return p_219622_.getVillagerData().getProfession().heldJobSite().test(p_219623_);
   }, MemoryModuleType.POTENTIAL_JOB_SITE, (p_219619_, p_219620_) -> {
      return VillagerProfession.ALL_ACQUIRABLE_JOBS.test(p_219620_);
   }, MemoryModuleType.MEETING_POINT, (p_219616_, p_219617_) -> {
      return p_219617_.is(PoiTypes.MEETING);
   });

   public Villager(EntityType<? extends Villager> pEntityType, Level pLevel) {
      this(pEntityType, pLevel, VillagerType.PLAINS);
   }

   public Villager(EntityType<? extends Villager> pEntityType, Level pLevel, VillagerType pVillagerType) {
      super(pEntityType, pLevel);
      ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      this.getNavigation().setCanFloat(true);
      this.setCanPickUpLoot(true);
      this.setVillagerData(this.getVillagerData().setType(pVillagerType).setProfession(VillagerProfession.NONE));
   }

   public Brain<Villager> getBrain() {
      return (Brain<Villager>)super.getBrain();
   }

   protected Brain.Provider<Villager> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      Brain<Villager> brain = this.brainProvider().makeBrain(pDynamic);
      this.registerBrainGoals(brain);
      return brain;
   }

   public void refreshBrain(ServerLevel pServerLevel) {
      Brain<Villager> brain = this.getBrain();
      brain.stopAll(pServerLevel, this);
      this.brain = brain.copyWithoutBehaviors();
      this.registerBrainGoals(this.getBrain());
   }

   private void registerBrainGoals(Brain<Villager> pVillagerBrain) {
      VillagerProfession villagerprofession = this.getVillagerData().getProfession();
      if (this.isBaby()) {
         pVillagerBrain.setSchedule(Schedule.VILLAGER_BABY);
         pVillagerBrain.addActivity(Activity.PLAY, VillagerGoalPackages.getPlayPackage(0.5F));
      } else {
         pVillagerBrain.setSchedule(Schedule.VILLAGER_DEFAULT);
         pVillagerBrain.addActivityWithConditions(Activity.WORK, VillagerGoalPackages.getWorkPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)));
      }

      pVillagerBrain.addActivity(Activity.CORE, VillagerGoalPackages.getCorePackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivityWithConditions(Activity.MEET, VillagerGoalPackages.getMeetPackage(villagerprofession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT)));
      pVillagerBrain.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage(villagerprofession, 0.5F));
      pVillagerBrain.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage(villagerprofession, 0.5F));
      pVillagerBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pVillagerBrain.setDefaultActivity(Activity.IDLE);
      pVillagerBrain.setActiveActivityIfPossible(Activity.IDLE);
      pVillagerBrain.updateActivityFromSchedule(this.level().getDayTime(), this.level().getGameTime());
   }

   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      if (this.level() instanceof ServerLevel) {
         this.refreshBrain((ServerLevel)this.level());
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 48.0D);
   }

   public boolean assignProfessionWhenSpawned() {
      return this.assignProfessionWhenSpawned;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("villagerBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      if (this.assignProfessionWhenSpawned) {
         this.assignProfessionWhenSpawned = false;
      }

      if (!this.isTrading() && this.updateMerchantTimer > 0) {
         --this.updateMerchantTimer;
         if (this.updateMerchantTimer <= 0) {
            if (this.increaseProfessionLevelOnUpdate) {
               this.increaseMerchantCareer();
               this.increaseProfessionLevelOnUpdate = false;
            }

            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
         }
      }

      if (this.lastTradedPlayer != null && this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).onReputationEvent(ReputationEventType.TRADE, this.lastTradedPlayer, this);
         this.level().broadcastEntityEvent(this, (byte)14);
         this.lastTradedPlayer = null;
      }

      if (!this.isNoAi() && this.random.nextInt(100) == 0) {
         Raid raid = ((ServerLevel)this.level()).getRaidAt(this.blockPosition());
         if (raid != null && raid.isActive() && !raid.isOver()) {
            this.level().broadcastEntityEvent(this, (byte)42);
         }
      }

      if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.isTrading()) {
         this.stopTrading();
      }

      super.customServerAiStep();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.getUnhappyCounter() > 0) {
         this.setUnhappyCounter(this.getUnhappyCounter() - 1);
      }

      this.maybeDecayGossip();
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isSleeping() && !pPlayer.isSecondaryUseActive()) {
         if (this.isBaby()) {
            this.setUnhappy();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else {
            boolean flag = this.getOffers().isEmpty();
            if (pHand == InteractionHand.MAIN_HAND) {
               if (flag && !this.level().isClientSide) {
                  this.setUnhappy();
               }

               pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (flag) {
               return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
               if (!this.level().isClientSide && !this.offers.isEmpty()) {
                  this.startTrading(pPlayer);
               }

               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   private void setUnhappy() {
      this.setUnhappyCounter(40);
      if (!this.level().isClientSide()) {
         this.playSound(SoundEvents.VILLAGER_NO, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   private void startTrading(Player pPlayer) {
      this.updateSpecialPrices(pPlayer);
      this.setTradingPlayer(pPlayer);
      this.openTradingScreen(pPlayer, this.getDisplayName(), this.getVillagerData().getLevel());
   }

   public void setTradingPlayer(@Nullable Player pPlayer) {
      boolean flag = this.getTradingPlayer() != null && pPlayer == null;
      super.setTradingPlayer(pPlayer);
      if (flag) {
         this.stopTrading();
      }

   }

   protected void stopTrading() {
      super.stopTrading();
      this.resetSpecialPrices();
   }

   private void resetSpecialPrices() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.resetSpecialPriceDiff();
      }

   }

   public boolean canRestock() {
      return true;
   }

   public boolean isClientSide() {
      return this.level().isClientSide;
   }

   public void restock() {
      this.updateDemand();

      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.resetUses();
      }

      this.resendOffersToTradingPlayer();
      this.lastRestockGameTime = this.level().getGameTime();
      ++this.numberOfRestocksToday;
   }

   private void resendOffersToTradingPlayer() {
      MerchantOffers merchantoffers = this.getOffers();
      Player player = this.getTradingPlayer();
      if (player != null && !merchantoffers.isEmpty()) {
         player.sendMerchantOffers(player.containerMenu.containerId, merchantoffers, this.getVillagerData().getLevel(), this.getVillagerXp(), this.showProgressBar(), this.canRestock());
      }

   }

   private boolean needsToRestock() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         if (merchantoffer.needsRestock()) {
            return true;
         }
      }

      return false;
   }

   private boolean allowedToRestock() {
      return this.numberOfRestocksToday == 0 || this.numberOfRestocksToday < 2 && this.level().getGameTime() > this.lastRestockGameTime + 2400L;
   }

   public boolean shouldRestock() {
      long i = this.lastRestockGameTime + 12000L;
      long j = this.level().getGameTime();
      boolean flag = j > i;
      long k = this.level().getDayTime();
      if (this.lastRestockCheckDayTime > 0L) {
         long l = this.lastRestockCheckDayTime / 24000L;
         long i1 = k / 24000L;
         flag |= i1 > l;
      }

      this.lastRestockCheckDayTime = k;
      if (flag) {
         this.lastRestockGameTime = j;
         this.resetNumberOfRestocks();
      }

      return this.allowedToRestock() && this.needsToRestock();
   }

   private void catchUpDemand() {
      int i = 2 - this.numberOfRestocksToday;
      if (i > 0) {
         for(MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.resetUses();
         }
      }

      for(int j = 0; j < i; ++j) {
         this.updateDemand();
      }

      this.resendOffersToTradingPlayer();
   }

   private void updateDemand() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.updateDemand();
      }

   }

   private void updateSpecialPrices(Player pPlayer) {
      int i = this.getPlayerReputation(pPlayer);
      if (i != 0) {
         for(MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.addToSpecialPriceDiff(-Mth.floor((float)i * merchantoffer.getPriceMultiplier()));
         }
      }

      if (pPlayer.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
         MobEffectInstance mobeffectinstance = pPlayer.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
         int k = mobeffectinstance.getAmplifier();

         for(MerchantOffer merchantoffer1 : this.getOffers()) {
            double d0 = 0.3D + 0.0625D * (double)k;
            int j = (int)Math.floor(d0 * (double)merchantoffer1.getBaseCostA().getCount());
            merchantoffer1.addToSpecialPriceDiff(-Math.max(j, 1));
         }
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData()).resultOrPartial(LOGGER::error).ifPresent((p_35454_) -> {
         pCompound.put("VillagerData", p_35454_);
      });
      pCompound.putByte("FoodLevel", (byte)this.foodLevel);
      pCompound.put("Gossips", this.gossips.store(NbtOps.INSTANCE));
      pCompound.putInt("Xp", this.villagerXp);
      pCompound.putLong("LastRestock", this.lastRestockGameTime);
      pCompound.putLong("LastGossipDecay", this.lastGossipDecayTime);
      pCompound.putInt("RestocksToday", this.numberOfRestocksToday);
      if (this.assignProfessionWhenSpawned) {
         pCompound.putBoolean("AssignProfessionWhenSpawned", true);
      }

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
         this.offers = new MerchantOffers(pCompound.getCompound("Offers"));
      }

      if (pCompound.contains("FoodLevel", 1)) {
         this.foodLevel = pCompound.getByte("FoodLevel");
      }

      ListTag listtag = pCompound.getList("Gossips", 10);
      this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, listtag));
      if (pCompound.contains("Xp", 3)) {
         this.villagerXp = pCompound.getInt("Xp");
      }

      this.lastRestockGameTime = pCompound.getLong("LastRestock");
      this.lastGossipDecayTime = pCompound.getLong("LastGossipDecay");
      this.setCanPickUpLoot(true);
      if (this.level() instanceof ServerLevel) {
         this.refreshBrain((ServerLevel)this.level());
      }

      this.numberOfRestocksToday = pCompound.getInt("RestocksToday");
      if (pCompound.contains("AssignProfessionWhenSpawned")) {
         this.assignProfessionWhenSpawned = pCompound.getBoolean("AssignProfessionWhenSpawned");
      }

   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return false;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return null;
      } else {
         return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.VILLAGER_DEATH;
   }

   public void playWorkSound() {
      SoundEvent soundevent = this.getVillagerData().getProfession().workSound();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public void setVillagerData(VillagerData p_35437_) {
      VillagerData villagerdata = this.getVillagerData();
      if (villagerdata.getProfession() != p_35437_.getProfession()) {
         this.offers = null;
      }

      this.entityData.set(DATA_VILLAGER_DATA, p_35437_);
   }

   public VillagerData getVillagerData() {
      return this.entityData.get(DATA_VILLAGER_DATA);
   }

   protected void rewardTradeXp(MerchantOffer pOffer) {
      int i = 3 + this.random.nextInt(4);
      this.villagerXp += pOffer.getXp();
      this.lastTradedPlayer = this.getTradingPlayer();
      if (this.shouldIncreaseLevel()) {
         this.updateMerchantTimer = 40;
         this.increaseProfessionLevelOnUpdate = true;
         i += 5;
      }

      if (pOffer.shouldRewardExp()) {
         this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), i));
      }

   }

   public void setChasing(boolean pChasing) {
      this.chasing = pChasing;
   }

   public boolean isChasing() {
      return this.chasing;
   }

   /**
    * Hint to AI tasks that we were attacked by the passed EntityLivingBase and should retaliate. Is not guaranteed to
    * change our actual active target (for example if we are currently busy attacking someone else)
    */
   public void setLastHurtByMob(@Nullable LivingEntity pLivingBase) {
      if (pLivingBase != null && this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).onReputationEvent(ReputationEventType.VILLAGER_HURT, pLivingBase, this);
         if (this.isAlive() && pLivingBase instanceof Player) {
            this.level().broadcastEntityEvent(this, (byte)13);
         }
      }

      super.setLastHurtByMob(pLivingBase);
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      LOGGER.info("Villager {} died, message: '{}'", this, pCause.getLocalizedDeathMessage(this).getString());
      Entity entity = pCause.getEntity();
      if (entity != null) {
         this.tellWitnessesThatIWasMurdered(entity);
      }

      this.releaseAllPois();
      super.die(pCause);
   }

   private void releaseAllPois() {
      this.releasePoi(MemoryModuleType.HOME);
      this.releasePoi(MemoryModuleType.JOB_SITE);
      this.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
      this.releasePoi(MemoryModuleType.MEETING_POINT);
   }

   private void tellWitnessesThatIWasMurdered(Entity pMurderer) {
      Level $$3 = this.level();
      if ($$3 instanceof ServerLevel serverlevel) {
         Optional<NearestVisibleLivingEntities> optional = this.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
         if (!optional.isEmpty()) {
            optional.get().findAll(ReputationEventHandler.class::isInstance).forEach((p_186297_) -> {
               serverlevel.onReputationEvent(ReputationEventType.VILLAGER_KILLED, pMurderer, (ReputationEventHandler)p_186297_);
            });
         }
      }
   }

   public void releasePoi(MemoryModuleType<GlobalPos> pModuleType) {
      if (this.level() instanceof ServerLevel) {
         MinecraftServer minecraftserver = ((ServerLevel)this.level()).getServer();
         this.brain.getMemory(pModuleType).ifPresent((p_186306_) -> {
            ServerLevel serverlevel = minecraftserver.getLevel(p_186306_.dimension());
            if (serverlevel != null) {
               PoiManager poimanager = serverlevel.getPoiManager();
               Optional<Holder<PoiType>> optional = poimanager.getType(p_186306_.pos());
               BiPredicate<Villager, Holder<PoiType>> bipredicate = POI_MEMORIES.get(pModuleType);
               if (optional.isPresent() && bipredicate.test(this, optional.get())) {
                  poimanager.release(p_186306_.pos());
                  DebugPackets.sendPoiTicketCountPacket(serverlevel, p_186306_.pos());
               }

            }
         });
      }
   }

   public boolean canBreed() {
      return this.foodLevel + this.countFoodPointsInInventory() >= 12 && !this.isSleeping() && this.getAge() == 0;
   }

   private boolean hungry() {
      return this.foodLevel < 12;
   }

   private void eatUntilFull() {
      if (this.hungry() && this.countFoodPointsInInventory() != 0) {
         for(int i = 0; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getInventory().getItem(i);
            if (!itemstack.isEmpty()) {
               Integer integer = FOOD_POINTS.get(itemstack.getItem());
               if (integer != null) {
                  int j = itemstack.getCount();

                  for(int k = j; k > 0; --k) {
                     this.foodLevel += integer;
                     this.getInventory().removeItem(i, 1);
                     if (!this.hungry()) {
                        return;
                     }
                  }
               }
            }
         }

      }
   }

   public int getPlayerReputation(Player pPlayer) {
      return this.gossips.getReputation(pPlayer.getUUID(), (p_186302_) -> {
         return true;
      });
   }

   private void digestFood(int pQty) {
      this.foodLevel -= pQty;
   }

   public void eatAndDigestFood() {
      this.eatUntilFull();
      this.digestFood(12);
   }

   public void setOffers(MerchantOffers pOffers) {
      this.offers = pOffers;
   }

   private boolean shouldIncreaseLevel() {
      int i = this.getVillagerData().getLevel();
      return VillagerData.canLevelUp(i) && this.villagerXp >= VillagerData.getMaxXpPerLevel(i);
   }

   private void increaseMerchantCareer() {
      this.setVillagerData(this.getVillagerData().setLevel(this.getVillagerData().getLevel() + 1));
      this.updateTrades();
   }

   protected Component getTypeName() {
      net.minecraft.resources.ResourceLocation profName = net.minecraftforge.registries.ForgeRegistries.VILLAGER_PROFESSIONS.getKey(this.getVillagerData().getProfession());
      return Component.translatable(this.getType().getDescriptionId() + '.' + (!"minecraft".equals(profName.getNamespace()) ? profName.getNamespace() + '.' : "") + profName.getPath());
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 12) {
         this.addParticlesAroundSelf(ParticleTypes.HEART);
      } else if (pId == 13) {
         this.addParticlesAroundSelf(ParticleTypes.ANGRY_VILLAGER);
      } else if (pId == 14) {
         this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
      } else if (pId == 42) {
         this.addParticlesAroundSelf(ParticleTypes.SPLASH);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      if (pReason == MobSpawnType.BREEDING) {
         this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.NONE));
      }

      if (pReason == MobSpawnType.COMMAND || pReason == MobSpawnType.SPAWN_EGG || pReason == MobSpawnType.SPAWNER || pReason == MobSpawnType.DISPENSER) {
         this.setVillagerData(this.getVillagerData().setType(VillagerType.byBiome(pLevel.getBiome(this.blockPosition()))));
      }

      if (pReason == MobSpawnType.STRUCTURE) {
         this.assignProfessionWhenSpawned = true;
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   @Nullable
   public Villager getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      double d0 = this.random.nextDouble();
      VillagerType villagertype;
      if (d0 < 0.5D) {
         villagertype = VillagerType.byBiome(pLevel.getBiome(this.blockPosition()));
      } else if (d0 < 0.75D) {
         villagertype = this.getVillagerData().getType();
      } else {
         villagertype = ((Villager)pOtherParent).getVillagerData().getType();
      }

      Villager villager = new Villager(EntityType.VILLAGER, pLevel, villagertype);
      villager.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.BREEDING, (SpawnGroupData)null, (CompoundTag)null);
      return villager;
   }

   public void thunderHit(ServerLevel pLevel, LightningBolt pLightning) {
      if (pLevel.getDifficulty() != Difficulty.PEACEFUL && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.WITCH, (timer) -> {})) {
         LOGGER.info("Villager {} was struck by lightning {}.", this, pLightning);
         Witch witch = EntityType.WITCH.create(pLevel);
         if (witch != null) {
            witch.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            witch.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(witch.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
            witch.setNoAi(this.isNoAi());
            if (this.hasCustomName()) {
               witch.setCustomName(this.getCustomName());
               witch.setCustomNameVisible(this.isCustomNameVisible());
            }

            witch.setPersistenceRequired();
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, witch);
            pLevel.addFreshEntityWithPassengers(witch);
            this.releaseAllPois();
            this.discard();
         } else {
            super.thunderHit(pLevel, pLightning);
         }
      } else {
         super.thunderHit(pLevel, pLightning);
      }

   }

   /**
    * Tests if this entity should pick up a weapon or an armor piece. Entity drops current weapon or armor if the new
    * one is better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      InventoryCarrier.pickUpItem(this, this, pItemEntity);
   }

   public boolean wantsToPickUp(ItemStack pStack) {
      Item item = pStack.getItem();
      return (WANTED_ITEMS.contains(item) || this.getVillagerData().getProfession().requestedItems().contains(item)) && this.getInventory().canAddItem(pStack);
   }

   /**
    * Used by {@link net.minecraft.world.entity.ai.behavior.TradeWithVillager} to check if the villager can give some
    * items from an inventory to another villager.
    */
   public boolean hasExcessFood() {
      return this.countFoodPointsInInventory() >= 24;
   }

   public boolean wantsMoreFood() {
      return this.countFoodPointsInInventory() < 12;
   }

   /**
    * @return calculated food value from item stacks in this villager's inventory
    */
   private int countFoodPointsInInventory() {
      SimpleContainer simplecontainer = this.getInventory();
      return FOOD_POINTS.entrySet().stream().mapToInt((p_186300_) -> {
         return simplecontainer.countItem(p_186300_.getKey()) * p_186300_.getValue();
      }).sum();
   }

   /**
    * Returns {@code true} if villager has seeds, potatoes or carrots in inventory
    */
   public boolean hasFarmSeeds() {
      return this.getInventory().hasAnyMatching((p_281096_) -> {
         return p_281096_.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
      });
   }

   protected void updateTrades() {
      VillagerData villagerdata = this.getVillagerData();
      Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap = VillagerTrades.TRADES.get(villagerdata.getProfession());
      if (int2objectmap != null && !int2objectmap.isEmpty()) {
         VillagerTrades.ItemListing[] avillagertrades$itemlisting = int2objectmap.get(villagerdata.getLevel());
         if (avillagertrades$itemlisting != null) {
            MerchantOffers merchantoffers = this.getOffers();
            this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, 2);
         }
      }
   }

   public void gossip(ServerLevel pServerLevel, Villager pTarget, long pGameTime) {
      if ((pGameTime < this.lastGossipTime || pGameTime >= this.lastGossipTime + 1200L) && (pGameTime < pTarget.lastGossipTime || pGameTime >= pTarget.lastGossipTime + 1200L)) {
         this.gossips.transferFrom(pTarget.gossips, this.random, 10);
         this.lastGossipTime = pGameTime;
         pTarget.lastGossipTime = pGameTime;
         this.spawnGolemIfNeeded(pServerLevel, pGameTime, 5);
      }
   }

   private void maybeDecayGossip() {
      long i = this.level().getGameTime();
      if (this.lastGossipDecayTime == 0L) {
         this.lastGossipDecayTime = i;
      } else if (i >= this.lastGossipDecayTime + 24000L) {
         this.gossips.decay();
         this.lastGossipDecayTime = i;
      }
   }

   public void spawnGolemIfNeeded(ServerLevel pServerLevel, long pGameTime, int pMinVillagerAmount) {
      if (this.wantsToSpawnGolem(pGameTime)) {
         AABB aabb = this.getBoundingBox().inflate(10.0D, 10.0D, 10.0D);
         List<Villager> list = pServerLevel.getEntitiesOfClass(Villager.class, aabb);
         List<Villager> list1 = list.stream().filter((p_186293_) -> {
            return p_186293_.wantsToSpawnGolem(pGameTime);
         }).limit(5L).collect(Collectors.toList());
         if (list1.size() >= pMinVillagerAmount) {
            if (SpawnUtil.trySpawnMob(EntityType.IRON_GOLEM, MobSpawnType.MOB_SUMMONED, pServerLevel, this.blockPosition(), 10, 8, 6, SpawnUtil.Strategy.LEGACY_IRON_GOLEM).isPresent()) {
               list.forEach(GolemSensor::golemDetected);
            }
         }
      }
   }

   public boolean wantsToSpawnGolem(long pGameTime) {
      if (!this.golemSpawnConditionsMet(this.level().getGameTime())) {
         return false;
      } else {
         return !this.brain.hasMemoryValue(MemoryModuleType.GOLEM_DETECTED_RECENTLY);
      }
   }

   public void onReputationEventFrom(ReputationEventType pType, Entity pTarget) {
      if (pType == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
         this.gossips.add(pTarget.getUUID(), GossipType.MAJOR_POSITIVE, 20);
         this.gossips.add(pTarget.getUUID(), GossipType.MINOR_POSITIVE, 25);
      } else if (pType == ReputationEventType.TRADE) {
         this.gossips.add(pTarget.getUUID(), GossipType.TRADING, 2);
      } else if (pType == ReputationEventType.VILLAGER_HURT) {
         this.gossips.add(pTarget.getUUID(), GossipType.MINOR_NEGATIVE, 25);
      } else if (pType == ReputationEventType.VILLAGER_KILLED) {
         this.gossips.add(pTarget.getUUID(), GossipType.MAJOR_NEGATIVE, 25);
      }

   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public void setVillagerXp(int pVillagerXp) {
      this.villagerXp = pVillagerXp;
   }

   private void resetNumberOfRestocks() {
      this.catchUpDemand();
      this.numberOfRestocksToday = 0;
   }

   public GossipContainer getGossips() {
      return this.gossips;
   }

   public void setGossips(Tag pGossip) {
      this.gossips.update(new Dynamic<>(NbtOps.INSTANCE, pGossip));
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public void startSleeping(BlockPos pPos) {
      super.startSleeping(pPos);
      this.brain.setMemory(MemoryModuleType.LAST_SLEPT, this.level().getGameTime());
      this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
   }

   public void stopSleeping() {
      super.stopSleeping();
      this.brain.setMemory(MemoryModuleType.LAST_WOKEN, this.level().getGameTime());
   }

   private boolean golemSpawnConditionsMet(long pGameTime) {
      Optional<Long> optional = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
      if (optional.isPresent()) {
         return pGameTime - optional.get() < 24000L;
      } else {
         return false;
      }
   }
}
