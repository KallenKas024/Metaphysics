package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;

public class CollectToTag implements StreamTagVisitor {
   private String lastId = "";
   @Nullable
   private Tag rootTag;
   private final Deque<Consumer<Tag>> consumerStack = new ArrayDeque<>();

   @Nullable
   public Tag getResult() {
      return this.rootTag;
   }

   protected int depth() {
      return this.consumerStack.size();
   }

   private void appendEntry(Tag pTag) {
      this.consumerStack.getLast().accept(pTag);
   }

   public StreamTagVisitor.ValueResult visitEnd() {
      this.appendEntry(EndTag.INSTANCE);
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(String pEntry) {
      this.appendEntry(StringTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(byte pEntry) {
      this.appendEntry(ByteTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(short pEntry) {
      this.appendEntry(ShortTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(int pEntry) {
      this.appendEntry(IntTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(long pEntry) {
      this.appendEntry(LongTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(float pEntry) {
      this.appendEntry(FloatTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(double pEntry) {
      this.appendEntry(DoubleTag.valueOf(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(byte[] pEntry) {
      this.appendEntry(new ByteArrayTag(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(int[] pEntry) {
      this.appendEntry(new IntArrayTag(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visit(long[] pEntry) {
      this.appendEntry(new LongArrayTag(pEntry));
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visitList(TagType<?> pType, int p_197688_) {
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.EntryResult visitElement(TagType<?> pType, int p_197710_) {
      this.enterContainerIfNeeded(pType);
      return StreamTagVisitor.EntryResult.ENTER;
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> pType) {
      return StreamTagVisitor.EntryResult.ENTER;
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> pType, String pId) {
      this.lastId = pId;
      this.enterContainerIfNeeded(pType);
      return StreamTagVisitor.EntryResult.ENTER;
   }

   private void enterContainerIfNeeded(TagType<?> pType) {
      if (pType == ListTag.TYPE) {
         ListTag listtag = new ListTag();
         this.appendEntry(listtag);
         this.consumerStack.addLast(listtag::add);
      } else if (pType == CompoundTag.TYPE) {
         CompoundTag compoundtag = new CompoundTag();
         this.appendEntry(compoundtag);
         this.consumerStack.addLast((p_197703_) -> {
            compoundtag.put(this.lastId, p_197703_);
         });
      }

   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      this.consumerStack.removeLast();
      return StreamTagVisitor.ValueResult.CONTINUE;
   }

   public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> pType) {
      if (pType == ListTag.TYPE) {
         ListTag listtag = new ListTag();
         this.rootTag = listtag;
         this.consumerStack.addLast(listtag::add);
      } else if (pType == CompoundTag.TYPE) {
         CompoundTag compoundtag = new CompoundTag();
         this.rootTag = compoundtag;
         this.consumerStack.addLast((p_197681_) -> {
            compoundtag.put(this.lastId, p_197681_);
         });
      } else {
         this.consumerStack.addLast((p_197705_) -> {
            this.rootTag = p_197705_;
         });
      }

      return StreamTagVisitor.ValueResult.CONTINUE;
   }
}