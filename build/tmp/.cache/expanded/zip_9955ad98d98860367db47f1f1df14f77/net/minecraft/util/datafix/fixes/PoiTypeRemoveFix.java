package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PoiTypeRemoveFix extends AbstractPoiSectionFix {
   private final Predicate<String> typesToKeep;

   public PoiTypeRemoveFix(Schema pOutputSchema, String pName, Predicate<String> pTypesToRemove) {
      super(pOutputSchema, pName);
      this.typesToKeep = pTypesToRemove.negate();
   }

   protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> pRecords) {
      return pRecords.filter(this::shouldKeepRecord);
   }

   private <T> boolean shouldKeepRecord(Dynamic<T> p_216705_) {
      return p_216705_.get("type").asString().result().filter(this.typesToKeep).isPresent();
   }
}