package net.minecraft.nbt;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class StringTagVisitor implements TagVisitor {
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   private final StringBuilder builder = new StringBuilder();

   public String visit(Tag pTag) {
      pTag.accept(this);
      return this.builder.toString();
   }

   public void visitString(StringTag pTag) {
      this.builder.append(StringTag.quoteAndEscape(pTag.getAsString()));
   }

   public void visitByte(ByteTag pTag) {
      this.builder.append((Object)pTag.getAsNumber()).append('b');
   }

   public void visitShort(ShortTag pTag) {
      this.builder.append((Object)pTag.getAsNumber()).append('s');
   }

   public void visitInt(IntTag pTag) {
      this.builder.append((Object)pTag.getAsNumber());
   }

   public void visitLong(LongTag pTag) {
      this.builder.append((Object)pTag.getAsNumber()).append('L');
   }

   public void visitFloat(FloatTag pTag) {
      this.builder.append(pTag.getAsFloat()).append('f');
   }

   public void visitDouble(DoubleTag pTag) {
      this.builder.append(pTag.getAsDouble()).append('d');
   }

   public void visitByteArray(ByteArrayTag pTag) {
      this.builder.append("[B;");
      byte[] abyte = pTag.getAsByteArray();

      for(int i = 0; i < abyte.length; ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append((int)abyte[i]).append('B');
      }

      this.builder.append(']');
   }

   public void visitIntArray(IntArrayTag pTag) {
      this.builder.append("[I;");
      int[] aint = pTag.getAsIntArray();

      for(int i = 0; i < aint.length; ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append(aint[i]);
      }

      this.builder.append(']');
   }

   public void visitLongArray(LongArrayTag pTag) {
      this.builder.append("[L;");
      long[] along = pTag.getAsLongArray();

      for(int i = 0; i < along.length; ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append(along[i]).append('L');
      }

      this.builder.append(']');
   }

   public void visitList(ListTag pTag) {
      this.builder.append('[');

      for(int i = 0; i < pTag.size(); ++i) {
         if (i != 0) {
            this.builder.append(',');
         }

         this.builder.append((new StringTagVisitor()).visit(pTag.get(i)));
      }

      this.builder.append(']');
   }

   public void visitCompound(CompoundTag pTag) {
      this.builder.append('{');
      List<String> list = Lists.newArrayList(pTag.getAllKeys());
      Collections.sort(list);

      for(String s : list) {
         if (this.builder.length() != 1) {
            this.builder.append(',');
         }

         this.builder.append(handleEscape(s)).append(':').append((new StringTagVisitor()).visit(pTag.get(s)));
      }

      this.builder.append('}');
   }

   protected static String handleEscape(String pText) {
      return SIMPLE_VALUE.matcher(pText).matches() ? pText : StringTag.quoteAndEscape(pText);
   }

   public void visitEnd(EndTag pTag) {
      this.builder.append("END");
   }
}