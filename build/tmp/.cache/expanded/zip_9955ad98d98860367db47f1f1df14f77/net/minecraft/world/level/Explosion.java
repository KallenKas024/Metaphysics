package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Explosion {
   private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
   private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
   private final boolean fire;
   private final Explosion.BlockInteraction blockInteraction;
   private final RandomSource random = RandomSource.create();
   private final Level level;
   private final double x;
   private final double y;
   private final double z;
   @Nullable
   private final Entity source;
   private final float radius;
   private final DamageSource damageSource;
   private final ExplosionDamageCalculator damageCalculator;
   private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
   private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
   private final Vec3 position;

   public Explosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, List<BlockPos> pPositions) {
      this(pLevel, pSource, pToBlowX, pToBlowY, pToBlowZ, pRadius, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY, pPositions);
   }

   public Explosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, Explosion.BlockInteraction pBlockInteraction, List<BlockPos> pPositions) {
      this(pLevel, pSource, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
      this.toBlow.addAll(pPositions);
   }

   public Explosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, Explosion.BlockInteraction pBlockInteraction) {
      this(pLevel, pSource, (DamageSource)null, (ExplosionDamageCalculator)null, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
   }

   public Explosion(Level pLevel, @Nullable Entity pSource, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pDamageCalculator, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, Explosion.BlockInteraction pBlockInteraction) {
      this.level = pLevel;
      this.source = pSource;
      this.radius = pRadius;
      this.x = pToBlowX;
      this.y = pToBlowY;
      this.z = pToBlowZ;
      this.fire = pFire;
      this.blockInteraction = pBlockInteraction;
      this.damageSource = pDamageSource == null ? pLevel.damageSources().explosion(this) : pDamageSource;
      this.damageCalculator = pDamageCalculator == null ? this.makeDamageCalculator(pSource) : pDamageCalculator;
      this.position = new Vec3(this.x, this.y, this.z);
   }

   private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity pEntity) {
      return (ExplosionDamageCalculator)(pEntity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(pEntity));
   }

   public static float getSeenPercent(Vec3 pExplosionVector, Entity pEntity) {
      AABB aabb = pEntity.getBoundingBox();
      double d0 = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
      double d1 = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
      double d2 = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
      double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
      double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
      if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
         int i = 0;
         int j = 0;

         for(double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
            for(double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
               for(double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                  double d8 = Mth.lerp(d5, aabb.minX, aabb.maxX);
                  double d9 = Mth.lerp(d6, aabb.minY, aabb.maxY);
                  double d10 = Mth.lerp(d7, aabb.minZ, aabb.maxZ);
                  Vec3 vec3 = new Vec3(d8 + d3, d9, d10 + d4);
                  if (pEntity.level().clip(new ClipContext(vec3, pExplosionVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pEntity)).getType() == HitResult.Type.MISS) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (float)i / (float)j;
      } else {
         return 0.0F;
      }
   }

   /**
    * Does the first part of the explosion (destroy blocks)
    */
   public void explode() {
      this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
      Set<BlockPos> set = Sets.newHashSet();
      int i = 16;

      for(int j = 0; j < 16; ++j) {
         for(int k = 0; k < 16; ++k) {
            for(int l = 0; l < 16; ++l) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                  double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                  double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 /= d3;
                  d1 /= d3;
                  d2 /= d3;
                  float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                  double d4 = this.x;
                  double d6 = this.y;
                  double d8 = this.z;

                  for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                     BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                     BlockState blockstate = this.level.getBlockState(blockpos);
                     FluidState fluidstate = this.level.getFluidState(blockpos);
                     if (!this.level.isInWorldBounds(blockpos)) {
                        break;
                     }

                     Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                     if (optional.isPresent()) {
                        f -= (optional.get() + 0.3F) * 0.3F;
                     }

                     if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                        set.add(blockpos);
                     }

                     d4 += d0 * (double)0.3F;
                     d6 += d1 * (double)0.3F;
                     d8 += d2 * (double)0.3F;
                  }
               }
            }
         }
      }

      this.toBlow.addAll(set);
      float f2 = this.radius * 2.0F;
      int k1 = Mth.floor(this.x - (double)f2 - 1.0D);
      int l1 = Mth.floor(this.x + (double)f2 + 1.0D);
      int i2 = Mth.floor(this.y - (double)f2 - 1.0D);
      int i1 = Mth.floor(this.y + (double)f2 + 1.0D);
      int j2 = Mth.floor(this.z - (double)f2 - 1.0D);
      int j1 = Mth.floor(this.z + (double)f2 + 1.0D);
      List<Entity> list = this.level.getEntities(this.source, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
      net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, list, f2);
      Vec3 vec3 = new Vec3(this.x, this.y, this.z);

      for(int k2 = 0; k2 < list.size(); ++k2) {
         Entity entity = list.get(k2);
         if (!entity.ignoreExplosion()) {
            double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
            if (d12 <= 1.0D) {
               double d5 = entity.getX() - this.x;
               double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
               double d9 = entity.getZ() - this.z;
               double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
               if (d13 != 0.0D) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  double d14 = (double)getSeenPercent(vec3, entity);
                  double d10 = (1.0D - d12) * d14;
                  entity.hurt(this.getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f2 + 1.0D)));
                  double d11;
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingentity = (LivingEntity)entity;
                     d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d10);
                  } else {
                     d11 = d10;
                  }

                  d5 *= d11;
                  d7 *= d11;
                  d9 *= d11;
                  Vec3 vec31 = new Vec3(d5, d7, d9);
                  entity.setDeltaMovement(entity.getDeltaMovement().add(vec31));
                  if (entity instanceof Player) {
                     Player player = (Player)entity;
                     if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                        this.hitPlayers.put(player, vec31);
                     }
                  }
               }
            }
         }
      }

   }

   /**
    * Does the second part of the explosion (sound, particles, drop spawn)
    */
   public void finalizeExplosion(boolean pSpawnParticles) {
      if (this.level.isClientSide) {
         this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
      }

      boolean flag = this.interactsWithBlocks();
      if (pSpawnParticles) {
         if (!(this.radius < 2.0F) && flag) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         } else {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         }
      }

      if (flag) {
         ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
         boolean flag1 = this.getIndirectSourceEntity() instanceof Player;
         Util.shuffle(this.toBlow, this.level.random);

         for(BlockPos blockpos : this.toBlow) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (!blockstate.isAir()) {
               BlockPos blockpos1 = blockpos.immutable();
               this.level.getProfiler().push("explosion_blocks");
               if (blockstate.canDropFromExplosion(this.level, blockpos, this)) {
                  Level $$9 = this.level;
                  if ($$9 instanceof ServerLevel) {
                     ServerLevel serverlevel = (ServerLevel)$$9;
                     BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                     LootParams.Builder lootparams$builder = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                     if (this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                        lootparams$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                     }

                     blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag1);
                     blockstate.getDrops(lootparams$builder).forEach((p_46074_) -> {
                        addBlockDrops(objectarraylist, p_46074_, blockpos1);
                     });
                  }
               }

               blockstate.onBlockExploded(this.level, blockpos, this);
               this.level.getProfiler().pop();
            }
         }

         for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
         }
      }

      if (this.fire) {
         for(BlockPos blockpos2 : this.toBlow) {
            if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
               this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
            }
         }
      }

   }

   public boolean interactsWithBlocks() {
      return this.blockInteraction != Explosion.BlockInteraction.KEEP;
   }

   private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack pStack, BlockPos pPos) {
      int i = pDropPositionArray.size();

      for(int j = 0; j < i; ++j) {
         Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(j);
         ItemStack itemstack = pair.getFirst();
         if (ItemEntity.areMergable(itemstack, pStack)) {
            ItemStack itemstack1 = ItemEntity.merge(itemstack, pStack, 16);
            pDropPositionArray.set(j, Pair.of(itemstack1, pair.getSecond()));
            if (pStack.isEmpty()) {
               return;
            }
         }
      }

      pDropPositionArray.add(Pair.of(pStack, pPos));
   }

   public DamageSource getDamageSource() {
      return this.damageSource;
   }

   public Map<Player, Vec3> getHitPlayers() {
      return this.hitPlayers;
   }

   @Nullable
   public LivingEntity getIndirectSourceEntity() {
      if (this.source == null) {
         return null;
      } else {
         Entity entity = this.source;
         if (entity instanceof PrimedTnt) {
            PrimedTnt primedtnt = (PrimedTnt)entity;
            return primedtnt.getOwner();
         } else {
            entity = this.source;
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity;
               return livingentity;
            } else {
               entity = this.source;
               if (entity instanceof Projectile) {
                  Projectile projectile = (Projectile)entity;
                  entity = projectile.getOwner();
                  if (entity instanceof LivingEntity) {
                     return (LivingEntity)entity;
                  }
               }

               return null;
            }
         }
      }
   }

   /**
    * Returns either the entity that placed the explosive block, the entity that caused the explosion or null.
    */
   @Nullable
   public Entity getDirectSourceEntity() {
      return this.source;
   }

   public void clearToBlow() {
      this.toBlow.clear();
   }

   public List<BlockPos> getToBlow() {
      return this.toBlow;
   }

   public Vec3 getPosition() {
      return this.position;
   }

   @Nullable
   public Entity getExploder() {
      return this.source;
   }

   public static enum BlockInteraction {
      KEEP,
      DESTROY,
      DESTROY_WITH_DECAY;
   }
}
