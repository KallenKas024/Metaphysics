package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
   ResourceLocation getId();

   void addPlayerListener(PlayerAdvancements pPlayerAdvancements, CriterionTrigger.Listener<T> pListener);

   void removePlayerListener(PlayerAdvancements pPlayerAdvancements, CriterionTrigger.Listener<T> pListener);

   void removePlayerListeners(PlayerAdvancements pPlayerAdvancements);

   T createInstance(JsonObject pJson, DeserializationContext pContext);

   public static class Listener<T extends CriterionTriggerInstance> {
      private final T trigger;
      private final Advancement advancement;
      private final String criterion;

      public Listener(T pTrigger, Advancement pAdvancement, String pCriterion) {
         this.trigger = pTrigger;
         this.advancement = pAdvancement;
         this.criterion = pCriterion;
      }

      public T getTriggerInstance() {
         return this.trigger;
      }

      public void run(PlayerAdvancements pPlayerAdvancements) {
         pPlayerAdvancements.award(this.advancement, this.criterion);
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (pOther != null && this.getClass() == pOther.getClass()) {
            CriterionTrigger.Listener<?> listener = (CriterionTrigger.Listener)pOther;
            if (!this.trigger.equals(listener.trigger)) {
               return false;
            } else {
               return !this.advancement.equals(listener.advancement) ? false : this.criterion.equals(listener.criterion);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.trigger.hashCode();
         i = 31 * i + this.advancement.hashCode();
         return 31 * i + this.criterion.hashCode();
      }
   }
}