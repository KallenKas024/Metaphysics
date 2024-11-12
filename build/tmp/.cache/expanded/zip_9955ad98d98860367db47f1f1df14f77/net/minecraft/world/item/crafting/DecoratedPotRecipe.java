package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

public class DecoratedPotRecipe extends CustomRecipe {
   public DecoratedPotRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pContainer, Level pLevel) {
      if (!this.canCraftInDimensions(pContainer.getWidth(), pContainer.getHeight())) {
         return false;
      } else {
         for(int i = 0; i < pContainer.getContainerSize(); ++i) {
            ItemStack itemstack = pContainer.getItem(i);
            switch (i) {
               case 1:
               case 3:
               case 5:
               case 7:
                  if (!itemstack.is(ItemTags.DECORATED_POT_INGREDIENTS)) {
                     return false;
                  }
                  break;
               case 2:
               case 4:
               case 6:
               default:
                  if (!itemstack.is(Items.AIR)) {
                     return false;
                  }
            }
         }

         return true;
      }
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      DecoratedPotBlockEntity.Decorations decoratedpotblockentity$decorations = new DecoratedPotBlockEntity.Decorations(pContainer.getItem(1).getItem(), pContainer.getItem(3).getItem(), pContainer.getItem(5).getItem(), pContainer.getItem(7).getItem());
      return createDecoratedPotItem(decoratedpotblockentity$decorations);
   }

   public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decorations pDecorations) {
      ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();
      CompoundTag compoundtag = pDecorations.save(new CompoundTag());
      BlockItem.setBlockEntityData(itemstack, BlockEntityType.DECORATED_POT, compoundtag);
      return itemstack;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth == 3 && pHeight == 3;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.DECORATED_POT_RECIPE;
   }
}