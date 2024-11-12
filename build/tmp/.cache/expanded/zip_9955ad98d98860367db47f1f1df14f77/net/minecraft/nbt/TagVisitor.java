package net.minecraft.nbt;

public interface TagVisitor {
   void visitString(StringTag pTag);

   void visitByte(ByteTag pTag);

   void visitShort(ShortTag pTag);

   void visitInt(IntTag pTag);

   void visitLong(LongTag pTag);

   void visitFloat(FloatTag pTag);

   void visitDouble(DoubleTag pTag);

   void visitByteArray(ByteArrayTag pTag);

   void visitIntArray(IntArrayTag pTag);

   void visitLongArray(LongArrayTag pTag);

   void visitList(ListTag pTag);

   void visitCompound(CompoundTag pTag);

   void visitEnd(EndTag pTag);
}