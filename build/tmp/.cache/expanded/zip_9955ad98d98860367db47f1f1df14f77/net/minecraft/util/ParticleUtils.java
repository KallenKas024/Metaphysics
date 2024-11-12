package net.minecraft.util;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
   public static void spawnParticlesOnBlockFaces(Level pLevel, BlockPos pPos, ParticleOptions pParticle, IntProvider pCount) {
      for(Direction direction : Direction.values()) {
         spawnParticlesOnBlockFace(pLevel, pPos, pParticle, pCount, direction, () -> {
            return getRandomSpeedRanges(pLevel.random);
         }, 0.55D);
      }

   }

   public static void spawnParticlesOnBlockFace(Level pLevel, BlockPos pPos, ParticleOptions pParticle, IntProvider pCount, Direction pDirection, Supplier<Vec3> pSpeedSupplier, double p_216325_) {
      int i = pCount.sample(pLevel.random);

      for(int j = 0; j < i; ++j) {
         spawnParticleOnFace(pLevel, pPos, pDirection, pParticle, pSpeedSupplier.get(), p_216325_);
      }

   }

   private static Vec3 getRandomSpeedRanges(RandomSource pRandom) {
      return new Vec3(Mth.nextDouble(pRandom, -0.5D, 0.5D), Mth.nextDouble(pRandom, -0.5D, 0.5D), Mth.nextDouble(pRandom, -0.5D, 0.5D));
   }

   public static void spawnParticlesAlongAxis(Direction.Axis pAxis, Level pLevel, BlockPos pPos, double p_144971_, ParticleOptions pParticle, UniformInt pCount) {
      Vec3 vec3 = Vec3.atCenterOf(pPos);
      boolean flag = pAxis == Direction.Axis.X;
      boolean flag1 = pAxis == Direction.Axis.Y;
      boolean flag2 = pAxis == Direction.Axis.Z;
      int i = pCount.sample(pLevel.random);

      for(int j = 0; j < i; ++j) {
         double d0 = vec3.x + Mth.nextDouble(pLevel.random, -1.0D, 1.0D) * (flag ? 0.5D : p_144971_);
         double d1 = vec3.y + Mth.nextDouble(pLevel.random, -1.0D, 1.0D) * (flag1 ? 0.5D : p_144971_);
         double d2 = vec3.z + Mth.nextDouble(pLevel.random, -1.0D, 1.0D) * (flag2 ? 0.5D : p_144971_);
         double d3 = flag ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
         double d4 = flag1 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
         double d5 = flag2 ? Mth.nextDouble(pLevel.random, -1.0D, 1.0D) : 0.0D;
         pLevel.addParticle(pParticle, d0, d1, d2, d3, d4, d5);
      }

   }

   public static void spawnParticleOnFace(Level pLevel, BlockPos pPos, Direction pDirection, ParticleOptions pParticle, Vec3 pSpeed, double p_216312_) {
      Vec3 vec3 = Vec3.atCenterOf(pPos);
      int i = pDirection.getStepX();
      int j = pDirection.getStepY();
      int k = pDirection.getStepZ();
      double d0 = vec3.x + (i == 0 ? Mth.nextDouble(pLevel.random, -0.5D, 0.5D) : (double)i * p_216312_);
      double d1 = vec3.y + (j == 0 ? Mth.nextDouble(pLevel.random, -0.5D, 0.5D) : (double)j * p_216312_);
      double d2 = vec3.z + (k == 0 ? Mth.nextDouble(pLevel.random, -0.5D, 0.5D) : (double)k * p_216312_);
      double d3 = i == 0 ? pSpeed.x() : 0.0D;
      double d4 = j == 0 ? pSpeed.y() : 0.0D;
      double d5 = k == 0 ? pSpeed.z() : 0.0D;
      pLevel.addParticle(pParticle, d0, d1, d2, d3, d4, d5);
   }

   public static void spawnParticleBelow(Level pLevel, BlockPos pPos, RandomSource pRandom, ParticleOptions pParticle) {
      double d0 = (double)pPos.getX() + pRandom.nextDouble();
      double d1 = (double)pPos.getY() - 0.05D;
      double d2 = (double)pPos.getZ() + pRandom.nextDouble();
      pLevel.addParticle(pParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }
}