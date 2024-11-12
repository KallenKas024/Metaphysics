package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class RandomSequence {
   public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create((p_287586_) -> {
      return p_287586_.group(XoroshiroRandomSource.CODEC.fieldOf("source").forGetter((p_287757_) -> {
         return p_287757_.source;
      })).apply(p_287586_, RandomSequence::new);
   });
   private final XoroshiroRandomSource source;

   public RandomSequence(XoroshiroRandomSource p_287597_) {
      this.source = p_287597_;
   }

   public RandomSequence(long pSeed, ResourceLocation pLocation) {
      this(createSequence(pSeed, pLocation));
   }

   private static XoroshiroRandomSource createSequence(long pSeed, ResourceLocation pLocation) {
      return new XoroshiroRandomSource(RandomSupport.upgradeSeedTo128bitUnmixed(pSeed).xor(seedForKey(pLocation)).mixed());
   }

   public static RandomSupport.Seed128bit seedForKey(ResourceLocation pKey) {
      return RandomSupport.seedFromHashOf(pKey.toString());
   }

   public RandomSource random() {
      return this.source;
   }
}