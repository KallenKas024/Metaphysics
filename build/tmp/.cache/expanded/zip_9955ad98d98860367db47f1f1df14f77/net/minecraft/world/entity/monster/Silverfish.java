package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Silverfish extends Monster {
   @Nullable
   private Silverfish.SilverfishWakeUpFriendsGoal friendsGoal;

   public Silverfish(EntityType<? extends Silverfish> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void registerGoals() {
      this.friendsGoal = new Silverfish.SilverfishWakeUpFriendsGoal(this);
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
      this.goalSelector.addGoal(3, this.friendsGoal);
      this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(5, new Silverfish.SilverfishMergeWithStoneGoal(this));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return 0.1D;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 0.13F;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 1.0D);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SILVERFISH_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SILVERFISH_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SILVERFISH_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         if ((pSource.getEntity() != null || pSource.is(DamageTypeTags.ALWAYS_TRIGGERS_SILVERFISH)) && this.friendsGoal != null) {
            this.friendsGoal.notifyHurt();
         }

         return super.hurt(pSource, pAmount);
      }
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      this.yBodyRot = this.getYRot();
      super.tick();
   }

   /**
    * Set the render yaw offset
    */
   public void setYBodyRot(float pOffset) {
      this.setYRot(pOffset);
      super.setYBodyRot(pOffset);
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return InfestedBlock.isCompatibleHostBlock(pLevel.getBlockState(pPos.below())) ? 10.0F : super.getWalkTargetValue(pPos, pLevel);
   }

   public static boolean checkSilverfishSpawnRules(EntityType<Silverfish> pSilverfish, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      if (checkAnyLightMonsterSpawnRules(pSilverfish, pLevel, pSpawnType, pPos, pRandom)) {
         Player player = pLevel.getNearestPlayer((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 5.0D, true);
         return player == null;
      } else {
         return false;
      }
   }

   public MobType getMobType() {
      return MobType.ARTHROPOD;
   }

   static class SilverfishMergeWithStoneGoal extends RandomStrollGoal {
      @Nullable
      private Direction selectedDirection;
      private boolean doMerge;

      public SilverfishMergeWithStoneGoal(Silverfish pSilverfish) {
         super(pSilverfish, 1.0D, 10);
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.mob.getTarget() != null) {
            return false;
         } else if (!this.mob.getNavigation().isDone()) {
            return false;
         } else {
            RandomSource randomsource = this.mob.getRandom();
            if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.mob.level(), this.mob) && randomsource.nextInt(reducedTickDelay(10)) == 0) {
               this.selectedDirection = Direction.getRandom(randomsource);
               BlockPos blockpos = BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()).relative(this.selectedDirection);
               BlockState blockstate = this.mob.level().getBlockState(blockpos);
               if (InfestedBlock.isCompatibleHostBlock(blockstate)) {
                  this.doMerge = true;
                  return true;
               }
            }

            this.doMerge = false;
            return super.canUse();
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.doMerge ? false : super.canContinueToUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         if (!this.doMerge) {
            super.start();
         } else {
            LevelAccessor levelaccessor = this.mob.level();
            BlockPos blockpos = BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()).relative(this.selectedDirection);
            BlockState blockstate = levelaccessor.getBlockState(blockpos);
            if (InfestedBlock.isCompatibleHostBlock(blockstate)) {
               levelaccessor.setBlock(blockpos, InfestedBlock.infestedStateByHost(blockstate), 3);
               this.mob.spawnAnim();
               this.mob.discard();
            }

         }
      }
   }

   static class SilverfishWakeUpFriendsGoal extends Goal {
      private final Silverfish silverfish;
      private int lookForFriends;

      public SilverfishWakeUpFriendsGoal(Silverfish pSilverfish) {
         this.silverfish = pSilverfish;
      }

      public void notifyHurt() {
         if (this.lookForFriends == 0) {
            this.lookForFriends = this.adjustedTickDelay(20);
         }

      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.lookForFriends > 0;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         --this.lookForFriends;
         if (this.lookForFriends <= 0) {
            Level level = this.silverfish.level();
            RandomSource randomsource = this.silverfish.getRandom();
            BlockPos blockpos = this.silverfish.blockPosition();

            for(int i = 0; i <= 5 && i >= -5; i = (i <= 0 ? 1 : 0) - i) {
               for(int j = 0; j <= 10 && j >= -10; j = (j <= 0 ? 1 : 0) - j) {
                  for(int k = 0; k <= 10 && k >= -10; k = (k <= 0 ? 1 : 0) - k) {
                     BlockPos blockpos1 = blockpos.offset(j, i, k);
                     BlockState blockstate = level.getBlockState(blockpos1);
                     Block block = blockstate.getBlock();
                     if (block instanceof InfestedBlock) {
                        if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this.silverfish)) {
                           level.destroyBlock(blockpos1, true, this.silverfish);
                        } else {
                           level.setBlock(blockpos1, ((InfestedBlock)block).hostStateByInfested(level.getBlockState(blockpos1)), 3);
                        }

                        if (randomsource.nextBoolean()) {
                           return;
                        }
                     }
                  }
               }
            }
         }

      }
   }
}
