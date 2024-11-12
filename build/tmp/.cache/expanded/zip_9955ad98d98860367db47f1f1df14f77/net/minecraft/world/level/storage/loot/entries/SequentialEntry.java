package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A composite loot pool entry container that expands all its children in order until one of them fails.
 * This container succeeds if all children succeed.
 */
public class SequentialEntry extends CompositeEntryBase {
   SequentialEntry(LootPoolEntryContainer[] pChildren, LootItemCondition[] pConditions) {
      super(pChildren, pConditions);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.SEQUENCE;
   }

   /**
    * Compose the given children into one container.
    */
   protected ComposableEntryContainer compose(ComposableEntryContainer[] pEntries) {
      switch (pEntries.length) {
         case 0:
            return ALWAYS_TRUE;
         case 1:
            return pEntries[0];
         case 2:
            return pEntries[0].and(pEntries[1]);
         default:
            return (p_79819_, p_79820_) -> {
               for(ComposableEntryContainer composableentrycontainer : pEntries) {
                  if (!composableentrycontainer.expand(p_79819_, p_79820_)) {
                     return false;
                  }
               }

               return true;
            };
      }
   }

   public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... pChildren) {
      return new SequentialEntry.Builder(pChildren);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

      public Builder(LootPoolEntryContainer.Builder<?>... pChildren) {
         for(LootPoolEntryContainer.Builder<?> builder : pChildren) {
            this.entries.add(builder.build());
         }

      }

      protected SequentialEntry.Builder getThis() {
         return this;
      }

      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> pChildBuilder) {
         this.entries.add(pChildBuilder.build());
         return this;
      }

      public LootPoolEntryContainer build() {
         return new SequentialEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
      }
   }
}