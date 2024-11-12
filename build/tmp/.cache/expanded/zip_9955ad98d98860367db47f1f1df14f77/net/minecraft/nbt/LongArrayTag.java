package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class LongArrayTag extends CollectionTag<LongTag> {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>() {
      public LongArrayTag load(DataInput p_128865_, int p_128866_, NbtAccounter p_128867_) throws IOException {
         p_128867_.accountBytes(24L);
         int i = p_128865_.readInt();
         p_128867_.accountBytes(8L * (long)i);
         long[] along = new long[i];

         for(int j = 0; j < i; ++j) {
            along[j] = p_128865_.readLong();
         }

         return new LongArrayTag(along);
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197501_, StreamTagVisitor p_197502_) throws IOException {
         int i = p_197501_.readInt();
         long[] along = new long[i];

         for(int j = 0; j < i; ++j) {
            along[j] = p_197501_.readLong();
         }

         return p_197502_.visit(along);
      }

      public void skip(DataInput p_197499_) throws IOException {
         p_197499_.skipBytes(p_197499_.readInt() * 8);
      }

      public String getName() {
         return "LONG[]";
      }

      public String getPrettyName() {
         return "TAG_Long_Array";
      }
   };
   private long[] data;

   public LongArrayTag(long[] pData) {
      this.data = pData;
   }

   public LongArrayTag(LongSet pDataSet) {
      this.data = pDataSet.toLongArray();
   }

   public LongArrayTag(List<Long> pDataList) {
      this(toArray(pDataList));
   }

   private static long[] toArray(List<Long> pDataList) {
      long[] along = new long[pDataList.size()];

      for(int i = 0; i < pDataList.size(); ++i) {
         Long olong = pDataList.get(i);
         along[i] = olong == null ? 0L : olong;
      }

      return along;
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeInt(this.data.length);

      for(long i : this.data) {
         pOutput.writeLong(i);
      }

   }

   public int sizeInBytes() {
      return 24 + 8 * this.data.length;
   }

   public byte getId() {
      return 12;
   }

   public TagType<LongArrayTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public LongArrayTag copy() {
      long[] along = new long[this.data.length];
      System.arraycopy(this.data, 0, along, 0, this.data.length);
      return new LongArrayTag(along);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)pOther).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitLongArray(this);
   }

   public long[] getAsLongArray() {
      return this.data;
   }

   public int size() {
      return this.data.length;
   }

   public LongTag get(int p_128811_) {
      return LongTag.valueOf(this.data[p_128811_]);
   }

   public LongTag set(int p_128813_, LongTag p_128814_) {
      long i = this.data[p_128813_];
      this.data[p_128813_] = p_128814_.getAsLong();
      return LongTag.valueOf(i);
   }

   public void add(int p_128832_, LongTag p_128833_) {
      this.data = ArrayUtils.add(this.data, p_128832_, p_128833_.getAsLong());
   }

   public boolean setTag(int pIndex, Tag pNbt) {
      if (pNbt instanceof NumericTag) {
         this.data[pIndex] = ((NumericTag)pNbt).getAsLong();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int pIndex, Tag pNbt) {
      if (pNbt instanceof NumericTag) {
         this.data = ArrayUtils.add(this.data, pIndex, ((NumericTag)pNbt).getAsLong());
         return true;
      } else {
         return false;
      }
   }

   public LongTag remove(int p_128830_) {
      long i = this.data[p_128830_];
      this.data = ArrayUtils.remove(this.data, p_128830_);
      return LongTag.valueOf(i);
   }

   public byte getElementType() {
      return 4;
   }

   public void clear() {
      this.data = new long[0];
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visit(this.data);
   }
}