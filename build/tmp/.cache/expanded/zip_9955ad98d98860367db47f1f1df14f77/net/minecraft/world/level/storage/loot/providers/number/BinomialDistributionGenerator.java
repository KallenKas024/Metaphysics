package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * A number provider which generates a random number based on a binomial distribution.
 */
public final class BinomialDistributionGenerator implements NumberProvider {
   final NumberProvider n;
   final NumberProvider p;

   BinomialDistributionGenerator(NumberProvider pN, NumberProvider pP) {
      this.n = pN;
      this.p = pP;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.BINOMIAL;
   }

   public int getInt(LootContext pLootContext) {
      int i = this.n.getInt(pLootContext);
      float f = this.p.getFloat(pLootContext);
      RandomSource randomsource = pLootContext.getRandom();
      int j = 0;

      for(int k = 0; k < i; ++k) {
         if (randomsource.nextFloat() < f) {
            ++j;
         }
      }

      return j;
   }

   public float getFloat(LootContext pLootContext) {
      return (float)this.getInt(pLootContext);
   }

   public static BinomialDistributionGenerator binomial(int pN, float pP) {
      return new BinomialDistributionGenerator(ConstantValue.exactly((float)pN), ConstantValue.exactly(pP));
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BinomialDistributionGenerator> {
      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public BinomialDistributionGenerator deserialize(JsonObject p_165680_, JsonDeserializationContext p_165681_) {
         NumberProvider numberprovider = GsonHelper.getAsObject(p_165680_, "n", p_165681_, NumberProvider.class);
         NumberProvider numberprovider1 = GsonHelper.getAsObject(p_165680_, "p", p_165681_, NumberProvider.class);
         return new BinomialDistributionGenerator(numberprovider, numberprovider1);
      }

      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165672_, BinomialDistributionGenerator p_165673_, JsonSerializationContext p_165674_) {
         p_165672_.add("n", p_165674_.serialize(p_165673_.n));
         p_165672_.add("p", p_165674_.serialize(p_165673_.p));
      }
   }
}