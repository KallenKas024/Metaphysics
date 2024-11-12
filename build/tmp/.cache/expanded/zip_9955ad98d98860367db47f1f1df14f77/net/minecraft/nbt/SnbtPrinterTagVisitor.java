package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.Util;

public class SnbtPrinterTagVisitor implements TagVisitor {
   private static final Map<String, List<String>> KEY_ORDER = Util.make(Maps.newHashMap(), (p_178114_) -> {
      p_178114_.put("{}", Lists.newArrayList("DataVersion", "author", "size", "data", "entities", "palette", "palettes"));
      p_178114_.put("{}.data.[].{}", Lists.newArrayList("pos", "state", "nbt"));
      p_178114_.put("{}.entities.[].{}", Lists.newArrayList("blockPos", "pos"));
   });
   private static final Set<String> NO_INDENTATION = Sets.newHashSet("{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}");
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
   private static final String ELEMENT_SEPARATOR = String.valueOf(',');
   private static final String LIST_OPEN = "[";
   private static final String LIST_CLOSE = "]";
   private static final String LIST_TYPE_SEPARATOR = ";";
   private static final String ELEMENT_SPACING = " ";
   private static final String STRUCT_OPEN = "{";
   private static final String STRUCT_CLOSE = "}";
   private static final String NEWLINE = "\n";
   private final String indentation;
   private final int depth;
   private final List<String> path;
   private String result = "";

   public SnbtPrinterTagVisitor() {
      this("    ", 0, Lists.newArrayList());
   }

   public SnbtPrinterTagVisitor(String pIndentation, int pDepth, List<String> pPath) {
      this.indentation = pIndentation;
      this.depth = pDepth;
      this.path = pPath;
   }

   public String visit(Tag pTag) {
      pTag.accept(this);
      return this.result;
   }

   public void visitString(StringTag pTag) {
      this.result = StringTag.quoteAndEscape(pTag.getAsString());
   }

   public void visitByte(ByteTag pTag) {
      this.result = pTag.getAsNumber() + "b";
   }

   public void visitShort(ShortTag pTag) {
      this.result = pTag.getAsNumber() + "s";
   }

   public void visitInt(IntTag pTag) {
      this.result = String.valueOf((Object)pTag.getAsNumber());
   }

   public void visitLong(LongTag pTag) {
      this.result = pTag.getAsNumber() + "L";
   }

   public void visitFloat(FloatTag pTag) {
      this.result = pTag.getAsFloat() + "f";
   }

   public void visitDouble(DoubleTag pTag) {
      this.result = pTag.getAsDouble() + "d";
   }

   public void visitByteArray(ByteArrayTag pTag) {
      StringBuilder stringbuilder = (new StringBuilder("[")).append("B").append(";");
      byte[] abyte = pTag.getAsByteArray();

      for(int i = 0; i < abyte.length; ++i) {
         stringbuilder.append(" ").append((int)abyte[i]).append("B");
         if (i != abyte.length - 1) {
            stringbuilder.append(ELEMENT_SEPARATOR);
         }
      }

      stringbuilder.append("]");
      this.result = stringbuilder.toString();
   }

   public void visitIntArray(IntArrayTag pTag) {
      StringBuilder stringbuilder = (new StringBuilder("[")).append("I").append(";");
      int[] aint = pTag.getAsIntArray();

      for(int i = 0; i < aint.length; ++i) {
         stringbuilder.append(" ").append(aint[i]);
         if (i != aint.length - 1) {
            stringbuilder.append(ELEMENT_SEPARATOR);
         }
      }

      stringbuilder.append("]");
      this.result = stringbuilder.toString();
   }

   public void visitLongArray(LongArrayTag pTag) {
      String s = "L";
      StringBuilder stringbuilder = (new StringBuilder("[")).append("L").append(";");
      long[] along = pTag.getAsLongArray();

      for(int i = 0; i < along.length; ++i) {
         stringbuilder.append(" ").append(along[i]).append("L");
         if (i != along.length - 1) {
            stringbuilder.append(ELEMENT_SEPARATOR);
         }
      }

      stringbuilder.append("]");
      this.result = stringbuilder.toString();
   }

   public void visitList(ListTag pTag) {
      if (pTag.isEmpty()) {
         this.result = "[]";
      } else {
         StringBuilder stringbuilder = new StringBuilder("[");
         this.pushPath("[]");
         String s = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
         if (!s.isEmpty()) {
            stringbuilder.append("\n");
         }

         for(int i = 0; i < pTag.size(); ++i) {
            stringbuilder.append(Strings.repeat(s, this.depth + 1));
            stringbuilder.append((new SnbtPrinterTagVisitor(s, this.depth + 1, this.path)).visit(pTag.get(i)));
            if (i != pTag.size() - 1) {
               stringbuilder.append(ELEMENT_SEPARATOR).append(s.isEmpty() ? " " : "\n");
            }
         }

         if (!s.isEmpty()) {
            stringbuilder.append("\n").append(Strings.repeat(s, this.depth));
         }

         stringbuilder.append("]");
         this.result = stringbuilder.toString();
         this.popPath();
      }
   }

   public void visitCompound(CompoundTag pTag) {
      if (pTag.isEmpty()) {
         this.result = "{}";
      } else {
         StringBuilder stringbuilder = new StringBuilder("{");
         this.pushPath("{}");
         String s = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
         if (!s.isEmpty()) {
            stringbuilder.append("\n");
         }

         Collection<String> collection = this.getKeys(pTag);
         Iterator<String> iterator = collection.iterator();

         while(iterator.hasNext()) {
            String s1 = iterator.next();
            Tag tag = pTag.get(s1);
            this.pushPath(s1);
            stringbuilder.append(Strings.repeat(s, this.depth + 1)).append(handleEscapePretty(s1)).append(NAME_VALUE_SEPARATOR).append(" ").append((new SnbtPrinterTagVisitor(s, this.depth + 1, this.path)).visit(tag));
            this.popPath();
            if (iterator.hasNext()) {
               stringbuilder.append(ELEMENT_SEPARATOR).append(s.isEmpty() ? " " : "\n");
            }
         }

         if (!s.isEmpty()) {
            stringbuilder.append("\n").append(Strings.repeat(s, this.depth));
         }

         stringbuilder.append("}");
         this.result = stringbuilder.toString();
         this.popPath();
      }
   }

   private void popPath() {
      this.path.remove(this.path.size() - 1);
   }

   private void pushPath(String pKey) {
      this.path.add(pKey);
   }

   protected List<String> getKeys(CompoundTag pTag) {
      Set<String> set = Sets.newHashSet(pTag.getAllKeys());
      List<String> list = Lists.newArrayList();
      List<String> list1 = KEY_ORDER.get(this.pathString());
      if (list1 != null) {
         for(String s : list1) {
            if (set.remove(s)) {
               list.add(s);
            }
         }

         if (!set.isEmpty()) {
            set.stream().sorted().forEach(list::add);
         }
      } else {
         list.addAll(set);
         Collections.sort(list);
      }

      return list;
   }

   public String pathString() {
      return String.join(".", this.path);
   }

   protected static String handleEscapePretty(String pText) {
      return SIMPLE_VALUE.matcher(pText).matches() ? pText : StringTag.quoteAndEscape(pText);
   }

   public void visitEnd(EndTag pTag) {
   }
}