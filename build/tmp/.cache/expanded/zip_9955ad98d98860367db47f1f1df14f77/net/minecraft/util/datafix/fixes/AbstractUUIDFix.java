package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractUUIDFix extends DataFix {
   protected DSL.TypeReference typeReference;

   public AbstractUUIDFix(Schema pOutputSchema, DSL.TypeReference pTypeReference) {
      super(pOutputSchema, false);
      this.typeReference = pTypeReference;
   }

   protected Typed<?> updateNamedChoice(Typed<?> pTyped, String pChoiceName, Function<Dynamic<?>, Dynamic<?>> pUpdater) {
      Type<?> type = this.getInputSchema().getChoiceType(this.typeReference, pChoiceName);
      Type<?> type1 = this.getOutputSchema().getChoiceType(this.typeReference, pChoiceName);
      return pTyped.updateTyped(DSL.namedChoice(pChoiceName, type), type1, (p_14607_) -> {
         return p_14607_.update(DSL.remainderFinder(), pUpdater);
      });
   }

   protected static Optional<Dynamic<?>> replaceUUIDString(Dynamic<?> pDynamic, String pOldKey, String pNewKey) {
      return createUUIDFromString(pDynamic, pOldKey).map((p_14616_) -> {
         return pDynamic.remove(pOldKey).set(pNewKey, p_14616_);
      });
   }

   protected static Optional<Dynamic<?>> replaceUUIDMLTag(Dynamic<?> pDynamic, String pOldKey, String pNewKey) {
      return pDynamic.get(pOldKey).result().flatMap(AbstractUUIDFix::createUUIDFromML).map((p_14598_) -> {
         return pDynamic.remove(pOldKey).set(pNewKey, p_14598_);
      });
   }

   protected static Optional<Dynamic<?>> replaceUUIDLeastMost(Dynamic<?> pDynamic, String pOldKey, String pNewKey) {
      String s = pOldKey + "Most";
      String s1 = pOldKey + "Least";
      return createUUIDFromLongs(pDynamic, s, s1).map((p_14604_) -> {
         return pDynamic.remove(s).remove(s1).set(pNewKey, p_14604_);
      });
   }

   protected static Optional<Dynamic<?>> createUUIDFromString(Dynamic<?> pDynamic, String pUuidKey) {
      return pDynamic.get(pUuidKey).result().flatMap((p_14586_) -> {
         String s = p_14586_.asString((String)null);
         if (s != null) {
            try {
               UUID uuid = UUID.fromString(s);
               return createUUIDTag(pDynamic, uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
            } catch (IllegalArgumentException illegalargumentexception) {
            }
         }

         return Optional.empty();
      });
   }

   protected static Optional<Dynamic<?>> createUUIDFromML(Dynamic<?> p_14579_) {
      return createUUIDFromLongs(p_14579_, "M", "L");
   }

   protected static Optional<Dynamic<?>> createUUIDFromLongs(Dynamic<?> pDynamic, String pMostKey, String pLeastKey) {
      long i = pDynamic.get(pMostKey).asLong(0L);
      long j = pDynamic.get(pLeastKey).asLong(0L);
      return i != 0L && j != 0L ? createUUIDTag(pDynamic, i, j) : Optional.empty();
   }

   protected static Optional<Dynamic<?>> createUUIDTag(Dynamic<?> pDynamic, long pMost, long pLeast) {
      return Optional.of(pDynamic.createIntList(Arrays.stream(new int[]{(int)(pMost >> 32), (int)pMost, (int)(pLeast >> 32), (int)pLeast})));
   }
}