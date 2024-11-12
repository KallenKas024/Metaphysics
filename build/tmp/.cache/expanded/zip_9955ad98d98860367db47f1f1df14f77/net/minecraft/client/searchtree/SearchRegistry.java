package net.minecraft.client.searchtree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SearchRegistry implements ResourceManagerReloadListener {
   public static final SearchRegistry.Key<ItemStack> CREATIVE_NAMES = new SearchRegistry.Key<>();
   public static final SearchRegistry.Key<ItemStack> CREATIVE_TAGS = new SearchRegistry.Key<>();
   public static final SearchRegistry.Key<RecipeCollection> RECIPE_COLLECTIONS = new SearchRegistry.Key<>();
   private final Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees = new HashMap<>();

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      for(SearchRegistry.TreeEntry<?> treeentry : this.searchTrees.values()) {
         treeentry.refresh();
      }

   }

   public <T> void register(SearchRegistry.Key<T> pKey, SearchRegistry.TreeBuilderSupplier<T> pFactory) {
      this.searchTrees.put(pKey, new SearchRegistry.TreeEntry<>(pFactory));
   }

   private <T> SearchRegistry.TreeEntry<T> getSupplier(SearchRegistry.Key<T> pKey) {
      SearchRegistry.TreeEntry<T> treeentry = (SearchRegistry.TreeEntry<T>)this.searchTrees.get(pKey);
      if (treeentry == null) {
         throw new IllegalStateException("Tree builder not registered");
      } else {
         return treeentry;
      }
   }

   public <T> void populate(SearchRegistry.Key<T> pKey, List<T> pValues) {
      this.getSupplier(pKey).populate(pValues);
   }

   public <T> SearchTree<T> getTree(SearchRegistry.Key<T> pKey) {
      return this.getSupplier(pKey).tree;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Key<T> {
   }

   @OnlyIn(Dist.CLIENT)
   public interface TreeBuilderSupplier<T> extends Function<List<T>, RefreshableSearchTree<T>> {
   }

   @OnlyIn(Dist.CLIENT)
   static class TreeEntry<T> {
      private final SearchRegistry.TreeBuilderSupplier<T> factory;
      RefreshableSearchTree<T> tree = RefreshableSearchTree.empty();

      TreeEntry(SearchRegistry.TreeBuilderSupplier<T> pFactory) {
         this.factory = pFactory;
      }

      void populate(List<T> pValues) {
         this.tree = this.factory.apply(pValues);
         this.tree.refresh();
      }

      void refresh() {
         this.tree.refresh();
      }
   }
}