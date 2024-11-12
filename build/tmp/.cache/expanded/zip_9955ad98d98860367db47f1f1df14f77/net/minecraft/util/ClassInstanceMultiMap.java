package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInstanceMultiMap<T> extends AbstractCollection<T> {
   private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
   private final Class<T> baseClass;
   private final List<T> allInstances = Lists.newArrayList();

   public ClassInstanceMultiMap(Class<T> pBaseClass) {
      this.baseClass = pBaseClass;
      this.byClass.put(pBaseClass, this.allInstances);
   }

   public boolean add(T pValue) {
      boolean flag = false;

      for(Map.Entry<Class<?>, List<T>> entry : this.byClass.entrySet()) {
         if (entry.getKey().isInstance(pValue)) {
            flag |= entry.getValue().add(pValue);
         }
      }

      return flag;
   }

   public boolean remove(Object pKey) {
      boolean flag = false;

      for(Map.Entry<Class<?>, List<T>> entry : this.byClass.entrySet()) {
         if (entry.getKey().isInstance(pKey)) {
            List<T> list = entry.getValue();
            flag |= list.remove(pKey);
         }
      }

      return flag;
   }

   public boolean contains(Object pKey) {
      return this.find(pKey.getClass()).contains(pKey);
   }

   public <S> Collection<S> find(Class<S> pType) {
      if (!this.baseClass.isAssignableFrom(pType)) {
         throw new IllegalArgumentException("Don't know how to search for " + pType);
      } else {
         List<? extends T> list = this.byClass.computeIfAbsent(pType, (p_13538_) -> {
            return this.allInstances.stream().filter(p_13538_::isInstance).collect(Collectors.toList());
         });
         return (Collection<S>)Collections.unmodifiableCollection(list);
      }
   }

   public Iterator<T> iterator() {
      return (Iterator<T>)(this.allInstances.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances.iterator()));
   }

   public List<T> getAllInstances() {
      return ImmutableList.copyOf(this.allInstances);
   }

   public int size() {
      return this.allInstances.size();
   }
}