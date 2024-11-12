package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;

public class IdMapper<T> implements IdMap<T> {
   protected int nextId;
   protected final Object2IntMap<T> tToId;
   protected final List<T> idToT;

   public IdMapper() {
      this(512);
   }

   public IdMapper(int pExpectedSize) {
      this.idToT = Lists.newArrayListWithExpectedSize(pExpectedSize);
      this.tToId = new Object2IntOpenCustomHashMap<>(pExpectedSize, Util.identityStrategy());
      this.tToId.defaultReturnValue(-1);
   }

   public void addMapping(T pKey, int pValue) {
      this.tToId.put(pKey, pValue);

      while(this.idToT.size() <= pValue) {
         this.idToT.add((T)null);
      }

      this.idToT.set(pValue, pKey);
      if (this.nextId <= pValue) {
         this.nextId = pValue + 1;
      }

   }

   public void add(T pKey) {
      this.addMapping(pKey, this.nextId);
   }

   /**
    * @return the integer ID used to identify the given object
    */
   public int getId(T pValue) {
      return this.tToId.getInt(pValue);
   }

   @Nullable
   public final T byId(int pId) {
      return (T)(pId >= 0 && pId < this.idToT.size() ? this.idToT.get(pId) : null);
   }

   public Iterator<T> iterator() {
      return Iterators.filter(this.idToT.iterator(), Objects::nonNull);
   }

   public boolean contains(int pId) {
      return this.byId(pId) != null;
   }

   public int size() {
      return this.tToId.size();
   }
}