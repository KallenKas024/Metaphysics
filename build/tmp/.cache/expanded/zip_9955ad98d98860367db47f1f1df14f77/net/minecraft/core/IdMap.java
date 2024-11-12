package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
   int DEFAULT = -1;

   /**
    * @return the integer ID used to identify the given object
    */
   int getId(T pValue);

   @Nullable
   T byId(int pId);

   default T byIdOrThrow(int pId) {
      T t = this.byId(pId);
      if (t == null) {
         throw new IllegalArgumentException("No value with id " + pId);
      } else {
         return t;
      }
   }

   int size();
}