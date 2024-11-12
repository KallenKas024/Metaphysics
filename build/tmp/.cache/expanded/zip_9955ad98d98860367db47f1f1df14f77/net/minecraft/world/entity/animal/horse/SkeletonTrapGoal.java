package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SkeletonTrapGoal extends Goal {
   private final SkeletonHorse horse;

   public SkeletonTrapGoal(SkeletonHorse pHorse) {
      this.horse = pHorse;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.horse.level().hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0D);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      ServerLevel serverlevel = (ServerLevel)this.horse.level();
      // Forge: Trigger the trap in a tick task to avoid crashes when mods add goals to skeleton horses
      // (MC-206338/Forge PR #7509)
      serverlevel.getServer().tell(new net.minecraft.server.TickTask(serverlevel.getServer().getTickCount(), () -> {
      if (!this.horse.isAlive()) return;
      DifficultyInstance difficultyinstance = serverlevel.getCurrentDifficultyAt(this.horse.blockPosition());
      this.horse.setTrap(false);
      this.horse.setTamed(true);
      this.horse.setAge(0);
      LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(serverlevel);
      if (lightningbolt != null) {
         lightningbolt.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
         lightningbolt.setVisualOnly(true);
         serverlevel.addFreshEntity(lightningbolt);
         Skeleton skeleton = this.createSkeleton(difficultyinstance, this.horse);
         if (skeleton != null) {
            skeleton.startRiding(this.horse);
            serverlevel.addFreshEntityWithPassengers(skeleton);

            for(int i = 0; i < 3; ++i) {
               AbstractHorse abstracthorse = this.createHorse(difficultyinstance);
               if (abstracthorse != null) {
                  Skeleton skeleton1 = this.createSkeleton(difficultyinstance, abstracthorse);
                  if (skeleton1 != null) {
                     skeleton1.startRiding(abstracthorse);
                     abstracthorse.push(this.horse.getRandom().triangle(0.0D, 1.1485D), 0.0D, this.horse.getRandom().triangle(0.0D, 1.1485D));
                     serverlevel.addFreshEntityWithPassengers(abstracthorse);
                  }
               }
            }

         }
      }
      }));
   }

   @Nullable
   private AbstractHorse createHorse(DifficultyInstance pDifficulty) {
      SkeletonHorse skeletonhorse = EntityType.SKELETON_HORSE.create(this.horse.level());
      if (skeletonhorse != null) {
         skeletonhorse.finalizeSpawn((ServerLevel)this.horse.level(), pDifficulty, MobSpawnType.TRIGGERED, (SpawnGroupData)null, (CompoundTag)null);
         skeletonhorse.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
         skeletonhorse.invulnerableTime = 60;
         skeletonhorse.setPersistenceRequired();
         skeletonhorse.setTamed(true);
         skeletonhorse.setAge(0);
      }

      return skeletonhorse;
   }

   @Nullable
   private Skeleton createSkeleton(DifficultyInstance pDifficulty, AbstractHorse pHorse) {
      Skeleton skeleton = EntityType.SKELETON.create(pHorse.level());
      if (skeleton != null) {
         skeleton.finalizeSpawn((ServerLevel)pHorse.level(), pDifficulty, MobSpawnType.TRIGGERED, (SpawnGroupData)null, (CompoundTag)null);
         skeleton.setPos(pHorse.getX(), pHorse.getY(), pHorse.getZ());
         skeleton.invulnerableTime = 60;
         skeleton.setPersistenceRequired();
         if (skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
         }

         skeleton.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(skeleton.getRandom(), this.disenchant(skeleton.getMainHandItem()), (int)(5.0F + pDifficulty.getSpecialMultiplier() * (float)skeleton.getRandom().nextInt(18)), false));
         skeleton.setItemSlot(EquipmentSlot.HEAD, EnchantmentHelper.enchantItem(skeleton.getRandom(), this.disenchant(skeleton.getItemBySlot(EquipmentSlot.HEAD)), (int)(5.0F + pDifficulty.getSpecialMultiplier() * (float)skeleton.getRandom().nextInt(18)), false));
      }

      return skeleton;
   }

   private ItemStack disenchant(ItemStack pStack) {
      pStack.removeTagKey("Enchantments");
      return pStack;
   }
}
