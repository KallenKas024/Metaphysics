package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Selector {
   private final Condition condition;
   private final MultiVariant variant;

   public Selector(Condition pCondition, MultiVariant pVariant) {
      if (pCondition == null) {
         throw new IllegalArgumentException("Missing condition for selector");
      } else if (pVariant == null) {
         throw new IllegalArgumentException("Missing variant for selector");
      } else {
         this.condition = pCondition;
         this.variant = pVariant;
      }
   }

   public MultiVariant getVariant() {
      return this.variant;
   }

   public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> pDefinition) {
      return this.condition.getPredicate(pDefinition);
   }

   public boolean equals(Object pOther) {
      return this == pOther;
   }

   public int hashCode() {
      return System.identityHashCode(this);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Deserializer implements JsonDeserializer<Selector> {
      public Selector deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException {
         JsonObject jsonobject = pJson.getAsJsonObject();
         return new Selector(this.getSelector(jsonobject), pContext.deserialize(jsonobject.get("apply"), MultiVariant.class));
      }

      private Condition getSelector(JsonObject pJson) {
         return pJson.has("when") ? getCondition(GsonHelper.getAsJsonObject(pJson, "when")) : Condition.TRUE;
      }

      @VisibleForTesting
      static Condition getCondition(JsonObject pJson) {
         Set<Map.Entry<String, JsonElement>> set = pJson.entrySet();
         if (set.isEmpty()) {
            throw new JsonParseException("No elements found in selector");
         } else if (set.size() == 1) {
            if (pJson.has("OR")) {
               List<Condition> list1 = Streams.stream(GsonHelper.getAsJsonArray(pJson, "OR")).map((p_112038_) -> {
                  return getCondition(p_112038_.getAsJsonObject());
               }).collect(Collectors.toList());
               return new OrCondition(list1);
            } else if (pJson.has("AND")) {
               List<Condition> list = Streams.stream(GsonHelper.getAsJsonArray(pJson, "AND")).map((p_112028_) -> {
                  return getCondition(p_112028_.getAsJsonObject());
               }).collect(Collectors.toList());
               return new AndCondition(list);
            } else {
               return getKeyValueCondition(set.iterator().next());
            }
         } else {
            return new AndCondition(set.stream().map(Selector.Deserializer::getKeyValueCondition).collect(Collectors.toList()));
         }
      }

      private static Condition getKeyValueCondition(Map.Entry<String, JsonElement> p_112036_) {
         return new KeyValueCondition(p_112036_.getKey(), p_112036_.getValue().getAsString());
      }
   }
}