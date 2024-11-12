package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IdSearchTree<T> implements RefreshableSearchTree<T> {
   protected final Comparator<T> additionOrder;
   protected final ResourceLocationSearchTree<T> resourceLocationSearchTree;

   public IdSearchTree(Function<T, Stream<ResourceLocation>> pIdGetter, List<T> pContents) {
      ToIntFunction<T> tointfunction = Util.createIndexLookup(pContents);
      this.additionOrder = Comparator.comparingInt(tointfunction);
      this.resourceLocationSearchTree = ResourceLocationSearchTree.create(pContents, pIdGetter);
   }

   /**
    * Searches this search tree for the given text.
    * <p>
    * If the query does not contain a {@code :}, then only {@link #byName} is searched. If it does contain a colon, both
    * {@link #byName} and {@link #byId} are searched and the results are merged using a {@link MergingIterator}.
    * @return A list of all matching items in this search tree.
    */
   public List<T> search(String pQuery) {
      int i = pQuery.indexOf(58);
      return i == -1 ? this.searchPlainText(pQuery) : this.searchResourceLocation(pQuery.substring(0, i).trim(), pQuery.substring(i + 1).trim());
   }

   protected List<T> searchPlainText(String pQuery) {
      return this.resourceLocationSearchTree.searchPath(pQuery);
   }

   protected List<T> searchResourceLocation(String pNamespace, String pPath) {
      List<T> list = this.resourceLocationSearchTree.searchNamespace(pNamespace);
      List<T> list1 = this.resourceLocationSearchTree.searchPath(pPath);
      return ImmutableList.copyOf(new IntersectionIterator<>(list.iterator(), list1.iterator(), this.additionOrder));
   }
}