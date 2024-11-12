package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

/**
 * The SerializerType for {@link LootPoolEntryContainer}.
 */
public class LootPoolEntryType extends SerializerType<LootPoolEntryContainer> {
   public LootPoolEntryType(Serializer<? extends LootPoolEntryContainer> pSerializer) {
      super(pSerializer);
   }
}