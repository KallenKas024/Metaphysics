package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {
   private Graph() {
   }

   /**
    * Detects if a cycle is present in the given graph, via a depth first search, and returns {@code true} if a cycle
    * was found.
    * @param pNonCyclicalNodes Nodes that are verified to have no cycles involving them.
    * @param pPathSet The current collection of seen nodes. When invoked not recursively, this should be an empty set.
    * @param pOnNonCyclicalNodeFound Invoked on each node as we prove that no cycles can be reached starting from this
    * node.
    */
   public static <T> boolean depthFirstSearch(Map<T, Set<T>> pGraph, Set<T> pNonCyclicalNodes, Set<T> pPathSet, Consumer<T> pOnNonCyclicalNodeFound, T pCurrentNode) {
      if (pNonCyclicalNodes.contains(pCurrentNode)) {
         return false;
      } else if (pPathSet.contains(pCurrentNode)) {
         return true;
      } else {
         pPathSet.add(pCurrentNode);

         for(T t : pGraph.getOrDefault(pCurrentNode, ImmutableSet.of())) {
            if (depthFirstSearch(pGraph, pNonCyclicalNodes, pPathSet, pOnNonCyclicalNodeFound, t)) {
               return true;
            }
         }

         pPathSet.remove(pCurrentNode);
         pNonCyclicalNodes.add(pCurrentNode);
         pOnNonCyclicalNodeFound.accept(pCurrentNode);
         return false;
      }
   }
}