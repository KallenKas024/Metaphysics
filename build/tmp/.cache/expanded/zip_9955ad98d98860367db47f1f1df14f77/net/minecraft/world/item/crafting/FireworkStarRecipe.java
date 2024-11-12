package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkStarRecipe extends CustomRecipe {
   private static final Ingredient SHAPE_INGREDIENT = Ingredient.of(Items.FIRE_CHARGE, Items.FEATHER, Items.GOLD_NUGGET, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD, Items.PIGLIN_HEAD);
   private static final Ingredient TRAIL_INGREDIENT = Ingredient.of(Items.DIAMOND);
   private static final Ingredient FLICKER_INGREDIENT = Ingredient.of(Items.GLOWSTONE_DUST);
   private static final Map<Item, FireworkRocketItem.Shape> SHAPE_BY_ITEM = Util.make(Maps.newHashMap(), (p_261449_) -> {
      p_261449_.put(Items.FIRE_CHARGE, FireworkRocketItem.Shape.LARGE_BALL);
      p_261449_.put(Items.FEATHER, FireworkRocketItem.Shape.BURST);
      p_261449_.put(Items.GOLD_NUGGET, FireworkRocketItem.Shape.STAR);
      p_261449_.put(Items.SKELETON_SKULL, FireworkRocketItem.Shape.CREEPER);
      p_261449_.put(Items.WITHER_SKELETON_SKULL, FireworkRocketItem.Shape.CREEPER);
      p_261449_.put(Items.CREEPER_HEAD, FireworkRocketItem.Shape.CREEPER);
      p_261449_.put(Items.PLAYER_HEAD, FireworkRocketItem.Shape.CREEPER);
      p_261449_.put(Items.DRAGON_HEAD, FireworkRocketItem.Shape.CREEPER);
      p_261449_.put(Items.ZOMBIE_HEAD, FireworkRocketItem.Shape.CREEPER);
      p_261449_.put(Items.PIGLIN_HEAD, FireworkRocketItem.Shape.CREEPER);
   });
   private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);

   public FireworkStarRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      boolean flag = false;
      boolean flag1 = false;
      boolean flag2 = false;
      boolean flag3 = false;
      boolean flag4 = false;

      for(int i = 0; i < pInv.getContainerSize(); ++i) {
         ItemStack itemstack = pInv.getItem(i);
         if (!itemstack.isEmpty()) {
            if (SHAPE_INGREDIENT.test(itemstack)) {
               if (flag2) {
                  return false;
               }

               flag2 = true;
            } else if (FLICKER_INGREDIENT.test(itemstack)) {
               if (flag4) {
                  return false;
               }

               flag4 = true;
            } else if (TRAIL_INGREDIENT.test(itemstack)) {
               if (flag3) {
                  return false;
               }

               flag3 = true;
            } else if (GUNPOWDER_INGREDIENT.test(itemstack)) {
               if (flag) {
                  return false;
               }

               flag = true;
            } else {
               if (!(itemstack.getItem() instanceof DyeItem)) {
                  return false;
               }

               flag1 = true;
            }
         }
      }

      return flag && flag1;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = new ItemStack(Items.FIREWORK_STAR);
      CompoundTag compoundtag = itemstack.getOrCreateTagElement("Explosion");
      FireworkRocketItem.Shape fireworkrocketitem$shape = FireworkRocketItem.Shape.SMALL_BALL;
      List<Integer> list = Lists.newArrayList();

      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack1 = pContainer.getItem(i);
         if (!itemstack1.isEmpty()) {
            if (SHAPE_INGREDIENT.test(itemstack1)) {
               fireworkrocketitem$shape = SHAPE_BY_ITEM.get(itemstack1.getItem());
            } else if (FLICKER_INGREDIENT.test(itemstack1)) {
               compoundtag.putBoolean("Flicker", true);
            } else if (TRAIL_INGREDIENT.test(itemstack1)) {
               compoundtag.putBoolean("Trail", true);
            } else if (itemstack1.getItem() instanceof DyeItem) {
               list.add(((DyeItem)itemstack1.getItem()).getDyeColor().getFireworkColor());
            }
         }
      }

      compoundtag.putIntArray("Colors", list);
      fireworkrocketitem$shape.save(compoundtag);
      return itemstack;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return new ItemStack(Items.FIREWORK_STAR);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.FIREWORK_STAR;
   }
}
