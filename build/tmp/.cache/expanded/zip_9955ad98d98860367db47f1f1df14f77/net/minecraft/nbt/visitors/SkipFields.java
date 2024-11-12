package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class SkipFields extends CollectToTag {
   private final Deque<FieldTree> stack = new ArrayDeque<>();

   public SkipFields(FieldSelector... pSelectors) {
      FieldTree fieldtree = FieldTree.createRoot();

      for(FieldSelector fieldselector : pSelectors) {
         fieldtree.addEntry(fieldselector);
      }

      this.stack.push(fieldtree);
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> pType, String pId) {
      FieldTree fieldtree = this.stack.element();
      if (fieldtree.isSelected(pType, pId)) {
         return StreamTagVisitor.EntryResult.SKIP;
      } else {
         if (pType == CompoundTag.TYPE) {
            FieldTree fieldtree1 = fieldtree.fieldsToRecurse().get(pId);
            if (fieldtree1 != null) {
               this.stack.push(fieldtree1);
            }
         }

         return super.visitEntry(pType, pId);
      }
   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }
}