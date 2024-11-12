package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;

/**
 * A LootContextParamSet defines a set of required and optional {@link LootContextParam}s.
 * This is used to validate that conditions, functions and other {@link LootContextUser}s only use those parameters that
 * are present for the given loot table.
 * 
 * @see LootContextParamSets
 * @see ValidationContext
 */
public class LootContextParamSet {
   private final Set<LootContextParam<?>> required;
   private final Set<LootContextParam<?>> all;

   LootContextParamSet(Set<LootContextParam<?>> pRequired, Set<LootContextParam<?>> pOptional) {
      this.required = ImmutableSet.copyOf(pRequired);
      this.all = ImmutableSet.copyOf(Sets.union(pRequired, pOptional));
   }

   /**
    * Whether the given parameter is allowed in this set.
    */
   public boolean isAllowed(LootContextParam<?> pParam) {
      return this.all.contains(pParam);
   }

   /**
    * Gets only the required parameters
    */
   public Set<LootContextParam<?>> getRequired() {
      return this.required;
   }

   /**
    * Gets the required and optional parameters
    */
   public Set<LootContextParam<?>> getAllowed() {
      return this.all;
   }

   public String toString() {
      return "[" + Joiner.on(", ").join(this.all.stream().map((p_81400_) -> {
         return (this.required.contains(p_81400_) ? "!" : "") + p_81400_.getName();
      }).iterator()) + "]";
   }

   /**
    * Validate that all parameters referenced by the given LootContextUser are present in this set.
    */
   public void validateUser(ValidationContext pValidationContext, LootContextUser pLootContextUser) {
      Set<LootContextParam<?>> set = pLootContextUser.getReferencedContextParams();
      Set<LootContextParam<?>> set1 = Sets.difference(set, this.all);
      if (!set1.isEmpty()) {
         pValidationContext.reportProblem("Parameters " + set1 + " are not provided in this context");
      }

   }

   public static LootContextParamSet.Builder builder() {
      return new LootContextParamSet.Builder();
   }

   public static class Builder {
      private final Set<LootContextParam<?>> required = Sets.newIdentityHashSet();
      private final Set<LootContextParam<?>> optional = Sets.newIdentityHashSet();

      public LootContextParamSet.Builder required(LootContextParam<?> pParameter) {
         if (this.optional.contains(pParameter)) {
            throw new IllegalArgumentException("Parameter " + pParameter.getName() + " is already optional");
         } else {
            this.required.add(pParameter);
            return this;
         }
      }

      public LootContextParamSet.Builder optional(LootContextParam<?> pParameter) {
         if (this.required.contains(pParameter)) {
            throw new IllegalArgumentException("Parameter " + pParameter.getName() + " is already required");
         } else {
            this.optional.add(pParameter);
            return this;
         }
      }

      public LootContextParamSet build() {
         return new LootContextParamSet(this.required, this.optional);
      }
   }
}