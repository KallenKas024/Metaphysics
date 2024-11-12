package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

/**
 * Base interface for builders that can accept loot conditions.
 * 
 * @see LootItemCondition
 */
public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
   T when(LootItemCondition.Builder pConditionBuilder);

   default <E> T when(Iterable<E> pBuilderSources, Function<E, LootItemCondition.Builder> pToBuilderFunction) {
      T t = this.unwrap();

      for(E e : pBuilderSources) {
         t = t.when(pToBuilderFunction.apply(e));
      }

      return t;
   }

   T unwrap();
}