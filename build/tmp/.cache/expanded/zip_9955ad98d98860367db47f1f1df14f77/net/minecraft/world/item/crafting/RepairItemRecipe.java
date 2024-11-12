package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class RepairItemRecipe extends CustomRecipe {
   public RepairItemRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      List<ItemStack> list = Lists.newArrayList();

      for(int i = 0; i < pInv.getContainerSize(); ++i) {
         ItemStack itemstack = pInv.getItem(i);
         if (!itemstack.isEmpty()) {
            list.add(itemstack);
            if (list.size() > 1) {
               ItemStack itemstack1 = list.get(0);
               if (itemstack.getItem() != itemstack1.getItem() || itemstack1.getCount() != 1 || itemstack.getCount() != 1 || !itemstack1.isRepairable()) {
                  return false;
               }
            }
         }
      }

      return list.size() == 2;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      List<ItemStack> list = Lists.newArrayList();

      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack = pContainer.getItem(i);
         if (!itemstack.isEmpty()) {
            list.add(itemstack);
            if (list.size() > 1) {
               ItemStack itemstack1 = list.get(0);
               if (itemstack.getItem() != itemstack1.getItem() || itemstack1.getCount() != 1 || itemstack.getCount() != 1 || !itemstack1.isRepairable()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      if (list.size() == 2) {
         ItemStack itemstack3 = list.get(0);
         ItemStack itemstack4 = list.get(1);
         if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getCount() == 1 && itemstack4.getCount() == 1 && itemstack3.isRepairable()) {
            Item item = itemstack3.getItem();
            int j = itemstack3.getMaxDamage() - itemstack3.getDamageValue();
            int k = itemstack3.getMaxDamage() - itemstack4.getDamageValue();
            int l = j + k + itemstack3.getMaxDamage() * 5 / 100;
            int i1 = itemstack3.getMaxDamage() - l;
            if (i1 < 0) {
               i1 = 0;
            }

            ItemStack itemstack2 = new ItemStack(itemstack3.getItem());
            itemstack2.setDamageValue(i1);
            Map<Enchantment, Integer> map = Maps.newHashMap();
            Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack3);
            Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemstack4);
            BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isCurse).forEach((p_44144_) -> {
               int j1 = Math.max(map1.getOrDefault(p_44144_, 0), map2.getOrDefault(p_44144_, 0));
               if (j1 > 0) {
                  map.put(p_44144_, j1);
               }

            });
            if (!map.isEmpty()) {
               EnchantmentHelper.setEnchantments(map, itemstack2);
            }

            return itemstack2;
         }
      }

      return ItemStack.EMPTY;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.REPAIR_ITEM;
   }
}
