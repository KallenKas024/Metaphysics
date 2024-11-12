package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Base interface for builders that accept loot functions.
 * 
 * @see LootItemFunction
 */
public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
   T apply(LootItemFunction.Builder pFunctionBuilder);

   default <E> T apply(Iterable<E> pBuilderSources, Function<E, LootItemFunction.Builder> pToBuilderFunction) {
      T t = this.unwrap();

      for(E e : pBuilderSources) {
         t = t.apply(pToBuilderFunction.apply(e));
      }

      return t;
   }

   default <E> T apply(E[] pBuilderSources, Function<E, LootItemFunction.Builder> pToBuilderFunction) {
      return this.apply(Arrays.asList(pBuilderSources), pToBuilderFunction);
   }

   T unwrap();
}