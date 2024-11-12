package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("brewed_potion");

   public ResourceLocation getId() {
      return ID;
   }

   public BrewedPotionTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      Potion potion = null;
      if (pJson.has("potion")) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "potion"));
         potion = BuiltInRegistries.POTION.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown potion '" + resourcelocation + "'");
         });
      }

      return new BrewedPotionTrigger.TriggerInstance(pPredicate, potion);
   }

   public void trigger(ServerPlayer pPlayer, Potion pPotion) {
      this.trigger(pPlayer, (p_19125_) -> {
         return p_19125_.matches(pPotion);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final Potion potion;

      public TriggerInstance(ContextAwarePredicate pPlayer, @Nullable Potion pPotion) {
         super(BrewedPotionTrigger.ID, pPlayer);
         this.potion = pPotion;
      }

      public static BrewedPotionTrigger.TriggerInstance brewedPotion() {
         return new BrewedPotionTrigger.TriggerInstance(ContextAwarePredicate.ANY, (Potion)null);
      }

      public boolean matches(Potion pPotion) {
         return this.potion == null || this.potion == pPotion;
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.potion != null) {
            jsonobject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
         }

         return jsonobject;
      }
   }
}