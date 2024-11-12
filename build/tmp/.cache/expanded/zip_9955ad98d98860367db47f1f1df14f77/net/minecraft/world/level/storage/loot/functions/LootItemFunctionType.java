package net.minecraft.world.level.storage.loot.functions;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

/**
 * The SerializerType for {@link LootItemFunction}.
 */
public class LootItemFunctionType extends SerializerType<LootItemFunction> {
   public LootItemFunctionType(Serializer<? extends LootItemFunction> pSerializer) {
      super(pSerializer);
   }
}