package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends DependencySorter.Entry<K>> {
   private final Map<K, V> contents = new HashMap<>();

   public DependencySorter<K, V> addEntry(K pKey, V pValue) {
      this.contents.put(pKey, pValue);
      return this;
   }

   private void visitDependenciesAndElement(Multimap<K, K> pDependencies, Set<K> pVisited, K pElement, BiConsumer<K, V> pAction) {
      if (pVisited.add(pElement)) {
         pDependencies.get(pElement).forEach((p_285443_) -> {
            this.visitDependenciesAndElement(pDependencies, pVisited, p_285443_, pAction);
         });
         V v = this.contents.get(pElement);
         if (v != null) {
            pAction.accept(pElement, v);
         }

      }
   }

   private static <K> boolean isCyclic(Multimap<K, K> pDependencies, K pSource, K pTarget) {
      Collection<K> collection = pDependencies.get(pTarget);
      return collection.contains(pSource) ? true : collection.stream().anyMatch((p_284974_) -> {
         return isCyclic(pDependencies, pSource, p_284974_);
      });
   }

   private static <K> void addDependencyIfNotCyclic(Multimap<K, K> pDependencies, K pSource, K pTarget) {
      if (!isCyclic(pDependencies, pSource, pTarget)) {
         pDependencies.put(pSource, pTarget);
      }

   }

   public void orderByDependencies(BiConsumer<K, V> pAction) {
      Multimap<K, K> multimap = HashMultimap.create();
      this.contents.forEach((p_285415_, p_285018_) -> {
         p_285018_.visitRequiredDependencies((p_285287_) -> {
            addDependencyIfNotCyclic(multimap, p_285415_, p_285287_);
         });
      });
      this.contents.forEach((p_285462_, p_285526_) -> {
         p_285526_.visitOptionalDependencies((p_285513_) -> {
            addDependencyIfNotCyclic(multimap, p_285462_, p_285513_);
         });
      });
      Set<K> set = new HashSet<>();
      this.contents.keySet().forEach((p_284996_) -> {
         this.visitDependenciesAndElement(multimap, set, p_284996_, pAction);
      });
   }

   public interface Entry<K> {
      void visitRequiredDependencies(Consumer<K> pVisitor);

      void visitOptionalDependencies(Consumer<K> pVisitor);
   }
}