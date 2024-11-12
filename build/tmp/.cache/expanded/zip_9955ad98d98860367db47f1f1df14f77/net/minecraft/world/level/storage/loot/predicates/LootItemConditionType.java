package net.minecraft.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

/**
 * The SerializerType for {@link LootItemCondition}.
 */
public class LootItemConditionType extends SerializerType<LootItemCondition> {
   public LootItemConditionType(Serializer<? extends LootItemCondition> pSerializer) {
      super(pSerializer);
   }
}