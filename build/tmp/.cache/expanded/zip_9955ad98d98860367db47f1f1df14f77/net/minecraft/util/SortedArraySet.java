package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

public class SortedArraySet<T> extends AbstractSet<T> {
   private static final int DEFAULT_INITIAL_CAPACITY = 10;
   private final Comparator<T> comparator;
   T[] contents;
   int size;

   private SortedArraySet(int pInitialCapacity, Comparator<T> pComparator) {
      this.comparator = pComparator;
      if (pInitialCapacity < 0) {
         throw new IllegalArgumentException("Initial capacity (" + pInitialCapacity + ") is negative");
      } else {
         this.contents = (T[])castRawArray(new Object[pInitialCapacity]);
      }
   }

   public static <T extends Comparable<T>> SortedArraySet<T> create() {
      return create(10);
   }

   public static <T extends Comparable<T>> SortedArraySet<T> create(int pInitialCapacity) {
      return new SortedArraySet<>(pInitialCapacity, Comparator.<T>naturalOrder());
   }

   public static <T> SortedArraySet<T> create(Comparator<T> pComparator) {
      return create(pComparator, 10);
   }

   public static <T> SortedArraySet<T> create(Comparator<T> pComparator, int pInitialCapacity) {
      return new SortedArraySet<>(pInitialCapacity, pComparator);
   }

   private static <T> T[] castRawArray(Object[] pArray) {
      return (T[])pArray;
   }

   private int findIndex(T pObject) {
      return Arrays.binarySearch(this.contents, 0, this.size, pObject, this.comparator);
   }

   private static int getInsertionPosition(int pIndex) {
      return -pIndex - 1;
   }

   public boolean add(T pElement) {
      int i = this.findIndex(pElement);
      if (i >= 0) {
         return false;
      } else {
         int j = getInsertionPosition(i);
         this.addInternal(pElement, j);
         return true;
      }
   }

   private void grow(int pSize) {
      if (pSize > this.contents.length) {
         if (this.contents != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            pSize = (int)Math.max(Math.min((long)this.contents.length + (long)(this.contents.length >> 1), 2147483639L), (long)pSize);
         } else if (pSize < 10) {
            pSize = 10;
         }

         Object[] aobject = new Object[pSize];
         System.arraycopy(this.contents, 0, aobject, 0, this.size);
         this.contents = (T[])castRawArray(aobject);
      }
   }

   private void addInternal(T pElement, int pIndex) {
      this.grow(this.size + 1);
      if (pIndex != this.size) {
         System.arraycopy(this.contents, pIndex, this.contents, pIndex + 1, this.size - pIndex);
      }

      this.contents[pIndex] = pElement;
      ++this.size;
   }

   void removeInternal(int pIndex) {
      --this.size;
      if (pIndex != this.size) {
         System.arraycopy(this.contents, pIndex + 1, this.contents, pIndex, this.size - pIndex);
      }

      this.contents[this.size] = null;
   }

   private T getInternal(int pIndex) {
      return this.contents[pIndex];
   }

   public T addOrGet(T pElement) {
      int i = this.findIndex(pElement);
      if (i >= 0) {
         return this.getInternal(i);
      } else {
         this.addInternal(pElement, getInsertionPosition(i));
         return pElement;
      }
   }

   public boolean remove(Object pElement) {
      int i = this.findIndex((T)pElement);
      if (i >= 0) {
         this.removeInternal(i);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public T get(T pElement) {
      int i = this.findIndex(pElement);
      return (T)(i >= 0 ? this.getInternal(i) : null);
   }

   /**
    * Gets the smallest element in the set
    */
   public T first() {
      return this.getInternal(0);
   }

   public T last() {
      return this.getInternal(this.size - 1);
   }

   public boolean contains(Object pElement) {
      int i = this.findIndex((T)pElement);
      return i >= 0;
   }

   public Iterator<T> iterator() {
      return new SortedArraySet.ArrayIterator();
   }

   public int size() {
      return this.size;
   }

   public Object[] toArray() {
      return Arrays.copyOf(this.contents, this.size, Object[].class);
   }

   public <U> U[] toArray(U[] pOutput) {
      if (pOutput.length < this.size) {
         return (U[])Arrays.copyOf(this.contents, this.size, pOutput.getClass());
      } else {
         System.arraycopy(this.contents, 0, pOutput, 0, this.size);
         if (pOutput.length > this.size) {
            pOutput[this.size] = null;
         }

         return pOutput;
      }
   }

   public void clear() {
      Arrays.fill(this.contents, 0, this.size, (Object)null);
      this.size = 0;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof SortedArraySet) {
            SortedArraySet<?> sortedarrayset = (SortedArraySet)pOther;
            if (this.comparator.equals(sortedarrayset.comparator)) {
               return this.size == sortedarrayset.size && Arrays.equals(this.contents, sortedarrayset.contents);
            }
         }

         return super.equals(pOther);
      }
   }

   class ArrayIterator implements Iterator<T> {
      private int index;
      private int last = -1;

      public boolean hasNext() {
         return this.index < SortedArraySet.this.size;
      }

      public T next() {
         if (this.index >= SortedArraySet.this.size) {
            throw new NoSuchElementException();
         } else {
            this.last = this.index++;
            return SortedArraySet.this.contents[this.last];
         }
      }

      public void remove() {
         if (this.last == -1) {
            throw new IllegalStateException();
         } else {
            SortedArraySet.this.removeInternal(this.last);
            --this.index;
            this.last = -1;
         }
      }
   }
}