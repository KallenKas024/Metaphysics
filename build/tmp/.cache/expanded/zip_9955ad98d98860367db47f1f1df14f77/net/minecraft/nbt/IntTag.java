package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 12;
   public static final TagType<IntTag> TYPE = new TagType.StaticSize<IntTag>() {
      public IntTag load(DataInput p_128703_, int p_128704_, NbtAccounter p_128705_) throws IOException {
         p_128705_.accountBytes(12L);
         return IntTag.valueOf(p_128703_.readInt());
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197483_, StreamTagVisitor p_197484_) throws IOException {
         return p_197484_.visit(p_197483_.readInt());
      }

      public int size() {
         return 4;
      }

      public String getName() {
         return "INT";
      }

      public String getPrettyName() {
         return "TAG_Int";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final int data;

   IntTag(int pData) {
      this.data = pData;
   }

   public static IntTag valueOf(int pData) {
      return pData >= -128 && pData <= 1024 ? IntTag.Cache.cache[pData - -128] : new IntTag(pData);
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeInt(this.data);
   }

   public int sizeInBytes() {
      return 12;
   }

   public byte getId() {
      return 3;
   }

   public TagType<IntTag> getType() {
      return TYPE;
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public IntTag copy() {
      return this;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof IntTag && this.data == ((IntTag)pOther).data;
      }
   }

   public int hashCode() {
      return this.data;
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitInt(this);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return this.data;
   }

   public short getAsShort() {
      return (short)(this.data & '\uffff');
   }

   public byte getAsByte() {
      return (byte)(this.data & 255);
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
      static final IntTag[] cache = new IntTag[1153];

      private Cache() {
      }

      static {
         for(int i = 0; i < cache.length; ++i) {
            cache[i] = new IntTag(-128 + i);
         }

      }
   }
}