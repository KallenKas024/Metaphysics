package net.minecraft;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
   boolean test(char pValue);

   default CharPredicate and(CharPredicate pPredicate) {
      Objects.requireNonNull(pPredicate);
      return (p_178295_) -> {
         return this.test(p_178295_) && pPredicate.test(p_178295_);
      };
   }

   default CharPredicate negate() {
      return (p_178285_) -> {
         return !this.test(p_178285_);
      };
   }

   default CharPredicate or(CharPredicate pPredicate) {
      Objects.requireNonNull(pPredicate);
      return (p_178290_) -> {
         return this.test(p_178290_) || pPredicate.test(p_178290_);
      };
   }
}