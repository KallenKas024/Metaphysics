package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * An object that will use some parameters from a LootContext. Used for validation purposes to validate that the correct
 * parameters are present.
 */
public interface LootContextUser {
   /**
    * Get the parameters used by this object.
    */
   default Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of();
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   default void validate(ValidationContext pContext) {
      pContext.validateUser(this);
   }
}