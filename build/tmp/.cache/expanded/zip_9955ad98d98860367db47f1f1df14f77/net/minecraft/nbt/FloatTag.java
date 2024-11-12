package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class FloatTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 12;
   public static final FloatTag ZERO = new FloatTag(0.0F);
   public static final TagType<FloatTag> TYPE = new TagType.StaticSize<FloatTag>() {
      public FloatTag load(DataInput p_128590_, int p_128591_, NbtAccounter p_128592_) throws IOException {
         p_128592_.accountBytes(12L);
         return FloatTag.valueOf(p_128590_.readFloat());
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197470_, StreamTagVisitor p_197471_) throws IOException {
         return p_197471_.visit(p_197470_.readFloat());
      }

      public int size() {
         return 4;
      }

      public String getName() {
         return "FLOAT";
      }

      public String getPrettyName() {
         return "TAG_Float";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final float data;

   private FloatTag(float pData) {
      this.data = pData;
   }

   public static FloatTag valueOf(float pData) {
      return pData == 0.0F ? ZERO : new FloatTag(pData);
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeFloat(this.data);
   }

   public int sizeInBytes() {
      return 12;
   }

   public byte getId() {
      return 5;
   }

   public TagType<FloatTag> getType() {
      return TYPE;
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public FloatTag copy() {
      return this;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof FloatTag && this.data == ((FloatTag)pOther).data;
      }
   }

   public int hashCode() {
      return Float.floatToIntBits(this.data);
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitFloat(this);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return Mth.floor(this.data);
   }

   public short getAsShort() {
      return (short)(Mth.floor(this.data) & '\uffff');
   }

   public byte getAsByte() {
      return (byte)(Mth.floor(this.data) & 255);
   }

   public double getAsDouble() {
      return (double)this.data;
   }

   public float getAsFloat() {
      return this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visit(this.data);
   }
}