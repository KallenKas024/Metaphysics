package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Locale;

public abstract class EntityRenameFix extends DataFix {
   protected final String name;

   public EntityRenameFix(String pName, Schema pOutputSchema, boolean pChangesType) {
      super(pOutputSchema, pChangesType);
      this.name = pName;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoice.TaggedChoiceType<String> taggedchoicetype = (TaggedChoice.TaggedChoiceType<String>)this.getInputSchema().findChoiceType(References.ENTITY);
      TaggedChoice.TaggedChoiceType<String> taggedchoicetype1 = (TaggedChoice.TaggedChoiceType<String>)this.getOutputSchema().findChoiceType(References.ENTITY);
      return this.fixTypeEverywhere(this.name, taggedchoicetype, taggedchoicetype1, (p_15624_) -> {
         return (p_145311_) -> {
            String s = p_145311_.getFirst();
            Type<?> type = taggedchoicetype.types().get(s);
            Pair<String, Typed<?>> pair = this.fix(s, this.getEntity(p_145311_.getSecond(), p_15624_, type));
            Type<?> type1 = taggedchoicetype1.types().get(pair.getFirst());
            if (!type1.equals(pair.getSecond().getType(), true, true)) {
               throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", type1, pair.getSecond().getType()));
            } else {
               return Pair.of(pair.getFirst(), pair.getSecond().getValue());
            }
         };
      });
   }

   private <A> Typed<A> getEntity(Object pValue, DynamicOps<?> pOps, Type<A> pType) {
      return new Typed<>(pType, pOps, (A)pValue);
   }

   protected abstract Pair<String, Typed<?>> fix(String pEntityName, Typed<?> pTyped);
}