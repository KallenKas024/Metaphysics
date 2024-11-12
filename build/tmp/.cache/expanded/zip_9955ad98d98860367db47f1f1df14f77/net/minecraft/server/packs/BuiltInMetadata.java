package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public class BuiltInMetadata {
   private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
   private final Map<MetadataSectionSerializer<?>, ?> values;

   private BuiltInMetadata(Map<MetadataSectionSerializer<?>, ?> pValues) {
      this.values = pValues;
   }

   public <T> T get(MetadataSectionSerializer<T> pSerializer) {
      return (T)this.values.get(pSerializer);
   }

   public static BuiltInMetadata of() {
      return EMPTY;
   }

   public static <T> BuiltInMetadata of(MetadataSectionSerializer<T> pSerializer, T pValue) {
      return new BuiltInMetadata(Map.of(pSerializer, pValue));
   }

   public static <T1, T2> BuiltInMetadata of(MetadataSectionSerializer<T1> pSerializer1, T1 pValue1, MetadataSectionSerializer<T2> pSerializer2, T2 pValue2) {
      return new BuiltInMetadata(Map.of(pSerializer1, pValue1, pSerializer2, (T1)pValue2));
   }
}