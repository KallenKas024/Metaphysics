package net.minecraft.core;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class NonNullList<E> extends AbstractList<E> {
   private final List<E> list;
   @Nullable
   private final E defaultValue;

   public static <E> NonNullList<E> create() {
      return new NonNullList<>(Lists.newArrayList(), (E)null);
   }

   public static <E> NonNullList<E> createWithCapacity(int pInitialCapacity) {
      return new NonNullList<>(Lists.newArrayListWithCapacity(pInitialCapacity), (E)null);
   }

   /**
    * Creates a new NonNullList with <i>fixed</i> size and default value. The list will be filled with the default
    * value.
    */
   public static <E> NonNullList<E> withSize(int pSize, E pDefaultValue) {
      Validate.notNull(pDefaultValue);
      Object[] aobject = new Object[pSize];
      Arrays.fill(aobject, pDefaultValue);
      return new NonNullList<>(Arrays.asList((E[])aobject), pDefaultValue);
   }

   @SafeVarargs
   public static <E> NonNullList<E> of(E pDefaultValue, E... pElements) {
      return new NonNullList<>(Arrays.asList(pElements), pDefaultValue);
   }

   protected NonNullList(List<E> pList, @Nullable E pDefaultValue) {
      this.list = pList;
      this.defaultValue = pDefaultValue;
   }

   @Nonnull
   public E get(int pIndex) {
      return this.list.get(pIndex);
   }

   public E set(int pIndex, E pValue) {
      Validate.notNull(pValue);
      return this.list.set(pIndex, pValue);
   }

   public void add(int pIndex, E pValue) {
      Validate.notNull(pValue);
      this.list.add(pIndex, pValue);
   }

   public E remove(int pIndex) {
      return this.list.remove(pIndex);
   }

   public int size() {
      return this.list.size();
   }

   public void clear() {
      if (this.defaultValue == null) {
         super.clear();
      } else {
         for(int i = 0; i < this.size(); ++i) {
            this.set(i, this.defaultValue);
         }
      }

   }
}