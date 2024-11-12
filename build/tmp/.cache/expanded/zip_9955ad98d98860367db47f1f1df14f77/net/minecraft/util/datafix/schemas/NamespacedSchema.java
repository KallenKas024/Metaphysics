package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema extends Schema {
   public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>() {
      public <T> DataResult<String> read(DynamicOps<T> p_17321_, T p_17322_) {
         return p_17321_.getStringValue(p_17322_).map(NamespacedSchema::ensureNamespaced);
      }

      public <T> T write(DynamicOps<T> p_17318_, String p_17319_) {
         return p_17318_.createString(p_17319_);
      }

      public String toString() {
         return "NamespacedString";
      }
   };
   private static final Type<String> NAMESPACED_STRING = new Const.PrimitiveType<>(NAMESPACED_STRING_CODEC);

   public NamespacedSchema(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public static String ensureNamespaced(String pString) {
      ResourceLocation resourcelocation = ResourceLocation.tryParse(pString);
      return resourcelocation != null ? resourcelocation.toString() : pString;
   }

   public static Type<String> namespacedString() {
      return NAMESPACED_STRING;
   }

   public Type<?> getChoiceType(DSL.TypeReference pType, String pChoiceName) {
      return super.getChoiceType(pType, ensureNamespaced(pChoiceName));
   }
}