package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

public class RemoveBlockGoal extends MoveToBlockGoal {
   private final Block blockToRemove;
   private final Mob removerMob;
   private int ticksSinceReachedGoal;
   private static final int WAIT_AFTER_BLOCK_FOUND = 20;

   public RemoveBlockGoal(Block pBlockToRemove, PathfinderMob pRemoverMob, double pSpeedModifier, int pSearchRange) {
      super(pRemoverMob, pSpeedModifier, 24, pSearchRange);
      this.blockToRemove = pBlockToRemove;
      this.removerMob = pRemoverMob;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.removerMob.level(), this.removerMob)) {
         return false;
      } else if (this.nextStartTick > 0) {
         --this.nextStartTick;
         return false;
      } else if (this.findNearestBlock()) {
         this.nextStartTick = reducedTickDelay(20);
         return true;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         return false;
      }
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.removerMob.fallDistance = 1.0F;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.ticksSinceReachedGoal = 0;
   }

   public void playDestroyProgressSound(LevelAccessor pLevel, BlockPos pPos) {
   }

   public void playBreakSound(Level pLevel, BlockPos pPos) {
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      super.tick();
      Level level = this.removerMob.level();
      BlockPos blockpos = this.removerMob.blockPosition();
      BlockPos blockpos1 = this.getPosWithBlock(blockpos, level);
      RandomSource randomsource = this.removerMob.getRandom();
      if (this.isReachedTarget() && blockpos1 != null) {
         if (this.ticksSinceReachedGoal > 0) {
            Vec3 vec3 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vec3.x, 0.3D, vec3.z);
            if (!level.isClientSide) {
               double d0 = 0.08D;
               ((ServerLevel)level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)), (double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.7D, (double)blockpos1.getZ() + 0.5D, 3, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, ((double)randomsource.nextFloat() - 0.5D) * 0.08D, (double)0.15F);
            }
         }

         if (this.ticksSinceReachedGoal % 2 == 0) {
            Vec3 vec31 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vec31.x, -0.3D, vec31.z);
            if (this.ticksSinceReachedGoal % 6 == 0) {
               this.playDestroyProgressSound(level, this.blockPos);
            }
         }

         if (this.ticksSinceReachedGoal > 60) {
            level.removeBlock(blockpos1, false);
            if (!level.isClientSide) {
               for(int i = 0; i < 20; ++i) {
                  double d3 = randomsource.nextGaussian() * 0.02D;
                  double d1 = randomsource.nextGaussian() * 0.02D;
                  double d2 = randomsource.nextGaussian() * 0.02D;
                  ((ServerLevel)level).sendParticles(ParticleTypes.POOF, (double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double)0.15F);
               }

               this.playBreakSound(level, blockpos1);
            }
         }

         ++this.ticksSinceReachedGoal;
      }

   }

   @Nullable
   private BlockPos getPosWithBlock(BlockPos pPos, BlockGetter pLevel) {
      if (pLevel.getBlockState(pPos).is(this.blockToRemove)) {
         return pPos;
      } else {
         BlockPos[] ablockpos = new BlockPos[]{pPos.below(), pPos.west(), pPos.east(), pPos.north(), pPos.south(), pPos.below().below()};

         for(BlockPos blockpos : ablockpos) {
            if (pLevel.getBlockState(blockpos).is(this.blockToRemove)) {
               return blockpos;
            }
         }

         return null;
      }
   }

   /**
    * Return {@code true} to set given position as destination
    */
   protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
      ChunkAccess chunkaccess = pLevel.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), ChunkStatus.FULL, false);
      if (chunkaccess == null) {
         return false;
      } else {
         if (!chunkaccess.getBlockState(pPos).canEntityDestroy(pLevel, pPos, this.removerMob)) return false;
         return chunkaccess.getBlockState(pPos).is(this.blockToRemove) && chunkaccess.getBlockState(pPos.above()).isAir() && chunkaccess.getBlockState(pPos.above(2)).isAir();
      }
   }
}
