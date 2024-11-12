package net.minecraft.util.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface WeightedEntry {
   Weight getWeight();

   static <T> WeightedEntry.Wrapper<T> wrap(T pData, int pWeight) {
      return new WeightedEntry.Wrapper<>(pData, Weight.of(pWeight));
   }

   public static class IntrusiveBase implements WeightedEntry {
      private final Weight weight;

      public IntrusiveBase(int pWeight) {
         this.weight = Weight.of(pWeight);
      }

      public IntrusiveBase(Weight pWeight) {
         this.weight = pWeight;
      }

      public Weight getWeight() {
         return this.weight;
      }
   }

   public static class Wrapper<T> implements WeightedEntry {
      private final T data;
      private final Weight weight;

      Wrapper(T p_146302_, Weight p_146303_) {
         this.data = p_146302_;
         this.weight = p_146303_;
      }

      public T getData() {
         return this.data;
      }

      public Weight getWeight() {
         return this.weight;
      }

      public static <E> Codec<WeightedEntry.Wrapper<E>> codec(Codec<E> pElementCodec) {
         return RecordCodecBuilder.create((p_146309_) -> {
            return p_146309_.group(pElementCodec.fieldOf("data").forGetter(WeightedEntry.Wrapper::getData), Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.Wrapper::getWeight)).apply(p_146309_, WeightedEntry.Wrapper::new);
         });
      }
   }
}