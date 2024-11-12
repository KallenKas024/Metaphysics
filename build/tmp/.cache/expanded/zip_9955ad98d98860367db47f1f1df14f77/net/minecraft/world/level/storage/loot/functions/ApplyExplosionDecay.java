package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that reduces a stack's count based on the {@linkplain LootContextParams#EXPLOSION_RADIUS explosion
 * radius}.
 */
public class ApplyExplosionDecay extends LootItemConditionalFunction {
   ApplyExplosionDecay(LootItemCondition[] p_80029_) {
      super(p_80029_);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.EXPLOSION_DECAY;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Float f = pContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
      if (f != null) {
         RandomSource randomsource = pContext.getRandom();
         float f1 = 1.0F / f;
         int i = pStack.getCount();
         int j = 0;

         for(int k = 0; k < i; ++k) {
            if (randomsource.nextFloat() <= f1) {
               ++j;
            }
         }

         pStack.setCount(j);
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> explosionDecay() {
      return simpleBuilder(ApplyExplosionDecay::new);
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyExplosionDecay> {
      public ApplyExplosionDecay deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         return new ApplyExplosionDecay(pConditions);
      }
   }
}