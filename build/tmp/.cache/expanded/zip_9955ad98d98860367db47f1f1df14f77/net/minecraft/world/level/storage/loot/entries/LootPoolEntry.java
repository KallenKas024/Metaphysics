package net.minecraft.world.level.storage.loot.entries;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * A loot pool entry generates zero or more stacks of items based on the LootContext.
 * Each loot pool entry has a weight that determines how likely it is to be generated within a given loot pool.
 */
public interface LootPoolEntry {
   /**
    * Gets the effective weight based on the loot entry's weight and quality multiplied by looter's luck.
    */
   int getWeight(float pLuck);

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext);
}