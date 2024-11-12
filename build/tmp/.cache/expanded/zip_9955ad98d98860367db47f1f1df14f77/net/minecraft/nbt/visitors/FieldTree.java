package net.minecraft.nbt.visitors;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.TagType;

public record FieldTree(int depth, Map<String, TagType<?>> selectedFields, Map<String, FieldTree> fieldsToRecurse) {
   private FieldTree(int pDepth) {
      this(pDepth, new HashMap<>(), new HashMap<>());
   }

   public static FieldTree createRoot() {
      return new FieldTree(1);
   }

   public void addEntry(FieldSelector pSelector) {
      if (this.depth <= pSelector.path().size()) {
         this.fieldsToRecurse.computeIfAbsent(pSelector.path().get(this.depth - 1), (p_202534_) -> {
            return new FieldTree(this.depth + 1);
         }).addEntry(pSelector);
      } else {
         this.selectedFields.put(pSelector.name(), pSelector.type());
      }

   }

   public boolean isSelected(TagType<?> pType, String pName) {
      return pType.equals(this.selectedFields().get(pName));
   }
}