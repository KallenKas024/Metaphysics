package net.minecraft.world.level.storage.loot.predicates;

public class AnyOfCondition extends CompositeLootItemCondition {
   AnyOfCondition(LootItemCondition[] pConditions) {
      super(pConditions, LootItemConditions.orConditions(pConditions));
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ANY_OF;
   }

   public static AnyOfCondition.Builder anyOf(LootItemCondition.Builder... pConditions) {
      return new AnyOfCondition.Builder(pConditions);
   }

   public static class Builder extends CompositeLootItemCondition.Builder {
      public Builder(LootItemCondition.Builder... p_286497_) {
         super(p_286497_);
      }

      public AnyOfCondition.Builder or(LootItemCondition.Builder p_286344_) {
         this.addTerm(p_286344_);
         return this;
      }

      protected LootItemCondition create(LootItemCondition[] p_286715_) {
         return new AnyOfCondition(p_286715_);
      }
   }

   public static class Serializer extends CompositeLootItemCondition.Serializer<AnyOfCondition> {
      protected AnyOfCondition create(LootItemCondition[] p_286467_) {
         return new AnyOfCondition(p_286467_);
      }
   }
}