package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayTag extends CollectionTag<IntTag> {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final TagType<IntArrayTag> TYPE = new TagType.VariableSize<IntArrayTag>() {
      public IntArrayTag load(DataInput p_128662_, int p_128663_, NbtAccounter p_128664_) throws IOException {
         p_128664_.accountBytes(24L);
         int i = p_128662_.readInt();
         p_128664_.accountBytes(4L * (long)i);
         int[] aint = new int[i];

         for(int j = 0; j < i; ++j) {
            aint[j] = p_128662_.readInt();
         }

         return new IntArrayTag(aint);
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197478_, StreamTagVisitor p_197479_) throws IOException {
         int i = p_197478_.readInt();
         int[] aint = new int[i];

         for(int j = 0; j < i; ++j) {
            aint[j] = p_197478_.readInt();
         }

         return p_197479_.visit(aint);
      }

      public void skip(DataInput p_197476_) throws IOException {
         p_197476_.skipBytes(p_197476_.readInt() * 4);
      }

      public String getName() {
         return "INT[]";
      }

      public String getPrettyName() {
         return "TAG_Int_Array";
      }
   };
   private int[] data;

   public IntArrayTag(int[] pData) {
      this.data = pData;
   }

   public IntArrayTag(List<Integer> pDataList) {
      this(toArray(pDataList));
   }

   private static int[] toArray(List<Integer> pDataList) {
      int[] aint = new int[pDataList.size()];

      for(int i = 0; i < pDataList.size(); ++i) {
         Integer integer = pDataList.get(i);
         aint[i] = integer == null ? 0 : integer;
      }

      return aint;
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeInt(this.data.length);

      for(int i : this.data) {
         pOutput.writeInt(i);
      }

   }

   public int sizeInBytes() {
      return 24 + 4 * this.data.length;
   }

   public byte getId() {
      return 11;
   }

   public TagType<IntArrayTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public IntArrayTag copy() {
      int[] aint = new int[this.data.length];
      System.arraycopy(this.data, 0, aint, 0, this.data.length);
      return new IntArrayTag(aint);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)pOther).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public int[] getAsIntArray() {
      return this.data;
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitIntArray(this);
   }

   public int size() {
      return this.data.length;
   }

   public IntTag get(int p_128608_) {
      return IntTag.valueOf(this.data[p_128608_]);
   }

   public IntTag set(int p_128610_, IntTag p_128611_) {
      int i = this.data[p_128610_];
      this.data[p_128610_] = p_128611_.getAsInt();
      return IntTag.valueOf(i);
   }

   public void add(int p_128629_, IntTag p_128630_) {
      this.data = ArrayUtils.add(this.data, p_128629_, p_128630_.getAsInt());
   }

   public boolean setTag(int pIndex, Tag pNbt) {
      if (pNbt instanceof NumericTag) {
         this.data[pIndex] = ((NumericTag)pNbt).getAsInt();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int pIndex, Tag pNbt) {
      if (pNbt instanceof NumericTag) {
         this.data = ArrayUtils.add(this.data, pIndex, ((NumericTag)pNbt).getAsInt());
         return true;
      } else {
         return false;
      }
   }

   public IntTag remove(int p_128627_) {
      int i = this.data[p_128627_];
      this.data = ArrayUtils.remove(this.data, p_128627_);
      return IntTag.valueOf(i);
   }

   public byte getElementType() {
      return 3;
   }

   public void clear() {
      this.data = new int[0];
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visit(this.data);
   }
}