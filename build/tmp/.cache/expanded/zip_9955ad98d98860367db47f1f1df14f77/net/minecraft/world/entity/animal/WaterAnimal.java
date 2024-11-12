package net.minecraft.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class WaterAnimal extends PathfinderMob {
   protected WaterAnimal(EntityType<? extends WaterAnimal> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public MobType getMobType() {
      return MobType.WATER;
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      return pLevel.isUnobstructed(this);
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 120;
   }

   public int getExperienceReward() {
      return 1 + this.level().random.nextInt(3);
   }

   protected void handleAirSupply(int pAirSupply) {
      if (this.isAlive() && !this.isInWaterOrBubble()) {
         this.setAirSupply(pAirSupply - 1);
         if (this.getAirSupply() == -20) {
            this.setAirSupply(0);
            this.hurt(this.damageSources().drown(), 2.0F);
         }
      } else {
         this.setAirSupply(300);
      }

   }

   /**
    * Gets called every tick from main Entity class
    */
   public void baseTick() {
      int i = this.getAirSupply();
      super.baseTick();
      this.handleAirSupply(i);
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean canBeLeashed(Player pPlayer) {
      return false;
   }

   public static boolean checkSurfaceWaterAnimalSpawnRules(EntityType<? extends WaterAnimal> pWaterAnimal, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      int i = pLevel.getSeaLevel();
      int j = i - 13;
      return pPos.getY() >= j && pPos.getY() <= i && pLevel.getFluidState(pPos.below()).is(FluidTags.WATER) && pLevel.getBlockState(pPos.above()).is(Blocks.WATER);
   }
}