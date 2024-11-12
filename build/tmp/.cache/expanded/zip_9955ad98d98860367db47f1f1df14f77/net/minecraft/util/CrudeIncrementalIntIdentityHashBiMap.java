package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;

public class CrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {
   private static final int NOT_FOUND = -1;
   private static final Object EMPTY_SLOT = null;
   private static final float LOADFACTOR = 0.8F;
   private K[] keys;
   private int[] values;
   private K[] byId;
   private int nextId;
   private int size;

   private CrudeIncrementalIntIdentityHashBiMap(int pSize) {
      this.keys = (K[])(new Object[pSize]);
      this.values = new int[pSize];
      this.byId = (K[])(new Object[pSize]);
   }

   private CrudeIncrementalIntIdentityHashBiMap(K[] pKeys, int[] pValues, K[] pById, int pNextId, int pSize) {
      this.keys = pKeys;
      this.values = pValues;
      this.byId = pById;
      this.nextId = pNextId;
      this.size = pSize;
   }

   public static <A> CrudeIncrementalIntIdentityHashBiMap<A> create(int pSize) {
      return new CrudeIncrementalIntIdentityHashBiMap<>((int)((float)pSize / 0.8F));
   }

   /**
    * @return the integer ID used to identify the given object
    */
   public int getId(@Nullable K pValue) {
      return this.getValue(this.indexOf(pValue, this.hash(pValue)));
   }

   @Nullable
   public K byId(int pValue) {
      return (K)(pValue >= 0 && pValue < this.byId.length ? this.byId[pValue] : null);
   }

   private int getValue(int pKey) {
      return pKey == -1 ? -1 : this.values[pKey];
   }

   public boolean contains(K pValue) {
      return this.getId(pValue) != -1;
   }

   public boolean contains(int pValue) {
      return this.byId(pValue) != null;
   }

   /**
    * Adds the given object while expanding this map
    */
   public int add(K pObject) {
      int i = this.nextId();
      this.addMapping(pObject, i);
      return i;
   }

   private int nextId() {
      while(this.nextId < this.byId.length && this.byId[this.nextId] != null) {
         ++this.nextId;
      }

      return this.nextId;
   }

   /**
    * Rehashes the map to the new capacity
    */
   private void grow(int pCapacity) {
      K[] ak = this.keys;
      int[] aint = this.values;
      CrudeIncrementalIntIdentityHashBiMap<K> crudeincrementalintidentityhashbimap = new CrudeIncrementalIntIdentityHashBiMap<>(pCapacity);

      for(int i = 0; i < ak.length; ++i) {
         if (ak[i] != null) {
            crudeincrementalintidentityhashbimap.addMapping(ak[i], aint[i]);
         }
      }

      this.keys = crudeincrementalintidentityhashbimap.keys;
      this.values = crudeincrementalintidentityhashbimap.values;
      this.byId = crudeincrementalintidentityhashbimap.byId;
      this.nextId = crudeincrementalintidentityhashbimap.nextId;
      this.size = crudeincrementalintidentityhashbimap.size;
   }

   /**
    * Puts the provided object value with the integer key.
    */
   public void addMapping(K pObject, int pIntKey) {
      int i = Math.max(pIntKey, this.size + 1);
      if ((float)i >= (float)this.keys.length * 0.8F) {
         int j;
         for(j = this.keys.length << 1; j < pIntKey; j <<= 1) {
         }

         this.grow(j);
      }

      int k = this.findEmpty(this.hash(pObject));
      this.keys[k] = pObject;
      this.values[k] = pIntKey;
      this.byId[pIntKey] = pObject;
      ++this.size;
      if (pIntKey == this.nextId) {
         ++this.nextId;
      }

   }

   private int hash(@Nullable K pObject) {
      return (Mth.murmurHash3Mixer(System.identityHashCode(pObject)) & Integer.MAX_VALUE) % this.keys.length;
   }

   private int indexOf(@Nullable K pObject, int pStartIndex) {
      for(int i = pStartIndex; i < this.keys.length; ++i) {
         if (this.keys[i] == pObject) {
            return i;
         }

         if (this.keys[i] == EMPTY_SLOT) {
            return -1;
         }
      }

      for(int j = 0; j < pStartIndex; ++j) {
         if (this.keys[j] == pObject) {
            return j;
         }

         if (this.keys[j] == EMPTY_SLOT) {
            return -1;
         }
      }

      return -1;
   }

   private int findEmpty(int pStartIndex) {
      for(int i = pStartIndex; i < this.keys.length; ++i) {
         if (this.keys[i] == EMPTY_SLOT) {
            return i;
         }
      }

      for(int j = 0; j < pStartIndex; ++j) {
         if (this.keys[j] == EMPTY_SLOT) {
            return j;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   public Iterator<K> iterator() {
      return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
   }

   public void clear() {
      Arrays.fill(this.keys, (Object)null);
      Arrays.fill(this.byId, (Object)null);
      this.nextId = 0;
      this.size = 0;
   }

   public int size() {
      return this.size;
   }

   public CrudeIncrementalIntIdentityHashBiMap<K> copy() {
      return new CrudeIncrementalIntIdentityHashBiMap<>((K[])((Object[])this.keys.clone()), (int[])this.values.clone(), (K[])((Object[])this.byId.clone()), this.nextId, this.size);
   }
}