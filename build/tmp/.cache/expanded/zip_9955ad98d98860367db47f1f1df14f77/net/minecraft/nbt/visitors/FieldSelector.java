package net.minecraft.nbt.visitors;

import java.util.List;
import net.minecraft.nbt.TagType;

public record FieldSelector(List<String> path, TagType<?> type, String name) {
   public FieldSelector(TagType<?> pType, String pName) {
      this(List.of(), pType, pName);
   }

   public FieldSelector(String pElement, TagType<?> pType, String pName) {
      this(List.of(pElement), pType, pName);
   }

   public FieldSelector(String pFirstElement, String pSecondElement, TagType<?> pType, String pName) {
      this(List.of(pFirstElement, pSecondElement), pType, pName);
   }
}