package net.minecraft.world.entity;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LightningBolt extends Entity {
   private static final int START_LIFE = 2;
   private static final double DAMAGE_RADIUS = 3.0D;
   private static final double DETECTION_RADIUS = 15.0D;
   private int life;
   public long seed;
   private int flashes;
   private boolean visualOnly;
   @Nullable
   private ServerPlayer cause;
   private final Set<Entity> hitEntities = Sets.newHashSet();
   private int blocksSetOnFire;
   private float damage = 5.0F;

   public LightningBolt(EntityType<? extends LightningBolt> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.noCulling = true;
      this.life = 2;
      this.seed = this.random.nextLong();
      this.flashes = this.random.nextInt(3) + 1;
   }

   public void setVisualOnly(boolean pVisualOnly) {
      this.visualOnly = pVisualOnly;
   }

   public SoundSource getSoundSource() {
      return SoundSource.WEATHER;
   }

   @Nullable
   public ServerPlayer getCause() {
      return this.cause;
   }

   public void setCause(@Nullable ServerPlayer pCause) {
      this.cause = pCause;
   }

   private void powerLightningRod() {
      BlockPos blockpos = this.getStrikePosition();
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (blockstate.is(Blocks.LIGHTNING_ROD)) {
         ((LightningRodBlock)blockstate.getBlock()).onLightningStrike(blockstate, this.level(), blockpos);
      }

   }

   public void setDamage(float damage) {
      this.damage = damage;
   }

   public float getDamage() {
      return this.damage;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.life == 2) {
         if (this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000.0F, 0.8F + this.random.nextFloat() * 0.2F, false);
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0F, 0.5F + this.random.nextFloat() * 0.2F, false);
         } else {
            Difficulty difficulty = this.level().getDifficulty();
            if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
               this.spawnFire(4);
            }

            this.powerLightningRod();
            clearCopperOnLightningStrike(this.level(), this.getStrikePosition());
            this.gameEvent(GameEvent.LIGHTNING_STRIKE);
         }
      }

      --this.life;
      if (this.life < 0) {
         if (this.flashes == 0) {
            if (this.level() instanceof ServerLevel) {
               List<Entity> list = this.level().getEntities(this, new AABB(this.getX() - 15.0D, this.getY() - 15.0D, this.getZ() - 15.0D, this.getX() + 15.0D, this.getY() + 6.0D + 15.0D, this.getZ() + 15.0D), (p_147140_) -> {
                  return p_147140_.isAlive() && !this.hitEntities.contains(p_147140_);
               });

               for(ServerPlayer serverplayer : ((ServerLevel)this.level()).getPlayers((p_147157_) -> {
                  return p_147157_.distanceTo(this) < 256.0F;
               })) {
                  CriteriaTriggers.LIGHTNING_STRIKE.trigger(serverplayer, this, list);
               }
            }

            this.discard();
         } else if (this.life < -this.random.nextInt(10)) {
            --this.flashes;
            this.life = 1;
            this.seed = this.random.nextLong();
            this.spawnFire(0);
         }
      }

      if (this.life >= 0) {
         if (!(this.level() instanceof ServerLevel)) {
            this.level().setSkyFlashTime(2);
         } else if (!this.visualOnly) {
            List<Entity> list1 = this.level().getEntities(this, new AABB(this.getX() - 3.0D, this.getY() - 3.0D, this.getZ() - 3.0D, this.getX() + 3.0D, this.getY() + 6.0D + 3.0D, this.getZ() + 3.0D), Entity::isAlive);

            for(Entity entity : list1) {
               if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this))
               entity.thunderHit((ServerLevel)this.level(), this);
            }

            this.hitEntities.addAll(list1);
            if (this.cause != null) {
               CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, list1);
            }
         }
      }

   }

   private BlockPos getStrikePosition() {
      Vec3 vec3 = this.position();
      return BlockPos.containing(vec3.x, vec3.y - 1.0E-6D, vec3.z);
   }

   private void spawnFire(int pExtraIgnitions) {
      if (!this.visualOnly && !this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
         BlockPos blockpos = this.blockPosition();
         BlockState blockstate = BaseFireBlock.getState(this.level(), blockpos);
         if (this.level().getBlockState(blockpos).isAir() && blockstate.canSurvive(this.level(), blockpos)) {
            this.level().setBlockAndUpdate(blockpos, blockstate);
            ++this.blocksSetOnFire;
         }

         for(int i = 0; i < pExtraIgnitions; ++i) {
            BlockPos blockpos1 = blockpos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
            blockstate = BaseFireBlock.getState(this.level(), blockpos1);
            if (this.level().getBlockState(blockpos1).isAir() && blockstate.canSurvive(this.level(), blockpos1)) {
               this.level().setBlockAndUpdate(blockpos1, blockstate);
               ++this.blocksSetOnFire;
            }
         }

      }
   }

   private static void clearCopperOnLightningStrike(Level pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      BlockPos blockpos;
      BlockState blockstate1;
      if (blockstate.is(Blocks.LIGHTNING_ROD)) {
         blockpos = pPos.relative(blockstate.getValue(LightningRodBlock.FACING).getOpposite());
         blockstate1 = pLevel.getBlockState(blockpos);
      } else {
         blockpos = pPos;
         blockstate1 = blockstate;
      }

      if (blockstate1.getBlock() instanceof WeatheringCopper) {
         pLevel.setBlockAndUpdate(blockpos, WeatheringCopper.getFirst(pLevel.getBlockState(blockpos)));
         BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
         int i = pLevel.random.nextInt(3) + 3;

         for(int j = 0; j < i; ++j) {
            int k = pLevel.random.nextInt(8) + 1;
            randomWalkCleaningCopper(pLevel, blockpos, blockpos$mutableblockpos, k);
         }

      }
   }

   private static void randomWalkCleaningCopper(Level pLevel, BlockPos pPos, BlockPos.MutableBlockPos pMutable, int pSteps) {
      pMutable.set(pPos);

      for(int i = 0; i < pSteps; ++i) {
         Optional<BlockPos> optional = randomStepCleaningCopper(pLevel, pMutable);
         if (!optional.isPresent()) {
            break;
         }

         pMutable.set(optional.get());
      }

   }

   private static Optional<BlockPos> randomStepCleaningCopper(Level pLevel, BlockPos pPos) {
      for(BlockPos blockpos : BlockPos.randomInCube(pLevel.random, 10, pPos, 1)) {
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.getBlock() instanceof WeatheringCopper) {
            WeatheringCopper.getPrevious(blockstate).ifPresent((p_147144_) -> {
               pLevel.setBlockAndUpdate(blockpos, p_147144_);
            });
            pLevel.levelEvent(3002, blockpos, -1);
            return Optional.of(blockpos);
         }
      }

      return Optional.empty();
   }

   /**
    * Checks if the entity is in range to render.
    */
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = 64.0D * getViewScale();
      return pDistance < d0 * d0;
   }

   protected void defineSynchedData() {
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
   }

   public int getBlocksSetOnFire() {
      return this.blocksSetOnFire;
   }

   public Stream<Entity> getHitEntities() {
      return this.hitEntities.stream().filter(Entity::isAlive);
   }
}
