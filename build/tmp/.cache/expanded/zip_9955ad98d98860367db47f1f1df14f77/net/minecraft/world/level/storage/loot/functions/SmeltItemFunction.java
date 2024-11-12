package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

/**
 * LootItemFunction that tries to smelt any items using {@link RecipeType.SMELTING}.
 */
public class SmeltItemFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();

   SmeltItemFunction(LootItemCondition[] p_81263_) {
      super(p_81263_);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.FURNACE_SMELT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isEmpty()) {
         return pStack;
      } else {
         Optional<SmeltingRecipe> optional = pContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(pStack), pContext.getLevel());
         if (optional.isPresent()) {
            ItemStack itemstack = optional.get().getResultItem(pContext.getLevel().registryAccess());
            if (!itemstack.isEmpty()) {
               return itemstack.copyWithCount(pStack.getCount() * itemstack.getCount()); // Forge: Support smelting returning multiple
            }
         }

         LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)pStack);
         return pStack;
      }
   }

   public static LootItemConditionalFunction.Builder<?> smelted() {
      return simpleBuilder(SmeltItemFunction::new);
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SmeltItemFunction> {
      public SmeltItemFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         return new SmeltItemFunction(pConditions);
      }
   }
}
