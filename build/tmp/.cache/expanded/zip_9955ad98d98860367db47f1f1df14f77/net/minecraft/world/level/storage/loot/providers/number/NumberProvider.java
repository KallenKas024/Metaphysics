package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

/**
 * Provides a float or int based on a {@link LootContext}.
 */
public interface NumberProvider extends LootContextUser {
   float getFloat(LootContext pLootContext);

   default int getInt(LootContext pLootContext) {
      return Math.round(this.getFloat(pLootContext));
   }

   LootNumberProviderType getType();
}