package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class DoubleTag extends NumericTag {
   private static final int SELF_SIZE_IN_BYTES = 16;
   public static final DoubleTag ZERO = new DoubleTag(0.0D);
   public static final TagType<DoubleTag> TYPE = new TagType.StaticSize<DoubleTag>() {
      public DoubleTag load(DataInput p_128524_, int p_128525_, NbtAccounter p_128526_) throws IOException {
         p_128526_.accountBytes(16L);
         return DoubleTag.valueOf(p_128524_.readDouble());
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197454_, StreamTagVisitor p_197455_) throws IOException {
         return p_197455_.visit(p_197454_.readDouble());
      }

      public int size() {
         return 8;
      }

      public String getName() {
         return "DOUBLE";
      }

      public String getPrettyName() {
         return "TAG_Double";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final double data;

   private DoubleTag(double pData) {
      this.data = pData;
   }

   public static DoubleTag valueOf(double pData) {
      return pData == 0.0D ? ZERO : new DoubleTag(pData);
   }

   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeDouble(this.data);
   }

   public int sizeInBytes() {
      return 16;
   }

   public byte getId() {
      return 6;
   }

   public TagType<DoubleTag> getType() {
      return TYPE;
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public DoubleTag copy() {
      return this;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof DoubleTag && this.data == ((DoubleTag)pOther).data;
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.data);
      return (int)(i ^ i >>> 32);
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitDouble(this);
   }

   public long getAsLong() {
      return (long)Math.floor(this.data);
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
      return this.data;
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
}