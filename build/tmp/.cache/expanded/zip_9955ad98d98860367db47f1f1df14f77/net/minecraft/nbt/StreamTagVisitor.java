package net.minecraft.nbt;

public interface StreamTagVisitor {
   StreamTagVisitor.ValueResult visitEnd();

   StreamTagVisitor.ValueResult visit(String pEntry);

   StreamTagVisitor.ValueResult visit(byte pEntry);

   StreamTagVisitor.ValueResult visit(short pEntry);

   StreamTagVisitor.ValueResult visit(int pEntry);

   StreamTagVisitor.ValueResult visit(long pEntry);

   StreamTagVisitor.ValueResult visit(float pEntry);

   StreamTagVisitor.ValueResult visit(double pEntry);

   StreamTagVisitor.ValueResult visit(byte[] pEntry);

   StreamTagVisitor.ValueResult visit(int[] pEntry);

   StreamTagVisitor.ValueResult visit(long[] pEntry);

   StreamTagVisitor.ValueResult visitList(TagType<?> pType, int p_197528_);

   StreamTagVisitor.EntryResult visitEntry(TagType<?> pType);

   StreamTagVisitor.EntryResult visitEntry(TagType<?> pType, String pId);

   StreamTagVisitor.EntryResult visitElement(TagType<?> pType, int p_197537_);

   StreamTagVisitor.ValueResult visitContainerEnd();

   StreamTagVisitor.ValueResult visitRootEntry(TagType<?> pType);

   public static enum EntryResult {
      ENTER,
      SKIP,
      BREAK,
      HALT;
   }

   public static enum ValueResult {
      CONTINUE,
      BREAK,
      HALT;
   }
}