package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
   public BlockEntitySignDoubleSidedEditableTextFix(Schema pOutputSchema, String pName, String pEntityName) {
      super(pOutputSchema, false, pName, References.BLOCK_ENTITY, pEntityName);
   }

   private static Dynamic<?> fixTag(Dynamic<?> p_278110_) {
      String s = "black";
      Dynamic<?> dynamic = p_278110_.emptyMap();
      dynamic = dynamic.set("messages", getTextList(p_278110_, "Text"));
      dynamic = dynamic.set("filtered_messages", getTextList(p_278110_, "FilteredText"));
      Optional<? extends Dynamic<?>> optional = p_278110_.get("Color").result();
      dynamic = dynamic.set("color", optional.isPresent() ? optional.get() : dynamic.createString("black"));
      Optional<? extends Dynamic<?>> optional1 = p_278110_.get("GlowingText").result();
      dynamic = dynamic.set("has_glowing_text", optional1.isPresent() ? optional1.get() : dynamic.createBoolean(false));
      Dynamic<?> dynamic1 = p_278110_.emptyMap();
      Dynamic<?> dynamic2 = getEmptyTextList(p_278110_);
      dynamic1 = dynamic1.set("messages", dynamic2);
      dynamic1 = dynamic1.set("filtered_messages", dynamic2);
      dynamic1 = dynamic1.set("color", dynamic1.createString("black"));
      dynamic1 = dynamic1.set("has_glowing_text", dynamic1.createBoolean(false));
      p_278110_ = p_278110_.set("front_text", dynamic);
      return p_278110_.set("back_text", dynamic1);
   }

   private static <T> Dynamic<T> getTextList(Dynamic<T> pTag, String pKey) {
      Dynamic<T> dynamic = pTag.createString(getEmptyComponent());
      return pTag.createList(Stream.of(pTag.get(pKey + "1").result().orElse(dynamic), pTag.get(pKey + "2").result().orElse(dynamic), pTag.get(pKey + "3").result().orElse(dynamic), pTag.get(pKey + "4").result().orElse(dynamic)));
   }

   private static <T> Dynamic<T> getEmptyTextList(Dynamic<T> pTag) {
      Dynamic<T> dynamic = pTag.createString(getEmptyComponent());
      return pTag.createList(Stream.of(dynamic, dynamic, dynamic, dynamic));
   }

   private static String getEmptyComponent() {
      return Component.Serializer.toJson(CommonComponents.EMPTY);
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
   }
}