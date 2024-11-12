package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

/**
 * A LootItemFunction modifies an ItemStack based on the current LootContext.
 * 
 * @see LootItemFunctions
 */
public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
   LootItemFunctionType getType();

   /**
    * Create a decorated Consumer. The resulting consumer will first apply {@code stackModification} to all stacks
    * before passing them on to {@code originalConsumer}.
    */
   static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> pStackModification, Consumer<ItemStack> pOriginalConsumer, LootContext pLootContext) {
      return (p_80732_) -> {
         pOriginalConsumer.accept(pStackModification.apply(p_80732_, pLootContext));
      };
   }

   public interface Builder {
      LootItemFunction build();
   }
}