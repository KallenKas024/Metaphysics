package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class KnowledgeBookItem extends Item {
   private static final String RECIPE_TAG = "Recipes";
   private static final Logger LOGGER = LogUtils.getLogger();

   public KnowledgeBookItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      CompoundTag compoundtag = itemstack.getTag();
      if (!pPlayer.getAbilities().instabuild) {
         pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
      }

      if (compoundtag != null && compoundtag.contains("Recipes", 9)) {
         if (!pLevel.isClientSide) {
            ListTag listtag = compoundtag.getList("Recipes", 8);
            List<Recipe<?>> list = Lists.newArrayList();
            RecipeManager recipemanager = pLevel.getServer().getRecipeManager();

            for(int i = 0; i < listtag.size(); ++i) {
               String s = listtag.getString(i);
               Optional<? extends Recipe<?>> optional = recipemanager.byKey(new ResourceLocation(s));
               if (!optional.isPresent()) {
                  LOGGER.error("Invalid recipe: {}", (Object)s);
                  return InteractionResultHolder.fail(itemstack);
               }

               list.add(optional.get());
            }

            pPlayer.awardRecipes(list);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
         }

         return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
      } else {
         LOGGER.error("Tag not valid: {}", (Object)compoundtag);
         return InteractionResultHolder.fail(itemstack);
      }
   }
}