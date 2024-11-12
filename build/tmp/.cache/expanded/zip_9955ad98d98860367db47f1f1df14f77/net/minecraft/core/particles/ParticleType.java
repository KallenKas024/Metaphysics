package net.minecraft.core.particles;

import com.mojang.serialization.Codec;

public abstract class ParticleType<T extends ParticleOptions> {
   private final boolean overrideLimiter;
   private final ParticleOptions.Deserializer<T> deserializer;

   public ParticleType(boolean pOverrideLimiter, ParticleOptions.Deserializer<T> pDeserializer) {
      this.overrideLimiter = pOverrideLimiter;
      this.deserializer = pDeserializer;
   }

   public boolean getOverrideLimiter() {
      return this.overrideLimiter;
   }

   public ParticleOptions.Deserializer<T> getDeserializer() {
      return this.deserializer;
   }

   public abstract Codec<T> codec();
}