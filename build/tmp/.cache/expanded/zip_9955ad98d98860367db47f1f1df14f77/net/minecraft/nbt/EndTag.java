package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
   private static final int SELF_SIZE_IN_BYTES = 8;
   public static final TagType<EndTag> TYPE = new TagType<EndTag>() {
      public EndTag load(DataInput p_128550_, int p_128551_, NbtAccounter p_128552_) {
         p_128552_.accountBytes(8L);
         return EndTag.INSTANCE;
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197465_, StreamTagVisitor p_197466_) {
         return p_197466_.visitEnd();
      }

      public void skip(DataInput p_197462_, int p_197463_) {
      }

      public void skip(DataInput p_197460_) {
      }

      public String getName() {
         return "END";
      }

      public String getPrettyName() {
         return "TAG_End";
      }

      public boolean isValue() {
         return true;
      }
   };
   public static final EndTag INSTANCE = new EndTag();

   private EndTag() {
   }

   public void write(DataOutput pOutput) throws IOException {
   }

   public int sizeInBytes() {
      return 8;
   }

   public byte getId() {
      return 0;
   }

   public TagType<EndTag> getType() {
      return TYPE;
   }

   public String toString() {
      return this.getAsString();
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public EndTag copy() {
      return this;
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitEnd(this);
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visitEnd();
   }
}