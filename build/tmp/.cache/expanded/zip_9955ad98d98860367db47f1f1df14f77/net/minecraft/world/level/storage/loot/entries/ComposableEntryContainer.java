package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Base interface for loot pool entry containers.
 * A loot pool entry container holds one or more loot pools and will expand into those.
 * Additionally, the container can either succeed or fail, based on its conditions.
 */
@FunctionalInterface
interface ComposableEntryContainer {
   /** A container which always fails. */
   ComposableEntryContainer ALWAYS_FALSE = (p_79418_, p_79419_) -> {
      return false;
   };
   /** A container that always succeeds. */
   ComposableEntryContainer ALWAYS_TRUE = (p_79409_, p_79410_) -> {
      return true;
   };

   /**
    * Expand this loot pool entry container by calling {@code entryConsumer} with any applicable entries
    * 
    * @return whether this loot pool entry container successfully expanded or not
    */
   boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer);

   default ComposableEntryContainer and(ComposableEntryContainer pEntry) {
      Objects.requireNonNull(pEntry);
      return (p_79424_, p_79425_) -> {
         return this.expand(p_79424_, p_79425_) && pEntry.expand(p_79424_, p_79425_);
      };
   }

   default ComposableEntryContainer or(ComposableEntryContainer pEntry) {
      Objects.requireNonNull(pEntry);
      return (p_79415_, p_79416_) -> {
         return this.expand(p_79415_, p_79416_) || pEntry.expand(p_79415_, p_79416_);
      };
   }
}