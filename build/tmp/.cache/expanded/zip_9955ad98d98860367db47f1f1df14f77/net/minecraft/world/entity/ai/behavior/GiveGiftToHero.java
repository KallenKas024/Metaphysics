package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class GiveGiftToHero extends Behavior<Villager> {
   private static final int THROW_GIFT_AT_DISTANCE = 5;
   private static final int MIN_TIME_BETWEEN_GIFTS = 600;
   private static final int MAX_TIME_BETWEEN_GIFTS = 6600;
   private static final int TIME_TO_DELAY_FOR_HEAD_TO_FINISH_TURNING = 20;
   private static final Map<VillagerProfession, ResourceLocation> GIFTS = Util.make(Maps.newHashMap(), (p_23020_) -> {
      p_23020_.put(VillagerProfession.ARMORER, BuiltInLootTables.ARMORER_GIFT);
      p_23020_.put(VillagerProfession.BUTCHER, BuiltInLootTables.BUTCHER_GIFT);
      p_23020_.put(VillagerProfession.CARTOGRAPHER, BuiltInLootTables.CARTOGRAPHER_GIFT);
      p_23020_.put(VillagerProfession.CLERIC, BuiltInLootTables.CLERIC_GIFT);
      p_23020_.put(VillagerProfession.FARMER, BuiltInLootTables.FARMER_GIFT);
      p_23020_.put(VillagerProfession.FISHERMAN, BuiltInLootTables.FISHERMAN_GIFT);
      p_23020_.put(VillagerProfession.FLETCHER, BuiltInLootTables.FLETCHER_GIFT);
      p_23020_.put(VillagerProfession.LEATHERWORKER, BuiltInLootTables.LEATHERWORKER_GIFT);
      p_23020_.put(VillagerProfession.LIBRARIAN, BuiltInLootTables.LIBRARIAN_GIFT);
      p_23020_.put(VillagerProfession.MASON, BuiltInLootTables.MASON_GIFT);
      p_23020_.put(VillagerProfession.SHEPHERD, BuiltInLootTables.SHEPHERD_GIFT);
      p_23020_.put(VillagerProfession.TOOLSMITH, BuiltInLootTables.TOOLSMITH_GIFT);
      p_23020_.put(VillagerProfession.WEAPONSMITH, BuiltInLootTables.WEAPONSMITH_GIFT);
   });
   private static final float SPEED_MODIFIER = 0.5F;
   private int timeUntilNextGift = 600;
   private boolean giftGivenDuringThisRun;
   private long timeSinceStart;

   public GiveGiftToHero(int pDuration) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT), pDuration);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      if (!this.isHeroVisible(pOwner)) {
         return false;
      } else if (this.timeUntilNextGift > 0) {
         --this.timeUntilNextGift;
         return false;
      } else {
         return true;
      }
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      this.giftGivenDuringThisRun = false;
      this.timeSinceStart = pGameTime;
      Player player = this.getNearestTargetableHero(pEntity).get();
      pEntity.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, player);
      BehaviorUtils.lookAtEntity(pEntity, player);
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return this.isHeroVisible(pEntity) && !this.giftGivenDuringThisRun;
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      Player player = this.getNearestTargetableHero(pOwner).get();
      BehaviorUtils.lookAtEntity(pOwner, player);
      if (this.isWithinThrowingDistance(pOwner, player)) {
         if (pGameTime - this.timeSinceStart > 20L) {
            this.throwGift(pOwner, player);
            this.giftGivenDuringThisRun = true;
         }
      } else {
         BehaviorUtils.setWalkAndLookTargetMemories(pOwner, player, 0.5F, 5);
      }

   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      this.timeUntilNextGift = calculateTimeUntilNextGift(pLevel);
      pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   private void throwGift(Villager pVillager, LivingEntity pHero) {
      for(ItemStack itemstack : this.getItemToThrow(pVillager)) {
         BehaviorUtils.throwItem(pVillager, itemstack, pHero.position());
      }

   }

   private List<ItemStack> getItemToThrow(Villager pVillager) {
      if (pVillager.isBaby()) {
         return ImmutableList.of(new ItemStack(Items.POPPY));
      } else {
         VillagerProfession villagerprofession = pVillager.getVillagerData().getProfession();
         if (GIFTS.containsKey(villagerprofession)) {
            LootTable loottable = pVillager.level().getServer().getLootData().getLootTable(GIFTS.get(villagerprofession));
            LootParams lootparams = (new LootParams.Builder((ServerLevel)pVillager.level())).withParameter(LootContextParams.ORIGIN, pVillager.position()).withParameter(LootContextParams.THIS_ENTITY, pVillager).create(LootContextParamSets.GIFT);
            return loottable.getRandomItems(lootparams);
         } else {
            return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
         }
      }
   }

   private boolean isHeroVisible(Villager pVillager) {
      return this.getNearestTargetableHero(pVillager).isPresent();
   }

   private Optional<Player> getNearestTargetableHero(Villager pVillager) {
      return pVillager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
   }

   private boolean isHero(Player p_23018_) {
      return p_23018_.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
   }

   private boolean isWithinThrowingDistance(Villager pVillager, Player pHero) {
      BlockPos blockpos = pHero.blockPosition();
      BlockPos blockpos1 = pVillager.blockPosition();
      return blockpos1.closerThan(blockpos, 5.0D);
   }

   private static int calculateTimeUntilNextGift(ServerLevel pLevel) {
      return 600 + pLevel.random.nextInt(6001);
   }
}