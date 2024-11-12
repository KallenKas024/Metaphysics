package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Objects;
import net.minecraft.Util;

public class StringTag implements Tag {
   private static final int SELF_SIZE_IN_BYTES = 36;
   public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>() {
      public StringTag load(DataInput p_129315_, int p_129316_, NbtAccounter p_129317_) throws IOException {
         p_129317_.accountBytes(36L);
         String s = p_129315_.readUTF();
         p_129317_.readUTF(s);
         return StringTag.valueOf(s);
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197570_, StreamTagVisitor p_197571_) throws IOException {
         return p_197571_.visit(p_197570_.readUTF());
      }

      public void skip(DataInput p_197568_) throws IOException {
         StringTag.skipString(p_197568_);
      }

      public String getName() {
         return "STRING";
      }

      public String getPrettyName() {
         return "TAG_String";
      }

      public boolean isValue() {
         return true;
      }
   };
   private static final StringTag EMPTY = new StringTag("");
   private static final char DOUBLE_QUOTE = '"';
   private static final char SINGLE_QUOTE = '\'';
   private static final char ESCAPE = '\\';
   private static final char NOT_SET = '\u0000';
   private final String data;

   public static void skipString(DataInput pInput) throws IOException {
      pInput.skipBytes(pInput.readUnsignedShort());
   }

   private StringTag(String pData) {
      Objects.requireNonNull(pData, "Null string not allowed");
      this.data = pData;
   }

   public static StringTag valueOf(String pData) {
      return pData.isEmpty() ? EMPTY : new StringTag(pData);
   }

   public void write(DataOutput pOutput) throws IOException {
      try {
         pOutput.writeUTF(this.data);
      } catch (UTFDataFormatException utfdataformatexception) {
         Util.logAndPauseIfInIde("Failed to write NBT String", utfdataformatexception);
         pOutput.writeUTF("");
      }

   }

   public int sizeInBytes() {
      return 36 + 2 * this.data.length();
   }

   public byte getId() {
      return 8;
   }

   public TagType<StringTag> getType() {
      return TYPE;
   }

   public String toString() {
      return Tag.super.getAsString();
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public StringTag copy() {
      return this;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof StringTag && Objects.equals(this.data, ((StringTag)pOther).data);
      }
   }

   public int hashCode() {
      return this.data.hashCode();
   }

   public String getAsString() {
      return this.data;
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitString(this);
   }

   public static String quoteAndEscape(String pText) {
      StringBuilder stringbuilder = new StringBuilder(" ");
      char c0 = 0;

      for(int i = 0; i < pText.length(); ++i) {
         char c1 = pText.charAt(i);
         if (c1 == '\\') {
            stringbuilder.append('\\');
         } else if (c1 == '"' || c1 == '\'') {
            if (c0 == 0) {
               c0 = (char)(c1 == '"' ? 39 : 34);
            }

            if (c0 == c1) {
               stringbuilder.append('\\');
            }
         }

         stringbuilder.append(c1);
      }

      if (c0 == 0) {
         c0 = '"';
      }

      stringbuilder.setCharAt(0, c0);
      stringbuilder.append(c0);
      return stringbuilder.toString();
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      return pVisitor.visit(this.data);
   }
}
