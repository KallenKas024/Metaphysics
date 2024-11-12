package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;

public class WeightedRandomList<E extends WeightedEntry> {
   private final int totalWeight;
   private final ImmutableList<E> items;

   WeightedRandomList(List<? extends E> pItems) {
      this.items = ImmutableList.copyOf(pItems);
      this.totalWeight = WeightedRandom.getTotalWeight(pItems);
   }

   public static <E extends WeightedEntry> WeightedRandomList<E> create() {
      return new WeightedRandomList<>(ImmutableList.of());
   }

   @SafeVarargs
   public static <E extends WeightedEntry> WeightedRandomList<E> create(E... pItems) {
      return new WeightedRandomList<>(ImmutableList.copyOf(pItems));
   }

   public static <E extends WeightedEntry> WeightedRandomList<E> create(List<E> p_146329_) {
      return new WeightedRandomList<>(p_146329_);
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   public Optional<E> getRandom(RandomSource pRandom) {
      if (this.totalWeight == 0) {
         return Optional.empty();
      } else {
         int i = pRandom.nextInt(this.totalWeight);
         return WeightedRandom.getWeightedItem(this.items, i);
      }
   }

   public List<E> unwrap() {
      return this.items;
   }

   public static <E extends WeightedEntry> Codec<WeightedRandomList<E>> codec(Codec<E> pElementCodec) {
      return pElementCodec.listOf().xmap(WeightedRandomList::create, WeightedRandomList::unwrap);
   }
}