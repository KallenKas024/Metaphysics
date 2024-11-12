package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgument implements ArgumentType<NbtPathArgument.NbtPath> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
   public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.node.invalid"));
   public static final SimpleCommandExceptionType ERROR_DATA_TOO_DEEP = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.too_deep"));
   public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType((p_99502_) -> {
      return Component.translatable("arguments.nbtpath.nothing_found", p_99502_);
   });
   static final DynamicCommandExceptionType ERROR_EXPECTED_LIST = new DynamicCommandExceptionType((p_263253_) -> {
      return Component.translatable("commands.data.modify.expected_list", p_263253_);
   });
   static final DynamicCommandExceptionType ERROR_INVALID_INDEX = new DynamicCommandExceptionType((p_263252_) -> {
      return Component.translatable("commands.data.modify.invalid_index", p_263252_);
   });
   private static final char INDEX_MATCH_START = '[';
   private static final char INDEX_MATCH_END = ']';
   private static final char KEY_MATCH_START = '{';
   private static final char KEY_MATCH_END = '}';
   private static final char QUOTED_KEY_START = '"';
   private static final char SINGLE_QUOTED_KEY_START = '\'';

   public static NbtPathArgument nbtPath() {
      return new NbtPathArgument();
   }

   public static NbtPathArgument.NbtPath getPath(CommandContext<CommandSourceStack> pContext, String pName) {
      return pContext.getArgument(pName, NbtPathArgument.NbtPath.class);
   }

   public NbtPathArgument.NbtPath parse(StringReader pReader) throws CommandSyntaxException {
      List<NbtPathArgument.Node> list = Lists.newArrayList();
      int i = pReader.getCursor();
      Object2IntMap<NbtPathArgument.Node> object2intmap = new Object2IntOpenHashMap<>();
      boolean flag = true;

      while(pReader.canRead() && pReader.peek() != ' ') {
         NbtPathArgument.Node nbtpathargument$node = parseNode(pReader, flag);
         list.add(nbtpathargument$node);
         object2intmap.put(nbtpathargument$node, pReader.getCursor() - i);
         flag = false;
         if (pReader.canRead()) {
            char c0 = pReader.peek();
            if (c0 != ' ' && c0 != '[' && c0 != '{') {
               pReader.expect('.');
            }
         }
      }

      return new NbtPathArgument.NbtPath(pReader.getString().substring(i, pReader.getCursor()), list.toArray(new NbtPathArgument.Node[0]), object2intmap);
   }

   private static NbtPathArgument.Node parseNode(StringReader pReader, boolean pFirst) throws CommandSyntaxException {
      Object object;
      switch (pReader.peek()) {
         case '"':
         case '\'':
            object = readObjectNode(pReader, pReader.readString());
            break;
         case '[':
            pReader.skip();
            int i = pReader.peek();
            if (i == 123) {
               CompoundTag compoundtag1 = (new TagParser(pReader)).readStruct();
               pReader.expect(']');
               object = new NbtPathArgument.MatchElementNode(compoundtag1);
            } else if (i == 93) {
               pReader.skip();
               object = NbtPathArgument.AllElementsNode.INSTANCE;
            } else {
               int j = pReader.readInt();
               pReader.expect(']');
               object = new NbtPathArgument.IndexedElementNode(j);
            }
            break;
         case '{':
            if (!pFirst) {
               throw ERROR_INVALID_NODE.createWithContext(pReader);
            }

            CompoundTag compoundtag = (new TagParser(pReader)).readStruct();
            object = new NbtPathArgument.MatchRootObjectNode(compoundtag);
            break;
         default:
            object = readObjectNode(pReader, readUnquotedName(pReader));
      }

      return (NbtPathArgument.Node)object;
   }

   private static NbtPathArgument.Node readObjectNode(StringReader pReader, String pName) throws CommandSyntaxException {
      if (pReader.canRead() && pReader.peek() == '{') {
         CompoundTag compoundtag = (new TagParser(pReader)).readStruct();
         return new NbtPathArgument.MatchObjectNode(pName, compoundtag);
      } else {
         return new NbtPathArgument.CompoundChildNode(pName);
      }
   }

   /**
    * Reads a tag name until the next special character. Throws if the result would be a 0-length string. Does not
    * handle quoted tag names.
    */
   private static String readUnquotedName(StringReader pReader) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedInUnquotedName(pReader.peek())) {
         pReader.skip();
      }

      if (pReader.getCursor() == i) {
         throw ERROR_INVALID_NODE.createWithContext(pReader);
      } else {
         return pReader.getString().substring(i, pReader.getCursor());
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   /**
    * @return {@code true} if the given character is normal for a tag name; otherwise {@code false} if it has special
    * meaning for paths.
    */
   private static boolean isAllowedInUnquotedName(char pCh) {
      return pCh != ' ' && pCh != '"' && pCh != '\'' && pCh != '[' && pCh != ']' && pCh != '.' && pCh != '{' && pCh != '}';
   }

   static Predicate<Tag> createTagPredicate(CompoundTag pTag) {
      return (p_99507_) -> {
         return NbtUtils.compareNbt(pTag, p_99507_, true);
      };
   }

   static class AllElementsNode implements NbtPathArgument.Node {
      public static final NbtPathArgument.AllElementsNode INSTANCE = new NbtPathArgument.AllElementsNode();

      private AllElementsNode() {
      }

      public void getTag(Tag pTag, List<Tag> pTags) {
         if (pTag instanceof CollectionTag) {
            pTags.addAll((CollectionTag)pTag);
         }

      }

      public void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags) {
         if (pTag instanceof CollectionTag<?> collectiontag) {
            if (collectiontag.isEmpty()) {
               Tag tag = pSupplier.get();
               if (collectiontag.addTag(0, tag)) {
                  pTags.add(tag);
               }
            } else {
               pTags.addAll(collectiontag);
            }
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      public int setTag(Tag pTag, Supplier<Tag> pSupplier) {
         if (!(pTag instanceof CollectionTag<?> collectiontag)) {
            return 0;
         } else {
            int i = collectiontag.size();
            if (i == 0) {
               collectiontag.addTag(0, pSupplier.get());
               return 1;
            } else {
               Tag tag = pSupplier.get();
               int j = i - (int)collectiontag.stream().filter(tag::equals).count();
               if (j == 0) {
                  return 0;
               } else {
                  collectiontag.clear();
                  if (!collectiontag.addTag(0, tag)) {
                     return 0;
                  } else {
                     for(int k = 1; k < i; ++k) {
                        collectiontag.addTag(k, pSupplier.get());
                     }

                     return j;
                  }
               }
            }
         }
      }

      public int removeTag(Tag pTag) {
         if (pTag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            if (i > 0) {
               collectiontag.clear();
               return i;
            }
         }

         return 0;
      }
   }

   static class CompoundChildNode implements NbtPathArgument.Node {
      private final String name;

      public CompoundChildNode(String pName) {
         this.name = pName;
      }

      public void getTag(Tag pTag, List<Tag> pTags) {
         if (pTag instanceof CompoundTag) {
            Tag tag = ((CompoundTag)pTag).get(this.name);
            if (tag != null) {
               pTags.add(tag);
            }
         }

      }

      public void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags) {
         if (pTag instanceof CompoundTag compoundtag) {
            Tag tag;
            if (compoundtag.contains(this.name)) {
               tag = compoundtag.get(this.name);
            } else {
               tag = pSupplier.get();
               compoundtag.put(this.name, tag);
            }

            pTags.add(tag);
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      public int setTag(Tag pTag, Supplier<Tag> pSupplier) {
         if (pTag instanceof CompoundTag compoundtag) {
            Tag tag = pSupplier.get();
            Tag tag1 = compoundtag.put(this.name, tag);
            if (!tag.equals(tag1)) {
               return 1;
            }
         }

         return 0;
      }

      public int removeTag(Tag pTag) {
         if (pTag instanceof CompoundTag compoundtag) {
            if (compoundtag.contains(this.name)) {
               compoundtag.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class IndexedElementNode implements NbtPathArgument.Node {
      private final int index;

      public IndexedElementNode(int pIndex) {
         this.index = pIndex;
      }

      public void getTag(Tag pTag, List<Tag> pTags) {
         if (pTag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               pTags.add(collectiontag.get(j));
            }
         }

      }

      public void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags) {
         this.getTag(pTag, pTags);
      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      public int setTag(Tag pTag, Supplier<Tag> pSupplier) {
         if (pTag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               Tag tag = collectiontag.get(j);
               Tag tag1 = pSupplier.get();
               if (!tag1.equals(tag) && collectiontag.setTag(j, tag1)) {
                  return 1;
               }
            }
         }

         return 0;
      }

      public int removeTag(Tag pTag) {
         if (pTag instanceof CollectionTag<?> collectiontag) {
            int i = collectiontag.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               collectiontag.remove(j);
               return 1;
            }
         }

         return 0;
      }
   }

   static class MatchElementNode implements NbtPathArgument.Node {
      private final CompoundTag pattern;
      private final Predicate<Tag> predicate;

      public MatchElementNode(CompoundTag pPattern) {
         this.pattern = pPattern;
         this.predicate = NbtPathArgument.createTagPredicate(pPattern);
      }

      public void getTag(Tag pTag, List<Tag> pTags) {
         if (pTag instanceof ListTag listtag) {
            listtag.stream().filter(this.predicate).forEach(pTags::add);
         }

      }

      public void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags) {
         MutableBoolean mutableboolean = new MutableBoolean();
         if (pTag instanceof ListTag listtag) {
            listtag.stream().filter(this.predicate).forEach((p_99571_) -> {
               pTags.add(p_99571_);
               mutableboolean.setTrue();
            });
            if (mutableboolean.isFalse()) {
               CompoundTag compoundtag = this.pattern.copy();
               listtag.add(compoundtag);
               pTags.add(compoundtag);
            }
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      public int setTag(Tag pTag, Supplier<Tag> pSupplier) {
         int i = 0;
         if (pTag instanceof ListTag listtag) {
            int j = listtag.size();
            if (j == 0) {
               listtag.add(pSupplier.get());
               ++i;
            } else {
               for(int k = 0; k < j; ++k) {
                  Tag tag = listtag.get(k);
                  if (this.predicate.test(tag)) {
                     Tag tag1 = pSupplier.get();
                     if (!tag1.equals(tag) && listtag.setTag(k, tag1)) {
                        ++i;
                     }
                  }
               }
            }
         }

         return i;
      }

      public int removeTag(Tag pTag) {
         int i = 0;
         if (pTag instanceof ListTag listtag) {
            for(int j = listtag.size() - 1; j >= 0; --j) {
               if (this.predicate.test(listtag.get(j))) {
                  listtag.remove(j);
                  ++i;
               }
            }
         }

         return i;
      }
   }

   static class MatchObjectNode implements NbtPathArgument.Node {
      private final String name;
      private final CompoundTag pattern;
      private final Predicate<Tag> predicate;

      public MatchObjectNode(String pName, CompoundTag pPattern) {
         this.name = pName;
         this.pattern = pPattern;
         this.predicate = NbtPathArgument.createTagPredicate(pPattern);
      }

      public void getTag(Tag pTag, List<Tag> pTags) {
         if (pTag instanceof CompoundTag) {
            Tag tag = ((CompoundTag)pTag).get(this.name);
            if (this.predicate.test(tag)) {
               pTags.add(tag);
            }
         }

      }

      public void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags) {
         if (pTag instanceof CompoundTag compoundtag) {
            Tag tag = compoundtag.get(this.name);
            if (tag == null) {
               Tag compoundtag1 = this.pattern.copy();
               compoundtag.put(this.name, compoundtag1);
               pTags.add(compoundtag1);
            } else if (this.predicate.test(tag)) {
               pTags.add(tag);
            }
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      public int setTag(Tag pTag, Supplier<Tag> pSupplier) {
         if (pTag instanceof CompoundTag compoundtag) {
            Tag tag = compoundtag.get(this.name);
            if (this.predicate.test(tag)) {
               Tag tag1 = pSupplier.get();
               if (!tag1.equals(tag)) {
                  compoundtag.put(this.name, tag1);
                  return 1;
               }
            }
         }

         return 0;
      }

      public int removeTag(Tag pTag) {
         if (pTag instanceof CompoundTag compoundtag) {
            Tag tag = compoundtag.get(this.name);
            if (this.predicate.test(tag)) {
               compoundtag.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class MatchRootObjectNode implements NbtPathArgument.Node {
      private final Predicate<Tag> predicate;

      public MatchRootObjectNode(CompoundTag pTag) {
         this.predicate = NbtPathArgument.createTagPredicate(pTag);
      }

      public void getTag(Tag pTag, List<Tag> pTags) {
         if (pTag instanceof CompoundTag && this.predicate.test(pTag)) {
            pTags.add(pTag);
         }

      }

      public void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags) {
         this.getTag(pTag, pTags);
      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      public int setTag(Tag pTag, Supplier<Tag> pSupplier) {
         return 0;
      }

      public int removeTag(Tag pTag) {
         return 0;
      }
   }

   public static class NbtPath {
      private final String original;
      private final Object2IntMap<NbtPathArgument.Node> nodeToOriginalPosition;
      private final NbtPathArgument.Node[] nodes;

      public NbtPath(String pOriginal, NbtPathArgument.Node[] pNodes, Object2IntMap<NbtPathArgument.Node> pNodeToOriginPosition) {
         this.original = pOriginal;
         this.nodes = pNodes;
         this.nodeToOriginalPosition = pNodeToOriginPosition;
      }

      public List<Tag> get(Tag pTag) throws CommandSyntaxException {
         List<Tag> list = Collections.singletonList(pTag);

         for(NbtPathArgument.Node nbtpathargument$node : this.nodes) {
            list = nbtpathargument$node.get(list);
            if (list.isEmpty()) {
               throw this.createNotFoundException(nbtpathargument$node);
            }
         }

         return list;
      }

      public int countMatching(Tag pTag) {
         List<Tag> list = Collections.singletonList(pTag);

         for(NbtPathArgument.Node nbtpathargument$node : this.nodes) {
            list = nbtpathargument$node.get(list);
            if (list.isEmpty()) {
               return 0;
            }
         }

         return list.size();
      }

      private List<Tag> getOrCreateParents(Tag pTag) throws CommandSyntaxException {
         List<Tag> list = Collections.singletonList(pTag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            NbtPathArgument.Node nbtpathargument$node = this.nodes[i];
            int j = i + 1;
            list = nbtpathargument$node.getOrCreate(list, this.nodes[j]::createPreferredParentTag);
            if (list.isEmpty()) {
               throw this.createNotFoundException(nbtpathargument$node);
            }
         }

         return list;
      }

      public List<Tag> getOrCreate(Tag pTag, Supplier<Tag> pSupplier) throws CommandSyntaxException {
         List<Tag> list = this.getOrCreateParents(pTag);
         NbtPathArgument.Node nbtpathargument$node = this.nodes[this.nodes.length - 1];
         return nbtpathargument$node.getOrCreate(list, pSupplier);
      }

      private static int apply(List<Tag> pTags, Function<Tag, Integer> pFunction) {
         return pTags.stream().map(pFunction).reduce(0, (p_99633_, p_99634_) -> {
            return p_99633_ + p_99634_;
         });
      }

      public static boolean isTooDeep(Tag pTag, int pCurrentDepth) {
         if (pCurrentDepth >= 512) {
            return true;
         } else {
            if (pTag instanceof CompoundTag) {
               CompoundTag compoundtag = (CompoundTag)pTag;

               for(String s : compoundtag.getAllKeys()) {
                  Tag tag = compoundtag.get(s);
                  if (tag != null && isTooDeep(tag, pCurrentDepth + 1)) {
                     return true;
                  }
               }
            } else if (pTag instanceof ListTag) {
               for(Tag tag1 : (ListTag)pTag) {
                  if (isTooDeep(tag1, pCurrentDepth + 1)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public int set(Tag pTag, Tag pOther) throws CommandSyntaxException {
         if (isTooDeep(pOther, this.estimatePathDepth())) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
         } else {
            Tag tag = pOther.copy();
            List<Tag> list = this.getOrCreateParents(pTag);
            if (list.isEmpty()) {
               return 0;
            } else {
               NbtPathArgument.Node nbtpathargument$node = this.nodes[this.nodes.length - 1];
               MutableBoolean mutableboolean = new MutableBoolean(false);
               return apply(list, (p_263259_) -> {
                  return nbtpathargument$node.setTag(p_263259_, () -> {
                     if (mutableboolean.isFalse()) {
                        mutableboolean.setTrue();
                        return tag;
                     } else {
                        return tag.copy();
                     }
                  });
               });
            }
         }
      }

      private int estimatePathDepth() {
         return this.nodes.length;
      }

      public int insert(int pIndex, CompoundTag pRootTag, List<Tag> pTagsToInsert) throws CommandSyntaxException {
         List<Tag> list = new ArrayList<>(pTagsToInsert.size());

         for(Tag tag : pTagsToInsert) {
            Tag tag1 = tag.copy();
            list.add(tag1);
            if (isTooDeep(tag1, this.estimatePathDepth())) {
               throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
            }
         }

         Collection<Tag> collection = this.getOrCreate(pRootTag, ListTag::new);
         int j = 0;
         boolean flag1 = false;

         for(Tag tag2 : collection) {
            if (!(tag2 instanceof CollectionTag)) {
               throw NbtPathArgument.ERROR_EXPECTED_LIST.create(tag2);
            }

            CollectionTag<?> collectiontag = (CollectionTag)tag2;
            boolean flag = false;
            int i = pIndex < 0 ? collectiontag.size() + pIndex + 1 : pIndex;

            for(Tag tag3 : list) {
               try {
                  if (collectiontag.addTag(i, flag1 ? tag3.copy() : tag3)) {
                     ++i;
                     flag = true;
                  }
               } catch (IndexOutOfBoundsException indexoutofboundsexception) {
                  throw NbtPathArgument.ERROR_INVALID_INDEX.create(i);
               }
            }

            flag1 = true;
            j += flag ? 1 : 0;
         }

         return j;
      }

      public int remove(Tag pTag) {
         List<Tag> list = Collections.singletonList(pTag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            list = this.nodes[i].get(list);
         }

         NbtPathArgument.Node nbtpathargument$node = this.nodes[this.nodes.length - 1];
         return apply(list, nbtpathargument$node::removeTag);
      }

      private CommandSyntaxException createNotFoundException(NbtPathArgument.Node pNode) {
         int i = this.nodeToOriginalPosition.getInt(pNode);
         return NbtPathArgument.ERROR_NOTHING_FOUND.create(this.original.substring(0, i));
      }

      public String toString() {
         return this.original;
      }
   }

   interface Node {
      void getTag(Tag p_99666_, List<Tag> p_99667_);

      void getOrCreateTag(Tag pTag, Supplier<Tag> pSupplier, List<Tag> pTags);

      /**
       * Creates an empty element of the type read by this node.
       */
      Tag createPreferredParentTag();

      int setTag(Tag pTag, Supplier<Tag> pSupplier);

      int removeTag(Tag pTag);

      default List<Tag> get(List<Tag> pTags) {
         return this.collect(pTags, this::getTag);
      }

      default List<Tag> getOrCreate(List<Tag> pTags, Supplier<Tag> pSupplier) {
         return this.collect(pTags, (p_99663_, p_99664_) -> {
            this.getOrCreateTag(p_99663_, pSupplier, p_99664_);
         });
      }

      default List<Tag> collect(List<Tag> pTags, BiConsumer<Tag, List<Tag>> pConsumer) {
         List<Tag> list = Lists.newArrayList();

         for(Tag tag : pTags) {
            pConsumer.accept(tag, list);
         }

         return list;
      }
   }
}