package net.minecraft.world.effect;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class MobEffectUtil {
   public static Component formatDuration(MobEffectInstance pEffect, float pDurationFactor) {
      if (pEffect.isInfiniteDuration()) {
         return Component.translatable("effect.duration.infinite");
      } else {
         int i = Mth.floor((float)pEffect.getDuration() * pDurationFactor);
         return Component.literal(StringUtil.formatTickDuration(i));
      }
   }

   public static boolean hasDigSpeed(LivingEntity pEntity) {
      return pEntity.hasEffect(MobEffects.DIG_SPEED) || pEntity.hasEffect(MobEffects.CONDUIT_POWER);
   }

   public static int getDigSpeedAmplification(LivingEntity pEntity) {
      int i = 0;
      int j = 0;
      if (pEntity.hasEffect(MobEffects.DIG_SPEED)) {
         i = pEntity.getEffect(MobEffects.DIG_SPEED).getAmplifier();
      }

      if (pEntity.hasEffect(MobEffects.CONDUIT_POWER)) {
         j = pEntity.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
      }

      return Math.max(i, j);
   }

   public static boolean hasWaterBreathing(LivingEntity pEntity) {
      return pEntity.hasEffect(MobEffects.WATER_BREATHING) || pEntity.hasEffect(MobEffects.CONDUIT_POWER);
   }

   public static List<ServerPlayer> addEffectToPlayersAround(ServerLevel pLevel, @Nullable Entity pSource, Vec3 pPos, double pRadius, MobEffectInstance pEffect, int pDuration) {
      MobEffect mobeffect = pEffect.getEffect();
      List<ServerPlayer> list = pLevel.getPlayers((p_267925_) -> {
         return p_267925_.gameMode.isSurvival() && (pSource == null || !pSource.isAlliedTo(p_267925_)) && pPos.closerThan(p_267925_.position(), pRadius) && (!p_267925_.hasEffect(mobeffect) || p_267925_.getEffect(mobeffect).getAmplifier() < pEffect.getAmplifier() || p_267925_.getEffect(mobeffect).endsWithin(pDuration - 1));
      });
      list.forEach((p_238232_) -> {
         p_238232_.addEffect(new MobEffectInstance(pEffect), pSource);
      });
      return list;
   }
}