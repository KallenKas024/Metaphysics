package net.minecraft.world.level.storage.loot;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface LootDataResolver {
   @Nullable
   <T> T getElement(LootDataId<T> pId);

   @Nullable
   default <T> T getElement(LootDataType<T> pType, ResourceLocation pLocation) {
      return this.getElement(new LootDataId<>(pType, pLocation));
   }

   default <T> Optional<T> getElementOptional(LootDataId<T> pId) {
      return Optional.ofNullable(this.getElement(pId));
   }

   default <T> Optional<T> getElementOptional(LootDataType<T> pType, ResourceLocation pLocation) {
      return this.getElementOptional(new LootDataId<>(pType, pLocation));
   }

   default LootTable getLootTable(ResourceLocation pLocation) {
      return this.getElementOptional(LootDataType.TABLE, pLocation).orElse(LootTable.EMPTY);
   }
}