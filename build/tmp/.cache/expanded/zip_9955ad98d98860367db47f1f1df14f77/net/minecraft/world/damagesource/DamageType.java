package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
   public static final Codec<DamageType> CODEC = RecordCodecBuilder.create((p_270460_) -> {
      return p_270460_.group(Codec.STRING.fieldOf("message_id").forGetter(DamageType::msgId), DamageScaling.CODEC.fieldOf("scaling").forGetter(DamageType::scaling), Codec.FLOAT.fieldOf("exhaustion").forGetter(DamageType::exhaustion), DamageEffects.CODEC.optionalFieldOf("effects", DamageEffects.HURT).forGetter(DamageType::effects), DeathMessageType.CODEC.optionalFieldOf("death_message_type", DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)).apply(p_270460_, DamageType::new);
   });

   public DamageType(String pMsgId, DamageScaling pScaling, float pExhaustion) {
      this(pMsgId, pScaling, pExhaustion, DamageEffects.HURT, DeathMessageType.DEFAULT);
   }

   public DamageType(String pMsgId, DamageScaling pScaling, float pExhaustion, DamageEffects pEffects) {
      this(pMsgId, pScaling, pExhaustion, pEffects, DeathMessageType.DEFAULT);
   }

   public DamageType(String pMsgId, float pExhaustion, DamageEffects pEffects) {
      this(pMsgId, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, pExhaustion, pEffects);
   }

   public DamageType(String pMsgId, float pExhaustion) {
      this(pMsgId, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, pExhaustion);
   }
}