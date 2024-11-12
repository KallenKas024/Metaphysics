package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("recipe_crafted");

   public ResourceLocation getId() {
      return ID;
   }

   protected RecipeCraftedTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "recipe_id"));
      ItemPredicate[] aitempredicate = ItemPredicate.fromJsonArray(pJson.get("ingredients"));
      return new RecipeCraftedTrigger.TriggerInstance(pPredicate, resourcelocation, List.of(aitempredicate));
   }

   public void trigger(ServerPlayer pPlayer, ResourceLocation pRecipeId, List<ItemStack> pItems) {
      this.trigger(pPlayer, (p_282798_) -> {
         return p_282798_.matches(pRecipeId, pItems);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation recipeId;
      private final List<ItemPredicate> predicates;

      public TriggerInstance(ContextAwarePredicate pPlayer, ResourceLocation pRecipeId, List<ItemPredicate> pPredicates) {
         super(RecipeCraftedTrigger.ID, pPlayer);
         this.recipeId = pRecipeId;
         this.predicates = pPredicates;
      }

      public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation pRecipeId, List<ItemPredicate> pPredicates) {
         return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, pRecipeId, pPredicates);
      }

      public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation pRecipeId) {
         return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, pRecipeId, List.of());
      }

      boolean matches(ResourceLocation pRecipeId, List<ItemStack> pItems) {
         if (!pRecipeId.equals(this.recipeId)) {
            return false;
         } else {
            List<ItemStack> list = new ArrayList<>(pItems);

            for(ItemPredicate itempredicate : this.predicates) {
               boolean flag = false;
               Iterator<ItemStack> iterator = list.iterator();

               while(iterator.hasNext()) {
                  if (itempredicate.matches(iterator.next())) {
                     iterator.remove();
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.addProperty("recipe_id", this.recipeId.toString());
         if (this.predicates.size() > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ItemPredicate itempredicate : this.predicates) {
               jsonarray.add(itempredicate.serializeToJson());
            }

            jsonobject.add("ingredients", jsonarray);
         }

         return jsonobject;
      }
   }
}