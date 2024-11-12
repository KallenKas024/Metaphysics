package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag extends CollectionTag<ByteTag> {
   private static final int SELF_SIZE_IN_BYTES = 24;
   public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>() {
      public ByteArrayTag load(DataInput p_128247_, int p_128248_, NbtAccounter p_128249_) throws IOException {
         p_128249_.accountBytes(24L);
         int i = p_128247_.readInt();
         p_128249_.accountBytes(1L * (long)i);
         byte[] abyte = new byte[i];
         p_128247_.readFully(abyte);
         return new ByteArrayTag(abyte);
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197433_, StreamTagVisitor p_197434_) throws IOException {
         int i = p_197433_.readInt();
         byte[] abyte = new byte[i];
         p_197433_.readFully(abyte);
         return p_197434_.visit(abyte);
      }

      public void skip(DataInput p_197431_) throws IOException {
         p_197431_.skipBytes(p_197431_.readInt() * 1);
      }

      public String getName() {
         return "BYTE[]";
      }

      public String getPrettyName() {
         return "TAG_Byte_Array";
      }
   };
   private byte[] data;

   public ByteArrayTag(byte[] pData) {
      this.data = pData;
   }

   public ByteArrayTag(List<Byte> pDataList) {
      this(toArray(pDataList));
   }

   private static byte[] toArray(List<Byte> pDataList) {
      byte[] abyte = new byte[pDataList.size()];

      for(int i = 0; i < pDataList.size(); ++i) {
         Byte obyte = pDataList.get(i);
         abyte[i] = obyte == null ? 0 : obyte;
      }

      return abyte;
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeInt(this.data.length);
      pOutput.write(this.data);
   }

   public int sizeInBytes() {
      return 24 + 1 * this.data.length;
   }

   public byte getId() {
      return 7;
   }

   public TagType<ByteArrayTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public Tag copy() {
      byte[] abyte = new byte[this.data.length];
      System.arraycopy(this.data, 0, abyte, 0, this.data.length);
      return new ByteArrayTag(abyte);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)pOther).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitByteArray(this);
   }

   public byte[] getAsByteArray() {
      return this.data;
   }

   public int size() {
      return this.data.length;
   }

   public ByteTag get(int p_128194_) {
      return ByteTag.valueOf(this.data[p_128194_]);
   }

   public ByteTag set(int p_128196_, ByteTag p_128197_) {
      byte b0 = this.data[p_128196_];
      this.data[p_128196_] = p_128197_.getAsByte();
      return ByteTag.valueOf(b0);
   }

   public void add(int p_128215_, ByteTag p_128216_) {
      this.data = ArrayUtils.add(this.data, p_128215_, p_128216_.getAsByte());
   }

   public boolean setTag(int pIndex, Tag pNbt) {
      if (pNbt instanceof NumericTag) {
         this.data[pIndex] = ((NumericTag)pNbt).getAsByte();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int pIndex, Tag pNbt) {
      if (pNbt instanceof NumericTag) {
         this.data = ArrayUtils.add(this.data, pIndex, ((NumericTag)pNbt).getAsByte());
         return true;
      } else {
         return false;
      }
   }

   public ByteTag remove(int p_128213_) {
      byte b0 = this.data[p_128213_];
      this.data = ArrayUtils.remove(this.data, p_128213_);
      return ByteTag.valueOf(b0);
   }

   public byte getElementType() {
      return 1;
   }

   public void clear() {
      this.data = new byte[0];
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visit(this.data);
   }
}