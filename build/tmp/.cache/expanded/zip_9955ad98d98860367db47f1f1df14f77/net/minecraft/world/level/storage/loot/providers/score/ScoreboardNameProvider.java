package net.minecraft.world.level.storage.loot.providers.score;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * Provides a scoreboard name based on a {@link LootContext}.
 */
public interface ScoreboardNameProvider {
   /**
    * Get the scoreboard name based on the given loot context.
    */
   @Nullable
   String getScoreboardName(LootContext pLootContext);

   LootScoreProviderType getType();

   Set<LootContextParam<?>> getReferencedContextParams();
}