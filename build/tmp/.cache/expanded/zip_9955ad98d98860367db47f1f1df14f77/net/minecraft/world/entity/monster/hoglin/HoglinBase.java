package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
   int ATTACK_ANIMATION_DURATION = 10;

   int getAttackAnimationRemainingTicks();

   static boolean hurtAndThrowTarget(LivingEntity pHoglin, LivingEntity pTarget) {
      float f1 = (float)pHoglin.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f;
      if (!pHoglin.isBaby() && (int)f1 > 0) {
         f = f1 / 2.0F + (float)pHoglin.level().random.nextInt((int)f1);
      } else {
         f = f1;
      }

      boolean flag = pTarget.hurt(pHoglin.damageSources().mobAttack(pHoglin), f);
      if (flag) {
         pHoglin.doEnchantDamageEffects(pHoglin, pTarget);
         if (!pHoglin.isBaby()) {
            throwTarget(pHoglin, pTarget);
         }
      }

      return flag;
   }

   static void throwTarget(LivingEntity pHoglin, LivingEntity pTarget) {
      double d0 = pHoglin.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      double d1 = pTarget.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
      double d2 = d0 - d1;
      if (!(d2 <= 0.0D)) {
         double d3 = pTarget.getX() - pHoglin.getX();
         double d4 = pTarget.getZ() - pHoglin.getZ();
         float f = (float)(pHoglin.level().random.nextInt(21) - 10);
         double d5 = d2 * (double)(pHoglin.level().random.nextFloat() * 0.5F + 0.2F);
         Vec3 vec3 = (new Vec3(d3, 0.0D, d4)).normalize().scale(d5).yRot(f);
         double d6 = d2 * (double)pHoglin.level().random.nextFloat() * 0.5D;
         pTarget.push(vec3.x, d6, vec3.z);
         pTarget.hurtMarked = true;
      }
   }
}