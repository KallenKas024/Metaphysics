package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A composite loot pool entry container that expands all its children in order.
 * This container always succeeds.
 */
public class EntryGroup extends CompositeEntryBase {
   EntryGroup(LootPoolEntryContainer[] pChildren, LootItemCondition[] pConditions) {
      super(pChildren, pConditions);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.GROUP;
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
            ComposableEntryContainer composableentrycontainer = pEntries[0];
            ComposableEntryContainer composableentrycontainer1 = pEntries[1];
            return (p_79556_, p_79557_) -> {
               composableentrycontainer.expand(p_79556_, p_79557_);
               composableentrycontainer1.expand(p_79556_, p_79557_);
               return true;
            };
         default:
            return (p_79562_, p_79563_) -> {
               for(ComposableEntryContainer composableentrycontainer2 : pEntries) {
                  composableentrycontainer2.expand(p_79562_, p_79563_);
               }

               return true;
            };
      }
   }

   public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... pChildren) {
      return new EntryGroup.Builder(pChildren);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

      public Builder(LootPoolEntryContainer.Builder<?>... pChildren) {
         for(LootPoolEntryContainer.Builder<?> builder : pChildren) {
            this.entries.add(builder.build());
         }

      }

      protected EntryGroup.Builder getThis() {
         return this;
      }

      public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> pChildBuilder) {
         this.entries.add(pChildBuilder.build());
         return this;
      }

      public LootPoolEntryContainer build() {
         return new EntryGroup(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
      }
   }
}