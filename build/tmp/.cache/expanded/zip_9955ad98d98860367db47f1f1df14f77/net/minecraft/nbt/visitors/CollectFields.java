package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
   private int fieldsToGetCount;
   private final Set<TagType<?>> wantedTypes;
   private final Deque<FieldTree> stack = new ArrayDeque<>();

   public CollectFields(FieldSelector... pSelectors) {
      this.fieldsToGetCount = pSelectors.length;
      ImmutableSet.Builder<TagType<?>> builder = ImmutableSet.builder();
      FieldTree fieldtree = FieldTree.createRoot();

      for(FieldSelector fieldselector : pSelectors) {
         fieldtree.addEntry(fieldselector);
         builder.add(fieldselector.type());
      }

      this.stack.push(fieldtree);
      builder.add(CompoundTag.TYPE);
      this.wantedTypes = builder.build();
   }

   public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> pType) {
      return pType != CompoundTag.TYPE ? StreamTagVisitor.ValueResult.HALT : super.visitRootEntry(pType);
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> pType) {
      FieldTree fieldtree = this.stack.element();
      if (this.depth() > fieldtree.depth()) {
         return super.visitEntry(pType);
      } else if (this.fieldsToGetCount <= 0) {
         return StreamTagVisitor.EntryResult.HALT;
      } else {
         return !this.wantedTypes.contains(pType) ? StreamTagVisitor.EntryResult.SKIP : super.visitEntry(pType);
      }
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> pType, String pId) {
      FieldTree fieldtree = this.stack.element();
      if (this.depth() > fieldtree.depth()) {
         return super.visitEntry(pType, pId);
      } else if (fieldtree.selectedFields().remove(pId, pType)) {
         --this.fieldsToGetCount;
         return super.visitEntry(pType, pId);
      } else {
         if (pType == CompoundTag.TYPE) {
            FieldTree fieldtree1 = fieldtree.fieldsToRecurse().get(pId);
            if (fieldtree1 != null) {
               this.stack.push(fieldtree1);
               return super.visitEntry(pType, pId);
            }
         }

         return StreamTagVisitor.EntryResult.SKIP;
      }
   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }

   public int getMissingFieldCount() {
      return this.fieldsToGetCount;
   }
}