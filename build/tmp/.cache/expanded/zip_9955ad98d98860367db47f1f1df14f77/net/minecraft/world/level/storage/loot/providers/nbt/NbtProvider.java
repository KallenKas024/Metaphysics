package net.minecraft.world.level.storage.loot.providers.nbt;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * A provider for NBT data based on a LootContext.
 * 
 * @see NbtProviders
 */
public interface NbtProvider {
   @Nullable
   Tag get(LootContext pLootContext);

   Set<LootContextParam<?>> getReferencedContextParams();

   LootNbtProviderType getType();
}