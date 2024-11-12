package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 16;
   public static final TagType<LongTag> TYPE = new TagType.StaticSize<LongTag>() {
      public LongTag load(DataInput p_128906_, int p_128907_, NbtAccounter p_128908_) throws IOException {
         p_128908_.accountBytes(16L);
         return LongTag.valueOf(p_128906_.readLong());
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197506_, StreamTagVisitor p_197507_) throws IOException {
         return p_197507_.visit(p_197506_.readLong());
      }

      public int size() {
         return 8;
      }

      public String getName() {
         return "LONG";
      }

      public String getPrettyName() {
         return "TAG_Long";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final long data;

   LongTag(long pData) {
      this.data = pData;
   }

   public static LongTag valueOf(long pData) {
      return pData >= -128L && pData <= 1024L ? LongTag.Cache.cache[(int)pData - -128] : new LongTag(pData);
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeLong(this.data);
   }

   public int sizeInBytes() {
      return 16;
   }

   public byte getId() {
      return 4;
   }

   public TagType<LongTag> getType() {
      return TYPE;
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public LongTag copy() {
      return this;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof LongTag && this.data == ((LongTag)pOther).data;
      }
   }

   public int hashCode() {
      return (int)(this.data ^ this.data >>> 32);
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitLong(this);
   }

   public long getAsLong() {
      return this.data;
   }

   public int getAsInt() {
      return (int)(this.data & -1L);
   }

   public short getAsShort() {
      return (short)((int)(this.data & 65535L));
   }

   public byte getAsByte() {
      return (byte)((int)(this.data & 255L));
   }

   public double getAsDouble() {
      return (double)this.data;
   }

   public float getAsFloat() {
      return (float)this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visit(this.data);
   }

   static class Cache {
      private static final int HIGH = 1024;
      private static final int LOW = -128;
      static final LongTag[] cache = new LongTag[1153];

      private Cache() {
      }

      static {
         for(int i = 0; i < cache.length; ++i) {
            cache[i] = new LongTag((long)(-128 + i));
         }

      }
   }
}