package net.minecraft.world.entity.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class StackedContents {
   private static final int EMPTY = 0;
   public final Int2IntMap contents = new Int2IntOpenHashMap();

   public void accountSimpleStack(ItemStack pStack) {
      if (!pStack.isDamaged() && !pStack.isEnchanted() && !pStack.hasCustomHoverName()) {
         this.accountStack(pStack);
      }

   }

   public void accountStack(ItemStack pStack) {
      this.accountStack(pStack, 64);
   }

   public void accountStack(ItemStack pStack, int pAmount) {
      if (!pStack.isEmpty()) {
         int i = getStackingIndex(pStack);
         int j = Math.min(pAmount, pStack.getCount());
         this.put(i, j);
      }

   }

   public static int getStackingIndex(ItemStack pStack) {
      return BuiltInRegistries.ITEM.getId(pStack.getItem());
   }

   boolean has(int pStackingIndex) {
      return this.contents.get(pStackingIndex) > 0;
   }

   int take(int pStackingIndex, int pAmount) {
      int i = this.contents.get(pStackingIndex);
      if (i >= pAmount) {
         this.contents.put(pStackingIndex, i - pAmount);
         return pStackingIndex;
      } else {
         return 0;
      }
   }

   void put(int pStackingIndex, int pIncrement) {
      this.contents.put(pStackingIndex, this.contents.get(pStackingIndex) + pIncrement);
   }

   public boolean canCraft(Recipe<?> pRecipe, @Nullable IntList pStackingIndexList) {
      return this.canCraft(pRecipe, pStackingIndexList, 1);
   }

   public boolean canCraft(Recipe<?> pRecipe, @Nullable IntList pStackingIndexList, int pAmount) {
      return (new StackedContents.RecipePicker(pRecipe)).tryPick(pAmount, pStackingIndexList);
   }

   public int getBiggestCraftableStack(Recipe<?> pRecipe, @Nullable IntList pStackingIndexList) {
      return this.getBiggestCraftableStack(pRecipe, Integer.MAX_VALUE, pStackingIndexList);
   }

   public int getBiggestCraftableStack(Recipe<?> pRecipe, int pAmount, @Nullable IntList pStackingIndexList) {
      return (new StackedContents.RecipePicker(pRecipe)).tryPickAll(pAmount, pStackingIndexList);
   }

   public static ItemStack fromStackingIndex(int pStackingIndex) {
      return pStackingIndex == 0 ? ItemStack.EMPTY : new ItemStack(Item.byId(pStackingIndex));
   }

   public void clear() {
      this.contents.clear();
   }

   class RecipePicker {
      private final Recipe<?> recipe;
      private final List<Ingredient> ingredients = Lists.newArrayList();
      private final int ingredientCount;
      private final int[] items;
      private final int itemCount;
      private final BitSet data;
      private final IntList path = new IntArrayList();

      public RecipePicker(Recipe<?> pRecipe) {
         this.recipe = pRecipe;
         this.ingredients.addAll(pRecipe.getIngredients());
         this.ingredients.removeIf(Ingredient::isEmpty);
         this.ingredientCount = this.ingredients.size();
         this.items = this.getUniqueAvailableIngredientItems();
         this.itemCount = this.items.length;
         this.data = new BitSet(this.ingredientCount + this.itemCount + this.ingredientCount + this.ingredientCount * this.itemCount);

         for(int i = 0; i < this.ingredients.size(); ++i) {
            IntList intlist = this.ingredients.get(i).getStackingIds();

            for(int j = 0; j < this.itemCount; ++j) {
               if (intlist.contains(this.items[j])) {
                  this.data.set(this.getIndex(true, j, i));
               }
            }
         }

      }

      public boolean tryPick(int pAmount, @Nullable IntList pStackingIndexList) {
         if (pAmount <= 0) {
            return true;
         } else {
            int i;
            for(i = 0; this.dfs(pAmount); ++i) {
               StackedContents.this.take(this.items[this.path.getInt(0)], pAmount);
               int j = this.path.size() - 1;
               this.setSatisfied(this.path.getInt(j));

               for(int k = 0; k < j; ++k) {
                  this.toggleResidual((k & 1) == 0, this.path.get(k), this.path.get(k + 1));
               }

               this.path.clear();
               this.data.clear(0, this.ingredientCount + this.itemCount);
            }

            boolean flag = i == this.ingredientCount;
            boolean flag1 = flag && pStackingIndexList != null;
            if (flag1) {
               pStackingIndexList.clear();
            }

            this.data.clear(0, this.ingredientCount + this.itemCount + this.ingredientCount);
            int l = 0;
            List<Ingredient> list = this.recipe.getIngredients();

            for(int i1 = 0; i1 < list.size(); ++i1) {
               if (flag1 && list.get(i1).isEmpty()) {
                  pStackingIndexList.add(0);
               } else {
                  for(int j1 = 0; j1 < this.itemCount; ++j1) {
                     if (this.hasResidual(false, l, j1)) {
                        this.toggleResidual(true, j1, l);
                        StackedContents.this.put(this.items[j1], pAmount);
                        if (flag1) {
                           pStackingIndexList.add(this.items[j1]);
                        }
                     }
                  }

                  ++l;
               }
            }

            return flag;
         }
      }

      private int[] getUniqueAvailableIngredientItems() {
         IntCollection intcollection = new IntAVLTreeSet();

         for(Ingredient ingredient : this.ingredients) {
            intcollection.addAll(ingredient.getStackingIds());
         }

         IntIterator intiterator = intcollection.iterator();

         while(intiterator.hasNext()) {
            if (!StackedContents.this.has(intiterator.nextInt())) {
               intiterator.remove();
            }
         }

         return intcollection.toIntArray();
      }

      private boolean dfs(int pAmount) {
         int i = this.itemCount;

         for(int j = 0; j < i; ++j) {
            if (StackedContents.this.contents.get(this.items[j]) >= pAmount) {
               this.visit(false, j);

               while(!this.path.isEmpty()) {
                  int k = this.path.size();
                  boolean flag = (k & 1) == 1;
                  int l = this.path.getInt(k - 1);
                  if (!flag && !this.isSatisfied(l)) {
                     break;
                  }

                  int i1 = flag ? this.ingredientCount : i;

                  for(int j1 = 0; j1 < i1; ++j1) {
                     if (!this.hasVisited(flag, j1) && this.hasConnection(flag, l, j1) && this.hasResidual(flag, l, j1)) {
                        this.visit(flag, j1);
                        break;
                     }
                  }

                  int k1 = this.path.size();
                  if (k1 == k) {
                     this.path.removeInt(k1 - 1);
                  }
               }

               if (!this.path.isEmpty()) {
                  return true;
               }
            }
         }

         return false;
      }

      private boolean isSatisfied(int pStackingIndex) {
         return this.data.get(this.getSatisfiedIndex(pStackingIndex));
      }

      private void setSatisfied(int pStackingIndex) {
         this.data.set(this.getSatisfiedIndex(pStackingIndex));
      }

      private int getSatisfiedIndex(int pStackingIndex) {
         return this.ingredientCount + this.itemCount + pStackingIndex;
      }

      private boolean hasConnection(boolean pIsIngredientPath, int pStackingIndex, int pPathIndex) {
         return this.data.get(this.getIndex(pIsIngredientPath, pStackingIndex, pPathIndex));
      }

      private boolean hasResidual(boolean pIsIngredientPath, int pStackingIndex, int pPathIndex) {
         return pIsIngredientPath != this.data.get(1 + this.getIndex(pIsIngredientPath, pStackingIndex, pPathIndex));
      }

      private void toggleResidual(boolean pIsIngredientPath, int pStackingIndex, int pPathIndex) {
         this.data.flip(1 + this.getIndex(pIsIngredientPath, pStackingIndex, pPathIndex));
      }

      private int getIndex(boolean pIsIngredientPath, int pStackingIndex, int pPathIndex) {
         int i = pIsIngredientPath ? pStackingIndex * this.ingredientCount + pPathIndex : pPathIndex * this.ingredientCount + pStackingIndex;
         return this.ingredientCount + this.itemCount + this.ingredientCount + 2 * i;
      }

      private void visit(boolean pIsIngredientPath, int pPathIndex) {
         this.data.set(this.getVisitedIndex(pIsIngredientPath, pPathIndex));
         this.path.add(pPathIndex);
      }

      private boolean hasVisited(boolean pIsIngredientPath, int pPathIndex) {
         return this.data.get(this.getVisitedIndex(pIsIngredientPath, pPathIndex));
      }

      private int getVisitedIndex(boolean pIsIngredientPath, int pPathIndex) {
         return (pIsIngredientPath ? 0 : this.ingredientCount) + pPathIndex;
      }

      public int tryPickAll(int pAmount, @Nullable IntList pStackingIndexList) {
         int i = 0;
         int j = Math.min(pAmount, this.getMinIngredientCount()) + 1;

         while(true) {
            int k = (i + j) / 2;
            if (this.tryPick(k, (IntList)null)) {
               if (j - i <= 1) {
                  if (k > 0) {
                     this.tryPick(k, pStackingIndexList);
                  }

                  return k;
               }

               i = k;
            } else {
               j = k;
            }
         }
      }

      private int getMinIngredientCount() {
         int i = Integer.MAX_VALUE;

         for(Ingredient ingredient : this.ingredients) {
            int j = 0;

            for(int k : ingredient.getStackingIds()) {
               j = Math.max(j, StackedContents.this.contents.get(k));
            }

            if (i > 0) {
               i = Math.min(i, j);
            }
         }

         return i;
      }
   }
}